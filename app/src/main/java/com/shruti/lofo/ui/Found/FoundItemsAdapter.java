package com.shruti.lofo.ui.Found;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.shruti.lofo.R;
import com.shruti.lofo.Utility;

public class FoundItemsAdapter
        extends FirestoreRecyclerAdapter<FoundItems, FoundItemsAdapter.ItemViewHolder> {

    private Context context;
    private boolean showDeleteButton;
    private String category;

    public FoundItemsAdapter(
            @NonNull FirestoreRecyclerOptions<FoundItems> options,
            Context context,
            String category,
            boolean showDeleteButton
    ) {
        super(options);
        this.context = context;
        this.category = category == null ? "" : category;
        this.showDeleteButton = showDeleteButton;
    }

    // ✅ REQUIRED METHOD (FIXES YOUR ERROR)
    public void setCategory(String category) {
        this.category = category == null ? "" : category;
        notifyDataSetChanged();
    }

    public String getCategory() {
        return category;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.found_item_card, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(
            @NonNull ItemViewHolder holder,
            int position,
            @NonNull FoundItems item
    ) {

        // CATEGORY FILTER (SAME AS LOST)
        if (!category.isEmpty() && !category.equals(item.getCategory())) {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(
                    new RecyclerView.LayoutParams(0, 0));
            return;
        }

        // IMAGE (OPTIONAL, SAFE)
        if (item.getImageURI() != null && !item.getImageURI().isEmpty()) {
            Glide.with(context)
                    .load(item.getImageURI())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.baseline_image_search_24)
                    .into(holder.itemImageView);
        } else {
            holder.itemImageView.setImageResource(R.drawable.placeholder_image);
        }

        holder.itemNameTextView.setText(
                item.getItemName() != null ? item.getItemName() : "Unknown Item");

        holder.finderNameTextView.setText(
                item.getfinderName() != null ? item.getfinderName() : "Unknown");

        holder.description.setText(
                item.getDescription() != null ? item.getDescription() : "");

        holder.location.setText(
                item.getLocation() != null ? item.getLocation() : "");

        holder.date.setText(
                item.getDateFound() != null ? item.getDateFound() : "");

        // OPEN DETAILS
        holder.itemView.setOnClickListener(v -> {
            String docId = getSnapshots().getSnapshot(position).getId();

            if (docId == null || docId.isEmpty()) return;

            Intent intent = new Intent(v.getContext(), FoundDetails.class);
            intent.putExtra("itemId", docId);
            v.getContext().startActivity(intent);
        });



        // DELETE BUTTON
        if (showDeleteButton) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> {
                String docId = getSnapshots()
                        .getSnapshot(position).getId();
                Utility.getCollectionReferrenceForFound()
                        .document(docId)
                        .delete();
            });
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        ImageView itemImageView;
        TextView itemNameTextView;
        TextView finderNameTextView;
        TextView description;
        TextView location;
        TextView date;
        ImageButton deleteButton;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImageView = itemView.findViewById(R.id.itemImageView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            finderNameTextView = itemView.findViewById(R.id.finderNameTextView);
            description = itemView.findViewById(R.id.item_description);
            location = itemView.findViewById(R.id.location);
            date = itemView.findViewById(R.id.dateFound);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
