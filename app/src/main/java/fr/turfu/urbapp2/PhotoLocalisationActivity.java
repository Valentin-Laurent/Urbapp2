/**
 * Activité PhotoLocalisationActivity
 * -----------------------------------------
 * Activité dans laquelle l'utilisateur définit la zone d'intéret de la photo,
 * qui peut être une ligne ou une surface.
 */

package fr.turfu.urbapp2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;

import fr.turfu.urbapp2.DB.Data;
import fr.turfu.urbapp2.DB.GpsGeom;
import fr.turfu.urbapp2.DB.Photo;
import fr.turfu.urbapp2.DB.PhotoBDD;
import fr.turfu.urbapp2.DB.Project;
import fr.turfu.urbapp2.DB.ProjectBDD;
import fr.turfu.urbapp2.Request.Request;

public class PhotoLocalisationActivity extends AppCompatActivity implements MapEventsReceiver, Sync {

    /**
     * Id du projet ouvert
     */
    private long project_id;

    /**
     * Path de la photo ouverte
     */
    private String photo_path;

    /**
     * Menu item pour le nom du projet
     */
    private MenuItem mi;

    /**
     * Permet d'obtenir la position de l'utilisateur
     */
    private LocationManager locationManager;

    /**
     * Carte
     */
    private MapView map;

    /**
     * Liste des anciens points  délimitant la zone de la photo
     */
    private ArrayList<GeoPoint> oldpoints;

    /**
     * Liste des nouveaux points délimitant la zone de la photo
     */
    private ArrayList<GeoPoint> points;

    /**
     * Booléen qui indique si la zone tracée par l'utilisateur est fermée ou non.
     * En effet, si elle est fermée, on interdit à l'utilisateur de placer d'autres points, une photo ne pouvant pas montrer des zones discontinues.
     */
    private boolean locked;

    /**
     * Zone de la photo (issue de la base de données)
     */
    private GpsGeom gpsgeom;

