package me.zed.elementhistorydialog;

import java.net.MalformedURLException;
import java.net.URL;

public class Util {

    public static final String BASE_HISTORY_URL = "https://api.openstreetmap.org/api/0.6/";

    URL getElementHistoryUrl(long osmId, String elementType) {
        URL url = null;
        try {
            url = new URL(BASE_HISTORY_URL + elementType + "/" + osmId + "/history");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
}
