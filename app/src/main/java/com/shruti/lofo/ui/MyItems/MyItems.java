package com.shruti.lofo.ui.MyItems;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Query;
import com.shruti.lofo.R;
import com.shruti.lofo.Utility;
import com.shruti.lofo.ui.Found.FoundItems;
import com.shruti.lofo.ui.Found.FoundItemsAdapter;
import com.shruti.lofo.ui.Lost.LostItems;
import com.shruti.lofo.ui.Lost.LostItemsAdapter;

public class MyItems extends Fragment {

    private LostItemsAdapter lostAdapter;
    private FoundItemsAdapter foundAdapter;
    private String userId;

    private RecyclerView lostRecyclerView;
    private RecyclerView foundRecyclerView;
    private TextView lostEmptyText, foundEmptyText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_my_items, container, false);

        // Firebase current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentUser != null ? currentUser.getUid() : "";

        // Views
        lostRecyclerView = root.findViewById(R.id.lostRecyclerView);
        foundRecyclerView = root.findViewById(R.id.foundRecyclerView);
        lostEmptyText = root.findViewById(R.id.lostEmptyText);
        foundEmptyText = root.findViewById(R.id.foundEmptyText);

        // Setup RecyclerViews
        setupLostRecyclerView();
        setupFoundRecyclerView();

        return root;
    }

    private void setupLostRecyclerView() {
        Query lostQuery = Utility.getCollectionReferrenceForItems2().whereEqualTo("userId", userId);

        FirestoreRecyclerOptions<LostItems> lostOptions = new FirestoreRecyclerOptions.Builder<LostItems>()
                .setQuery(lostQuery, LostItems.class)
                .build();

        lostRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        lostAdapter = new LostItemsAdapter(lostOptions, requireContext(), "", false); // false = hide delete button
        lostRecyclerView.setAdapter(lostAdapter);

        // Show empty state
        lostAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (lostAdapter.getItemCount() == 0) {
                    lostEmptyText.setVisibility(View.VISIBLE);
                } else {
                    lostEmptyText.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setupFoundRecyclerView() {
        Query foundQuery = Utility.getCollectionReferrenceForFound().whereEqualTo("finderId", userId);

        FirestoreRecyclerOptions<FoundItems> foundOptions = new FirestoreRecyclerOptions.Builder<FoundItems>()
                .setQuery(foundQuery, FoundItems.class)
                .build();

        foundRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        foundAdapter = new FoundItemsAdapter(foundOptions, requireContext(), "", false);
        foundRecyclerView.setAdapter(foundAdapter);

        // Show empty state
        foundAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (foundAdapter.getItemCount() == 0) {
                    foundEmptyText.setVisibility(View.VISIBLE);
                } else {
                    foundEmptyText.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (lostAdapter != null) lostAdapter.startListening();
        if (foundAdapter != null) foundAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (lostAdapter != null) lostAdapter.stopListening();
        if (foundAdapter != null) foundAdapter.stopListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (lostAdapter != null) lostAdapter.notifyDataSetChanged();
        if (foundAdapter != null) foundAdapter.notifyDataSetChanged();
    }
}
