package fr.turfu.urbapp2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import fr.turfu.urbapp2.R;
import fr.turfu.urbapp2.db.LocalDataSource;

public class MainActivity extends AppCompatActivity {

    /**
     * Attribut representing the local database
     */
    public static LocalDataSource datasource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        datasource = new LocalDataSource(this);

    }
}
