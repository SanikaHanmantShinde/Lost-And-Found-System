package com.shruti.lofo.ui.Lost;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.Query;
import com.shruti.lofo.R;
import com.shruti.lofo.Utility;

public class LostFragment extends Fragment {

    private RecyclerView recyclerView;
    private LostItemsAdapter adapter;
    private EditText searchEditText;
    private Spinner categorySpinner;
    private FloatingActionButton addLost;
    private TextView filterButton;

    private String selectedCategory = "";

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_lost, container, false);

        recyclerView = view.findViewById(R.id.lostRecyclerView);
        searchEditText = view.findViewById(R.id.searchEditText);
        categorySpinner = view.findViewById(R.id.categorySpinner);
        addLost = view.findViewById(R.id.add_lost);
        filterButton = view.findViewById(R.id.filterButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setupSpinner();
        setupRecycler("", "");
        setupSearch();
        setupFilterClick();

        addLost.setOnClickListener(v ->
                new LostItemsFragment().show(getParentFragmentManager(), "LostDialog")
        );

        return view;
    }

    // 🔹 Load items
    private void setupRecycler(String searchText, String category) {

        Query query = Utility.getCollectionReferrenceForItems2();

        if (!category.isEmpty()) {
            query = query.whereEqualTo("category", category);
        }

        if (!searchText.isEmpty()) {
            query = query.orderBy("itemName")
                    .startAt(searchText)
                    .endAt(searchText + "\uf8ff");
        }

        FirestoreRecyclerOptions<LostItems> options =
                new FirestoreRecyclerOptions.Builder<LostItems>()
                        .setQuery(query, LostItems.class)
                        .build();

        if (adapter != null) adapter.stopListening();

        adapter = new LostItemsAdapter(options, getContext(), "", false);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    // 🔹 Search logic
    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setupRecycler(s.toString().trim(), selectedCategory);
            }
        });
    }

    // 🔹 Filter button click
    private void setupFilterClick() {
        filterButton.setOnClickListener(v -> {
            if (categorySpinner.getVisibility() == View.GONE) {
                categorySpinner.setVisibility(View.VISIBLE);
            } else {
                categorySpinner.setVisibility(View.GONE);
            }
        });
    }

    // 🔹 Spinner setup
    private void setupSpinner() {

        ArrayAdapter<CharSequence> spinnerAdapter =
                ArrayAdapter.createFromResource(
                        requireContext(),
                        R.array.categories_array,
                        android.R.layout.simple_spinner_item
                );

        spinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        categorySpinner.setAdapter(spinnerAdapter);

        categorySpinner.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        String category =
                                categorySpinner.getSelectedItem().toString();

                        selectedCategory =
                                category.equals("All") ? "" : category;

                        setupRecycler(
                                searchEditText.getText().toString().trim(),
                                selectedCategory
                        );
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {}
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }
}
