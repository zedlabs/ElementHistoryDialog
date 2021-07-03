package me.zed.elementhistorydialog;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import me.zed.elementhistorydialog.elements.OsmElement;

import static me.zed.elementhistorydialog.Changeset.KEY_COMMENT;
import static me.zed.elementhistorydialog.Changeset.KEY_IMAGERY_USED;
import static me.zed.elementhistorydialog.Changeset.KEY_SOURCE;
import static me.zed.elementhistorydialog.Util.getChangeSetUrl;
import static me.zed.elementhistorydialog.Util.openConnection;

/**
 * Extension of the Element History Dialog used to compare the different selections that
 * have been passed from the ElementHistoryDialog
 */
public class ComparisonScreen extends DialogFragment {

    private XmlPullParserFactory xmlParserFactory = null;
    public static final String DEBUG_TAG = "ComparisonScreen";
    public static final String versionA = "A";
    public static final String versionB = "B";

    LinearLayout llA;
    LinearLayout llB;
    Changeset resultA = null, resultB = null;

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
        View parent = inflater.inflate(R.layout.comparison_screen, null);

        return parent;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();

        OsmElement elementA = (OsmElement) args.getSerializable("DataA");
        OsmElement elementB = (OsmElement) args.getSerializable("DataB");

        llA = view.findViewById(R.id.version_A);
        llB = view.findViewById(R.id.version_B);

        TextView versionATextView = llA.findViewById(R.id.version_number);
        TextView versionBTextView = llB.findViewById(R.id.version_number);
        TextView timestampA = llA.findViewById(R.id.date_created);
        TextView timestampB = llB.findViewById(R.id.date_created);
        TextView usernameA = llA.findViewById(R.id.username);
        TextView usernameB = llB.findViewById(R.id.username);
        TextView changesetA = llA.findViewById(R.id.changeset_version);
        TextView changesetB = llB.findViewById(R.id.changeset_version);

        versionATextView.setText(String.valueOf(elementA.osmVersion));
        versionBTextView.setText(String.valueOf(elementB.osmVersion));
        timestampA.setText(elementA.timestamp);
        timestampB.setText(elementB.timestamp);
        usernameA.setText(elementA.username);
        usernameB.setText(elementB.username);
        changesetA.setText(String.valueOf(elementA.changeset));
        changesetB.setText(String.valueOf(elementB.changeset));

        TableLayout tl1 = llA.findViewById(R.id.tag_table);
        TableRow tr = new TableRow(getActivity());
        TextView tv1 = new TextView(getActivity());

        tr.addView(tv1);
        tl1.addView(tr);

        try {
            xmlParserFactory = XmlPullParserFactory.newInstance();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        fetchChangeset(elementA.changeset, versionA);
        fetchChangeset(elementB.changeset, versionB);

    }

    /**
     * Function to fetch the element history data through the '/history' endpoint
     * on the background thread and post the result back on the main thread
     */
    void fetchChangeset(long csId, String version) {
        URL url = getChangeSetUrl(csId);
        try {
            new AsyncTask<Void, Void, Boolean>() {

                @Override
                protected Boolean doInBackground(Void... voids) {
                    InputStream is = null;
                    try {
                        is = openConnection(getActivity(), url);
                    } catch (IOException e) {
                        Log.e(DEBUG_TAG, e.getMessage());
                    }
                    if (is != null) {
                        try {
                            if (version == versionA) {
                                resultA = Changeset.parse(xmlParserFactory.newPullParser(), is);
                            } else {
                                resultB = Changeset.parse(xmlParserFactory.newPullParser(), is);
                            }

                            return true;
                        } catch (IOException | XmlPullParserException e) {
                            e.printStackTrace();
                        }
                    }
                    return false;
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    super.onPostExecute(result);
                    if (result == false) {
                        //handle failed case
                    } else {
                        //add data to the rows
                        displayChangeSetData(version);
                    }
                }
            }.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void displayChangeSetData(String version) {

        LinearLayout ll = llA;
        Changeset result = resultA;
        if (version.equals(versionB)) {
            ll = llB;
            result = resultB;
        }

        TextView description = ll.findViewById(R.id.description);
        TextView source = ll.findViewById(R.id.source);
        TextView imagery = ll.findViewById(R.id.imagery);

        if (result != null) {
            for (Map.Entry<String, String> s : result.tags.entrySet()) {
                switch (s.getKey()) {
                    case KEY_COMMENT:
                        description.setText(s.getValue());
                        break;
                    case KEY_SOURCE:
                        source.setText(s.getValue());
                        break;
                    case KEY_IMAGERY_USED:
                        imagery.setText(s.getValue());
                        break;
                    default:
                        //nothing
                }
            }
        }

    }
}
