package fr.turfu.urbapp2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import fr.turfu.urbapp2.db.Project;
import fr.turfu.urbapp2.db.ProjectBDD;

/**
 * Activité qui permet de visualiser un projet ouvert
 */
public class ProjectOpenActivity extends AppCompatActivity {

    /**
     * Bouton pour ajouter une nouvelle photo
     */
    private Button b1;

    /**
     * Liste pour les photos
     */
    ListView mListView;

    /**
     * Tableau pour les photo
     */
    String[] list_photo = new String[]{};

    /**
     * Projet
     */
    private Long project_id;

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
        super.onCreate(savedInstanceState);

        //Ajout du layout
        setContentView(R.layout.activity_project_open);

        //Mise en place de la toolbar
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mainToolbar.setTitle("");
        mainToolbar.setSubtitle("");

        // Button new photo
        b1 = (Button) findViewById(R.id.buttonNewPhoto);
        b1.setTextColor(Color.parseColor("#ffffff"));
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent intent = new Intent(ProjectOpenActivity.this, PhotoOpenActivity.class);
                //startActivity(intent);
            }
        });

        //Nom du projet
        final Intent intent = getIntent();
        String name = intent.getStringExtra("projectName");

        //Id du projet
        ProjectBDD pbdd = new ProjectBDD(ProjectOpenActivity.this); //Instanciation de ProjectBdd pour manipuler les projets de la base de données
        pbdd.open(); //Ouverture de la base de données
        Project p = pbdd.getProjectByName(name); // Récupération du projet
        pbdd.close(); // Fermeture de la base de données
        project_id = p.getProjectId();
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
                finish();
                return true;

            case R.id.settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                finish();
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


    @Override
    protected void onResume() {

        /* Lister les photos*/
        List<String> lpn = getPhotoNames();

        list_photo = new String[lpn.size()];
        for (int i = 0; i < lpn.size(); i++) {
            list_photo[i] = lpn.get(i);
        }

        //Displaying the list
        mListView = (ListView) findViewById(R.id.listViewPhoto);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ProjectOpenActivity.this,
                android.R.layout.simple_list_item_1, list_photo);
        mListView.setAdapter(adapter);

        //Si il n'y a pas de photo, affichage d'un message
        if (lpn.size() == 0) {
            TextView tv = (TextView) findViewById(R.id.textViewNoPhoto);
            tv.setVisibility(View.VISIBLE);
        }

        super.onResume();
    }

    /**
     * Lancement de la pop up avec les détails du projet
     */
    public void popUpDetails(String name, String descr) {
        PopUpDetails pud = new PopUpDetails(ProjectOpenActivity.this, name, descr, mi);
        pud.show();
    }


    /**
     * Obtenir le projet ouvert
     *
     * @return Projet ouvert
     */
    public Project getProject() {
        ProjectBDD pbdd = new ProjectBDD(ProjectOpenActivity.this); //Instanciation de ProjectBdd pour manipuler les projets de la base de données
        pbdd.open(); //Ouverture de la base de données
        Project p = pbdd.getProjectById(project_id); // Récupération du projet
        pbdd.close(); // Fermeture de la base de données
        return p;
    }

    /**
     * Lister les photos du projet
     *
     * @return Liste des photos du projet
     */
    public List<String> getPhotoNames() {
        ProjectBDD pbdd = new ProjectBDD(ProjectOpenActivity.this); //Instanciation de ProjectBdd pour manipuler les projets de la base de données
        pbdd.open(); //Ouverture de la base de données
        List<String> lp = pbdd.getPhotos(pbdd.getProjectById(project_id));
        pbdd.close(); // Fermeture de la base de données
        return lp;
    }

}
