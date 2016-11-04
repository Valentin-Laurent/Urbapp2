/**
 * Dialog PopUpDetails
 * ----------------------------------------------------
 * Pop up dans laquelle sont récapitulées toutes les informations d'un projet
 */

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
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Polygon;

import fr.turfu.urbapp2.DB.Data;
import fr.turfu.urbapp2.DB.GpsGeom;
import fr.turfu.urbapp2.DB.Project;
import fr.turfu.urbapp2.DB.ProjectBDD;
import fr.turfu.urbapp2.Request.Request;

public class PopUpDetails extends Dialog implements MapEventsReceiver, android.view.View.OnClickListener, Sync {

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

    /**
     * Nom du projet
     */
    private String name;

    /**
     * Description du projet
     */
    private String descr;

    /**
     * Localisation du projet
     */
    private GeoPoint point;
    /**
     * Menu Item of the activity
     */
    private MenuItem mi;

    /**
     * Id du projet
     */
    private long project_id;

    /**
     * Carte
     */
    private MapView map;
    private IMapController mapController;

    /**
     * Constructeur
     *
     * @param a          Activité
     * @param n          Nom du projet
     * @param d          Description du projet
     * @param mi         Menu item de l'activité dans lequel est inscrit le nom du projet
     * @param project_id Id du projet
     */
    public PopUpDetails(Activity a, String n, String d, MenuItem mi, long project_id) {
        super(a);
        this.c = a;
        this.name = n;
        this.descr = d;
        this.mi = mi;
        this.project_id = project_id;
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
        setContentView(R.layout.see_details_pop_up);

        //Ajout des informations
        EditText et1 = (EditText) findViewById(R.id.NameValue);
        et1.setText(name);
        EditText et2 = (EditText) findViewById(R.id.DescrValue);
        et2.setText(descr);

        //Création des boutons
        yes = (Button) findViewById(R.id.btn_ok);
        no = (Button) findViewById(R.id.btn_cancel);

        //Ajout des actions
        yes.setOnClickListener(this);
        no.setOnClickListener(this);

        //Carte
        org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.setUserAgentValue(BuildConfig.APPLICATION_ID);
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setMaxZoomLevel(19);

        mapController = map.getController();
        mapController.setZoom(16);

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

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(c, this);
        map.getOverlays().add(0, mapEventsOverlay);

        //Initialisation de la localisation
        ProjectBDD pbdd = new ProjectBDD(c);
        pbdd.open();
        GpsGeom gp = pbdd.getGpsGeom(pbdd.getProjectByName(name).getProjectId());

        //On en extrait la position gps
        GeometryFactory gf = new GeometryFactory();
        WKTReader wktr = new WKTReader(gf);
        String thegeom = gp.getGpsGeomCoord();
        while (thegeom.charAt(0) == 's') {
            thegeom = thegeom.substring(10, thegeom.length());
        }
        pbdd.updateGpsgeom(gp.getGpsGeomsId(), thegeom);
        Geometry geom = null;
        try {
            geom = wktr.read(thegeom);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Coordinate[] coord = geom.getCoordinates();
        point = new GeoPoint(coord[0].x, coord[0].y);
        pbdd.close();

        //On affiche le point
        refresh();
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
                //On récupère les changements
                EditText et1 = (EditText) findViewById(R.id.NameValue);
                String newName = et1.getText().toString();
                EditText et2 = (EditText) findViewById(R.id.DescrValue);
                String newDescr = et2.getText().toString();

                //Mise à jour du projet
                ProjectBDD pbdd = new ProjectBDD(this.getContext()); //Instanciation de ProjectBdd pour manipuler les projets de la base de données
                pbdd.open(); //Ouverture de la base de données
                Project p = pbdd.getProjectByName(name); // Récupération du projet
                p.setProjectName(newName);
                p.setProjectDescription(newDescr); //Mise à jour de la description
                pbdd.update(p); //Mise à jour

                //Mise à jour de la position gps
                String thegeom = "POINT(" + point.getLatitude() + " " + point.getLongitude() + ")";
                pbdd.updateGpsgeom(p.getGpsGeom_id(), thegeom);
                pbdd.close(); // Fermeture de la base de données

                //Synchronisation
                //Export
                Data d = Data.ToData(project_id, c);
                Request.saveProject(c, d);

                //Mise à jour de l'affichage
                mi.setTitle("Projet : " + p.getProjectName());

                //Fermeture de la pop up
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

    @Override
    public void updateView() {

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
        map.invalidate();
    }

    /**
     * Suppression du point
     */
    public void clear() {
        map.getOverlays().clear();
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(c, this);
        map.getOverlays().add(0, mapEventsOverlay);
    }

    /**
     * Méthode pour rafraichir le tracé de la localisation gps du projet
     */
    public void refresh() {
        drawPoint(point);
        mapController.setCenter(point);
        map.invalidate();
    }


}