    /**
     * Création de l'activité
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Creation
        super.onCreate(savedInstanceState);

        //Mise en place du layout
        setContentView(R.layout.activity_photo_localisation);

        //Photo path
        final Intent intent = getIntent();
        photo_path = intent.getStringExtra("photo_path");

        // Project ID
        project_id = intent.getLongExtra("project_id", 0);

        //Initialisation
        points = new ArrayList<>();
        oldpoints = new ArrayList<>();
        locked = false;

        //On récupère le gpsgeom
        PhotoBDD pbdd = new PhotoBDD(this);
        pbdd.open();
        Photo p = pbdd.getPhotoByPath(photo_path);
        pbdd.close();

        ProjectBDD prbdd = new ProjectBDD(this);
        prbdd.open();
        gpsgeom = prbdd.getGpsGeomById(p.getGpsGeom_id());
        prbdd.close();

        //On initialise le tableau de points
        initPoints();

        //Mise en place de la toolbar
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mainToolbar.setTitle("");
        mainToolbar.setSubtitle("");

        //Bouton aide
        Button help = (Button) findViewById(R.id.buttonHelp);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                AlertDialog.Builder builder = new AlertDialog.Builder(PhotoLocalisationActivity.this);

                //Layout
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.help_localisation_pop_up, null);
                builder.setView(dialogView);

                //Bouton close
                Button closeBtn = (Button) dialogView.findViewById(R.id.btn_close_pop);

                final AlertDialog dialog = builder.create();

                closeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        //Map
        org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.setUserAgentValue(BuildConfig.APPLICATION_ID);
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setMaxZoomLevel(19);

        IMapController mapController = map.getController();
        mapController.setZoom(16);

        //The following code is to get the location of the user
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //Even of the following code is useless, it is necessary or Android Studio won't compile the code
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
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

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this, this);
        map.getOverlays().add(0, mapEventsOverlay);

        //Tracé des anciens points
        refresh();

        //Bouton Annuler
        Button cancel = (Button) findViewById(R.id.buttonCancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                cancel();
            }
        });

        //Bouton Annuler Tout
        Button cancelAll = (Button) findViewById(R.id.buttonCancelAll);
        cancelAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                points.clear();
                clear();
                refresh();
            }
        });

        //Bouton Save
        final Button save = (Button) findViewById(R.id.buttonSave);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                save();

                //Export
                Data d = Data.ToData(project_id, PhotoLocalisationActivity.this);
                Request.saveProject(PhotoLocalisationActivity.this, d);

            }
        });

        //Bouton Supprimer tout
        final Button erase = (Button) findViewById(R.id.buttonErase);
        erase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                oldpoints.clear();
                points.clear();
                clear();
                refresh();
            }
        });

        //Bouton Back
        final Button back = (Button) findViewById(R.id.buttonBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PhotoLocalisationActivity.this);

                //Layout
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.save_pop_up, null);
                builder.setView(dialogView);
                final AlertDialog dialog = builder.create();

                //Bouton close
                Button closeBtn = (Button) dialogView.findViewById(R.id.btn_close_pop);
                closeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View w) {
                        dialog.dismiss();
                    }
                });

                //Bouton cancel
                Button cancel = (Button) dialogView.findViewById(R.id.btn_cancel_change);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View w) {
                        Intent i = new Intent(PhotoLocalisationActivity.this, PhotoOpenActivity.class);
                        i.putExtra("photo_path", photo_path);
                        i.putExtra("project_id", project_id);
                        startActivity(i);
                        finish();
                        dialog.dismiss();
                    }
                });

                //Bouton save
                Button save = (Button) dialogView.findViewById(R.id.btn_save_change);
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Sauvegarde locale
                        save();

                        //Export
                        Data d = Data.ToData(project_id, PhotoLocalisationActivity.this);
                        Request.saveProject(PhotoLocalisationActivity.this, d);

                        Intent i = new Intent(PhotoLocalisationActivity.this, PhotoOpenActivity.class);
                        i.putExtra("photo_path", photo_path);
                        i.putExtra("project_id", project_id);
                        startActivity(i);
                        finish();
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

    }

    /**
     * Method to handle the clicks on the items of the toolbar
     *
     * @param item the item
     * @return true if everything went good
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.home:

                AlertDialog.Builder builder = new AlertDialog.Builder(PhotoLocalisationActivity.this);

                //Layout
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.save_pop_up, null);
                builder.setView(dialogView);
                final AlertDialog dialog = builder.create();

                //Bouton close
                Button closeBtn = (Button) dialogView.findViewById(R.id.btn_close_pop);
                closeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View w) {
                        dialog.dismiss();
                    }
                });

                //Bouton cancel
                Button cancel = (Button) dialogView.findViewById(R.id.btn_cancel_change);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View w) {
                        //Fermeture du projet
                        Request.close(PhotoLocalisationActivity.this, project_id);

                        Intent i = new Intent(PhotoLocalisationActivity.this, MainActivity.class);
                        startActivity(i);
                        finish();
                        dialog.dismiss();
                    }
                });

                //Bouton save
                Button save = (Button) dialogView.findViewById(R.id.btn_save_change);
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Sauvegarde
                        save();

                        //Export
                        Data d = Data.ToData(project_id, PhotoLocalisationActivity.this);
                        Request.closeProject(PhotoLocalisationActivity.this, d);

                        Intent i = new Intent(PhotoLocalisationActivity.this, MainActivity.class);
                        startActivity(i);
                        finish();
                        dialog.dismiss();
                    }
                });

                dialog.show();
                return true;

            case R.id.settings:

                AlertDialog.Builder builder1 = new AlertDialog.Builder(PhotoLocalisationActivity.this);

                //Layout
                LayoutInflater inflater1 = getLayoutInflater();
                View dialogView1 = inflater1.inflate(R.layout.save_pop_up, null);
                builder1.setView(dialogView1);
                final AlertDialog dialog1 = builder1.create();

                //Bouton close
                Button closeBtn1 = (Button) dialogView1.findViewById(R.id.btn_close_pop);
                closeBtn1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View w) {
                        dialog1.dismiss();
                    }
                });

                //Bouton cancel
                Button cancel1 = (Button) dialogView1.findViewById(R.id.btn_cancel_change);
                cancel1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View w) {
                        //Fermeture du projet
                        Request.close(PhotoLocalisationActivity.this, project_id);

                        Intent i = new Intent(PhotoLocalisationActivity.this, SettingsActivity.class);
                        startActivity(i);
                        finish();
                        dialog1.dismiss();
                    }
                });

                //Bouton save
                Button save1 = (Button) dialogView1.findViewById(R.id.btn_save_change);
                save1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Sauvegarde
                        save();

                        //Export
                        Data d = Data.ToData(project_id, PhotoLocalisationActivity.this);
                        Request.closeProject(PhotoLocalisationActivity.this, d);

                        Intent i = new Intent(PhotoLocalisationActivity.this, SettingsActivity.class);
                        startActivity(i);
                        finish();
                        dialog1.dismiss();
                    }
                });
                dialog1.show();

                return true;

            case R.id.seeDetails:
                Project p = getProject();
                popUpDetails(p.getProjectName(), p.getProjectDescription());
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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

        //Display ProjectName
        MenuItem i1 = menu.findItem(R.id.projectTitle);
        i1.setVisible(true);
        Resources res = getResources();
        String s = res.getString(R.string.project);
        Project p = getProject();
        i1.setTitle(s + " " + p.getProjectName());
        mi = i1;

        //Affichage du bouton pour voir les détails du projet
        MenuItem i2 = menu.findItem(R.id.seeDetails);
        i2.setVisible(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void updateView() {

        //On récupère le gpsgeom
        PhotoBDD pbdd = new PhotoBDD(this);
        pbdd.open();
        Photo p = pbdd.getPhotoByPath(photo_path);
        pbdd.close();

        ProjectBDD prbdd = new ProjectBDD(this);
        prbdd.open();
        gpsgeom = prbdd.getGpsGeomById(p.getGpsGeom_id());
        prbdd.close();

        //On l'affiche
        initPoints();
        clear();
        refresh();
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint geoPoint) {
        double lat = geoPoint.getLatitude();
        double lon = geoPoint.getLongitude();

        if (!locked) {
            if (points.size() > 0 && isNear(geoPoint, points.get(0))) { // On forme un polygone
                locked = true;

                //Ajout du point
                points.add(points.get(0));

                //Tracé d'une line
                GeoPoint p1 = points.get(points.size() - 2);
                GeoPoint p2 = points.get(0);
                drawLine(p1, p2);

            } else if (oldpoints.size() > 0 && isNear(geoPoint, oldpoints.get(0))) {
                locked = true;

                //Ajout du point
                points.add(oldpoints.get(0));

                //Tracé d'une line
                GeoPoint p1 = points.get(points.size() - 2);
                GeoPoint p2 = oldpoints.get(0);
                drawLine(p1, p2);

            } else if (oldpoints.size() > 0 && isNear(geoPoint, oldpoints.get(oldpoints.size() - 1))) {
                locked = true;

                //Ajout du point
                points.add(oldpoints.get(oldpoints.size() - 1));

                //Tracé d'une line
                GeoPoint p1 = points.get(points.size() - 2);
                GeoPoint p2 = oldpoints.get(oldpoints.size() - 1);
                drawLine(p1, p2);

            } else {
                //Tracé du point
                drawPoint(geoPoint);

                //Ajout du point
                points.add(geoPoint);

                //Tracé d'une line
                if (points.size() > 1) {
                    GeoPoint p1 = geoPoint;
                    GeoPoint p2 = points.get(points.size() - 2);
                    drawLine(p1, p2);
                }
            }
        } else {
            Toast.makeText(PhotoLocalisationActivity.this, R.string.photo_localisation_locked, Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    /**
     * Distance entre deux points géographiques
     *
     * @param p1 Point 1
     * @param p2 Point 2
     * @return Distance entre les deux points
     */
    public double dist(GeoPoint p1, GeoPoint p2) {
        double lat1 = p1.getLatitude();
        double lat2 = p2.getLatitude();
        double lon1 = p1.getLongitude();
        double lon2 = p2.getLongitude();
        return Math.sqrt((lon1 - lon2) * (lon1 - lon2) + (lat1 - lat2) * (lat1 - lat2));
    }

