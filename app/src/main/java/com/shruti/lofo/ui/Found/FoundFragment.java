package com.shruti.lofo.ui.Found;

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

public class FoundFragment extends Fragment {

    private RecyclerView recyclerView;
    private FoundItemsAdapter adapter;
    private EditText searchEditText;
    private Spinner categorySpinner;
    private FloatingActionButton addFound;
    private TextView filterButton;

    private String selectedCategory = "";

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_found, container, false);

        recyclerView = view.findViewById(R.id.foundRecyclerView);
        searchEditText = view.findViewById(R.id.searchFound);
        categorySpinner = view.findViewById(R.id.categorySpinnerFound);
        addFound = view.findViewById(R.id.add_found);
        filterButton = view.findViewById(R.id.filterButtonFound);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setupSpinner();
        setupRecycler("", "");
        setupSearch();
        setupFilterClick();

        addFound.setOnClickListener(v ->
                new FoundItemsFragment()
                        .show(getParentFragmentManager(), "FoundDialog")
        );

        return view;
    }

    // 🔹 Load items (SAME AS LOST)
    private void setupRecycler(String searchText, String category) {

        Query query = Utility.getCollectionReferrenceForFound();

        if (!category.isEmpty()) {
            query = query.whereEqualTo("category", category);
        }

        if (!searchText.isEmpty()) {
            query = query.orderBy("itemName")
                    .startAt(searchText)
                    .endAt(searchText + "\uf8ff");
        }

        FirestoreRecyclerOptions<FoundItems> options =
                new FirestoreRecyclerOptions.Builder<FoundItems>()
                        .setQuery(query, FoundItems.class)
                        .build();

        if (adapter != null) adapter.stopListening();

        adapter = new FoundItemsAdapter(options, getContext(), "", false);
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
