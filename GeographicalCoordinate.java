public class GeographicalCoordinate {
    /**
     * The longitude.
     */
    private double longitude;

    /**
     * The latitude.
     */
    private double latitude;

    public GeographicalCoordinate(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
