package me.zed.elementhistorydialog;

import android.content.Context;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

public class Util {

    public static final String BASE_HISTORY_URL = "https://api.openstreetmap.org/api/0.6/";

    public static URL getElementHistoryUrl(long osmId, String elementType) {
        URL url = null;
        try {
            url = new URL(BASE_HISTORY_URL + elementType + "/" + osmId + "/history");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static void makeToast(Context ctx, String message){
        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
    }
}
