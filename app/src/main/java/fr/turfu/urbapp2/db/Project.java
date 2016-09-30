package fr.turfu.urbapp2.db;

import android.content.ContentValues;
//TODO WTF import src.com.ecn.urbapp.syncToExt.Sync;


public class Project extends DataObject {

    private static final String TAG = "projet";

    /**************************
     * Attributes
     ***************************/

    /**
     * long project id attributes
     */
    private long project_id;
    /**
     * String project name attributes
     */
    private String project_name;

    /**
     * String project description
     */
    private String project_description;

    /**
     * long id of the gpsgeom that locates to the project
     */
    private long gpsGeom_id;
    /**
     * value String that contains the value of the GpsGeom designed by the previous attribute
     */
    private String Ext_GpsGeomCoord;

    /*******************
     * CONSTRUCTORS
     *******************/

    public Project(){

    }

    public Project(String n, String d,long gps) {
        this.project_name = n;
        this.project_description = d;
        this.gpsGeom_id=gps;
    }


    /*******************
     * GETTERS
     *******************/


    /**
     * getter for gpsgeom id
     *
     * @return the id of the gpsgeom
     */
    public long getGpsGeom_id() {
        return gpsGeom_id;
    }

    /**
     * getter for the project id
     *
     * @return the id of the project
     */
    public long getProjectId() {
        return project_id;
    }

    /**
     * getter for the name of the project
     *
     * @return the name of the project
     */
    public String getProjectName() {
        return project_name;
    }

    /**
     * getter for the description of the project
     *
     * @return the description of the project
     */
    public String getProjectDescription() {
        return project_description;
    }

    /**
     * get the value of the gpsgeom
     *
     * @return String gpsgeom
     */
    public String getExt_GpsGeomCoord() {
        return Ext_GpsGeomCoord;
    }


    /*******************
     * SETTERS
     *******************/

    /**
     * setter for the gpsgeomcoord
     *
     * @param ext_GpsGeomCoord
     */
    public void setExt_GpsGeomCoord(String ext_GpsGeomCoord) {
        Ext_GpsGeomCoord = ext_GpsGeomCoord;
    }

    /**
     * setter for the gpsgeom id
     *
     * @param gpsGeom_id
     */
    public void setGpsGeom_id(long gpsGeom_id) {
        this.gpsGeom_id = gpsGeom_id;
    }

    /**
     * setter for the description of the project
     *
     * @param str
     */
    public void setProjectDescription(String str) {
        this.project_description = str;
    }

    /**
     * setter for the name of the project
     *
     * @param str
     */
    public void setProjectName(String str) {
        this.project_name = str;
    }

    /**
     * setter for the project id
     *
     * @param id
     */
    public void setProjectId(long id) {
        this.project_id = id;
    }


    //Override methods
    @Override
    public String toString() {
        return project_name + " " + project_id;
    }

    @Override
    public void saveToLocal(LocalDataSource datasource) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_PROJECTNAME, this.project_name);
        values.put(MySQLiteHelper.COLUMN_PROJECTDESCRIPTION, this.project_description);
    }


    /**
     * query to get the biggest photo_id from local db
     */
    private static final String
            GETMAXPROJECTID =
            "SELECT " + MySQLiteHelper.TABLE_PHOTO + "." + MySQLiteHelper.COLUMN_PHOTOID + " FROM "
                    + MySQLiteHelper.TABLE_PHOTO
                    + " ORDER BY " + MySQLiteHelper.TABLE_PHOTO + "." + MySQLiteHelper.COLUMN_PHOTOID
                    + " DESC LIMIT 1 ;";

}