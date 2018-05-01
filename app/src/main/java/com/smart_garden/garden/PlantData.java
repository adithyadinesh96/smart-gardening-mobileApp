package com.smart_garden.garden;

class PlantData {
    private String name;
    private String image_url;

    public PlantData(String name, String image_url) {
        this.name = name;
        this.image_url = image_url;
    }

    public String getName() {
        return name;
    }

    public String getImage_url() {
        return image_url;
    }
}