    /**
     * Méthodde pour voir si un point est très proche d'un autre
     *
     * @param p1 Point 1
     * @param p2 Point 2
     * @return Booléen vrai si p1 est très près de p2
     */
    public boolean isNear(GeoPoint p1, GeoPoint p2) {
        return dist(p1, p2) < 0.00005;
    }


    /**
     * Méthode pour annuler le dernier point placé
     */
    public void cancel() {

        if (points.isEmpty()) { //Si il n'y a rien à annuler
            Toast.makeText(PhotoLocalisationActivity.this, R.string.nothing_to_cancel, Toast.LENGTH_SHORT).show();
        } else {
            //On supprime le point
            points.remove(points.size() - 1);

            //On nettoie la vue
            clear();

            //On retrace
            refresh();
        }
    }

    /**
     * Méthode pour tracer l'ensemble des points et des segments délimitant la zone de la photo
     */
    public void refresh() {

        //Tracé des anciens points
        for (int i = 0; i < oldpoints.size(); i++) {
            if (i == 0) {
                //Tracé du premier point
                drawPoint(oldpoints.get(i));
                map.getController().setCenter(oldpoints.get(i));

            } else {
                //Tracé du point
                drawPoint(oldpoints.get(i));
                //Tracé du segment entre le point courant et le précédent point
                drawLine(oldpoints.get(i - 1), oldpoints.get(i));
            }
        }

        //Tracé des nouveaux points
        for (int i = 0; i < points.size(); i++) {
            if (i == 0) {
                //Tracé du premier point
                drawPoint(points.get(i));
                map.getController().setCenter(points.get(i));
                //Tracé du segment entre le point courant et le précédent point
                // Ici, le point précédent est un des anciens points, soit le premier, soit le dernier du tableau oldpoints.
                //On prend celui qui est le plus proche
                if (!oldpoints.isEmpty() && dist(oldpoints.get(0), points.get(i)) < dist(oldpoints.get(oldpoints.size() - 1), points.get(i))) {
                    drawLine(oldpoints.get(0), points.get(i));
                }
                if (!oldpoints.isEmpty() && dist(oldpoints.get(0), points.get(i)) > dist(oldpoints.get(oldpoints.size() - 1), points.get(i))) {
                    drawLine(oldpoints.get(oldpoints.size() - 1), points.get(i));
                }

            } else {
                //Tracé du point
                drawPoint(points.get(i));
                //Tracé du segment entre le point courant et le précédent point
                drawLine(points.get(i - 1), points.get(i));
            }
        }

        //Vérouillage si la figure est fermée
        locked = isClosed();
    }

