/**
 * Classe ElementBDD
 * -------------------------------------------------------------------------------------
 * Classe qui permet de manipuler les elements enregistrés dans la base de données locale
 */

package fr.turfu.urbapp2.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

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
            e.setSelected(false);

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
        values.put(MySQLiteHelper.COLUMN_ELEMENTID, e.getElement_id());
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
        Log.v("phid", ":" + id);
        Cursor cursor = bdd.query(MySQLiteHelper.TABLE_ELEMENT, new String[]{MySQLiteHelper.COLUMN_ELEMENTID, MySQLiteHelper.COLUMN_PHOTOID, MySQLiteHelper.COLUMN_ELEMENTTYPEID, MySQLiteHelper.COLUMN_MATERIALID, MySQLiteHelper.COLUMN_PIXELGEOMID, MySQLiteHelper.COLUMN_ELEMENTCOLOR}, "photo_id" + " =" + id, null, null, null, null);
        ArrayList<Element> lp = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Element e = cursorToElement(cursor);
            Log.v("epid", ":" + e.getPixelGeom_id());
            lp.add(e);
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
     * @param id id du pixelgeom
     * @return pixelgeom
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
     * @param c Curseur représentant le pixelgeom
     * @return pixelgeom
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

    /**
     * Récupérer le max des id des pixelgeom
     *
     * @return id max
     */
    public long getMaxPixelGeomId() {
        Cursor c = bdd.rawQuery("SELECT MAX(pixelGeom_id) FROM PixelGeom", new String[]{});
        if (c.getCount() != 0) {
            c.moveToFirst();
            return c.getLong(0);
        } else {
            return 0;
        }
    }

    /**
     * Récupérer le max des id des elements
     *
     * @return id max
     */
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

        return types;
    }

    /**
     * Insertion de la liste des types d'éléments
     *
     * @param et Liste de types d'élément obtenue en requetant le serveur
     */
    public void insertTypes(ElementType[] et) {
        for (ElementType e : et) {
            if (!exist(e)) {
                ContentValues values2 = new ContentValues();
                values2.put(MySQLiteHelper.COLUMN_ELEMENTTYPEID, e.getElementType_id());
                values2.put(MySQLiteHelper.COLUMN_ELEMENTTYPENAME, e.getElementType_name());
                bdd.insert(MySQLiteHelper.TABLE_ELEMENTTYPE, null, values2);
            }
        }
    }

    /**
     * Insertion de la liste des matériaux
     *
     * @param mat Liste de matériaux obtenue en requetant le serveur
     */
    public void insertMater(Material[] mat) {
        for (Material m : mat) {
            if (!exist(m)) {
                ContentValues values = new ContentValues();
                values.put(MySQLiteHelper.COLUMN_MATERIALID, m.getMaterial_id());
                values.put(MySQLiteHelper.COLUMN_MATERIALNAME, m.getMaterial_name());
                bdd.insert(MySQLiteHelper.TABLE_MATERIAL, null, values);
            }
        }
    }

    /**
     * Test de l'existence d'un type d'élément dans la base de données
     *
     * @param et Type d'élment recherché
     * @return booléen true si le type est dans la base de données locale
     */
    public boolean exist(ElementType et) {
        Cursor c = bdd.query(MySQLiteHelper.TABLE_ELEMENTTYPE, new String[]{MySQLiteHelper.COLUMN_ELEMENTTYPENAME}, "elementType_id" + " == \"" + et.getElementType_id() + "\"", null, null, null, null);
        return c.getCount() != 0;

    }

    /**
     * Test de l'existence d'un matériau dans la base de données
     *
     * @param m Matériau recherché
     * @return booléen true si le matériau est dans la base de données locale
     */
    public boolean exist(Material m) {
        Cursor c = bdd.query(MySQLiteHelper.TABLE_MATERIAL, new String[]{MySQLiteHelper.COLUMN_MATERIALNAME}, "material_id" + " == \"" + m.getMaterial_id() + "\"", null, null, null, null);
        return c.getCount() != 0;
    }


}
