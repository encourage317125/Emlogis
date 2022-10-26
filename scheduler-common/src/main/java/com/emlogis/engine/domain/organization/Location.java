package com.emlogis.engine.domain.organization;

public class Location {

    private double latitude;
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    /**
     * The distance is not in miles or km, but in the TSPLIB's unit of measurement.
     * @param location never null
     * @return a positive number, the distance multiplied by 1000 to avoid floating point arithmetic rounding errors
     */
    public int getMilliDistance(Location location) {
        // Implementation specified by TSPLIB http://www2.iwr.uni-heidelberg.de/groups/comopt/software/TSPLIB95/
        // Euclidean distance (Pythagorean theorem) - not correct when the surface is a sphere
        double latitudeDifference = location.latitude - latitude;
        double longitudeDifference = location.longitude - longitude;
        double distance = Math.sqrt(
                (latitudeDifference * latitudeDifference) + (longitudeDifference * longitudeDifference));
        return (int) (distance * 1000.0 + 0.5);
    }

}
