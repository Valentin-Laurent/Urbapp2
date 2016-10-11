package fr.turfu.urbapp2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import fr.turfu.urbapp2.db.Project;
import fr.turfu.urbapp2.db.ProjectBDD;

/**
 * Activité pour la création d'un nouveau projet
 */
public class NewProjectActivity extends AppCompatActivity {

    /**
     * Création de l'activité
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Ajout du layout
        setContentView(R.layout.activity_new_project);

        //Mise en place de la toolbar
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mainToolbar.setTitle("");
        mainToolbar.setSubtitle("");

        //Bouton de localisation qui affiche une pop up
        Button bouton = (Button) findViewById(R.id.buttonLocalisationProject);
        bouton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                popUp();
            }
        });

        //Bouton de validation
        Button valid = (Button) findViewById(R.id.ButtonValidProject);
        valid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText et1 = (EditText) findViewById(R.id.EditTextName);
                String name = et1.getText().toString();

                EditText et2 = (EditText) findViewById(R.id.EditTextDescr);
                String descr = et2.getText().toString();

                if(control(name)) {
                    save(name, descr);
                    Intent intent = new Intent(NewProjectActivity.this, ProjectOpenActivity.class);
                    intent.putExtra("projectName", name);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }


    /**
     * Method to inflate the xml menu file (Ajout des différents onglets dans la toolbar)
     *
     * @param menu the menu
     * @return true if everything went good
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //On sérialise le fichier menu.xml pour l'afficher dans la barre de menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        //Display Username
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String u = preferences.getString("user_preference", "");
        MenuItem i = menu.findItem(R.id.connectedAs);
        i.setTitle(u);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Method to handle the clicks on the items of the toolbar
     *
     * @param item the item
     * @return true if everything went good
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.home:
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;

            case R.id.settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Lancement de la pop up de localisation
     */
    public void popUp() {
        CustomPopUp cdd = new CustomPopUp(NewProjectActivity.this);
        cdd.show();
    }

    /**
     * Sauvegarde d'un nouveau projet
     */

    // TODO : prendre en compte la géolocalisation
    public void save(String name, String descr) {

        ProjectBDD pbdd = new ProjectBDD(NewProjectActivity.this);

        pbdd.open();

        Project p = pbdd.getProjectByName(name);
        long id = 0;

        if (p == null) {
            Project p1 = new Project(name, descr,1);
            pbdd.insert(p1);
            id = p1.getProjectId();

            Intent intent = new Intent(NewProjectActivity.this, ProjectOpenActivity.class);
            intent.putExtra("projectName", p1.getProjectName());
            startActivity(intent);
            finish();

        } else {
            id = p.getProjectId();
            Toast.makeText(this, R.string.project_already_created, Toast.LENGTH_SHORT).show();
        }
        pbdd.close();
    }


    /**
     * Méthode pour verifier que les champs du formulaire sont bien remplis : project_name non vide et géolocalisation effectuée
     * @param n Project_name
     * @return Boolean
     */
    // TODO : Prendre en compte la géolocalisation
    public boolean control(String n){
        if (n.matches("")) {
            Toast.makeText(this, R.string.empty_project_name, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }





}


