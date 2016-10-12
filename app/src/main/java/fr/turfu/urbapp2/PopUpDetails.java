package fr.turfu.urbapp2;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import fr.turfu.urbapp2.DB.Project;
import fr.turfu.urbapp2.DB.ProjectBDD;

/**
 * Pop up dans laquelle sont récapitulées toutes les information d'un projet
 */
public class PopUpDetails extends Dialog implements android.view.View.OnClickListener {

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
     * Nom du projet
     */
    private String name;

    /**
     * Description du projet
     */
    private String descr;

    /**
     * Menu Item of the activity
     */
    private MenuItem mi;


    /**
     * Constructeur
     *
     * @param a  Activité
     * @param n  Nom du projet
     * @param d  Description du projet
     * @param mi Menu item de l'activité dans lequel est inscrit le nom du projet
     */
    public PopUpDetails(Activity a, String n, String d, MenuItem mi) {
        super(a);
        this.c = a;
        this.name = n;
        this.descr = d;
        this.mi = mi;
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
        setContentView(R.layout.see_details_pop_up);

        //Ajout des informations
        EditText et1 = (EditText) findViewById(R.id.NameValue);
        et1.setText(name);

        EditText et2 = (EditText) findViewById(R.id.DescrValue);
        et2.setText(descr);

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
     *
     * @param v Vue représentant la pop up elle-même
     */
    @Override
    //TODO : prendre en compte la carte
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                //On récupère les changements
                EditText et1 = (EditText) findViewById(R.id.NameValue);
                String newName = et1.getText().toString();
                EditText et2 = (EditText) findViewById(R.id.DescrValue);
                String newDescr = et2.getText().toString();

                //Mise à jour du projet
                ProjectBDD pbdd = new ProjectBDD(this.getContext()); //Instanciation de ProjectBdd pour manipuler les projets de la base de données
                pbdd.open(); //Ouverture de la base de données
                Project p = pbdd.getProjectByName(name); // Récupération du projet

                //Vérification de l'unicité du nom du projet
                Project p1 = pbdd.getProjectByName(newName);
                if (p1 == null || p1.getProjectId() == p.getProjectId()) {
                    p.setProjectName(newName); //Mise à jour du nom
                } else {
                    Toast.makeText(c, R.string.projectName_taken, Toast.LENGTH_SHORT).show();
                }

                p.setProjectDescription(newDescr); //Mise à jour de la description

                pbdd.update(p); //Mise à jour

                pbdd.close(); // Fermeture de la base de données

                //Mise à jour de l'affichage
                mi.setTitle("Projet : " + p.getProjectName());

                //Fermeture de la pop up
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