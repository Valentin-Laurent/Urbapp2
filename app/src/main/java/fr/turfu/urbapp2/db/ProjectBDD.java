package fr.turfu.urbapp2.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe qui permet de manipuler les projets enregistrés dans la base de données locale
 */

public class ProjectBDD {

    private SQLiteDatabase bdd;
    private MySQLiteHelper sqlHelper;

    /**
     * Constructeur
     *
     * @param context contexte de l'activité
     */
    public ProjectBDD(Context context) {
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
     * @return Database
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
        Cursor c = bdd.query(MySQLiteHelper.TABLE_PROJECT, new String[]{MySQLiteHelper.COLUMN_PROJECTID, MySQLiteHelper.COLUMN_PROJECTVERSION, MySQLiteHelper.COLUMN_PROJECTNAME, MySQLiteHelper.COLUMN_PROJECTDESCRIPTION, MySQLiteHelper.COLUMN_GPSGEOMID, MySQLiteHelper.COLUMN_PROJECTISAVAILABLE}, "project_name" + " LIKE \"" + n + "\"", null, null, null, null);
        if (c.getCount() != 0) {
            c.moveToFirst();
        }
        return cursorToProject(c);
    }

    /**
     * Get a project with his id
     *
     * @param n id of a project
     * @return Project
     */
    public Project getProjectById(long n) {
        Cursor c = bdd.query(MySQLiteHelper.TABLE_PROJECT, new String[]{MySQLiteHelper.COLUMN_PROJECTID, MySQLiteHelper.COLUMN_PROJECTVERSION, MySQLiteHelper.COLUMN_PROJECTNAME, MySQLiteHelper.COLUMN_PROJECTDESCRIPTION, MySQLiteHelper.COLUMN_GPSGEOMID, MySQLiteHelper.COLUMN_PROJECTISAVAILABLE}, "project_id" + " == " + n + "", null, null, null, null);
        if (c.getCount() != 0) {
            c.moveToFirst();
        }
        return cursorToProject(c);
    }


    /**
     * Transformer un curseur en projet
     *
     * @param c Curseur
     * @return Projet
     */
    private Project cursorToProject(Cursor c) {
        //si aucun élément n'a été retourné dans la requête, on renvoie null
        if (c.getCount() == 0) {
            return null;
        } else {

            //On créé un projet
            Project p = new Project();

            //on lui affecte toutes les infos grâce aux infos contenues dans le Cursor
            p.setProjectId(c.getInt(0));
            p.setProjectVersion(c.getInt(1));
            p.setProjectName(c.getString(2));
            p.setProjectDescription(c.getString(3));
            p.setGpsGeom_id(c.getLong(4));
            p.setProjectIsavailable(c.getInt(5) > 0);

            //On retourne le projet
            return p;
        }
    }

    /**
     * Ajout d'un projet p à la base de données
     *
     * @param p Project
     * @return id du projet
     */
    public long insert(Project p) {

        ContentValues values = new ContentValues();

        values.put(MySQLiteHelper.COLUMN_PROJECTNAME, p.getProjectName());
        values.put(MySQLiteHelper.COLUMN_PROJECTVERSION, p.getVersion());
        values.put(MySQLiteHelper.COLUMN_PROJECTDESCRIPTION, p.getProjectDescription());
        values.put(MySQLiteHelper.COLUMN_GPSGEOMID, p.getGpsGeom_id());
        values.put(MySQLiteHelper.COLUMN_PROJECTISAVAILABLE, p.getIsAvailable());
        return bdd.insert(MySQLiteHelper.TABLE_PROJECT, null, values);
    }


    /**
     * Lister les projets de la base de données
     *
     * @return Liste des projets
     */
    public List<Project> getProjects() {

        Cursor cursor = bdd.query(MySQLiteHelper.TABLE_PROJECT, new String[]{MySQLiteHelper.COLUMN_PROJECTID, MySQLiteHelper.COLUMN_PROJECTVERSION, MySQLiteHelper.COLUMN_PROJECTNAME, MySQLiteHelper.COLUMN_PROJECTDESCRIPTION, MySQLiteHelper.COLUMN_GPSGEOMID, MySQLiteHelper.COLUMN_PROJECTISAVAILABLE}, null, null, null, null, MySQLiteHelper.COLUMN_PROJECTNAME + "  ASC");

        List<Project> lp = new ArrayList<>();

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Project p = cursorToProject(cursor);
            lp.add(p);
        }
        cursor.close();

        return lp;
    }

    /**
     * Mise à jour d'un projet
     *
     * @param p Nouveau projet à synchroniser
     */
    public void update(Project p) {
        bdd.execSQL("UPDATE Project SET project_name='" + p.getProjectName() + "' WHERE project_id =" + p.getProjectId());
        bdd.execSQL("UPDATE Project SET project_description= '" + p.getProjectDescription() + "' WHERE project_id =" + p.getProjectId());
    }



}
