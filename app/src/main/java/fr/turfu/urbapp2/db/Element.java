package fr.turfu.urbapp2.DB;

public class Element  {


    //Atributes
    /**
     * long id of the object element
     */
    private long element_id;
    /**
     * long id of the photo to which the element belongs
     */
    private long photo_id;
    /**
     * long id of the material of the element
     */
    private long material_id;
    /**
     * long id of the type of the element
     */
    private long elementType_id;
    /**
     * long id of the pixel_geom which represents the element
     */
    private long pixelGeom_id;
    /**
     * element_color of the element
     */
    private String element_color;

    /**
     * True if <code>this</code> Element is selected
     */
    private boolean selected;


    //Getters

    /**
     * getter for the element_id
     *
     * @return long element_id
     */
    public long getElement_id() {
        return element_id;
    }

    /**
     * getter for the photo_id
     *
     * @return long photo_id
     */
    public long getPhoto_id() {
        return photo_id;
    }

    /**
     * getter for the material_id
     *
     * @return long matterial_id
     */
    public long getMaterial_id() {
        return material_id;
    }

    /**
     * getter for the elementType_id
     *
     * @return long elementType_id
     */
    public long getElementType_id() {
        return elementType_id;
    }

    /**
     * getter for the pixelGeom_id
     *
     * @return long pixelGeom_id
     */
    public long getPixelGeom_id() {
        return pixelGeom_id;
    }


    /**
     * getter for the element_color
     *
     * @return String element_color
     */
    public String getElement_color() {
        return element_color;
    }



    /**
     * Return whether <code>this</code> Element is selected or not
     *
     * @return whether <code>this</code> Element is selected or not
     */
    public boolean isSelected() {
        return selected;
    }


    //Setters

    /**
     * setter for the element_id
     *
     * @param element_id
     */
    public void setElement_id(long element_id) {
        this.element_id = element_id;
    }

    /**
     * setter for the photo_id
     *
     * @param photo_id
     */
    public void setPhoto_id(long photo_id) {
        this.photo_id = photo_id;
    }

    /**
     * setter for the material_id
     *
     * @param material_id
     */
    public void setMaterial_id(long material_id) {
        this.material_id = material_id;
    }

    /**
     * setter for the elementType_id
     *
     * @param elementType_id
     */
    public void setElementType_id(long elementType_id) {
        this.elementType_id = elementType_id;
    }

    /**
     * setter for the pixelGeom_id
     *
     * @param pixelGeom_id
     */
    public void setPixelGeom_id(long pixelGeom_id) {
        this.pixelGeom_id = pixelGeom_id;
    }


    /**
     * setter for the element_color
     *
     * @param element_color
     */
    public void setElement_color(String element_color) {
        this.element_color = element_color;
    }



    /**
     * set whether <code>this</code> Element is selected or not
     *
     * @param selected the new state of selection for this Element
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }


    //Abstract methods

    @Override
    public String toString() {
        return "Element [element_id=" + element_id + ", photo_id=" + photo_id
                + ", material_id=" + material_id + ", elementType_id="
                + elementType_id + ", pixelGeom_id=" + pixelGeom_id
                + ", element_color=" + element_color + "]";
    }


}