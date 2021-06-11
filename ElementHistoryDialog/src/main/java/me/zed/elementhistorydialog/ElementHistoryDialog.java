package me.zed.elementhistorydialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ElementHistoryDialog extends DialogFragment {

    private long osmId;
    private String elementType;

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
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.edit_selection_screen, null);
    }
}
