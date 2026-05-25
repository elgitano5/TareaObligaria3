public class UTMCoordinate {
    /**
     * The easting (X coordinate).
     */
    private double easting;

    /**
     * The northing (Y coordinate).
     */
    private double northing;

    /**
     * The UTM zone number.
     */
    private int zone;

    /**
     * Indicates whether the coordinate is in the northern hemisphere.
     */
    private boolean northernHemisphere;

    public UTMCoordinate(double easting, double northing, int zone, boolean northernHemisphere) {
        this.easting = easting;
        this.northing = northing;
        this.zone = zone;
        this.northernHemisphere = northernHemisphere;
    }

    public double getEasting() {
        return easting;
    }

    public void setEasting(double easting) {
        this.easting = easting;
    }

    public double getNorthing() {
        return northing;
    }

    public void setNorthing(double northing) {
        this.northing = northing;
    }

    public int getZone() {
        return zone;
    }

    public void setZone(int zone) {
        this.zone = zone;
    }

    public boolean isNorthernHemisphere() {
        return northernHemisphere;
    }

    public void setNorthernHemisphere(boolean northernHemisphere) {
        this.northernHemisphere = northernHemisphere;
    }
}