package me.zed.elementhistorydialog;

import android.app.Dialog;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import me.zed.elementhistorydialog.elements.Node;
import me.zed.elementhistorydialog.elements.OsmElement;
import me.zed.elementhistorydialog.elements.Relation;
import me.zed.elementhistorydialog.elements.RelationMember;
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
    OsmElement elementA, elementB;
    Changeset resultA = null, resultB = null;

   public ComparisonScreen(){}

    public static ComparisonScreen newInstance(OsmElement elementA, OsmElement elementB){
        ComparisonScreen cs = new ComparisonScreen();
        Bundle args = new Bundle();
        args.putSerializable("DataA", elementA);
        args.putSerializable("DataB", elementB);
        cs.setArguments(args);
        return cs;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        elementA = (OsmElement) args.getSerializable("DataA");
        elementB = (OsmElement) args.getSerializable("DataB");
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
        View parent = inflater.inflate(R.layout.comparison_screen, null);
        return parent;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUi(view);

        try {
            xmlParserFactory = XmlPullParserFactory.newInstance();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        fetchChangeset(elementA.changeset, versionA);
        fetchChangeset(elementB.changeset, versionB);

    }

    /**
     * Display the common UI elements for all the Element Types
     *
     * @param view parent View
     */
    void initUi(View view) {
        llA = view.findViewById(R.id.version_A);
        llB = view.findViewById(R.id.version_B);
        backButton = view.findViewById(R.id.back_button_comparison);
        parent = view.findViewById(R.id.comparisonParent);
        progressBar = view.findViewById(R.id.comparisonProgressBar);

        backButton.setOnClickListener(v -> {
            if (getDialog() != null) {
                getDialog().onBackPressed();
                if (getFragmentManager() != null) {
                    getFragmentManager().beginTransaction()
                            .add(ElementHistoryDialog.create(elementA.osmId, elementA.getType().name().toLowerCase()), null)
                            .hide(this)
                            .commit();
                }
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
        addTagTableHeading(tl);
        if (!elementA.tags.isEmpty() || !elementB.tags.isEmpty()) {
            addTagTable(tl, elementA.tags, elementB.tags);
        } else {
            //case both are empty add indicator
            addEmptyRow(tl);
        }

        switch (elementA.getType()) {
            case NODE:
                displayNodeData(view);
                break;
            case WAY:
                displayWayData(view);
                break;
            case RELATION:
                displayRelationData(view);
        }

    }

    private void addEmptyRow(TableLayout tl) {
        TableRow tr = new TableRow(getActivity());
        tr.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));
        TextView tv1 = new TextView(getActivity());
        TextView tv2 = new TextView(getActivity());
        TextView tv3 = new TextView(getActivity());
        tv1.setText("-");
        tv2.setText("-");
        tv3.setText("-");
        tr.addView(tv1);
        tr.addView(tv2);
        tr.addView(tv3);
        tl.addView(tr);
    }

    void addTagTableHeading(TableLayout tl) {
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
                TableRow tr = addTableRow(s.getKey(), s.getValue(), tagsB.get(s.getKey()));
                if (!s.getValue().equals(tagsB.get(s.getKey()))) {
                    tr.setBackgroundColor(getResources().getColor(R.color.color_table_change));
                }
                tl.addView(tr);

            } else {
                //b does not contain - add with red colo
                TableRow tr = addTableRow(s.getKey(), s.getValue(), "");
                tr.setBackgroundColor(getResources().getColor(R.color.color_table_deletion));
                tl.addView(tr);
            }
        }
        for (Map.Entry<String, String> s : tagsB.entrySet()) {
            if (!tagsA.containsKey(s.getKey())) {
                //b contains a does not - add with green color
                TableRow tr = addTableRow(s.getKey(), "", s.getValue());
                tr.setBackgroundColor(getResources().getColor(R.color.color_table_addition));
                tl.addView(tr);
            }
        }
    }

    TableRow addTableRow(String keyValue, String aValue, String bValue) {

        TableRow tr = new TableRow(getActivity());
        tr.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));

        tr.addView(getTextViewForTable(7, keyValue));
        tr.addView(getTextViewForTable(9, aValue));
        tr.addView(getTextViewForTable(9, bValue));

        return tr;
    }

    /**
     * Displays relation members with roles for both versions if the elementType is {@link Relation}
     */
    private void displayRelationData(View view) {
        View parent = view.findViewById(R.id.relation_details_table);

        TableLayout tl = parent.findViewById(R.id.relation_member_list_table);
        tl.setStretchAllColumns(true);
        tl.addView(getCustomTableRow(Arrays.asList("NO.", "ROLE", "OBJECT", "|","NO.", "ROLE", "OBJECT")));
        List<RelationMember> membersA = ((Relation) elementA).getMembers();
        List<RelationMember> membersB = ((Relation) elementB).getMembers();
        addRelationTableRows(tl, membersA, membersB);
        parent.setVisibility(View.VISIBLE);
    }

    void addRelationTableRows(TableLayout tl, List<RelationMember> membersA, List<RelationMember> membersB) {

        for (int i = 0; i < membersA.size(); i++) {
            TableRow tr = new TableRow(getActivity());
            tr.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));

            tr.addView(getTextViewForTable(1, String.valueOf(i)));
            tr.addView(getTextViewForTable(5, membersA.get(i).getRole()));
            tr.addView(getTextViewForTable(11, membersA.get(i).getType() + " " + membersA.get(i).getRef()));
            tr.addView(getTextViewForTable(1, "|"));
            //todo bug 25
            tr.addView(getTextViewForTable(1, membersB.get(i) != null ? String.valueOf(i) : "-"));
            tr.addView(getTextViewForTable(5, membersB.get(i) != null ? membersB.get(i).getRole() : "-"));
            tr.addView(getTextViewForTable(11,  membersB.get(i) != null ? membersB.get(i).getType() + " " + membersB.get(i).getRef() : "-"));

            tl.addView(tr);
        }

    }

    /**
     * Displays node list for both the versions if the selected element is of the type {@link Way}
     */
    private void displayWayData(View view) {
        View parent = view.findViewById(R.id.way_details_table);

        TableLayout tl = parent.findViewById(R.id.node_list_table);
        tl.setStretchAllColumns(true);
        tl.addView(getCustomTableRow(Arrays.asList("NO.", "NODES", "NO.", "NODES")));

        List<String> nodesA = ((Way) elementA).getWayNodes();
        List<String> nodesB = ((Way) elementB).getWayNodes();

        addWayTableRows(tl, nodesA, nodesB);
        parent.setVisibility(View.VISIBLE);
    }

    void addWayTableRows(TableLayout tl, List<String> a, List<String> b) {

        for (int i = 0; i < a.size(); i++) {
            TableRow tr = new TableRow(getActivity());
            tr.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));

            tr.addView(getTextViewForTable(3, String.valueOf(i)));
            tr.addView(getTextViewForTable(9, a.get(i)));
            tr.addView(getTextViewForTable(3, b.get(i) != null ? String.valueOf(i) : "-"));
            tr.addView(getTextViewForTable(9, b.get(i) != null ? b.get(i) : "-"));

            tl.addView(tr);
        }

    }

    /**
     * Displays the node data for both the nodes if the selected element type is {@link Node}
     */
    private void displayNodeData(View view) {
        if (view != null && elementA != null && elementA instanceof Node) {

            LinearLayout ll = view.findViewById(R.id.node_details_table);
            TextView lat1 = ll.findViewById(R.id.node_A_latitude);
            TextView lat2 = ll.findViewById(R.id.node_B_latitude);
            TextView lon1 = ll.findViewById(R.id.node_A_longitude);
            TextView lon2 = ll.findViewById(R.id.node_B_longitude);
            LinearLayout distance = ll.findViewById(R.id.distance_details);

            if (((Node) elementA).getLat() == 0 && ((Node) elementA).getLon() == 0) {
                lat1.setText(R.string.none);
                lon1.setText(R.string.none);
            } else {
                lat1.setText(String.valueOf(((Node) elementA).getLat()));
                lon1.setText(String.valueOf(((Node) elementA).getLon()));
            }

            if (((Node) elementB).getLat() == 0 && ((Node) elementB).getLon() == 0) {
                lat2.setText(R.string.none);
                lon2.setText(R.string.none);
            } else {
                lat2.setText(String.valueOf(((Node) elementB).getLat()));
                lon2.setText(String.valueOf(((Node) elementB).getLon()));
            }

            //todo add distance calculations
            distance.setVisibility(View.GONE);
            ll.setVisibility(View.VISIBLE);
        }

    }

    private TextView getTextViewForTable(int ems, String text) {
        TextView tv = new TextView(getActivity());
        tv.setMaxEms(ems);
        tv.setSingleLine(true);
        tv.setEllipsize(TextUtils.TruncateAt.END);
        tv.setText(text);
        return tv;
    }

    TableRow getCustomTableRow(List<String> headings) {
        TableRow tr = new TableRow(getActivity());
        tr.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));

        for (String heading : headings) {
            TextView tv1 = new TextView(getActivity());
            tv1.setText(heading);
            tv1.setTypeface(null, Typeface.BOLD);
            tr.addView(tv1);
        }
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
            parent.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }

    }
}
