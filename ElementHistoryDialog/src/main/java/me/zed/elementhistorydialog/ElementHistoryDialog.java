package me.zed.elementhistorydialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import me.zed.elementhistorydialog.elements.OsmElement;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static me.zed.elementhistorydialog.Util.openConnection;

public class ElementHistoryDialog extends DialogFragment {

    //data associated with the selected OSM element
    private long osmId;
    private String elementType;
    private OsmParser osmParser;

    //selections to pass to the comparison screen
    private int positionA = -1, positionB = -1;

    RecyclerView versionList;
    ProgressBar progressBar;
    LinearLayout parentLayout;
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

        View parent = inflater.inflate(R.layout.edit_selection_screen, null);
        versionList = (RecyclerView) parent.findViewById(R.id.itemVersionList);
        progressBar = parent.findViewById(R.id.editSelectionProgressBar);
        parentLayout = parent.findViewById(R.id.editSelectionParent);

        return parent;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchHistoryData();
        View c = view.findViewById(R.id.compare);
        View e = view.findViewById(R.id.exit);

        c.setOnClickListener(v -> {
            if (positionA == -1 || positionB == -1) {
                Toast.makeText(requireContext(), "Select version A & B for comparison", Toast.LENGTH_SHORT).show();
            } else {
                //navigate to comparison screen
                ComparisonScreen cs = new ComparisonScreen();
                Bundle b = new Bundle();

                OsmElement elementA = osmParser.getStorage().getElementWithVersion(positionA);
                OsmElement elementB = osmParser.getStorage().getElementWithVersion(positionB);

                b.putSerializable("DataA", elementA);
                b.putSerializable("DataB", elementB);

                cs.setArguments(b);

                getFragmentManager().beginTransaction()
                        .add(cs, null)
                        .commit();
            }
        });

        e.setOnClickListener(v -> {
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
        final VersionListAdapter adapter = new VersionListAdapter(
                osmParser.getStorage().getAll(),
                new AOnCheckedChangeListener(osmParser.getStorage().getAll()),
                new BOnCheckedChangeListener(osmParser.getStorage().getAll()));
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
                positionA = position + 1;
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
                positionB = position + 1;
            } else {
                Log.e(DEBUG_TAG, "position out of range 0-" + (ids.size() - 1) + ": " + position);
            }

        }
    }

    /**
     * Function to fetch the element history data through the '/history' endpoint
     * on the background thread and post the result back on the main thread
     */
    void fetchHistoryData() {
        URL url = Util.getElementHistoryUrl(osmId, elementType);
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
                        if(progressBar != null) progressBar.setVisibility(View.GONE);
                        parentLayout.setVisibility(View.VISIBLE);
                        addToList(requireContext());
                    }
                }
            }.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
