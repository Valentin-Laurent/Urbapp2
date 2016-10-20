package fr.turfu.urbapp2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Laura on 10/10/2016.
 */
public class ElementDefinitionPopUp extends Activity {


    /**
     * Id de la photo ouverte
     */
    private long photo_id;

    /**
     * PixelGeom à définir
     */
    private long pixelGeom_id;

    /**
     * Boutons ok et cancel
     */
    public Button yes, no;

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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.element_definition_pop_up);


        //Photo ID
        final Intent intent = getIntent();
        photo_id = intent.getLongExtra("photo_id", 0);

        //PixelGeom
        pixelGeom_id = intent.getLongExtra("pixelGeom_id", 0);

        //Ajout de la liste des matériaux et de la liste des types d'éléments
        List mat = new ArrayList();
        mat.add("Bois");
        mat.add("Béton");
        mat.add("Verre");

        ArrayAdapter adapter = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                mat
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spin = (Spinner) findViewById(R.id.MaterialValue);
        spin.setAdapter(adapter);

        List elemType = new ArrayList();
        elemType.add("Sol");
        elemType.add("Mur");
        elemType.add("Toit");

        ArrayAdapter adapter1 = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                elemType
        );
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spin1 = (Spinner) findViewById(R.id.TypeValue);
        spin1.setAdapter(adapter1);

        //Affichage des informations de la zone


        //Boutons

    }


}
