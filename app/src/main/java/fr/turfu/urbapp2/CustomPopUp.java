package fr.turfu.urbapp2;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

/**
 * Class CustomPopUp
 * PopUp comprenant un titre, une carte et deux boutons (ok et cancel).
 * Elle est destinée à la géolocalisation d'un projet et sera affichée dans l'activité NewProjectActivity
 */
public class CustomPopUp extends Dialog implements
        android.view.View.OnClickListener {

    /**
     * Activité dans laquelle on affiche la pop up
     */
    public Activity c;

    /**
     * Boite de dialogue
     */
    public Dialog d;

    /**
     * Boutons ok et cancel
     */
    public Button yes, no;

    /**
     * Constructeur
     * @param a Activité
     */
    public CustomPopUp(Activity a) {
        super(a);
        this.c = a;
    }

    /**
     * Création de la pop up.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Mise en place du layout
        setContentView(R.layout.localisation_pop_up);
        //Création des boutons
        yes = (Button) findViewById(R.id.btn_ok);
        no = (Button) findViewById(R.id.btn_cancel);
        //Ajout des actions
        yes.setOnClickListener(this);
        no.setOnClickListener(this);

    }

    /**
     * Mise en place des actions sur les deux boutons :
     * Pour le bouton cancel, on démissionne de cette activité et on n'enregistre aucune modification effectuée sur la carte.
     * Pour le bouton ok, on modifie la zone du projet avec les modification apportées.
     * @param v Vue représentant la pop up elle-même
     */
    @Override
    //TODO : Quand on clique sur le bouton ok, valider les changements
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                //TODO
                dismiss();
                break;
            case R.id.btn_cancel:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }
}