public final class CoordinateConverter {

    /**
     * Elipsoid WGS84 Constants.
     */
    private static final double a = 6378137.0; // Semieje mayor
    private static final double eccSquared = 0.00669438; // Excentricidad al cuadrado
    private static final double k0 = 0.9996; // Factor de escala UTM

    /**
     * Convert a set of UTM coordinates to geographical ones.
     * @param uCoordinate The UTM coordinate to convert
     * @return the converted coordinates
     */
    public static GeographicalCoordinate convertUTMToGeographical(UTMCoordinate uCoordinate) {
        if (uCoordinate == null) return null;

        double x = uCoordinate.getEasting() - 500000.0; // Remove false easting
        double y = uCoordinate.getNorthing();

        if (!uCoordinate.isNorthernHemisphere()) {
            y -= 10000000.0; // Remove false north if it's southern hemisphere
        }

        double lonOrigin = (uCoordinate.getZone() - 1) * 6 - 180 + 3; // Central meridian
        double eccPrimeSquared = (eccSquared) / (1 - eccSquared);

        double M = y / k0;
        double mu = M / (a * (1 - eccSquared / 4 - 3 * Math.pow(eccSquared, 2) / 64 - 5 * Math.pow(eccSquared, 3) / 256));

        double e1 = (1 - Math.sqrt(1 - eccSquared)) / (1 + Math.sqrt(1 - eccSquared));
        double phi1Rad = mu + (3 * e1 / 2 - 27 * Math.pow(e1, 3) / 32) * Math.sin(2 * mu)
                + (21 * Math.pow(e1, 2) / 16 - 55 * Math.pow(e1, 4) / 32) * Math.sin(4 * mu)
                + (151 * Math.pow(e1, 3) / 96) * Math.sin(6 * mu);

        double N1 = a / Math.sqrt(1 - eccSquared * Math.pow(Math.sin(phi1Rad), 2));
        double T1 = Math.pow(Math.tan(phi1Rad), 2);
        double C1 = eccPrimeSquared * Math.pow(Math.cos(phi1Rad), 2);
        double R1 = a * (1 - eccSquared) / Math.pow(1 - eccSquared * Math.pow(Math.sin(phi1Rad), 2), 1.5);
        double D = x / (N1 * k0);

        double latRad = phi1Rad - (N1 * Math.tan(phi1Rad) / R1) * (Math.pow(D, 2) / 2 
                - (5 + 3 * T1 + 10 * C1 - 4 * Math.pow(C1, 2) - 9 * eccPrimeSquared) * Math.pow(D, 4) / 24
                + (61 + 90 * T1 + 298 * C1 + 45 * Math.pow(T1, 2) - 252 * eccPrimeSquared - 3 * Math.pow(C1, 2)) * Math.pow(D, 6) / 720);

        double lonRad = (D - (1 + 2 * T1 + C1) * Math.pow(D, 3) / 6
                + (5 - 2 * C1 + 28 * T1 - 3 * Math.pow(C1, 2) + 8 * eccPrimeSquared + 24 * Math.pow(T1, 2)) * Math.pow(D, 5) / 120) / Math.cos(phi1Rad);

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

        int zoneNumber = (int) Math.floor((lon + 180) / 6) + 1;
        double lonOrigin = (zoneNumber - 1) * 6 - 180 + 3;
        double lonOriginRad = Math.toRadians(lonOrigin);

        double eccPrimeSquared = (eccSquared) / (1 - eccSquared);

        double N = a / Math.sqrt(1 - eccSquared * Math.pow(Math.sin(latRad), 2));
        double T = Math.pow(Math.tan(latRad), 2);
        double C = eccPrimeSquared * Math.pow(Math.cos(latRad), 2);
        double A = Math.cos(latRad) * (lonRad - lonOriginRad);

        double M = a * ((1 - eccSquared / 4 - 3 * Math.pow(eccSquared, 2) / 64 - 5 * Math.pow(eccSquared, 3) / 256) * latRad
                - (3 * eccSquared / 8 + 3 * Math.pow(eccSquared, 2) / 32 + 45 * Math.pow(eccSquared, 3) / 1024) * Math.sin(2 * latRad)
                + (15 * Math.pow(eccSquared, 2) / 256 + 45 * Math.pow(eccSquared, 3) / 1024) * Math.sin(4 * latRad)
                - (35 * Math.pow(eccSquared, 3) / 3072) * Math.sin(6 * latRad));

        double utmEasting = (k0 * N * (A + (1 - T + C) * Math.pow(A, 3) / 6
                + (5 - 18 * T + Math.pow(T, 2) + 72 * C - 58 * eccPrimeSquared) * Math.pow(A, 5) / 120)
                + 500000.0);

        double utmNorthing = (k0 * (M + N * Math.tan(latRad) * (Math.pow(A, 2) / 2 
                + (5 - T + 9 * C + 4 * Math.pow(C, 2)) * Math.pow(A, 4) / 24
                + (61 - 58 * T + Math.pow(T, 2) + 600 * C - 330 * eccPrimeSquared) * Math.pow(A, 6) / 720)));

        boolean isNorthern = lat >= 0;
        if (!isNorthern) {
            utmNorthing += 10000000.0; // False northing for the southern hemisphere
        }

        return new UTMCoordinate(utmEasting, utmNorthing, zoneNumber, isNorthern);
    }
}