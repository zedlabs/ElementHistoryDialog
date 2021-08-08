package me.zed.elementhistorydialog;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import me.zed.elementhistorydialog.elements.RelationMember;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Util {

    public static final String BASE_HISTORY_URL = "https://api.openstreetmap.org/api/0.6/";
    public static final String DEBUG_TAG = "utility-method";

    /**
     * Utility URL building function for a given OSM element
     *
     * @param osmId       id of the current OSM element
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
     * Utility URL building function for a given changeset
     *
     * @param osmId id of the current OSM element
     * @return URL to fetch the changeset
     */
    public static URL getChangeSetUrl(long osmId) {
        URL url = null;
        try {
            url = new URL(BASE_HISTORY_URL + "changeset/" + osmId);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * Utility function to make a toast message
     */
    public static void makeToast(Context ctx, String message) {
        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Given an URL, open the connection and return the InputStream
     *
     * @param context Android context
     * @param url     the URL
     * @return the InputStream
     * @throws IOException on any IO and other error
     */
    @NonNull
    public static InputStream openConnection(@Nullable final Context context, @NonNull URL url) throws IOException {
        Log.d(DEBUG_TAG, "get history data for  " + url.toString());
        try {
            Request request = new Request.Builder().url(url).build();
            OkHttpClient.Builder builder = new OkHttpClient.Builder();

            OkHttpClient client = builder.build();
            Call readCall = client.newCall(request);
            Response readCallResponse = readCall.execute();

            if (readCallResponse.isSuccessful()) {
                if (readCallResponse.body() != null) {
                    return readCallResponse.body().byteStream();
                }
            } else {
                if (context != null) {
                    ((Activity) context).runOnUiThread(() -> Util.makeToast(context, readCallResponse.message()));
                }
            }
        } catch (IllegalArgumentException iaex) {
            throw new IOException("Illegal argument", iaex);
        }
        throw new IOException("openConnection this can't happen");
    }

    public static boolean areEqual(RelationMember rm1, RelationMember rm2) {
        return rm1.getRef() == rm2.getRef() && rm1.getType().equals(rm2.getType());
    }

    public static Boolean findInRelationList(List<RelationMember> list, RelationMember relationMember) {
        for (RelationMember currentMember : list) {
            if (areEqual(currentMember, relationMember)) return true;
        }
        return false;
    }

    public static int getIndexInList(List<RelationMember> list, RelationMember relationMember) {
        for (int i = 0; i < list.size(); i++) {
            if (areEqual(list.get(i), relationMember)) return i;
        }
        return -1;
    }
}
