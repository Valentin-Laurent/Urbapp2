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
    public long insertElement(Element e) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_PHOTOID, e.getPhoto_id());
        values.put(MySQLiteHelper.COLUMN_MATERIALID, e.getMaterial_id());
        values.put(MySQLiteHelper.COLUMN_ELEMENTTYPEID, e.getElementType_id());
        values.put(MySQLiteHelper.COLUMN_ELEMENTCOLOR, e.getElement_color());
        values.put(MySQLiteHelper.COLUMN_PIXELGEOMID, e.getPixelGeom_id());

        return bdd.insert(MySQLiteHelper.TABLE_ELEMENT, null, values);
    }

    /**
     * Ajout d'un pixelgeom à la base de données
     *
     * @param p pixelGeom
     * @return id de l'élément
     */
    public long insertPixelGeom(PixelGeom p) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_PIXELGEOMID, p.getPixelGeomId());
        values.put(MySQLiteHelper.COLUMN_PIXELGEOMCOORD, p.getPixelGeom_the_geom());

        return bdd.insert(MySQLiteHelper.TABLE_PIXELGEOM, null, values);
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


    public long getMaxPixelGeomId() {
        Cursor c = bdd.rawQuery("SELECT MAX(pixelGeom_id) FROM PixelGeom", new String[]{});
        if (c.getCount() != 0) {
            c.moveToFirst();
            return c.getLong(0);
        } else {
            return 0;
        }
    }

    public long getMaxElementId() {
        Cursor c = bdd.rawQuery("SELECT MAX(element_id) FROM Element", new String[]{});
        if (c.getCount() != 0) {
            c.moveToFirst();
            return c.getLong(0);
        } else {
            return 0;
        }
    }

    /**
     * Suppression de tous les éléments d'une photo et de leurs pixelgeoms
     *
     * @param photoid Id de la photo
     */
    public void deleteElement(long photoid) {
        ArrayList<Element> elem = getElement(photoid);
        ArrayList<PixelGeom> pixel = getPixelGeom(elem);
        for (Element e : elem) {
            bdd.execSQL("DELETE FROM Element WHERE element_id =" + e.getElement_id());
        }
        for (PixelGeom p : pixel) {
            bdd.execSQL("DELETE FROM PixelGeom WHERE pixelGeom_id =" + p.getPixelGeomId());
        }
    }

    /**
     * Récupérer tous les matériaux de la base de données
     *
     * @return Liste de matériaux
     */
    public ArrayList<String> getMaterials() {
        ArrayList<String> mater = new ArrayList<>();
        Cursor c = bdd.query(MySQLiteHelper.TABLE_MATERIAL, new String[]{MySQLiteHelper.COLUMN_MATERIALNAME}, null, null, null, null, null);
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            mater.add(c.getString(0));
        }

        if (mater.isEmpty()) {
            insertMater();
            mater = getMaterials();
        }

        return mater;
    }

    /**
     * Récupérer tous les types d'éléments de la base de données
     *
     * @return liste des types
     */
    public ArrayList<String> getTypes() {
        ArrayList<String> types = new ArrayList<>();
        Cursor c = bdd.query(MySQLiteHelper.TABLE_ELEMENTTYPE, new String[]{MySQLiteHelper.COLUMN_ELEMENTTYPENAME}, null, null, null, null, null);
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            types.add(c.getString(0));
        }

        if (types.isEmpty()) {
            insertTypes();
            types = getTypes();
        }

        return types;
    }

    public void insertTypes() {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_ELEMENTTYPEID, 1);
        values.put(MySQLiteHelper.COLUMN_ELEMENTTYPENAME, "Mur");
        bdd.insert(MySQLiteHelper.TABLE_ELEMENTTYPE, null, values);

        ContentValues values1 = new ContentValues();
        values1.put(MySQLiteHelper.COLUMN_ELEMENTTYPEID, 2);
        values1.put(MySQLiteHelper.COLUMN_ELEMENTTYPENAME, "Sol");
        bdd.insert(MySQLiteHelper.TABLE_ELEMENTTYPE, null, values1);

        ContentValues values2 = new ContentValues();
        values2.put(MySQLiteHelper.COLUMN_ELEMENTTYPEID, 3);
        values2.put(MySQLiteHelper.COLUMN_ELEMENTTYPENAME, "Toit");
        bdd.insert(MySQLiteHelper.TABLE_ELEMENTTYPE, null, values2);
    }

    public void insertMater() {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_MATERIALID, 1);
        values.put(MySQLiteHelper.COLUMN_MATERIALNAME, "Bois");
        bdd.insert(MySQLiteHelper.TABLE_MATERIAL, null, values);

        ContentValues values1 = new ContentValues();
        values1.put(MySQLiteHelper.COLUMN_MATERIALID, 2);
        values1.put(MySQLiteHelper.COLUMN_MATERIALNAME, "Verre");
        bdd.insert(MySQLiteHelper.TABLE_MATERIAL, null, values1);

        ContentValues values2 = new ContentValues();
        values2.put(MySQLiteHelper.COLUMN_MATERIALID, 3);
        values2.put(MySQLiteHelper.COLUMN_MATERIALNAME, "Béton");
        bdd.insert(MySQLiteHelper.TABLE_MATERIAL, null, values2);
    }

}
