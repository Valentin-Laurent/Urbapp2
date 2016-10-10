package fr.turfu.urbapp2;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.Button;


/**
 * Created by Laura on 10/10/2016.
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
                galery(); //ouverture de la galerie
                if (currentPhotoPath != null) {
                    Intent i = new Intent(NewPhotoPopUpActivity.this, PhotoOpenActivity.class);
                    i.putExtra("photo_path", currentPhotoPath);
                    //TODO : save photo in local database
                    startActivity(i);
                }
                finish();
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera(); //Ouverture de l'appareil photo
                if (currentPhotoPath != null) {
                    Intent i = new Intent(NewPhotoPopUpActivity.this, PhotoOpenActivity.class);
                    i.putExtra("photo_path", currentPhotoPath);
                    //TODO : save photo in local database
                    startActivity(i);
                }
                finish();
            }
        });
    }

    /**
     * Méthode pour lancer l'appareil photo
     */
    public void camera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, 1);

        //TODO : enregistrer la photo + set currentPhotoPath
    }


    /**
     * Méthode pour ouvrir la galerie
     */
    public void galery() {
        Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 2);
    //TODO : enregistrer la photo+ set currentPhotoPath
    }

}
