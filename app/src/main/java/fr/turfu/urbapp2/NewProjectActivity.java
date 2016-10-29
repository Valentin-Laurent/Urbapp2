package fr.turfu.urbapp2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Polygon;

import fr.turfu.urbapp2.DB.Project;
import fr.turfu.urbapp2.DB.ProjectBDD;

/**
 * Activité pour la création d'un nouveau projet
 */
public class NewProjectActivity extends AppCompatActivity implements MapEventsReceiver {

    /**
     * Vue pour la carte
     */
    private MapView map;

    public static GeoPoint point=null;

    /**
     * Création de l'activité
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Ajout du layout
        setContentView(R.layout.activity_new_project);

        //Mise en place de la toolbar
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mainToolbar.setTitle("");
        mainToolbar.setSubtitle("");

        //Bouton de localisation qui affiche une pop up
        Button bouton = (Button) findViewById(R.id.buttonLocalisationProject);
        bouton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                popUp();
            }
        });

        //Bouton de validation
        Button valid = (Button) findViewById(R.id.ButtonValidProject);
        valid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText et1 = (EditText) findViewById(R.id.EditTextName);
                String name = et1.getText().toString();

                EditText et2 = (EditText) findViewById(R.id.EditTextDescr);
                String descr = et2.getText().toString();

                if (control(name)) {
                    save(name, descr);

                }
            }
        });

        // Map
        org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.setUserAgentValue(BuildConfig.APPLICATION_ID);
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setMaxZoomLevel(19);

        IMapController mapController = map.getController();
        mapController.setZoom(16);

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this, this);
        map.getOverlays().add(0, mapEventsOverlay);


        //The following code is to get the location of the user
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //Even of the following code is useless, it is necessary or Android Studio won't compile the code
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        GeoPoint startPoint;
        if (lastLocation != null) {
            startPoint = new GeoPoint(lastLocation);
        } else {
            //These are the coordinate of the center of Nantes city
            startPoint = new GeoPoint(47.2172500, -1.5533600);
        }
        mapController.setCenter(startPoint);
        drawPoint(startPoint);
    }


    /**
     * Method to inflate the xml menu file (Ajout des différents onglets dans la toolbar)
     *
     * @param menu the menu
     * @return true if everything went good
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //On sérialise le fichier menu.xml pour l'afficher dans la barre de menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        //Display Username
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String u = preferences.getString("user_preference", "");
        MenuItem i = menu.findItem(R.id.connectedAs);
        i.setTitle(u);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Method to handle the clicks on the items of the toolbar
     *
     * @param item the item
     * @return true if everything went good
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.home:
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;

            case R.id.settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Lancement de la pop up de localisation
     */
    public void popUp() {
        CustomPopUp cdd = new CustomPopUp(NewProjectActivity.this);
        cdd.show();
    }

    /**
     * Sauvegarde d'un nouveau projet
     */

    public void save(String name, String descr) {

        long gpsgeomId = saveLocalisation();

        if (gpsgeomId > 0) {
            ProjectBDD pbdd = new ProjectBDD(NewProjectActivity.this);

            pbdd.open();

            Project p = pbdd.getProjectByName(name);
            long id = 0;

            if (p == null) {
                Project p1 = new Project(name, descr, gpsgeomId);
                pbdd.insert(p1);

                Intent intent = new Intent(NewProjectActivity.this, ProjectOpenActivity.class);
                intent.putExtra("projectName", p1.getProjectName());
                startActivity(intent);
                finish();

            } else {
                Toast.makeText(this, R.string.project_already_created, Toast.LENGTH_SHORT).show();
            }
            pbdd.close();
        } else {
            Toast.makeText(this, R.string.project_to_locate, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Méthode pour verifier que les champs du formulaire sont bien remplis : project_name non vide et géolocalisation effectuée
     *
     * @param n Project_name
     * @return Boolean
     */
    // TODO : Prendre en compte la géolocalisation
    public boolean control(String n) {
        if (n.matches("")) {
            Toast.makeText(this, R.string.empty_project_name, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    /**
     * Tracé d'un point p
     *
     * @param p Point à tracer
     */
    public void drawPoint(GeoPoint p) {
        Polygon circle = new Polygon(this);
        circle.setPoints(Polygon.pointsAsCircle(p, 18));
        circle.setFillColor(Color.YELLOW);
        circle.setStrokeColor(Color.RED);
        circle.setStrokeWidth(5);
        map.getOverlays().add(circle);
        map.invalidate();
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint geoPoint) {
        return false;
    }

    public long saveLocalisation() {
        if(point==null){
            return 0;
        }else{
        String thegeom = "POINT(" + point.getLatitude() + " " + point.getLongitude() + ")";
        ProjectBDD pbdd = new ProjectBDD(this);
        pbdd.open();
        long id = pbdd.insertGpsgeom(thegeom);
        pbdd.close();
        return id;
        }
    }

}


