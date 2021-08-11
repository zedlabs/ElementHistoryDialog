package me.zed.elementhistorydialog;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
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
import static me.zed.elementhistorydialog.TableUtil.addEmptyRow;
import static me.zed.elementhistorydialog.TableUtil.addTableRow;
import static me.zed.elementhistorydialog.TableUtil.addTagTableHeading;
import static me.zed.elementhistorydialog.TableUtil.getCustomTableRow;
import static me.zed.elementhistorydialog.TableUtil.getTextViewForTable;
import static me.zed.elementhistorydialog.Util.areEqual;
import static me.zed.elementhistorydialog.Util.findInRelationList;
import static me.zed.elementhistorydialog.Util.getChangeSetUrl;
import static me.zed.elementhistorydialog.Util.getIndexInList;
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

    public ComparisonScreen() {
    }

    public static ComparisonScreen newInstance(OsmElement elementA, OsmElement elementB) {
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
        tl.addView(addTagTableHeading(getActivity()));
        if (!elementA.tags.isEmpty() || !elementB.tags.isEmpty()) {
            addTagTable(tl, elementA.tags, elementB.tags);
        } else {
            //case both are empty add indicator
            tl.addView(addEmptyRow(getActivity()));
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
                TableRow tr = addTableRow(s.getKey(), s.getValue(), tagsB.get(s.getKey()), getActivity());
                if (!s.getValue().equals(tagsB.get(s.getKey()))) {
                    tr.setBackgroundColor(getResources().getColor(R.color.color_table_change));
                }
                tl.addView(tr);

            } else {
                //b does not contain - add with red colo
                TableRow tr = addTableRow(s.getKey(), s.getValue(), "", getActivity());
                tr.setBackgroundColor(getResources().getColor(R.color.color_table_deletion));
                tl.addView(tr);
            }
        }
        for (Map.Entry<String, String> s : tagsB.entrySet()) {
            if (!tagsA.containsKey(s.getKey())) {
                //b contains a does not - add with green color
                TableRow tr = addTableRow(s.getKey(), "", s.getValue(), getActivity());
                tr.setBackgroundColor(getResources().getColor(R.color.color_table_addition));
                tl.addView(tr);
            }
        }
    }

    /**
     * Displays relation members with roles for both versions if the elementType is {@link Relation}
     */
    private void displayRelationData(View view) {
        View parent = view.findViewById(R.id.relation_details_table);

        TableLayout tl = parent.findViewById(R.id.relation_member_list_table);
        tl.setStretchAllColumns(true);
        tl.addView(getCustomTableRow(
                Arrays.asList(
                        getString(R.string.no_text), getString(R.string.role_text), getString(R.string.object_text),
                        getString(R.string.no_text), getString(R.string.role_text), getString(R.string.object_text)
                ),
                getActivity()
                )
        );
        List<RelationMember> membersA = ((Relation) elementA).getMembers();
        List<RelationMember> membersB = ((Relation) elementB).getMembers();
        addRelationTableRows(tl, membersA, membersB);
        parent.setVisibility(View.VISIBLE);
    }

    void addRelationTableRows(TableLayout tl, List<RelationMember> a, List<RelationMember> b) {
        Boolean[] visited = new Boolean[b.size()];
        Arrays.fill(visited, false);

        for (int i = 0; i < a.size(); i++) {
            if (findInRelationList(b, a.get(i))) {
                if (getIndexInList(b, a.get(i)) != -1) visited[getIndexInList(b, a.get(i))] = true;
                //no change
                String objectA = String.format(getString(R.string.relation_object_notation), a.get(i).getType(), String.valueOf(a.get(i).getRef()));
                String objectB = String.format(getString(R.string.relation_object_notation), a.get(i).getType(), String.valueOf(a.get(i).getRef()));
                tl.addView(getRelationTableRow(i, i, a.get(i).getRole(), a.get(i).getRole(), objectA, objectB, getResources().getColor(R.color.white)));
            } else {
                // value deleted
                String objectA = String.format(getString(R.string.relation_object_notation), a.get(i).getType(), String.valueOf(a.get(i).getRef()));
                tl.addView(getRelationTableRow(i, -1, a.get(i).getRole(), "-", objectA, "-", getResources().getColor(R.color.color_table_deletion)));
            }
        }

        for (int i = 0; i < b.size(); i++) {
            //value added
            if (!visited[i]) {
                String objectB = String.format(getString(R.string.relation_object_notation), b.get(i).getType(), String.valueOf(b.get(i).getRef()));
                tl.addView(getRelationTableRow(-1, i, "-", b.get(i).getRole(), "-", objectB, getResources().getColor(R.color.color_table_addition)));
            }
        }

    }

    TableRow getRelationTableRow(int noA, int noB, String roleA, String roleB, String objectA, String objectB, int colorId) {
        TableRow tr = new TableRow(getActivity());
        tr.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));

        tr.addView(getTextViewForTable(3, noA != -1 ? String.valueOf(noA + 1) : "-", getActivity()));
        tr.addView(getTextViewForTable(12, roleA.isEmpty() ? "-" : roleA, getActivity()));
        tr.addView(getTextViewForTable(12, objectA, getActivity()));
        tr.addView(getTextViewForTable(3, noB != -1 ? String.valueOf(noB + 1) : "-", getActivity()));
        tr.addView(getTextViewForTable(12, roleB.isEmpty() ? "-" : roleB, getActivity()));
        tr.addView(getTextViewForTable(12, objectB, getActivity()));

        tr.setBackgroundColor(colorId);
        return tr;
    }

    /**
     * Displays node list for both the versions if the selected element is of the type {@link Way}
     */
    private void displayWayData(View view) {
        View parent = view.findViewById(R.id.way_details_table);

        TableLayout tl = parent.findViewById(R.id.node_list_table);
        tl.setStretchAllColumns(true);
        tl.addView(getCustomTableRow(Arrays.asList(getString(R.string.no_text), getString(R.string.nodes_text), getString(R.string.no_text), getString(R.string.nodes_text)), getActivity()));

        List<String> nodesA = ((Way) elementA).getWayNodes();
        List<String> nodesB = ((Way) elementB).getWayNodes();

        addWayTableRows(tl, nodesA, nodesB);
        parent.setVisibility(View.VISIBLE);
    }

    void addWayTableRows(TableLayout tl, List<String> a, List<String> b) {
        int j = 0, k = 0;
        while (j < a.size() || k < b.size()) {
            if (j < a.size() && k < b.size()) {
                if (a.get(j).equals(b.get(k))) {
                    //no change
                    tl.addView(getWayTableRow(j, k, a.get(j), b.get(k), getResources().getColor(R.color.white)));
                    j++;
                    k++;
                } else if (!a.get(j).equals(b.get(k)) && !b.contains(a.get(j))) {
                    if (j == k && !a.contains(b.get(k))) {
                        // value change
                        tl.addView(getWayTableRow(j, k, a.get(j), b.get(k), getResources().getColor(R.color.color_table_change)));
                        j++;
                        k++;
                    } else {
                        // value deleted
                        tl.addView(getWayTableRow(j, -1, a.get(j), "-", getResources().getColor(R.color.color_table_deletion)));
                        j++;
                    }
                } else if (!a.get(j).equals(b.get(k)) && b.contains(a.get(j))) {
                    //value added
                    tl.addView(getWayTableRow(-1, k, "-", b.get(k), getResources().getColor(R.color.color_table_addition)));
                    k++;
                }
            } else {
                if (k < b.size() && !a.contains(b.get(k))) {
                    //value added
                    tl.addView(getWayTableRow(-1, k, "-", b.get(k), getResources().getColor(R.color.color_table_addition)));
                    k++;
                } else if (j < a.size() && !b.contains(a.get(j))) {
                    // value deleted
                    tl.addView(getWayTableRow(j, -1, a.get(j), "-", getResources().getColor(R.color.color_table_deletion)));
                    j++;
                }
            }
        }
    }

    TableRow getWayTableRow(int noA, int noB, String nodeA, String nodeB, int colorId) {
        TableRow tr = new TableRow(getActivity());
        tr.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT));

        tr.addView(getTextViewForTable(3, noA != -1 ? String.valueOf(noA + 1) : "-", getActivity()));
        tr.addView(getTextViewForTable(9, nodeA, getActivity()));
        tr.addView(getTextViewForTable(3, noB != -1 ? String.valueOf(noB + 1) : "-", getActivity()));
        tr.addView(getTextViewForTable(9, nodeB, getActivity()));

        tr.setBackgroundColor(colorId);
        return tr;
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
