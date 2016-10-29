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

public class PhotoBDD {

    private SQLiteDatabase bdd;
    private MySQLiteHelper sqlHelper;

    /**
     * Constructeur
     *
     * @param context contexte de l'activité
     */
    public PhotoBDD(Context context) {
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
     * Transformer un curseur en photo
     *
     * @param c Curseur
     * @return Photo
     */
    private Photo cursorToPhoto(Cursor c) {
        //si aucun élément n'a été retourné dans la requête, on renvoie null
        if (c.getCount() == 0) {
            return null;
        } else {

            //On créé une photo
            Photo p = new Photo();

            //on lui affecte toutes les infos grâce aux infos contenues dans le Cursor
            p.setPhoto_id(c.getInt(0));
            p.setPhoto_name(c.getString(1));
            p.setProject_id(c.getLong(2));
            p.setPhoto_author(c.getString(3));
            p.setPhoto_description(c.getString(4));
            p.setPhoto_last_modification(c.getInt(5));
            p.setPhoto_path(c.getString(6));

            //On retourne la photo
            return p;
        }
    }

    /**
     * Ajout d'une photo p à la base de données
     *
     * @param p Photo
     * @return id de la photo
     */
    public long insert(Photo p) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_PHOTONAME, "");
        values.put(MySQLiteHelper.COLUMN_PHOTODESCRIPTION, "");
        values.put(MySQLiteHelper.COLUMN_PHOTOLASTMODIFICATION, 0);
        values.put(MySQLiteHelper.COLUMN_PHOTOAUTHOR, p.getPhoto_author());
        values.put(MySQLiteHelper.COLUMN_PHOTOPROJECTID, p.getProject_id());
        values.put(MySQLiteHelper.COLUMN_PHOTOPATH, p.getPhoto_path());

