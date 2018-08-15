
package com.watshout.watshout.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CreateRoadMap {

    @SerializedName("map_url")
    @Expose
    private String mapUrl;
    @SerializedName("distance_km")
    @Expose
    private Double distanceKm;

    public String getMapUrl() {
        return mapUrl;
    }

    public void setMapUrl(String mapUrl) {
        this.mapUrl = mapUrl;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

}
