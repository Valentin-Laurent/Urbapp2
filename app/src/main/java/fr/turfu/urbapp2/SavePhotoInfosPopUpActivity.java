package fr.turfu.urbapp2;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import fr.turfu.urbapp2.db.Photo;
import fr.turfu.urbapp2.db.PhotoBDD;
import fr.turfu.urbapp2.db.Project;
import fr.turfu.urbapp2.db.ProjectBDD;


/**
 * Activity in which users can choose between adding a photo from the gallery or taking a new photo with the camera
 */
public class SavePhotoInfosPopUpActivity extends Activity {

    /**
     * Boite de dialogue
     */
    public Dialog d;

    /**
     * Bouton cancel, valider ou close
     */
    public Button no, yes, close;

    /**
     * Path de la photo en cours d'ouverture
     */
    private String currentPhotoPath;


    /**
     * Id du projet
     */
    private long project_id;

    /**
     * Photo id
     */
    private long photo_id;

    /**
     * Nom de la photo
     */
    private String newName;

    /**
     * Description de la photo
     */
    private String newDescr;


    /**
     * Redirection
     */
    private String redirect;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    /**
     * Création de la pop up.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Creation
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Mise en place du layout
        setContentView(R.layout.save_pop_up);

        //Création des boutons
        no = (Button) findViewById(R.id.btn_cancel_change);
        yes = (Button) findViewById(R.id.btn_save_change);
        close = (Button) findViewById(R.id.btn_close_pop);

        //paramètres
        final Intent intent = getIntent();
        project_id = intent.getLongExtra("project_id", 0);
        photo_id = intent.getLongExtra("photo_id", 0);
        currentPhotoPath = intent.getStringExtra("photo_path");
        redirect = intent.getStringExtra("redirect");
        newName = intent.getStringExtra("name");
        newDescr = intent.getStringExtra("descr");

        //Ajout des actions
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirect();
            }
        });

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Sauvegarde
                PhotoBDD pbdd = new PhotoBDD(SavePhotoInfosPopUpActivity.this); //Instanciation de ProjectBdd pour manipuler les projets de la base de données
                pbdd.open(); //Ouverture de la base de données
                Photo p = pbdd.getPhotoById(photo_id); // Récupération de la photo

                //Vérification de l'unicité du nom de la photo
                Photo p1 = pbdd.getPhotoByName(newName);
                if (p1 == null || p1.getPhoto_id() == p.getPhoto_id()) {
                    p.setPhoto_name(newName); //Mise à jour du nom
                } else {
                    Toast.makeText(SavePhotoInfosPopUpActivity.this, R.string.photoName_taken, Toast.LENGTH_SHORT).show();
                }

                p.setPhoto_description(newDescr); //Mise à jour de la description

                pbdd.updatePhotoInfos(p); //Mise à jour

                pbdd.close(); // Fermeture de la base de données

                //Redirection
                redirect();
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirect = "PHOTO_OPEN";
                redirect();
            }
        });
    }

    /**
     * Redirection vers l'activité demandée
     */
    public void redirect() {
        switch (redirect) {
            case "PHOTO_OPEN":
                Intent i = new Intent(SavePhotoInfosPopUpActivity.this, PhotoOpenActivity.class);
                i.putExtra("project_id", project_id);
                i.putExtra("photo_id", photo_id);
                i.putExtra("photo_path", currentPhotoPath);
                startActivity(i);
                finish();
                break;
            case "HOME":
                Intent i1 = new Intent(SavePhotoInfosPopUpActivity.this, MainActivity.class);
                startActivity(i1);
                finish();
                break;
            case "SETTINGS":
                Intent i2 = new Intent(SavePhotoInfosPopUpActivity.this, SettingsActivity.class);
                startActivity(i2);
                finish();
                break;
            case "PROJECT_OPEN":
                Intent i3 = new Intent(SavePhotoInfosPopUpActivity.this, ProjectOpenActivity.class);
                i3.putExtra("project_id", project_id);

                ProjectBDD pbdd = new ProjectBDD(SavePhotoInfosPopUpActivity.this); //Instanciation de ProjectBdd pour manipuler les projets de la base de données
                pbdd.open(); //Ouverture de la base de données
                Project p = pbdd.getProjectById(project_id); // Récupération du projet
                pbdd.close(); // Fermeture de la base de données

                i3.putExtra("projectName",p.getProjectName());
                startActivity(i3);
                finish();
                break;
            default:
                break;
        }

    }


}

