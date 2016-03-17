package fr.turfu.urbapp2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.MenuItem;

import java.util.List;

import fr.turfu.urbapp2.R;
import fr.turfu.urbapp2.db.LocalDataSource;
import fr.turfu.urbapp2.db.Project;

public class MainActivity extends AppCompatActivity {

    /**
     * Attribut representing the local database
     */
    public static LocalDataSource datasource;
    ListView mListView ;
    String[] maListe = new String[]{
            "Valentin", "is big", "shit"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
     setSupportActionBar(mainToolbar);

     // test de recup des noms de projets: en espérant qu'il y en ait dans la db locale...........
        datasource = new LocalDataSource(this);
      //  List<Project> projs = datasource.getAllProjects();
     //   Project proj = projs.get(0);
     //   maListe[3]=proj.getProjectName();
        mListView = (ListView) findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, maListe);
        mListView.setAdapter(adapter);

    }


    /**
     * Method to inflate the xml menu file
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
        switch (item.getItemId()) {
            case R.id.home:
                return true;

            case R.id.virtual_reality:
                return true;

            case R.id.settings:
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
