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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import fr.turfu.urbapp2.DB.Photo;
import fr.turfu.urbapp2.DB.PhotoBDD;
import fr.turfu.urbapp2.DB.Project;
import fr.turfu.urbapp2.DB.ProjectBDD;

/**
 * Created by Laura on 10/10/2016.
 */
public class PhotoOpenActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_photo_open);

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

        //Bouton back
        Button bouton = (Button) findViewById(R.id.buttonBack);
        bouton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(PhotoOpenActivity.this, SavePhotoInfosPopUpActivity.class);
                i.putExtra("project_id", project_id);
                i.putExtra("photo_id", photo_id);
                i.putExtra("photo_path", photo_path);
                i.putExtra("redirect", "PROJECT_OPEN");

                //Get name and description
                EditText et1 = (EditText) findViewById(R.id.EditTextNamePhoto);
                String newName = et1.getText().toString();
                EditText et2 = (EditText) findViewById(R.id.EditTextDescrPhoto);
                String newDescr = et2.getText().toString();
                i.putExtra("name", newName);
                i.putExtra("descr", newDescr);

                startActivity(i);

            }
        });

        //Bouton enregistrer
        Button bouton1 = (Button) findViewById(R.id.buttonSavePhoto);
        bouton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                //Get name and description
                EditText et1 = (EditText) findViewById(R.id.EditTextNamePhoto);
                String newName = et1.getText().toString();
                EditText et2 = (EditText) findViewById(R.id.EditTextDescrPhoto);
                String newDescr = et2.getText().toString();

                //Sauvegarde
                PhotoBDD pbdd = new PhotoBDD(PhotoOpenActivity.this); //Instanciation de ProjectBdd pour manipuler les projets de la base de données
                pbdd.open(); //Ouverture de la base de données
                Photo p = pbdd.getPhotoById(photo_id); // Récupération de la photo

                //Vérification de l'unicité du nom de la photo
                Photo p1 = pbdd.getPhotoByName(newName);
                if (p1 == null || p1.getPhoto_id() == p.getPhoto_id()) {
                    p.setPhoto_name(newName); //Mise à jour du nom
                } else {
                    Toast.makeText(PhotoOpenActivity.this, R.string.photoName_taken, Toast.LENGTH_SHORT).show();
                }
                p.setPhoto_description(newDescr); //Mise à jour de la description
                pbdd.updatePhotoInfos(p); //Mise à jour
                pbdd.close(); // Fermeture de la base de données

                Toast.makeText(PhotoOpenActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
            }
        });

        //Affichage des infos de la photo
        PhotoBDD pbdd = new PhotoBDD(PhotoOpenActivity.this); //Instanciation de ProjectBdd pour manipuler les projets de la base de données
        pbdd.open(); //Ouverture de la base de données
        Photo p = pbdd.getPhotoById(photo_id); // Récupération de la photo
        pbdd.close();

        EditText et1 = (EditText) findViewById(R.id.EditTextNamePhoto);
        if (!p.getPhoto_name().equals("")) {
            et1.setText(p.getPhoto_name());
        }
        EditText et2 = (EditText) findViewById(R.id.EditTextDescrPhoto);
        if (!p.getPhoto_description().equals("")) {
            et2.setText(p.getPhoto_description());
        }
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
                Intent i = new Intent(PhotoOpenActivity.this, SavePhotoInfosPopUpActivity.class);
                i.putExtra("project_id", project_id);
                i.putExtra("photo_id", photo_id);
                i.putExtra("photo_path", photo_path);
                i.putExtra("redirect", "HOME");

                //Get name and description
                EditText et1 = (EditText) findViewById(R.id.EditTextNamePhoto);
                String newName = et1.getText().toString();
                EditText et2 = (EditText) findViewById(R.id.EditTextDescrPhoto);
                String newDescr = et2.getText().toString();
                i.putExtra("name", newName);
                i.putExtra("descr", newDescr);

                startActivity(i);

                return true;

            case R.id.settings:

                Intent i1 = new Intent(PhotoOpenActivity.this, SavePhotoInfosPopUpActivity.class);
                i1.putExtra("project_id", project_id);
                i1.putExtra("photo_id", photo_id);
                i1.putExtra("photo_path", photo_path);
                i1.putExtra("redirect", "SETTINGS");

                //Get name and description
                EditText et3 = (EditText) findViewById(R.id.EditTextNamePhoto);
                String newName1 = et3.getText().toString();
                EditText et4 = (EditText) findViewById(R.id.EditTextDescrPhoto);
                String newDescr1 = et4.getText().toString();
                i1.putExtra("name", newName1);
                i1.putExtra("descr", newDescr1);

                startActivity(i1);

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
        PopUpDetails pud = new PopUpDetails(PhotoOpenActivity.this, name, descr, mi);
        pud.show();
    }


    /**
     * Obtenir le projet ouvert
     *
     * @return Projet ouvert
     */
    public Project getProject() {
        ProjectBDD pbdd = new ProjectBDD(PhotoOpenActivity.this); //Instanciation de ProjectBdd pour manipuler les projets de la base de données
        pbdd.open(); //Ouverture de la base de données
        Project p = pbdd.getProjectById(project_id); // Récupération du projet
        pbdd.close(); // Fermeture de la base de données
        return p;
    }

}
