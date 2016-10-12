package fr.turfu.urbapp2.DB;


public class GpsGeom {

    //Attributes
    /**
     * long id of the gpsGeom
     */
    private long gpsGeom_id;
    /**
     * String that describes a the_geom dataType in postgis
     */
    private String gpsGeom_thegeom;


    //Getters

    /**
     * getter for gpsGeom_id
     *
     * @return long gpsGeom_id
     */
    public long getGpsGeomsId() {
        return gpsGeom_id;
    }

    /**
     * getter for gpsGeom geom
     *
     * @return String gpsGeom_the_geom;
     */
    public String getGpsGeomCoord() {
        return gpsGeom_thegeom;
    }


    //Setters

    /**
     * setter for gpsGeom_id
     *
     * @param id
     */
    public void setGpsGeomId(long id) {
        this.gpsGeom_id = id;
    }

    /**
     * setter for gpsGeom_the_geom
     *
     * @param str which will be gpsGeom_the_geom;
     */
    public void setGpsGeomCoord(String str) {
        this.gpsGeom_thegeom = "srid=4326;" + str;
    }


    public String toString() {
        return "gpsGeom_id =" + this.gpsGeom_id + "&" + "\n gpsGeom_thegeom =" + this.gpsGeom_thegeom + "&" + "\n coord =" + this.gpsGeom_id;

    }


}