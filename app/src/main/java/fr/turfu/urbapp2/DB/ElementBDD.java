package fr.turfu.urbapp2.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;


public class ElementBDD {

    private SQLiteDatabase bdd;
    private MySQLiteHelper sqlHelper;

    /**
     * Constructeur
     *
     * @param context contexte de l'activité
     */
    public ElementBDD(Context context) {
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
     * Transformer un curseur en element
     *
     * @param c Curseur
     * @return Element
     */
    private Element cursorToElement(Cursor c) {
        //si aucun élément n'a été retourné dans la requête, on renvoie null
        if (c.getCount() == 0) {
            return null;
        } else {

            //On créé un élément
            Element e = new Element();

            //on lui affecte toutes les infos grâce aux infos contenues dans le Cursor
            e.setElement_id(c.getLong(0));
            e.setPhoto_id(c.getLong(1));
            e.setElementType_id(c.getLong(2));
            e.setMaterial_id(c.getLong(3));
            e.setPixelGeom_id(c.getLong(4));
            e.setElement_color(c.getString(5));

            //On retourne l'élément
            return e;
        }
    }

    /**
     * Ajout d'un élément à la base de données
     *
     * @param e Element
     * @return id de l'élément
     */
    public long insert(Element e) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_PHOTOID, e.getPhoto_id());
        values.put(MySQLiteHelper.COLUMN_MATERIALID, e.getMaterial_id());
        values.put(MySQLiteHelper.COLUMN_ELEMENTTYPEID, e.getElement_id());
        values.put(MySQLiteHelper.COLUMN_ELEMENTCOLOR, e.getElement_color());
        values.put(MySQLiteHelper.COLUMN_PIXELGEOMID, e.getPixelGeom_id());

        return bdd.insert(MySQLiteHelper.TABLE_ELEMENT, null, values);
    }


    /**
     * Mise à jour d'un élément
     *
     * @param e
     */
    public void updatePhotoInfos(Element e) {
        bdd.execSQL("UPDATE Element SET element_color='" + e.getElement_color() + "' WHERE element_id =" + e.getElement_id());
        bdd.execSQL("UPDATE Element SET material_id='" + e.getMaterial_id() + "' WHERE element_id =" + e.getElement_id());
        bdd.execSQL("UPDATE Element SET elementType_id='" + e.getElementType_id() + "' WHERE element_id =" + e.getElement_id());
    }


    /**
     * Obtenir tous les éléments d'une photo
     *
     * @param id Id de la photo
     * @return Liste des éléments de la photo
     */

    public ArrayList<Element> getElement(long id) {
        Cursor cursor = bdd.query(MySQLiteHelper.TABLE_ELEMENT, new String[]{MySQLiteHelper.COLUMN_ELEMENTID, MySQLiteHelper.COLUMN_PHOTOID, MySQLiteHelper.COLUMN_ELEMENTTYPEID, MySQLiteHelper.COLUMN_MATERIALID, MySQLiteHelper.COLUMN_PIXELGEOMID, MySQLiteHelper.COLUMN_ELEMENTCOLOR}, "photo_id" + " =" + id, null, null, null, null);
        ArrayList<Element> lp = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Element ph = cursorToElement(cursor);
            lp.add(ph);
        }
        cursor.close();
        return lp;
    }


    /**
     * Lister tous les pixels geom d'une photo
     *
     * @param elem Liste des éléments de la photo
     * @returnListe des polygones correspondants
     */
    public ArrayList<PixelGeom> getPixelGeom(ArrayList<Element> elem) {
        ArrayList<PixelGeom> pg = new ArrayList<>();
        for (Element e : elem) {
            pg.add(findPixelGeomById(e.getPixelGeom_id()));
        }
        return pg;
    }

    /**
     * Récupérer un pixelGeom par son id
     *
     * @param id
     * @return
     */
    public PixelGeom findPixelGeomById(Long id) {
        Cursor c = bdd.query(MySQLiteHelper.TABLE_PIXELGEOM, new String[]{MySQLiteHelper.COLUMN_PIXELGEOMID, MySQLiteHelper.COLUMN_PIXELGEOMCOORD}, "pixelGeom_id" + " == \"" + id + "\"", null, null, null, null);
        if (c.getCount() != 0) {
            c.moveToFirst();
        }
        return cursorToPixelGeom(c);
    }

    /**
     * Transformation d'un curseur en pixel geom
     *
     * @param c
     * @return
     */
    public PixelGeom cursorToPixelGeom(Cursor c) {
        //si aucun élément n'a été retourné dans la requête, on renvoie null
        if (c.getCount() == 0) {
            return null;
        } else {

            //On créé un pixel Geom
            PixelGeom p = new PixelGeom();

            //on lui affecte toutes les infos grâce aux infos contenues dans le Cursor
            p.setPixelGeomId(c.getLong(0));
            p.setPixelGeom_the_geom(c.getString(1));
            p.selected = false;

            //On retourne l'objet
            return p;
        }
    }


}
