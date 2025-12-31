package com.shruti.lofo.ui.DashBoard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.shruti.lofo.R;

import java.util.ArrayList;

public class RecyclerRecentLoFoAdapter
        extends RecyclerView.Adapter<RecyclerRecentLoFoAdapter.ViewHolder> {

    private Context context;
    private ArrayList<DashBoardViewModel> list;

    public interface OnItemClickListener {
        void onItemClick(DashBoardViewModel item);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public RecyclerRecentLoFoAdapter(Context context, ArrayList<DashBoardViewModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.recent_lofo, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        DashBoardViewModel item = list.get(position);

        Glide.with(context)
                .load(item.getImageURI())
                .error(R.drawable.sample_img)
                .into(h.image);

        h.desc.setText(item.getDescription());

        if ("lost".equalsIgnoreCase(item.getTag())) {
            h.tag.setText("Lost");
            h.tag.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
            h.name.setText(item.getOwnerName());
            h.date.setText(item.getDateLost());
        } else {
            h.tag.setText("Found");
            h.tag.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
            h.label.setText("Finder:");
            h.name.setText(item.getFinderName());
            h.date.setText(item.getDateFound());
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, desc, tag, date, label;

        ViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.img_lofo_recent);
            name = v.findViewById(R.id.ownerName);
            desc = v.findViewById(R.id.description);
            tag = v.findViewById(R.id.tag);
            date = v.findViewById(R.id.date);
            label = v.findViewById(R.id.owner_label);
        }
    }
}
