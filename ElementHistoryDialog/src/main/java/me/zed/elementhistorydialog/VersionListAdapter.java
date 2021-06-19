package me.zed.elementhistorydialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import me.zed.elementhistorydialog.elements.OsmElement;

public class VersionListAdapter extends RecyclerView.Adapter<VersionListAdapter.ItemViewHolder> {
    private List<OsmElement> elements;
    private long currentIdA;
    private long currentIdB;
    private android.widget.RadioGroup.OnCheckedChangeListener groupAChangeListener;
    private android.widget.RadioGroup.OnCheckedChangeListener groupBChangeListener;

    private int selectedA = -1;
    private int selectedB = -1;

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        AppCompatRadioButton buttonA;
        AppCompatRadioButton buttonB;
        TextView versionText;
        TextView dateText;
        TextView usernameText;

        /**
         * Create a new ViewHolder
         *
         * @param v the RadioButton that will be displayed
         */
        public ItemViewHolder(@NonNull View v) {
            super(v);
            buttonA = v.findViewById(R.id.listItemRadioButtonA);
            buttonB = v.findViewById(R.id.listItemRadioButtonB);
            versionText = v.findViewById(R.id.osmVersion);
            dateText = v.findViewById(R.id.timestamp);
            usernameText = v.findViewById(R.id.user);

        }
    }

    public VersionListAdapter(@NonNull List<OsmElement> items,
                              @NonNull RadioGroup.OnCheckedChangeListener groupAChangeListener,
                              @NonNull RadioGroup.OnCheckedChangeListener groupBChangeListener) {
        this.elements = items;
        this.groupAChangeListener = groupAChangeListener;
        this.groupBChangeListener = groupBChangeListener;
    }

    final OnCheckedChangeListener onCheckedChangeListenerA = (buttonView, isChecked) -> {
        Integer position = (Integer) buttonView.getTag();
        if (position != null) {
            VersionListAdapter.this.notifyItemChanged(selectedA);
            currentIdA = elements.get(position).osmVersion;
            selectedA = position;
            groupAChangeListener.onCheckedChanged(null, position);
        }
    };

    final OnCheckedChangeListener onCheckedChangeListenerB = (buttonView, isChecked) -> {
        Integer position = (Integer) buttonView.getTag();
        if (position != null) {
            VersionListAdapter.this.notifyItemChanged(selectedB);
            currentIdB = elements.get(position).osmVersion;
            selectedB = position;
            groupBChangeListener.onCheckedChanged(null, position);
        }
    };

    @Override
    public VersionListAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.version_list_item, parent, false);
        return new ItemViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.versionText.setText(String.valueOf(elements.get(position).osmVersion));
        holder.dateText.setText(elements.get(position).timestamp);
        holder.usernameText.setText(elements.get(position).username);
        holder.buttonA.setTag(position);
        holder.buttonA.setOnCheckedChangeListener(null);
        if (elements.get(position).osmVersion == currentIdA) {
            holder.buttonA.setChecked(true);
            selectedA = position;
        } else {
            holder.buttonA.setChecked(false);
        }
        holder.buttonA.setOnCheckedChangeListener(onCheckedChangeListenerA);
        holder.buttonB.setTag(position);
        holder.buttonB.setOnCheckedChangeListener(null);
        if (elements.get(position).osmVersion == currentIdB) {
            holder.buttonB.setChecked(true);
            selectedB = position;
        } else {
            holder.buttonB.setChecked(false);
        }
        holder.buttonB.setOnCheckedChangeListener(onCheckedChangeListenerB);
    }

    @Override
    public int getItemCount() {
        return elements.size();
    }
}
