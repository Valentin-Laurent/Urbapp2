package fr.turfu.urbapp2;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import fr.turfu.urbapp2.db.Photo;
import fr.turfu.urbapp2.db.PhotoBDD;


/**
 * Activity in which users can choose between adding a photo from the gallery or taking a new photo with the camera
 */
public class NewPhotoPopUpActivity extends Activity {

    /**
     * Boite de dialogue
     */
    public Dialog d;

    /**
     * Bouton cancel
     */
    public Button no;

    /**
     * Bouton pour ouvrir la galerie photo
     */
    public Button galery;


    /**
     * Bouton pour ouvrir l'appareil photo
     */
    public Button camera;

    /**
     * Path de la photo en cours d'ouverture
     */
    private String currentPhotoPath;


    /**
     * Id du projet
     */
    private long project_id;

    private static final int CAMERA_REQUEST = 0;
    private static final int GALLERY_REQUEST = 1;


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
        setContentView(R.layout.new_photo_pop_up);

        //Création des boutons
        no = (Button) findViewById(R.id.btn_close);
        galery = (Button) findViewById(R.id.btn_galery);
        camera = (Button) findViewById(R.id.btn_camera);

        //Ajout des actions
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        galery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gallery(); //ouverture de la galerie

                //Sauvegarde de la photo
                if (currentPhotoPath != null) {
                    Intent i = new Intent(NewPhotoPopUpActivity.this, PhotoOpenActivity.class);
                    i.putExtra("photo_path", currentPhotoPath);

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(NewPhotoPopUpActivity.this);
                    String aut = preferences.getString("user_preference", "");
                    Photo p = new Photo(currentPhotoPath, project_id, aut);

                    PhotoBDD pbdd = new PhotoBDD(NewPhotoPopUpActivity.this);
                    pbdd.open();
                    pbdd.insert(p);
                    pbdd.close();

                    startActivity(i);
                }
                finish();
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera(); //Ouverture de l'appareil photo

                //Sauvegarde de la photo
                if (currentPhotoPath != null) {
                    Intent i = new Intent(NewPhotoPopUpActivity.this, PhotoOpenActivity.class);
                    i.putExtra("photo_path", currentPhotoPath);

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(NewPhotoPopUpActivity.this);
                    String aut = preferences.getString("user_preference", "");
                    Photo p = new Photo(currentPhotoPath, project_id, aut);

                    PhotoBDD pbdd = new PhotoBDD(NewPhotoPopUpActivity.this);
                    pbdd.open();
                    pbdd.insert(p);
                    pbdd.close();

                    startActivity(i);
                }
                finish();
            }
        });

        //Ajout du project_id
        final Intent intent = getIntent();
        project_id = intent.getLongExtra("project_id", 0);
    }

    /**
     * Méthode pour lancer l'appareil photo
     */
    public void camera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, CAMERA_REQUEST);
    }


    /**
     * Méthode pour ouvrir la galerie
     */
    public void gallery() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, GALLERY_REQUEST);
    }

    /**
     * Obtention du path de la nouvelle photo lors de son ouverture
     * @param requestCode Requete : prise de photo avec la camera ou ouverture de la galerie
     * @param resultCode Photo choisie
     * @param data Données obtenues
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if ((requestCode == CAMERA_REQUEST || requestCode == GALLERY_REQUEST) && resultCode == Activity.RESULT_OK) {


        }

    }

}
