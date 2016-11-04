/**
 * Activité principale
 * ---------------------------
 * Activité qui se lance à l'ouverture de l'application et qui affiche la liste des projets
 */
package fr.turfu.urbapp2;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;

import fr.turfu.urbapp2.DB.Project;
import fr.turfu.urbapp2.DB.ProjectBDD;
import fr.turfu.urbapp2.Request.Request;
import fr.turfu.urbapp2.Tools.ConnexionCheck;

public class MainActivity extends AppCompatActivity implements MapEventsReceiver {

    /**
     * Permet d'obtenir la position de l'utilisateur
     */
    private LocationManager locationManager;

    /**
     * Bouton pour créer un nouveau projet
     */
    private Button b1;

    /**
     * Liste pour les projets
     */
    private ListView mListView;

    /**
     * Tableau pour les projets
     */
    private String[] list_projs = new String[]{};
    public static ArrayList<Project> projects;

    /**
     * Link to ask google to create a specific connexion code to check if there is no portal between android and server
     */
    public static final String CONNECTIVITY_URL = "http://clients3.google.com/generate_204";

    /**
     * Context of the app
     */
    public static Context baseContext;

    /**
     * Dialog box displayed in the entire screen
     */
    private static AlertDialog.Builder alertDialog;

    /**
     * Boolean to check if internet is on
     */
    public static boolean internet = true;

    /**
     * Vue pour la carte
     */
    private MapView map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Creation
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setting the Context of app
        baseContext = getBaseContext();

        //Checking internet
        alertDialog = new AlertDialog.Builder(MainActivity.this);
        isInternetOn();

        //Initialisation des projets
        projects = new ArrayList<>();

        //Handling toolbar
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mainToolbar.setTitle("");
        mainToolbar.setSubtitle("");

        // Button new project
        b1 = (Button) findViewById(R.id.buttonNewProject);
        b1.setTextColor(Color.parseColor("#ffffff"));
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NewProjectActivity.class);
                startActivity(intent);
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
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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

        //Bouton réalité augmentée
        Button ar = (Button) findViewById(R.id.buttonAugmentedReality);
        ar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AugmentedRealityActivity.class);
                startActivity(intent);
            }
        });
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
                return true;

           /* case R.id.virtual_reality:
                intent = new Intent(this, AugmentedRealityActivity.class);
                startActivity(intent);
                return true;*/

            case R.id.settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onResume() {
        Request.getProjects(this);
        super.onResume();
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint geoPoint) {
        return false;
    }

    /**
     * Récupérer la liste des projets de la base de données
     *
     * @return Liste des projets
     */
    public List<Project> getProjects() {
        ProjectBDD pbdd = new ProjectBDD(MainActivity.this); //Instanciation de ProjectBdd pour manipuler les projets de la base de données
        pbdd.open(); //Ouverture de la base de données
        List<Project> lp = pbdd.getProjects(); // Récupération des projets
        pbdd.close(); // Fermeture de la base de données
        return lp;
    }


    /**
     * Method to check if internet is available (and no portal !)
     */
    public final void isInternetOn() {
        ConnectivityManager con = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        boolean wifi = con.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        boolean mobile = false;
        try {
            mobile = con.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        } catch (NullPointerException e) {
            mobile = false;
        }
        boolean internet = wifi | mobile;
        if (internet) {
            new ConnexionCheck().Connectivity();
        }
    }

    /**
     * Method if no internet connectivity to print a Dialog.
     */
    public static void errorConnect() {
        alertDialog.setTitle("");
        alertDialog.setView(R.layout.error_pop_up);
        alertDialog.show();
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

    /**
     * Affichage de la liste des projets obtenue en requetant le serveur
     */
    public void refreshList() {

        List<String> lpn = new ArrayList<>();
        for (Project p : projects) {
            lpn.add(p.getProjectName());
        }

        list_projs = new String[lpn.size()];
        for (int i = 0; i < lpn.size(); i++) {
            list_projs[i] = lpn.get(i);
        }

        //Displaying the list
        mListView = (ListView) findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, list_projs);
        mListView.setAdapter(adapter);

        //Ajouter les listeners
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = (String) mListView.getItemAtPosition(position);
                if (isAvailable(name)) {
                    Intent intent = new Intent(MainActivity.this, ProjectOpenActivity.class);
                    intent.putExtra("projectName", name);
                    intent.putExtra("project_id", getIdOfProject(name));
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, R.string.project_not_available, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Si il n'y a pas de projets, affichage d'un message
        TextView tv = (TextView) findViewById(R.id.textViewNoProject);
        if (lpn.size() == 0) {
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Méthode pour savoir si un projet est available ou non
     *
     * @param projectName Intitulé du projet
     * @return Booléen isavailable
     */
    public boolean isAvailable(String projectName) {
        boolean b = false;
        for (Project p : projects) {
            if (p.getProjectName().equals(projectName)) {
                b = p.getIsAvailable();
            }
        }
        return b;
    }

    /**
     * Récupérer l'id d'un projet par son nom
     *
     * @param projectName Nom du projet
     * @return id du projet
     */
    public long getIdOfProject(String projectName) {
        long id = 0;
        for (Project p : projects) {
            if (p.getProjectName().equals(projectName)) {
                id = p.getProjectId();
            }
        }
        return id;
    }
}
