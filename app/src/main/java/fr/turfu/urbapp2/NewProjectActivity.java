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


public class NewProjectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_project);

        //Handling toolbar
        Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mainToolbar.setTitle("");
        mainToolbar.setSubtitle("");

        //Bouton de localisation
        Button bouton = (Button) findViewById(R.id.buttonLocalisationProject);
        bouton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                popUp();

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
    //TODO : ajouter la carte dans la pop-up
    public void popUp() {
        CustomPopUp cdd=new CustomPopUp(NewProjectActivity.this);
        cdd.show();
    }

}