        return bdd.insert(MySQLiteHelper.TABLE_PHOTO, null, values);
    }

    /**
     * Get a photo with his path
     *
     * @param n Photo path
     * @return Photo
     */
    public Photo getPhotoByPath(String n) {
        Cursor c = bdd.query(MySQLiteHelper.TABLE_PHOTO, new String[]{MySQLiteHelper.COLUMN_PHOTOID, MySQLiteHelper.COLUMN_PHOTONAME, MySQLiteHelper.COLUMN_PHOTOPROJECTID, MySQLiteHelper.COLUMN_PHOTOAUTHOR, MySQLiteHelper.COLUMN_PHOTODESCRIPTION, MySQLiteHelper.COLUMN_PHOTOLASTMODIFICATION, MySQLiteHelper.COLUMN_PHOTOPATH}, "photo_path" + " LIKE \"" + n + "\"", null, null, null, null);
        if (c.getCount() != 0) {
            c.moveToFirst();
        }
        return cursorToPhoto(c);
    }

    /**
     * Mise à jour du nom et de la description d'une photo
     *
     * @param p
     */
    public void updatePhotoInfos(Photo p) {
        bdd.execSQL("UPDATE Photo SET photo_name='" + p.getPhoto_name() + "' WHERE photo_id =" + p.getPhoto_id());
        bdd.execSQL("UPDATE Photo SET photo_description='" + p.getPhoto_description() + "' WHERE photo_id =" + p.getPhoto_id());
    }

    /**
     * Get a photo with the name
     *
     * @param n Name of the photo we are looking for
     * @return the Photo
     */
    public Photo getPhotoByName(String n) {
        Cursor c = bdd.query(MySQLiteHelper.TABLE_PHOTO, new String[]{MySQLiteHelper.COLUMN_PHOTOID, MySQLiteHelper.COLUMN_PHOTONAME, MySQLiteHelper.COLUMN_PHOTOPROJECTID, MySQLiteHelper.COLUMN_PHOTOAUTHOR, MySQLiteHelper.COLUMN_PHOTODESCRIPTION, MySQLiteHelper.COLUMN_PHOTOLASTMODIFICATION, MySQLiteHelper.COLUMN_PHOTOPATH}, "photo_name" + " LIKE \"" + n + "\"", null, null, null, null);
        if (c.getCount() != 0) {
            c.moveToFirst();
        }
        return cursorToPhoto(c);
    }

    /**
     * Get a photo with the id
     *
     * @param i id of the photo
     * @return the Photo
     */
    public Photo getPhotoById(Long i) {
        Cursor c = bdd.query(MySQLiteHelper.TABLE_PHOTO, new String[]{MySQLiteHelper.COLUMN_PHOTOID, MySQLiteHelper.COLUMN_PHOTONAME, MySQLiteHelper.COLUMN_PHOTOPROJECTID, MySQLiteHelper.COLUMN_PHOTOAUTHOR, MySQLiteHelper.COLUMN_PHOTODESCRIPTION, MySQLiteHelper.COLUMN_PHOTOLASTMODIFICATION, MySQLiteHelper.COLUMN_PHOTOPATH}, "photo_id" + " =" + i, null, null, null, null);
        if (c.getCount() != 0) {
            c.moveToFirst();
        }
        return cursorToPhoto(c);
    }

    /**
     * Obtenir toutes les photos d'un projet
     *
     * @param id Id du projet
     * @return Liste des photos du projet
     */

    public List<Photo> getPhotos(long id) {
        Cursor cursor = bdd.query(MySQLiteHelper.TABLE_PHOTO, new String[]{MySQLiteHelper.COLUMN_PHOTOID, MySQLiteHelper.COLUMN_PHOTONAME, MySQLiteHelper.COLUMN_PHOTOPROJECTID, MySQLiteHelper.COLUMN_PHOTOAUTHOR, MySQLiteHelper.COLUMN_PHOTODESCRIPTION, MySQLiteHelper.COLUMN_PHOTOLASTMODIFICATION, MySQLiteHelper.COLUMN_PHOTOPATH}, "project_id" + " =" + id, null, null, null, null);
        List<Photo> lp = new ArrayList<>();

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Photo ph = cursorToPhoto(cursor);
            lp.add(ph);
        }
        cursor.close();

        return lp;
    }

    /**
     * Mise à jour d'un gpsgeom
     *
     * @param g
     */
    public void updateGpsGeom(GpsGeom g) {
        bdd.execSQL("UPDATE GpsGeom SET gpsGeom_thegeom='" + g.getGpsGeomCoord() + "' WHERE gpsGeom_id =" + g.getGpsGeomsId());
    }


    public GpsGeom getGpsGeomOfPhoto(long photoId) {
        GpsGeom gps = new GpsGeom();

        Cursor c = bdd.query(MySQLiteHelper.TABLE_PHOTO, new String[]{MySQLiteHelper.COLUMN_GPSGEOMID}, "photo_id" + " =" + photoId, null, null, null, null);
        c.moveToFirst();
        long id = c.getLong(0);

        if (id != 0) {
            //On récupère le GpsGeom
            Cursor cursor = bdd.query(MySQLiteHelper.TABLE_GPSGEOM, new String[]{MySQLiteHelper.COLUMN_GPSGEOMCOORD, MySQLiteHelper.COLUMN_GPSGEOMID}, "gpsGeom_id" + " =" + id, null, null, null, null);
            cursor.moveToFirst();
            gps.setGpsGeomCoord(cursor.getString(0));
            gps.setGpsGeomId(cursor.getLong(1));

        } else {
            //Insertion d'un nouveau gpsgeom
            ContentValues values = new ContentValues();
            values.put(MySQLiteHelper.COLUMN_GPSGEOMCOORD, "");
            long gid = bdd.insert(MySQLiteHelper.TABLE_GPSGEOM, null, values);

            //On récupère le nouveau gpsgeom
            gps.setGpsGeomId(gid);

            //On update la photo
            bdd.execSQL("UPDATE Photo SET gpsGeom_id='" + gid + "' WHERE photo_id =" + photoId);
        }

        return gps;
    }

}
