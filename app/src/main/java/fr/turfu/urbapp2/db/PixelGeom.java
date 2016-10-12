package fr.turfu.urbapp2.DB;

import java.util.Vector;

public class PixelGeom {


    //Attributes
    /**
     * long id attributes of pixelGeom
     */
    private long pixelGeom_id;
    /**
     * String polygon attributes of pixelGeom
     */
    private String pixelGeom_thegeom;
    /**
     * boolean attributes of pixelGeom that determines if a pixelGeom is selected or not
     */
    public boolean selected;

    /**
     * linked list of pixelGeom
     */
    private Vector<PixelGeom> linkedPixelGeom = new Vector<PixelGeom>();

    //Getters

    /**
     * getter for pixelGeom id
     *
     * @return long pixelGeom_id
     */
    public long getPixelGeomId() {
        return pixelGeom_id;
    }

    /**
     * getter for pixelGeom isSelected
     *
     * @return boolean isSelected
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * getter for the pixelGeom polygon geometry
     *
     * @return String geom
     */
    public String getPixelGeom_the_geom() {
        return pixelGeom_thegeom;
    }

    //Setters

    /**
     * setter PixelGeom Id
     *
     * @param id
     */
    public void setPixelGeomId(long id) {
        this.pixelGeom_id = id;
    }

    /**
     * setter PixelGeom Selected
     *
     * @param selected
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * setter of pixelgeom polygon
     *
     * @param pixelGeom_thegeom
     */
    public void setPixelGeom_the_geom(String pixelGeom_thegeom) {
        this.pixelGeom_thegeom = pixelGeom_thegeom;
    }


    //Override methods
    @Override
    public String toString() {
        return "pixelGeom_id =" + this.pixelGeom_id + "&" + "\ncoord =" + this.pixelGeom_thegeom;

    }


}