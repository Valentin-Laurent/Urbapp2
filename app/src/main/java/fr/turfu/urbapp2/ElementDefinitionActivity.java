package fr.turfu.urbapp2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import java.io.File;

import fr.turfu.urbapp2.DB.Project;
import fr.turfu.urbapp2.DB.ProjectBDD;

/**
 * Created by Laura on 10/10/2016.
 */
public class ElementDefinitionActivity extends AppCompatActivity {

    /**
     * Id du projet ouvert
     */
    private long project_id;

    /**
     * Id de la photo ouverte
     */
    private long photo_id;

    /**
     * Path de la photo ouverte
     */
    private String photo_path;

    /**
     * Menu item pour le nom du projet
     */
    private MenuItem mi;


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
        setContentView(R.layout.activity_element_definition);

        //Photo
        ImageView i = (ImageView) findViewById(R.id.photo);

        final Intent intent = getIntent();
        photo_path = intent.getStringExtra("photo_path");

        File imgFile = new File(Environment.getExternalStorageDirectory(), photo_path);
        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        i.setImageBitmap(myBitmap);

        // Project ID
        project_id = intent.getLongExtra("project_id", 0);

        //Photo ID
        photo_id = intent.getLongExtra("photo_id", 0);

        //Mise en place de la toolbar
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mainToolbar.setTitle("");
        mainToolbar.setSubtitle("");

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
                return true;

            case R.id.settings:
                return true;

            case R.id.seeDetails:
                Project p = getProject();
                popUpDetails(p.getProjectName(), p.getProjectDescription());
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
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


    /**
     * Lancement de la pop up avec les détails du projet
     */
    public void popUpDetails(String name, String descr) {
        PopUpDetails pud = new PopUpDetails(ElementDefinitionActivity.this, name, descr, mi);
        pud.show();
    }

    /**
     * Obtenir le projet ouvert
     *
     * @return Projet ouvert
     */
    public Project getProject() {
        ProjectBDD pbdd = new ProjectBDD(ElementDefinitionActivity.this); //Instanciation de ProjectBdd pour manipuler les projets de la base de données
        pbdd.open(); //Ouverture de la base de données
        Project p = pbdd.getProjectById(project_id); // Récupération du projet
        pbdd.close(); // Fermeture de la base de données
        return p;
    }




}
