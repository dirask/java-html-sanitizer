package org.owasp.html;

public class EncodingSettings {

    private static boolean encodePcdataOnto = true;

    public EncodingSettings(boolean _disableEncodePcdataOnto) {
        encodePcdataOnto = _disableEncodePcdataOnto;
    }

    public static void setEncodePcdataOnto(boolean encodePcdataOnto) {
        EncodingSettings.encodePcdataOnto = encodePcdataOnto;
    }

    public static boolean isEncodePcdataOnto() {
        return encodePcdataOnto;
    }
}
