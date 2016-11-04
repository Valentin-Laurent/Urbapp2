/**
 * Classe ProjectBDD
 * -------------------------------------------------------------------------------------
 * Classe qui permet de manipuler les projets enregistrés dans la base de données locale
 */


package fr.turfu.urbapp2.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

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

        Cursor c = bdd.rawQuery("SELECT MAX(project_id) FROM Project", new String[]{});
        if (c.getCount() != 0) {
            c.moveToFirst();
            p.setProjectId(c.getLong(0) + 1);
        } else {
            p.setProjectId(1);
        }

        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_PROJECTID, p.getProjectId());
        values.put(MySQLiteHelper.COLUMN_PROJECTNAME, p.getProjectName());
        values.put(MySQLiteHelper.COLUMN_PROJECTVERSION, p.getVersion());
        values.put(MySQLiteHelper.COLUMN_PROJECTDESCRIPTION, p.getProjectDescription());
        values.put(MySQLiteHelper.COLUMN_GPSGEOMID, p.getGpsGeom_id());
        values.put(MySQLiteHelper.COLUMN_PROJECTISAVAILABLE, p.getIsAvailable());
        return bdd.insert(MySQLiteHelper.TABLE_PROJECT, null, values);
    }


    /**
     * Ajout d'un projet p à la base de données
     *
     * @param p Project
     * @return id du projet
     */
    public long insertwithId(Project p) {

        ContentValues values = new ContentValues();

        values.put(MySQLiteHelper.COLUMN_PROJECTID, p.getProjectId());
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


    /**
     * Récupérer le gpsgeom d'un projet
     *
     * @param id
     * @return
     */
    public GpsGeom getGpsGeom(long id) {
        Project p = getProjectById(id);
        long gpid = p.getGpsGeom_id();

        Cursor cursor = bdd.query(MySQLiteHelper.TABLE_GPSGEOM, new String[]{MySQLiteHelper.COLUMN_GPSGEOMCOORD, MySQLiteHelper.COLUMN_GPSGEOMID}, "gpsGeom_id" + " =" + gpid, null, null, null, null);
        cursor.moveToFirst();
        GpsGeom gps = new GpsGeom();
        gps.setGpsGeomCoord(cursor.getString(0));
        gps.setGpsGeomId(cursor.getLong(1));

        return gps;
    }

    /**
     * Ajout d'un gpsGeom
     *
     * @param thegeom Géométrie à ajouter
     * @return id du gpsgeom créé
     */
    public long insertGpsgeom(String thegeom, long id) {

        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_GPSGEOMID, id);
        values.put(MySQLiteHelper.COLUMN_GPSGEOMCOORD, thegeom);
        return bdd.insert(MySQLiteHelper.TABLE_GPSGEOM, null, values);
    }

    /**
     * Mise à jour d'un gpsgeom
     *
     * @param gpsid Id du gpsgeom
     * @param geom  Nouvelle Géométrie
     */
    public void updateGpsgeom(long gpsid, String geom) {
        bdd.execSQL("UPDATE GpsGeom SET gpsGeom_thegeom='" + geom + "' WHERE gpsGeom_id =" + gpsid);
    }

    /**
     * Insertion d'un gpsgeom
     *
     * @param gps
     * @return
     */
    public long insertGpsGeom(GpsGeom gps) {
        if (getGpsGeomById(gps.getGpsGeomsId()) == null) {
            ContentValues values = new ContentValues();

            values.put(MySQLiteHelper.COLUMN_GPSGEOMID, gps.getGpsGeomsId());
            values.put(MySQLiteHelper.COLUMN_GPSGEOMCOORD, gps.getGpsGeomCoord());
            return bdd.insert(MySQLiteHelper.TABLE_GPSGEOM, null, values);
        } else {
            return gps.getGpsGeomsId();
        }
    }

    /**
     * Méthode pour vider entièrement la base de données locale (utilisée avant synchronisation)
     */
    public void cleanDataBase() {
        bdd.execSQL("DELETE FROM GpsGeom");
        bdd.execSQL("DELETE FROM PixelGeom");
        bdd.execSQL("DELETE FROM Element");
        bdd.execSQL("DELETE FROM Photo");
        bdd.execSQL("DELETE FROM Project");
    }

    /**
     * Obtenir un gpsgeom avec son id
     *
     * @param id id du gpsgeom
     * @return Gpsgeom
     */
    public GpsGeom getGpsGeomById(long id) {
        GpsGeom gps = null;

        Cursor cursor = bdd.query(MySQLiteHelper.TABLE_GPSGEOM, new String[]{MySQLiteHelper.COLUMN_GPSGEOMCOORD, MySQLiteHelper.COLUMN_GPSGEOMID}, "gpsGeom_id" + " =" + id, null, null, null, null);

        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            gps = new GpsGeom();
            gps.setGpsGeomCoord(cursor.getString(0));
            gps.setGpsGeomId(cursor.getLong(1));
        }

        return gps;
    }

    /**
     * Récupérer le max des id des gpsgeom
     *
     * @return id max
     */
    public long getMaxGpsgeomId() {
        Cursor c = bdd.rawQuery("SELECT MAX(gpsGeom_id) FROM GpsGeom", new String[]{});
        if (c.getCount() != 0) {
            c.moveToFirst();
            return c.getLong(0);
        } else {
            return 0;
        }
    }
}
