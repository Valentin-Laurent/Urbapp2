package fr.turfu.urbapp2.db;

public class Photo {

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
     * contains the id of the project
     */
    private Long project_id;

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
     * Constructeur
     *
     * @param path chemin menant à la photo
     */
    public Photo(String path, Long pid, String author) {
        this.photo_path = path;
        this.photo_author = author;
        this.project_id = pid;
    }

    /**
     * Constructeur vide
     */
    public Photo() {

    }

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

    /**
     * getter for the project id
     *
     * @return
     */
    public long getProject_id() {
        return project_id;
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
     * setter for the project id
     *
     * @param id
     */
    public void setProject_id(Long id) {
        project_id = id;
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


}
