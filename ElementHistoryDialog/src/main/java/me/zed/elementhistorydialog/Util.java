package me.zed.elementhistorydialog;

import android.content.Context;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

public class Util {

    public static final String BASE_HISTORY_URL = "https://api.openstreetmap.org/api/0.6/";

    /**
     * Utility URL building function for a given OSM element
     * @param osmId id of the current OSM element
     * @param elementType type of the current OSM element
     * @return URL to fetch the history data
     */
    public static URL getElementHistoryUrl(long osmId, String elementType) {
        URL url = null;
        try {
            url = new URL(BASE_HISTORY_URL + elementType + "/" + osmId + "/history");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * Utility function to make a toast message
     */
    public static void makeToast(Context ctx, String message){
        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
    }
}
