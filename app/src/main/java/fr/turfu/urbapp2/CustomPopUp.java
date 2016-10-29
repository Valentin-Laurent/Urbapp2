package fr.turfu.urbapp2;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Polygon;

/**
 * Class CustomPopUp
 * PopUp comprenant un titre, une carte et deux boutons (ok et cancel).
 * Elle est destinée à la géolocalisation d'un projet et sera affichée dans l'activité NewProjectActivity
 */
public class CustomPopUp extends Dialog implements
        MapEventsReceiver, android.view.View.OnClickListener {

    /**
     * Activité dans laquelle on affiche la pop up
     */
    public Activity c;

    /**
     * Boite de dialogue
     */
    public Dialog d;

    /**
     * Boutons ok et cancel
     */
    public Button yes, no;

    private MapView map;
    private GeoPoint point;

    /**
     * Constructeur
     *
     * @param a Activité
     */
    public CustomPopUp(Activity a) {
        super(a);
        this.c = a;
    }

    /**
     * Création de la pop up.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Mise en place du layout
        setContentView(R.layout.localisation_pop_up);
        //Création des boutons
        yes = (Button) findViewById(R.id.btn_ok);
        no = (Button) findViewById(R.id.btn_cancel);
        //Ajout des actions
        yes.setOnClickListener(this);
        no.setOnClickListener(this);

        //Map
        org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.setUserAgentValue(BuildConfig.APPLICATION_ID);
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setMaxZoomLevel(19);

        IMapController mapController = map.getController();
        mapController.setZoom(16);

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(c, this);
        map.getOverlays().add(0, mapEventsOverlay);

        //The following code is to get the location of the user
        LocationManager locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
        //Even of the following code is useless, it is necessary or Android Studio won't compile the code
        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        //Initialisation
        point = NewProjectActivity.point;
        if (point != null) {
            drawPoint(point);
        } else {
            GeoPoint startPoint;
            if (lastLocation != null) {
                startPoint = new GeoPoint(lastLocation);
            } else {
                //These are the coordinate of the center of Nantes city
                startPoint = new GeoPoint(47.2172500, -1.5533600);
            }
            mapController.setCenter(startPoint);
        }
    }

    /**
     * Mise en place des actions sur les deux boutons :
     * Pour le bouton cancel, on démissionne de cette activité et on n'enregistre aucune modification effectuée sur la carte.
     * Pour le bouton ok, on modifie la zone du projet avec les modification apportées.
     *
     * @param v Vue représentant la pop up elle-même
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                NewProjectActivity.point = point;
                dismiss();
                break;
            case R.id.btn_cancel:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint geoPoint) {
        point = geoPoint;
        clear();
        refresh();
        return false;
    }

    /**
     * Suppression du point
     */
    public void clear() {
        map.getOverlays().clear();
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(c, this);
        map.getOverlays().add(0, mapEventsOverlay);
    }


    public void refresh() {
        drawPoint(point);
        map.invalidate();
    }

    /**
     * Tracé d'un point p
     *
     * @param p Point à tracer
     */
    public void drawPoint(GeoPoint p) {
        Polygon circle = new Polygon(c);
        circle.setPoints(Polygon.pointsAsCircle(p, 15));
        circle.setFillColor(Color.RED);
        circle.setStrokeColor(Color.RED);
        circle.setStrokeWidth(3);
        map.getOverlays().add(circle);
        map.getController().setCenter(p);
        map.invalidate();
    }
}