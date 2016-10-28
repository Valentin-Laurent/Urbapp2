package fr.turfu.urbapp2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import fr.turfu.urbapp2.DB.Element;
import fr.turfu.urbapp2.DB.ElementBDD;
import fr.turfu.urbapp2.Tools.ColorPicker;

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
        ElementBDD ebdd = new ElementBDD(ElementDefinitionPopUp.this);
        ebdd.open();

        List mat = ebdd.getMaterials();
        /*mat.add("Bois");
        mat.add("Béton");
        mat.add("Verre");*/

        ArrayAdapter adapter = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                mat
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spin = (Spinner) findViewById(R.id.MaterialValue);
        spin.setAdapter(adapter);

        ArrayList<String> elemType = ebdd.getTypes();
        ebdd.close();
        /*elemType.add("Sol");
        elemType.add("Mur");
        elemType.add("Toit");*/

        ArrayAdapter adapter1 = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                elemType
        );
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spin1 = (Spinner) findViewById(R.id.TypeValue);
        spin1.setAdapter(adapter1);

        //Affichage des informations de la zone

        //On cherche l'élément du pixelGeom dans la liste
        Element e = null;

        for (Element el : ElementDefinitionActivity.elements) {
            if (el.getPixelGeom_id() == pixelGeom_id) {
                e = el;
            }
        }
        for (Element el : ElementDefinitionActivity.newElements) {
            if (el.getPixelGeom_id() == pixelGeom_id) {
                e = el;
            }
        }

        if (e != null) {
            //Affichage du type de surface
            spin1.setSelection((int) (e.getElementType_id()));
            Log.v("type", e.getElementType_id() + "");
            //Affichage du matériau
            spin.setSelection((int) e.getMaterial_id());
            Log.v("mat", e.getMaterial_id() + "");
            //Affichage de la couleur
            ColorPicker colorPicker = (ColorPicker) findViewById(R.id.colorPicker);
            colorPicker.setColor(Integer.parseInt(e.getElement_color()));
        }

        //Boutons
        no = (Button) findViewById(R.id.btn_cancel);
        yes = (Button) findViewById(R.id.btn_ok);

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Element e = null;

                int index = -1;
                for (Element el : ElementDefinitionActivity.elements) {
                    if (el.getPixelGeom_id() == pixelGeom_id) {
                        e = el;
                        index = ElementDefinitionActivity.elements.indexOf(el);
                    }
                }

                if (index >= 0) {
                    ElementDefinitionActivity.elements.remove(index);
                }

                index = -1;
                for (Element el : ElementDefinitionActivity.newElements) {
                    if (el.getPixelGeom_id() == pixelGeom_id) {
                        e = el;
                        index = ElementDefinitionActivity.newElements.indexOf(el);
                    }
                }
                if (index >= 0) {
                    ElementDefinitionActivity.newElements.remove(index);
                }

                if (e != null) {
                    Spinner spin = (Spinner) findViewById(R.id.MaterialValue);
                    e.setMaterial_id(spin.getSelectedItemId());

                    Spinner spin1 = (Spinner) findViewById(R.id.TypeValue);
                    e.setElementType_id(spin1.getSelectedItemId());

                    ColorPicker colorPicker = (ColorPicker) findViewById(R.id.colorPicker);
                    e.setElement_color(colorPicker.getColor() + "");
                } else {
                    e = new Element();

                    Spinner spin = (Spinner) findViewById(R.id.MaterialValue);
                    e.setMaterial_id(spin.getSelectedItemId());

                    Spinner spin1 = (Spinner) findViewById(R.id.TypeValue);
                    e.setElementType_id(spin1.getSelectedItemId());

                    ColorPicker colorPicker = (ColorPicker) findViewById(R.id.colorPicker);
                    e.setElement_color(colorPicker.getColor() + "");

                    e.setPixelGeom_id(pixelGeom_id);
                    e.setPhoto_id(photo_id);

                    ElementBDD ebdd = new ElementBDD(ElementDefinitionPopUp.this);
                    ebdd.open();
                    long id = ebdd.getMaxElementId();
                    ebdd.close();
                    if (ElementDefinitionActivity.newElements.isEmpty()) {
                        id = id + 1;
                    } else {
                        id = ElementDefinitionActivity.newElements.get(ElementDefinitionActivity.newElements.size() - 1).getPixelGeom_id() + 1;
                    }
                    e.setElement_id(id);
                }

                ElementDefinitionActivity.newElements.add(e);
                finish();
            }
        });


    }


}
