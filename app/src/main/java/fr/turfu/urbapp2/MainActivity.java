package fr.turfu.urbapp2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.MenuItem;

import fr.turfu.urbapp2.db.LocalDataSource;

public class MainActivity extends AppCompatActivity {

    /**
     * Attribut representing the local database, to try to get data from it
     */
    public static LocalDataSource datasource;
    ListView mListView ;
    /**
     * Projects list
     */
    String[] list_projs = new String[]{
            "Valentin", "is big", "shit"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

     Toolbar mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
     setSupportActionBar(mainToolbar);

     // trying to create and then get projects from DB... not working, nullpointerexception
        datasource = new LocalDataSource(this);
       // datasource.createProject(21, "TEST");

     //displaying the list
        mListView = (ListView) findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, list_projs);
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
