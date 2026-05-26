public final class CoordinateConverter {

    /**
     * Elipsoid WGS84 Constants.
     */
    private static final double SEMI_MAJOR_AXIS = 6378137.0; // Semieje mayor
    private static final double ECC_SQUARED = 0.00669438; // Excentricidad al cuadrado
    private static final double SCALE_FACTOR = 0.9996; // Factor de escala UTM

    private CoordinateConverter() {
    // Utility class
    }

    /**
     * Convert a set of UTM coordinates to geographical ones.
     * @param uCoordinate The UTM coordinate to convert
     * @return the converted coordinates
     */
    public static GeographicalCoordinate convertUTMToGeographical(UTMCoordinate uCoordinate) {
        if (uCoordinate == null) return null;

        double x = uCoordinate.getEasting() - 500000.0; // Remove false easting
        double y = uCoordinate.getNorthing();

        y = adjustSouthernHemisphere(y, uCoordinate.isNorthernHemisphere());

        double lonOrigin = getCentralMeridian(uCoordinate.getZone());
        double eccPrimeSquared = getECCPrimeSquared();

        double M = y / SCALE_FACTOR;
        double mu = calculateMu(M);

        double e1 = calculateE1();
        double phi1Rad = calculatePhi1(mu, e1);

        double N1 = calculateN(phi1Rad);
        double T1 = Math.pow(Math.tan(phi1Rad), 2);
        double C1 = eccPrimeSquared * Math.pow(Math.cos(phi1Rad), 2);
        double R1 = calculateR(phi1Rad);        double D = x / (N1 * SCALE_FACTOR);

        double latRad = calculateLatitude(phi1Rad, N1, R1, T1, C1, D);
        double lonRad = calculateLongitude(phi1Rad, T1, C1, D, eccPrimeSquared);
        double finalLat = Math.toDegrees(latRad);
        double finalLon = lonOrigin + Math.toDegrees(lonRad);

        // Note the constructor order: longitude, latitude
        return new GeographicalCoordinate(finalLon, finalLat);
    }

    /**
     * Convert a set of geographical coordinates to UTM coordinates.
     * @param gCoordinate The geographical coordinate to convert
     * @return the converted coordinates
     */
    public static UTMCoordinate convertGeographicalToUTM(GeographicalCoordinate gCoordinate) {
        if (gCoordinate == null) return null;

        double lat = gCoordinate.getLatitude();
        double lon = gCoordinate.getLongitude();

        double latRad = Math.toRadians(lat);
        double lonRad = Math.toRadians(lon);

        int zoneNumber = getZone(lon);
        double lonOrigin = getCentralMeridian(zoneNumber);
        double lonOriginRad = Math.toRadians(lonOrigin);

        double eccPrimeSquared = getECCPrimeSquared();

        double N = calculateN(latRad);
        double T = Math.pow(Math.tan(latRad), 2);
        double C = eccPrimeSquared * Math.pow(Math.cos(latRad), 2);
        double A = Math.cos(latRad) * (lonRad - lonOriginRad);

        double M = calculateMeridionalArc(latRad);

        double utmEasting = (SCALE_FACTOR * N * (A + (1 - T + C) * Math.pow(A, 3) / 6
                + (5 - 18 * T + Math.pow(T, 2) + 72 * C - 58 * eccPrimeSquared) * Math.pow(A, 5) / 120)
                + 500000.0);

        double utmNorthing = (SCALE_FACTOR * (M + N * Math.tan(latRad) * (Math.pow(A, 2) / 2 
                + (5 - T + 9 * C + 4 * Math.pow(C, 2)) * Math.pow(A, 4) / 24
                + (61 - 58 * T + Math.pow(T, 2) + 600 * C - 330 * eccPrimeSquared) * Math.pow(A, 6) / 720)));

        boolean isNorthern = lat >= 0;
        if (!isNorthern) {
            utmNorthing += 10000000.0; // False northing for the southern hemisphere
        }

        return new UTMCoordinate(utmEasting, utmNorthing, zoneNumber, isNorthern);
        
        }
        
        private static double adjustSouthernHemisphere(double northing, boolean isNorthern) {
                return isNorthern ? northing : northing - 10000000.0;
        }

        private static double getCentralMeridian(int zone) {
                return (zone - 1) * 6 - 180 + 3;
        }

        private static int getZone(double lon) {
                return (int) Math.floor((lon + 180) / 6) + 1;
        }

        private static double getECCPrimeSquared() {
                return ECC_SQUARED / (1 - ECC_SQUARED);
        }

        private static double calculateMu(double M) {
                return M / (SEMI_MAJOR_AXIS * (1 - ECC_SQUARED / 4
                - 3 * Math.pow(ECC_SQUARED, 2) / 64
                - 5 * Math.pow(ECC_SQUARED, 3) / 256));
        }

        private static double calculateE1() {
                return (1 - Math.sqrt(1 - ECC_SQUARED))
                / (1 + Math.sqrt(1 - ECC_SQUARED));
        }

        private static double calculatePhi1(double mu, double e1) {
                return mu
                + (3 * e1 / 2 - 27 * Math.pow(e1, 3) / 32) * Math.sin(2 * mu)
                + (21 * Math.pow(e1, 2) / 16 - 55 * Math.pow(e1, 4) / 32) * Math.sin(4 * mu)
                + (151 * Math.pow(e1, 3) / 96) * Math.sin(6 * mu);
        }

        private static double calculateN(double latRad) {
                return SEMI_MAJOR_AXIS
                / Math.sqrt(1 - ECC_SQUARED * Math.pow(Math.sin(latRad), 2));
        }

        private static double calculateR(double phi) {
                return SEMI_MAJOR_AXIS * (1 - ECC_SQUARED)
                / Math.pow(1 - ECC_SQUARED * Math.pow(Math.sin(phi), 2), 1.5);
        }

        private static double calculateLatitude(double phi1, double N1, double R1,
                                        double T1, double C1, double D) {
                return phi1 - (N1 * Math.tan(phi1) / R1) * (
                Math.pow(D, 2) / 2
                    - (5 + 3 * T1 + 10 * C1 - 4 * Math.pow(C1, 2)
                    - 9 * getECCPrimeSquared()) * Math.pow(D, 4) / 24
                    + (61 + 90 * T1 + 298 * C1 + 45 * Math.pow(T1, 2)
                    - 252 * getECCPrimeSquared()
                    - 3 * Math.pow(C1, 2)) * Math.pow(D, 6) / 720
                );
        }

        private static double calculateLongitude(double phi1, double T1, double C1,
                                         double D, double eccPrimeSquared) {
                return (D
                - (1 + 2 * T1 + C1) * Math.pow(D, 3) / 6
                + (5 - 2 * C1 + 28 * T1 - 3 * Math.pow(C1, 2)
                + 8 * eccPrimeSquared + 24 * Math.pow(T1, 2)) * Math.pow(D, 5) / 120)
                / Math.cos(phi1);
        }

        private static double calculateMeridionalArc(double latRad) {
                return SEMI_MAJOR_AXIS * (
                (1 - ECC_SQUARED / 4
                - 3 * Math.pow(ECC_SQUARED, 2) / 64
                - 5 * Math.pow(ECC_SQUARED, 3) / 256) * latRad
                - (3 * ECC_SQUARED / 8
                + 3 * Math.pow(ECC_SQUARED, 2) / 32
                + 45 * Math.pow(ECC_SQUARED, 3) / 1024) * Math.sin(2 * latRad)
                + (15 * Math.pow(ECC_SQUARED, 2) / 256
                + 45 * Math.pow(ECC_SQUARED, 3) / 1024) * Math.sin(4 * latRad)
                - (35 * Math.pow(ECC_SQUARED, 3) / 3072) * Math.sin(6 * latRad)
                );
        }

        
}