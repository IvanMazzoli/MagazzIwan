package me.ivanmazzoli.Models;

public class IlpraItem {

    private String name;
    private String ilpra_code;
    private String make_code;
    private String brand;
    private String location;
    private String type;
    private String image_url;
    private String docs;

    /**
     * Costruttore base classe
     */
    public IlpraItem() {
        super();
    }


    public String getName() {
        return name;
    }

    public String getIlpraCode() {
        return ilpra_code;
    }

    public String getMakeCode() {
        return make_code;
    }

    public String getBrand() {
        return brand;
    }

    public String getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }

    public String getIlpraInfo() {
        return this.ilpra_code + " - " + this.location;
    }

    public String getImageUrl() {
        return this.image_url;
    }

    public String getDocs() {
        return this.docs;
    }
}
