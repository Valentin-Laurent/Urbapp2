package fr.turfu.urbapp2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by Laura on 10/10/2016.
 */
public class PhotoOpenActivity extends AppCompatActivity {

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
        String path = intent.getStringExtra("photo_path");

        File imgFile = new File(Environment.getExternalStorageDirectory(), path);
        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        i.setImageBitmap(myBitmap);


    }
}