    /**
     * Tracé d'un point p
     *
     * @param p Point à tracer
     */
    public void drawPoint(GeoPoint p) {
        Polygon circle = new Polygon(this);
        circle.setPoints(Polygon.pointsAsCircle(p, 3));
        circle.setFillColor(Color.RED);
        circle.setStrokeColor(Color.RED);
        circle.setStrokeWidth(3);
        map.getOverlays().add(circle);
        map.invalidate();
    }

    /**
     * Tracé d'une ligne entre deux points
     *
     * @param p1 Point 1
     * @param p2 Point 2
     */
    public void drawLine(GeoPoint p1, GeoPoint p2) {
        Polygon rect = new Polygon(this);
        rect.setFillColor(Color.RED);
        rect.setStrokeColor(Color.RED);
        rect.setStrokeWidth(2);
        BoundingBox rectangle;
        ArrayList<GeoPoint> sommets = new ArrayList<>();
        sommets.add(p1);
        sommets.add(p2);
        rect.setPoints(sommets);
        map.getOverlays().add(rect);
        map.invalidate();
    }

    /**
     * Suppression de tous les tracés de la carte
     */
    public void clear() {
        map.getOverlays().clear();
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this, this);
        map.getOverlays().add(0, mapEventsOverlay);
        map.invalidate();
    }


    /**
     * Sauvegarde du tracé
     */

    public void save() {

        //On vide le tableau nouveau points
        ArrayList<GeoPoint> pts = new ArrayList<>();

        if (!oldpoints.isEmpty()) {
            if (!points.isEmpty() && dist(oldpoints.get(0), points.get(0)) < dist(oldpoints.get(oldpoints.size() - 1), points.get(0))) {
                for (int i = points.size() - 1; i >= 0; i--) {
                    pts.add(points.get(i));
                }
                for (int i = 0; i < oldpoints.size(); i++) {
                    pts.add(oldpoints.get(i));
                }
            } else {
                for (int i = 0; i < oldpoints.size(); i++) {
                    pts.add(oldpoints.get(i));
                }
                for (int i = 0; i < points.size(); i++) {
                    pts.add(points.get(i));
                }
            }
            oldpoints = pts;
            points.clear();
        } else {
            for (int i = 0; i < points.size(); i++) {
                pts.add(points.get(i));
            }
            oldpoints = pts;
            points.clear();
        }

        //On crée la géométrie
        String thegeom = "";

        if (locked) {
            //Création du polygone
            thegeom = "POLYGON((";
            for (GeoPoint p : oldpoints) {
                thegeom = thegeom + p.getLatitude() + " " + p.getLongitude() + ", ";
            }
            //Fermeture du polygone
            thegeom = thegeom + oldpoints.get(0).getLatitude() + " " + oldpoints.get(0).getLongitude();
            thegeom = thegeom + "))";
        } else {
            //Création de la ligne
            thegeom = "LINESTRING(";
            for (GeoPoint p : oldpoints) {
                thegeom = thegeom + p.getLatitude() + " " + p.getLongitude() + ", ";
            }
            //Fermeture
            thegeom = thegeom.substring(0, thegeom.length() - 2);
            thegeom = thegeom + ")";

        }

        //On l'enregistre
        PhotoBDD pbdd = new PhotoBDD(this);
        pbdd.open();
        gpsgeom.setGpsGeomCoord(thegeom);
        pbdd.updateGpsGeom(gpsgeom);
        pbdd.close();

        //Message
        Toast.makeText(PhotoLocalisationActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
    }

    /**
     * Initialisation du tableau de points à l'ouverture de l'activité et à la synchronisation
     */
    public void initPoints() {
        //On vide le tableau
        oldpoints.clear();

        //On récupère la géométrie
        String geom = gpsgeom.getGpsGeomCoord();

        if (geom != null && !geom.equals("")) {

            while (!geom.equals("") && geom.charAt(0) == 's') {
                geom = geom.substring(10, geom.length());
            }
            ProjectBDD pbdd = new ProjectBDD(this);
            pbdd.open();
            pbdd.updateGpsgeom(gpsgeom.getGpsGeomsId(), geom);
            pbdd.close();

            if (!geom.equals("")) {
                GeometryFactory gf = new GeometryFactory();
                WKTReader wktr = new WKTReader(gf);

                try {
                    //On récupère les points et on les ajoute au tableau
                    Geometry g = wktr.read(geom);
                    Coordinate[] coord = g.getCoordinates();
                    for (Coordinate c : coord) {
                        oldpoints.add(new GeoPoint(c.x, c.y));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Fonction qui teste si le tracé de l'utilisateur est fermé ou non
     *
     * @return
     */
    public boolean isClosed() {
        if (oldpoints.isEmpty()) {
            return points.isEmpty() ? false : isNear(points.get(0), points.get(points.size() - 1));
        } else {
            if (points.isEmpty()) {
                return isNear(oldpoints.get(0), oldpoints.get(oldpoints.size() - 1));
            } else {
                return isNear(oldpoints.get(0), points.get(points.size() - 1)) || isNear(oldpoints.get(oldpoints.size() - 1), points.get(points.size() - 1));
            }
        }

    }

    /**
     * Lancement de la pop up avec les détails du projet
     */
    public void popUpDetails(String name, String descr) {
        PopUpDetails pud = new PopUpDetails(PhotoLocalisationActivity.this, name, descr, mi, project_id);
        pud.show();
    }

    /**
     * Obtenir le projet ouvert
     *
     * @return Projet ouvert
     */
    public Project getProject() {
        ProjectBDD pbdd = new ProjectBDD(PhotoLocalisationActivity.this); //Instanciation de ProjectBdd pour manipuler les projets de la base de données
        pbdd.open(); //Ouverture de la base de données
        Project p = pbdd.getProjectById(project_id); // Récupération du projet
        pbdd.close(); // Fermeture de la base de données
        return p;
    }

}
