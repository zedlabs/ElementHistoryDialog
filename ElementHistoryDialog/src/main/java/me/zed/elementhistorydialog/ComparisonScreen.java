package me.zed.elementhistorydialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
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

import me.zed.elementhistorydialog.elements.Node;
import me.zed.elementhistorydialog.elements.OsmElement;
import me.zed.elementhistorydialog.elements.Relation;
import me.zed.elementhistorydialog.elements.Way;

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
    ImageButton backButton;
    ScrollView parent;
    ProgressBar progressBar;

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

        initUi(view, elementA, elementB);

        switch (elementA.getType()) {
            case NODE:
                displayNodeData();
                break;
            case WAY:
                displayWayData();
                break;
            case RELATION:
                displayRelationData();
        }

        try {
            xmlParserFactory = XmlPullParserFactory.newInstance();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        fetchChangeset(elementA.changeset, versionA);
        fetchChangeset(elementB.changeset, versionB);

    }

    /**
     * Displays relation members with roles for both versions if the elementType is {@link Relation}
     */
    private void displayRelationData() {

    }

    /**
     * Displays node list for both the versions if the selected element is of the type {@link Way}
     */
    private void displayWayData() {
    }

    /**
     * Displays the node data for both the nodes if the selected element type is {@link Node}
     */
    private void displayNodeData() {
    }

    /**
     * Display the common UI elements for all the Element Types
     *
     * @param view     parent View
     * @param elementA Selected OSM element A
     * @param elementB Selected OSM element B
     */
    void initUi(View view, OsmElement elementA, OsmElement elementB) {
        llA = view.findViewById(R.id.version_A);
        llB = view.findViewById(R.id.version_B);
        backButton = view.findViewById(R.id.back_button_comparison);
        parent = view.findViewById(R.id.comparisonParent);
        progressBar = view.findViewById(R.id.comparisonProgressBar);

        backButton.setOnClickListener(v -> {
            if (getDialog() != null) {
                getDialog().onBackPressed();
            }
        });

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

        TableLayout tl = view.findViewById(R.id.tag_table);
        tl.setStretchAllColumns(true);

        if (!elementA.tags.isEmpty() || !elementB.tags.isEmpty()) {
            addTableHeading(tl);
            addTagTable(tl, elementA.tags, elementB.tags);
        }
    }

    void addTableHeading(TableLayout tl) {
        TableRow tr = new TableRow(getActivity());
        tr.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));

        TextView tv1 = new TextView(getActivity());
        TextView tv2 = new TextView(getActivity());
        TextView tv3 = new TextView(getActivity());

        tv1.setText("KEY");
        tv2.setText(getString(R.string.version_a));
        tv3.setText(getString(R.string.version_b));

        tv1.setTypeface(null, Typeface.BOLD);
        tv2.setTypeface(null, Typeface.BOLD);
        tv3.setTypeface(null, Typeface.BOLD);

        tr.addView(tv1);
        tr.addView(tv2);
        tr.addView(tv3);
        tl.addView(tr);

    }

    /**
     * Displays a table to visualize the changes between the tags in the different versions
     *
     * @param tl    parent table layout
     * @param tagsA tag list for element A
     * @param tagsB tag list for element B
     */
    void addTagTable(TableLayout tl, Map<String, String> tagsA, Map<String, String> tagsB) {
        for (Map.Entry<String, String> s : tagsA.entrySet()) {
            if (tagsB.containsKey(s.getKey())) {
                //b also contains - add without bg color, add with change color for value change
                TableRow tr = addTableRow(getActivity(), s.getKey(), s.getValue(), tagsB.get(s.getKey()));
                if (!s.getValue().equals(tagsB.get(s.getKey()))) {
                    tr.setBackgroundColor(getResources().getColor(R.color.color_table_change));
                }
                tl.addView(tr);

            } else {
                //b does not contain - add with red colo
                TableRow tr = addTableRow(getActivity(), s.getKey(), s.getValue(), "");
                tr.setBackgroundColor(getResources().getColor(R.color.color_table_deletion));
                tl.addView(tr);
            }
        }
        for (Map.Entry<String, String> s : tagsB.entrySet()) {
            if (!tagsA.containsKey(s.getKey())) {
                //b contains a does not - add with green color
                TableRow tr = addTableRow(getActivity(), s.getKey(), "", s.getValue());
                tr.setBackgroundColor(getResources().getColor(R.color.color_table_addition));
                tl.addView(tr);
            }
        }
    }

    TableRow addTableRow(Context ctx, String keyValue, String aValue, String bValue) {

        TableRow tr = new TableRow(getActivity());
        tr.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));

        TextView keyText = new TextView(ctx);
        TextView valueATextView = new TextView(ctx);
        TextView valueBTextView = new TextView(ctx);

        keyText.setMaxEms(7);
        keyText.setSingleLine(true);
        keyText.setEllipsize(TextUtils.TruncateAt.END);
        keyText.setText(keyValue);

        valueATextView.setMaxEms(9);
        valueATextView.setSingleLine(true);
        valueATextView.setEllipsize(TextUtils.TruncateAt.END);
        valueATextView.setText(aValue);

        valueBTextView.setMaxEms(9);
        valueBTextView.setSingleLine(true);
        valueBTextView.setEllipsize(TextUtils.TruncateAt.END);
        valueBTextView.setText(bValue);

        tr.addView(keyText);
        tr.addView(valueATextView);
        tr.addView(valueBTextView);

        return tr;
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

    /**
     * Display the data fetched for a particular changeset
     *
     * @param version OSM element history version
     */
    void displayChangeSetData(String version) {

        LinearLayout ll = llA;
        Changeset result = resultA;
        if (version.equals(versionB)) {
            ll = llB;
            result = resultB;
        }

        TextView description = ll.findViewById(R.id.description_text);
        LinearLayout sourceParent = ll.findViewById(R.id.source_parent);
        LinearLayout imageryParent = ll.findViewById(R.id.imagery_parent);
        TextView source = ll.findViewById(R.id.source);
        TextView imagery = ll.findViewById(R.id.imagery);

        if (result != null) {
            for (Map.Entry<String, String> s : result.tags.entrySet()) {
                switch (s.getKey()) {
                    case KEY_COMMENT:
                        description.setText(s.getValue());
                        description.setVisibility(View.VISIBLE);
                        break;
                    case KEY_SOURCE:
                        source.setText(s.getValue());
                        sourceParent.setVisibility(View.VISIBLE);
                        break;
                    case KEY_IMAGERY_USED:
                        imagery.setText(s.getValue());
                        imageryParent.setVisibility(View.VISIBLE);
                        break;
                    default:
                        //nothing
                }
            }
        }
        if (version.equals(versionB)) {
            progressBar.setVisibility(View.GONE);
            parent.setVisibility(View.VISIBLE);
        }

    }
}
