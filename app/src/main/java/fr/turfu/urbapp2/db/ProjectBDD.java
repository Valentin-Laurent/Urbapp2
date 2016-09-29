package fr.turfu.urbapp2.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Laura on 29/09/2016.
 */

public class ProjectBDD {

    private SQLiteDatabase bdd;
    private MySQLiteHelper sqlHelper;
    public static final String DATABASE_NAME = "local3.db";
    public static final int DATABASE_VERSION = 3;


    /**
     * Constructeur
     * @param context
     */
    public ProjectBDD(Context context){
        sqlHelper = new MySQLiteHelper(context);
    }


    /**
     * Ouverture de la base de données en écriture
     */
    public void open() {
        bdd = sqlHelper.getWritableDatabase();
    }

    /**
     * Fermeture de la base de données
     */
    public void close() {
        bdd.close();
    }

    /**
     * Getter pour la base de données
     *
     * @return
     */
    public SQLiteDatabase getBDD() {
        return bdd;
    }


    /**
     * Get a project with his name
     *
     * @param n Name of the project
     * @return Project
     */
    public Project getProjectByName(String n) {
        Cursor c = bdd.query(MySQLiteHelper.TABLE_PROJECT, new String[]{MySQLiteHelper.COLUMN_PROJECTID, MySQLiteHelper.COLUMN_PROJECTNAME, MySQLiteHelper.COLUMN_PROJECTDESCRIPTION, MySQLiteHelper.COLUMN_GPSGEOMID}, "project_name" + " LIKE \"" + n + "\"", null, null, null, null);
        return cursorToProject(c);
    }


    /**
     * Transformer un curseur en projet
     * @param c Curseur
     * @return Projet
     */
    private Project cursorToProject(Cursor c) {
        //si aucun élément n'a été retourné dans la requête, on renvoie null
        if (c.getCount() == 0) {
            return null;
        } else {
            //Sinon on se place sur le premier élément
            c.moveToFirst();
            //On créé un projet
            Project p = new Project();
            //on lui affecte toutes les infos grâce aux infos contenues dans le Cursor
            p.setProjectId(c.getInt(0));
            p.setProjectName(c.getString(1));
            p.setProjectDescription(c.getString(2));
            p.setGpsGeom_id(c.getLong(3));
            //On ferme le cursor
            c.close();

            //On retourne le projet
            return p;
        }
    }


    public long insert(Project p){

        ContentValues values = new ContentValues();

        values.put(MySQLiteHelper.COLUMN_PROJECTNAME, p.getProjectName());
        values.put(MySQLiteHelper.COLUMN_PROJECTDESCRIPTION, p.getProjectDescription());
        values.put(MySQLiteHelper.COLUMN_GPSGEOMID, p.getGpsGeom_id());

        return bdd.insert(MySQLiteHelper.TABLE_PROJECT, null, values);
    }


}
