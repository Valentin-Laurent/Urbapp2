/**
 * Activité NewPhotoPopUp
 * ------------------------
 * Activity in which users can choose between adding a photo from the gallery or taking a new photo with the camera
 */

package fr.turfu.urbapp2;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import fr.turfu.urbapp2.DB.Photo;
import fr.turfu.urbapp2.DB.PhotoBDD;
import fr.turfu.urbapp2.DB.ProjectBDD;
import fr.turfu.urbapp2.Request.Request;
import fr.turfu.urbapp2.Tools.Utils;

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


    /**
     * Constantes
     */
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
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera(); //Ouverture de l'appareil photo
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
     *
     * @param requestCode Requete : prise de photo avec la camera ou ouverture de la galerie
     * @param resultCode  Photo choisie
     * @param data        Données obtenues
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {

            //Setting the directory for the save of the picture to featureapp
            File folder = new File(Environment.getExternalStorageDirectory(), "featureapp/");
            folder.mkdirs();

            //Get the date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String currentDateandTime = sdf.format(new Date());
            String path = "featureapp/Photo_" + currentDateandTime;

            // CAMERA
            if (requestCode == CAMERA_REQUEST) {
                path = path + ".png";
                currentPhotoPath = path;
                Bitmap bit = (Bitmap) data.getExtras().get("data");
                File photo = new File(Environment.getExternalStorageDirectory(), path);

                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    bit.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.close();
                    OutputStream outputStream = new FileOutputStream(photo);
                    out.writeTo(outputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //GALLERY
            if (requestCode == GALLERY_REQUEST) {
                path = path + ".jpg";
                String url = Utils.getRealPathFromURI(NewPhotoPopUpActivity.this, data.getData());
                File photo = new File(Environment.getExternalStorageDirectory(), path);
                Utils.copy(new File(url), photo);
                currentPhotoPath = path;
            }

            savePhoto();
        }

    }

    /**
     * Méthode pour enregistrer la photo et l'ajouter à la base de donnée locale
     */
    public void savePhoto() {
        if (currentPhotoPath != null) {
            Intent i = new Intent(NewPhotoPopUpActivity.this, PhotoOpenActivity.class);
            i.putExtra("photo_path", currentPhotoPath);
            i.putExtra("project_id", project_id);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(NewPhotoPopUpActivity.this);
            String aut = preferences.getString("user_preference", "");

            ProjectBDD prbdd = new ProjectBDD(NewPhotoPopUpActivity.this);
            prbdd.open();
            long gpsid = prbdd.getMaxGpsgeomId() + 1;
            prbdd.insertGpsgeom("", gpsid);
            prbdd.close();

            PhotoBDD pbdd = new PhotoBDD(NewPhotoPopUpActivity.this);
            pbdd.open();
            long id = pbdd.getMaxPhotoId();
            Photo p = new Photo(currentPhotoPath, project_id, aut, id + 1, gpsid);
            pbdd.insert(p);
            pbdd.close();

            //Export sur serveur
            Request.upload(this,currentPhotoPath);

            i.putExtra("photo_id", p.getPhoto_id());
            startActivity(i);
            finish();
        }
    }

}

