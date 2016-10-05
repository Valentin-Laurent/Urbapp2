package fr.turfu.urbapp2.db;

import android.content.ContentValues;

import java.util.ArrayList;

public class Photo extends DataObject {

    /**
     * contains the id of the photo
     */
    private long photo_id;
    /**
     * contains the description of the photo
     */
    private String photo_description;
    /**
     * contains the author of the photo
     */
    private String photo_author;
    /**
     * attributes that declare the name of the picture for instance : img1.png
     */
    private String photo_name;
    /**
     * contains the address of the photo
     */
    private String photo_path;

    /**
     * contains the last modification
     * set to 0 by default
     */

    private int photo_last_modification = 0;


    /**
     * contains the gpsgeom_id
     */
    private long gpsGeom_id;

    //Getters
    /**
     * contains the localization
     */
    private String Ext_GpsGeomCoord;


    /**
     * getter for the photo id
     *
     * @return
     */
    public long getPhoto_id() {
        return photo_id;
    }

    /**
     * getter for the description
     *
     * @return
     */
    public String getPhoto_description() {
        return photo_description;
    }

    /**
     * getter for the author
     *
     * @return
     */
    public String getPhoto_author() {
        return photo_author;
    }

    public String getPhoto_path() {
        return photo_path;
    }


    /**
     * getter for the gpsgeom id
     *
     * @return
     */
    public long getGpsGeom_id() {
        return gpsGeom_id;
    }

    /**
     * setter for the id of the gpsgeom
     *
     * @param gpsGeom_id
     */
    public void setGpsGeom_id(long gpsGeom_id) {
        this.gpsGeom_id = gpsGeom_id;
    }


    /**
     * getter for localisation fo the photo
     *
     * @return
     */
    public String getExt_GpsGeomCoord() {
        return Ext_GpsGeomCoord;
    }


    /**
     * getter for the date of last modification
     *
     * @return
     */
    public int getPhoto_last_modification() {
        return photo_last_modification;
    }


    /**
     * getter for the name
     *
     * @return
     */
    public String getPhoto_name() {
        return photo_name;
    }


    //Setters

    /**
     * setter for the localization of the photo
     *
     * @param ext_GpsGeomCoord
     */
    public void setExt_GpsGeomCoord(String ext_GpsGeomCoord) {
        Ext_GpsGeomCoord = ext_GpsGeomCoord;
    }


    /**
     * setter for the name of the photo
     *
     * @param photo_name
     */
    public void setPhoto_name(String photo_name) {
        this.photo_name = photo_name;
    }

    /**
     * setter for the id of the photo
     *
     * @param photo_id
     */
    public void setPhoto_id(long photo_id) {
        this.photo_id = photo_id;
    }

    /**
     * setter for the description
     *
     * @param photo_description
     */
    public void setPhoto_description(String photo_description) {
        this.photo_description = photo_description;
    }

    /**
     * setter for the author
     *
     * @param photo_author
     */
    public void setPhoto_author(String photo_author) {
        this.photo_author = photo_author;
    }

    /**
     * setter for the date of last modification
     *
     * @param d
     */
    public void setPhoto_last_modification(int d) {
        this.photo_last_modification = d;
    }


    /**
     * setter for the path of the Photo
     *
     * @param adresse
     */
    public void setPhoto_path(String adresse) {
        this.photo_path = adresse;
    }


    //Override methods
    @Override
    public String toString() {
        return "Photo [photo_id=" + photo_id + ", photo_description="
                + photo_description + ", photo_author=" + photo_author
                + ", photo_name=" + photo_name + ", gps_Geom_id=" + gpsGeom_id + "&" + "  position =" + this.Ext_GpsGeomCoord
                + "]";
    }

    @Override
    public void saveToLocal(LocalDataSource datasource) {
        ContentValues values = new ContentValues();

        values.put(MySQLiteHelper.COLUMN_PHOTONAME, this.photo_name);
        values.put(MySQLiteHelper.COLUMN_PHOTODESCRIPTION, this.photo_description);
        values.put(MySQLiteHelper.COLUMN_PHOTOAUTHOR, this.photo_author);
        values.put(MySQLiteHelper.COLUMN_PHOTOPATH, this.photo_path);
        values.put(MySQLiteHelper.COLUMN_PHOTOLASTMODIFICATION, this.photo_last_modification);

        // TODO WTF if(this.registredInLocal){
        String str = "photo_id " + "=" + this.photo_id;
        datasource.getDatabase().update(MySQLiteHelper.TABLE_PHOTO, values, str, null);
        /*}
		else{
			//Cursor cursor = datasource.getDatabase().rawQuery(GETMAXPHOTOID, null);
			//cursor.moveToFirst();

			long old_id = this.getPhoto_id();
			//long new_id = 1+cursor.getLong(0);
			long new_id = Sync.getMaxId().get("Photo")+1;
			this.setPhoto_id(new_id);
			this.trigger(old_id, new_id, MainActivity.element, MainActivity.composed);

			values.put(MySQLiteHelper.COLUMN_GPSGEOMID, this.gpsGeom_id);
			datasource.getDatabase().insert(MySQLiteHelper.TABLE_PHOTO, null, values);
		}*/
    }

    /**
     * query to get the biggest photo_id from local db
     */
    private static final String
            GETMAXPHOTOID =
            "SELECT " + MySQLiteHelper.TABLE_PHOTO + "." + MySQLiteHelper.COLUMN_PHOTOID + " FROM "
                    + MySQLiteHelper.TABLE_PHOTO
                    + " ORDER BY " + MySQLiteHelper.TABLE_PHOTO + "." + MySQLiteHelper.COLUMN_PHOTOID
                    + " DESC LIMIT 1 ;";

    /**
     * trigger method is used to update foreign keys in the dataObjects
     * this method is used before saving objects in database thank's to the "saved fragment"
     *
     * @param old_id
     * @param new_id
     * @param list_element
     */
    public void trigger(long old_id, long new_id, ArrayList<Element> list_element) {

        if (list_element != null) {
            for (Element e : list_element) {
                if (e.getPhoto_id() == old_id) {
                    e.setPhoto_id(new_id);
                }
            }

        }

    }

}
