package fr.turfu.urbapp2.DB;

public class Material {

    //Attributes
    /**
     * long id of material
     */
    private long material_id;
    /**
     * String name of the material
     */
    private String material_name;


    //Getters

    /**
     * getter for the attribute id of material
     *
     * @return long material_id
     */
    public long getMaterial_id() {
        return material_id;
    }

    /**
     * getter for the attribute name of material
     *
     * @return String material_name
     */
    public String getMaterial_name() {
        return material_name;
    }


    //Setters

    /**
     * setter for the id of material
     *
     * @param material_id long
     */
    public void setMaterial_id(long material_id) {
        this.material_id = material_id;
    }

    /**
     * setter for the name of material
     *
     * @param material_name String
     */
    public void setMaterial_name(String material_name) {
        this.material_name = material_name;
    }


    //Override Methods
    @Override
    public String toString() {
        return "Material [material_id=" + material_id + ", material_name="
                + material_name + "]";
    }


}