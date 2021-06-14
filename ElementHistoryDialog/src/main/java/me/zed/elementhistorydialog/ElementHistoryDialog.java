package me.zed.elementhistorydialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ElementHistoryDialog extends DialogFragment {

    private long osmId;
    private String elementType;
    private OsmParser osmParser;
    private static final String DEBUG_TAG = "ElementHistoryDialog";

    /**
     * Method that will create a new instance of the Dialog
     *
     * @param osmId       the id of the OSM element to be displayed
     * @param elementType the OSM element type
     * @return instance of the Dialog
     */
    public static ElementHistoryDialog create(long osmId, String elementType) {
        return new ElementHistoryDialog(osmId, elementType);
    }

    private ElementHistoryDialog(long osmId, String elementType) {
        this.osmId = osmId;
        this.elementType = elementType;
        osmParser = new OsmParser();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }


    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fetchHistoryData();
        View parent = inflater.inflate(R.layout.edit_selection_screen, null);
        return parent;
    }

    /**
     * Add a row to the TableLayout
     *
     * @param context Android context
     */
    private void addRows(@NonNull Context context) {

    }

    /**
     * Create a row in the dialog for each version of OSM element
     *
     * @param context Android context
     * @return a TableRow
     */
    @NonNull
    TableRow createRow(@NonNull Context context) {
        TableRow tr = new TableRow(context);

        return tr;
    }

    void fetchHistoryData() {
        URL url = Util.getElementHistoryUrl(osmId, elementType);
        try {
            new AsyncTask<Void, Void, InputStream>() {

                @Override
                protected InputStream doInBackground(Void... voids) {
                    InputStream is;
                    try {
                        is = openConnection(getActivity(), url);
                        return is;
                    } catch (IOException e) {
                        Log.e(DEBUG_TAG, e.getMessage());
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(InputStream is) {
                    super.onPostExecute(is);
                    if (is != null) {
                        try {
                            osmParser.start(is);
                        } catch (SAXException | IOException | ParserConfigurationException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                return readCallResponse.body().byteStream();
            } else {
                ((Activity) context).runOnUiThread(() -> Util.makeToast(context, readCallResponse.message()));
            }
        } catch (IllegalArgumentException iaex) {
            throw new IOException("Illegal argument", iaex);
        }
        throw new IOException("openConnection this can't happen");
    }

}
