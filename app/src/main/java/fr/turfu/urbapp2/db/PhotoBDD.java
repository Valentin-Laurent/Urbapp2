package fr.turfu.urbapp2.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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
        values.put(MySQLiteHelper.COLUMN_PHOTONAME,"");
        values.put(MySQLiteHelper.COLUMN_PHOTODESCRIPTION,"");
        values.put(MySQLiteHelper.COLUMN_PHOTOLASTMODIFICATION,0);
        values.put(MySQLiteHelper.COLUMN_PHOTOAUTHOR,p.getPhoto_author());
        values.put(MySQLiteHelper.COLUMN_PHOTOPROJECTID,p.getProject_id());
        values.put(MySQLiteHelper.COLUMN_PHOTOPATH,p.getPhoto_path());

        return bdd.insert(MySQLiteHelper.TABLE_PHOTO, null, values);
    }

    /**
     * Get a photo with his path
     *
     * @param n Photo path
     * @return Photo
     */
    public Photo getPhotoByPath(String n) {
        Cursor c = bdd.query(MySQLiteHelper.TABLE_PROJECT, new String[]{MySQLiteHelper.COLUMN_PHOTOID, MySQLiteHelper.COLUMN_PHOTONAME, MySQLiteHelper.COLUMN_PHOTOPROJECTID, MySQLiteHelper.COLUMN_PHOTOAUTHOR, MySQLiteHelper.COLUMN_PHOTODESCRIPTION, MySQLiteHelper.COLUMN_PHOTOLASTMODIFICATION,MySQLiteHelper.COLUMN_PHOTOPATH}, "photo_path" + " LIKE \"" + n + "\"", null, null, null, null);
        if (c.getCount() != 0) {
            c.moveToFirst();
        }
        return cursorToPhoto(c);
    }


}
