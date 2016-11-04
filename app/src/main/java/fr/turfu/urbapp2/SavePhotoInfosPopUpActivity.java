/**
 * Activité SavePhotoInfosPopUp
 * -----------------------------------------------------------
 * Activité (sous la forme d'une pop up) dans laquelle on demande à l'utilisateur s'il veut sauvegarder
 * les modifications qu'il a faites portant sur le nom et la descr d'une photo. On agit en conséquence.
 */
package fr.turfu.urbapp2;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import fr.turfu.urbapp2.DB.Data;
import fr.turfu.urbapp2.DB.Photo;
import fr.turfu.urbapp2.DB.PhotoBDD;
import fr.turfu.urbapp2.DB.Project;
import fr.turfu.urbapp2.DB.ProjectBDD;
import fr.turfu.urbapp2.Request.Request;

public class SavePhotoInfosPopUpActivity extends Activity implements Sync {

    /**
     * Boite de dialogue
     */
    public Dialog d;

    /**
     * Bouton cancel, save ou close
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
     * Nom de la photo
     */
    private String newName;
    /**
     * Description de la photo
     */
    private String newDescr;

    /**
     * Direction de redirection
     */
    private String redirect;


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

        //Paramètres
        final Intent intent = getIntent();
        project_id = intent.getLongExtra("project_id", 0);
        currentPhotoPath = intent.getStringExtra("photo_path");
        redirect = intent.getStringExtra("redirect");
        newName = intent.getStringExtra("name");
        newDescr = intent.getStringExtra("descr");

        //Ajout des actions

        //Bouton cancel : on redirige sans sauvegarder
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirect(false);
            }
        });

        //Bouton save : on redirige et on sauvegarde
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Sauvegarde
                PhotoBDD pbdd = new PhotoBDD(SavePhotoInfosPopUpActivity.this); //Instanciation de ProjectBdd pour manipuler les projets de la base de données
                pbdd.open(); //Ouverture de la base de données
                Photo p = pbdd.getPhotoByPath(currentPhotoPath); // Récupération de la photo
                p.setPhoto_name(newName); //Mise à jour du nom
                p.setPhoto_description(newDescr); //Mise à jour de la description
                pbdd.updatePhotoInfos(p); //Mise à jour
                pbdd.close(); // Fermeture de la base de données

                //Redirection
                redirect(true);
            }
        });

        //Bouton close : on ferme la boite de dialogue
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirect = "PHOTO_OPEN";
                redirect(false);
            }
        });
    }

    /**
     * Redirection vers l'activité demandée
     *
     * @param save booléen indiquand si il fut sauvegarder les modifications ou non
     */
    public void redirect(Boolean save) {

        //Récupération des données du projet
        Data d = Data.ToData(project_id, SavePhotoInfosPopUpActivity.this);

        switch (redirect) {
            case "PHOTO_OPEN":
                Intent i = new Intent(SavePhotoInfosPopUpActivity.this, PhotoOpenActivity.class);
                i.putExtra("project_id", project_id);
                i.putExtra("photo_path", currentPhotoPath);
                startActivity(i);
                finish();
                break;
            case "HOME":

                if (save) {
                    //Export sur serveur
                    Request.closeProject(SavePhotoInfosPopUpActivity.this, d);
                } else {
                    //Simple fermeture du projet
                    Request.close(SavePhotoInfosPopUpActivity.this, project_id);
                }

                //Redirection
                Intent i1 = new Intent(SavePhotoInfosPopUpActivity.this, MainActivity.class);
                startActivity(i1);
                finish();
                break;
            case "SETTINGS":

                if (save) {
                    //Export
                    Request.closeProject(SavePhotoInfosPopUpActivity.this, d);
                } else {
                    //Simple fermeture du projet
                    Request.close(SavePhotoInfosPopUpActivity.this, project_id);
                }

                //Redirection
                Intent i2 = new Intent(SavePhotoInfosPopUpActivity.this, SettingsActivity.class);
                startActivity(i2);
                finish();
                break;
            case "PROJECT_OPEN":

                if (save) {
                    //Export
                    Request.saveProject(SavePhotoInfosPopUpActivity.this, d);
                }

                //Redirection
                Intent i3 = new Intent(SavePhotoInfosPopUpActivity.this, ProjectOpenActivity.class);
                i3.putExtra("project_id", project_id);

                ProjectBDD pbdd = new ProjectBDD(SavePhotoInfosPopUpActivity.this); //Instanciation de ProjectBdd pour manipuler les projets de la base de données
                pbdd.open(); //Ouverture de la base de données
                Project p = pbdd.getProjectById(project_id); // Récupération du projet
                pbdd.close(); // Fermeture de la base de données

                i3.putExtra("projectName", p.getProjectName());
                startActivity(i3);
                finish();
                break;
            default:
                break;
        }

    }


    @Override
    public void updateView() {
    }
}

