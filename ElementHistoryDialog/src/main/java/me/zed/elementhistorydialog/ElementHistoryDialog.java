package me.zed.elementhistorydialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import me.zed.elementhistorydialog.elements.OsmElement;

import static me.zed.elementhistorydialog.Util.openConnection;

public class ElementHistoryDialog extends DialogFragment {
    private static final String DEBUG_TAG = ElementHistoryDialog.class.getSimpleName();

    private static final String ELEMENT_TYPE_KEY = "element_type";
    private static final String OSM_ID_KEY       = "osm_id";
    private static final String BASE_URL_KEY     = "base_url";

    // data associated with the selected OSM element
    private long      osmId;
    private String    elementType;
    private OsmParser osmParser;

    // selections to pass to the comparison screen
    private int positionA = -1, positionB = -1;

    RecyclerView versionList;
    ProgressBar  progressBar;
    LinearLayout parentLayout;
    LinearLayout errorLayout;
    Button       goBackBtn;
    String       baseUrl;

    /**
     * Method that will create a new instance of the Dialog
     *
     * @param osmId the id of the OSM element to be displayed
     * @param elementType the OSM element type
     * @return instance of the Dialog
     */
    public static ElementHistoryDialog create(long osmId, String elementType) {
        return new ElementHistoryDialog(Util.BASE_URL, osmId, elementType);
    }

    /**
     * Method that will create a new instance of the Dialog
     *
     * @param baseUrl base API url to use
     * @param osmId the id of the OSM element to be displayed
     * @param elementType the OSM element type
     * @return instance of the Dialog
     */
    public static ElementHistoryDialog create(String baseUrl, long osmId, String elementType) {
        return new ElementHistoryDialog(baseUrl, osmId, elementType);
    }

    private ElementHistoryDialog(String baseUrl, long osmId, String elementType) {
        this();
        this.osmId = osmId;
        this.elementType = elementType;
        this.baseUrl = baseUrl;
    }

    public ElementHistoryDialog() {
        osmParser = new OsmParser();
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
        if (savedInstanceState != null) {
            Log.d(DEBUG_TAG, "Restoring from instance state");
            String temp = savedInstanceState.getString(ELEMENT_TYPE_KEY);
            if (temp != null) {
                elementType = temp;
            }
            osmId = savedInstanceState.getLong(OSM_ID_KEY);
            temp = savedInstanceState.getString(BASE_URL_KEY);
            if (temp != null) {
                baseUrl = temp;
            }
        }
        View parent = inflater.inflate(R.layout.edit_selection_screen, null);
        versionList = (RecyclerView) parent.findViewById(R.id.itemVersionList);
        progressBar = parent.findViewById(R.id.editSelectionProgressBar);
        parentLayout = parent.findViewById(R.id.editSelectionParent);
        errorLayout = parent.findViewById(R.id.error_layout);
        return parent;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View c = view.findViewById(R.id.compare);
        View e = view.findViewById(R.id.exit);
        goBackBtn = view.findViewById(R.id.go_back_btn);

        fetchHistoryData();

        c.setOnClickListener(v -> {
            if (positionA == -1 || positionB == -1) {
                Toast.makeText(requireContext(), R.string.zed_ehd_select_a_b_toast, Toast.LENGTH_SHORT).show();
            } else {
                // navigate to comparison screen
                OsmElement elementA = osmParser.getStorage().getAll().get(positionA);
                OsmElement elementB = osmParser.getStorage().getAll().get(positionB);
                ComparisonScreen cs = ComparisonScreen.newInstance(baseUrl, elementA, elementB);

                if (getFragmentManager() != null) {
                    getFragmentManager().beginTransaction().add(cs, null).remove(this).commit();
                }
            }
        });

        e.setOnClickListener(v -> {
            if (getDialog() != null) {
                getDialog().dismiss();
            }
        });

        goBackBtn.setOnClickListener(v -> {
            if (getDialog() != null) {
                getDialog().dismiss();
            }
        });
    }

    /**
     * Initialize the versionList RecyclerView with initial data
     *
     * @param ctx android context
     */
    private void addToList(Context ctx) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(ctx);
        versionList.setLayoutManager(layoutManager);
        final VersionListAdapter adapter = new VersionListAdapter(osmParser.getStorage().getAll(),
                new AOnCheckedChangeListener(osmParser.getStorage().getAll()), new BOnCheckedChangeListener(osmParser.getStorage().getAll()));
        versionList.setAdapter(adapter);
    }

    /**
     * OnCheckChangeListener for column A in the versionList i.e the 1st element for the comparison
     */
    private class AOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
        final List<OsmElement> ids;

        AOnCheckedChangeListener(@NonNull List<OsmElement> ids) {
            this.ids = ids;
        }

        @SuppressLint("ResourceType")
        @Override
        public void onCheckedChanged(RadioGroup group, int position) {
            if (position != -1 && position < ids.size()) {
                positionA = position;
            } else {
                Log.e(DEBUG_TAG, "position out of range 0-" + (ids.size() - 1) + ": " + position);
            }

        }
    }

    /**
     * OnCheckChangeListener for column B in the versionList i.e the 2nd element for the comparison
     */
    private class BOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
        final List<OsmElement> ids;

        BOnCheckedChangeListener(@NonNull List<OsmElement> ids) {
            this.ids = ids;
        }

        @SuppressLint("ResourceType")
        @Override
        public void onCheckedChanged(RadioGroup group, int position) {
            if (position != -1 && position < ids.size()) {
                positionB = position;
            } else {
                Log.e(DEBUG_TAG, "position out of range 0-" + (ids.size() - 1) + ": " + position);
            }

        }
    }

    void showFailedCaseUI() {
        errorLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Function to fetch the element history data through the '/history' endpoint on the background thread and post the
     * result back on the main thread
     */
    void fetchHistoryData() {
        URL url = Util.getElementHistoryUrl(baseUrl, osmId, elementType);
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
                            osmParser.start(is);
                            return true;
                        } catch (SAXException | IOException | ParserConfigurationException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // element deleted go back
                        // return false;
                    }
                    return false;
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    super.onPostExecute(result);
                    if (result == false) {
                        // handle failed case
                        Log.e("AsyncTask", "failed to load result");
                        progressBar.setVisibility(View.GONE);
                        showFailedCaseUI();
                    } else {
                        // add data to the rows
                        if (progressBar != null)
                            progressBar.setVisibility(View.GONE);
                        parentLayout.setVisibility(View.VISIBLE);
                        // silently fail if the context has gone away
                        Context context = getContext();
                        if (context != null) {
                            addToList(context);
                        }
                    }
                }
            }.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        Log.d(DEBUG_TAG, "Saving instance state");
        bundle.putString(ELEMENT_TYPE_KEY, elementType);
        bundle.putLong(OSM_ID_KEY, osmId);
        bundle.putString(BASE_URL_KEY, baseUrl);
    }
}
