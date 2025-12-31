package com.shruti.lofo.ui.DashBoard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.shruti.lofo.R;
import com.shruti.lofo.databinding.FragmentDashboardBinding;
import com.shruti.lofo.ui.Found.FoundDetails;
import com.shruti.lofo.ui.Lost.LostDetails;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class DashBoardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private ArrayList<DashBoardViewModel> arr_recent_lofo;
    private RecyclerRecentLoFoAdapter adapter;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 🔹 Image Slider
        ImageSlider imageSlider = root.findViewById(R.id.imageSlider);
        ArrayList<SlideModel> slideModels = new ArrayList<>();
        slideModels.add(new SlideModel(R.drawable.dashboard_img1, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.dashboard_img2, ScaleTypes.FIT));
        imageSlider.setImageList(slideModels, ScaleTypes.FIT);

        // 🔹 RecyclerView setup
        RecyclerView recentList = root.findViewById(R.id.recent_lost_found_list);
        TextView noRecentItems = root.findViewById(R.id.noRecentItems);

        arr_recent_lofo = new ArrayList<>();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        recentList.setLayoutManager(gridLayoutManager);

        adapter = new RecyclerRecentLoFoAdapter(requireContext(), arr_recent_lofo);
        recentList.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // 🔹 Load recent lost + found items
        loadRecentItems(recentList, noRecentItems);

        // 🔹 Item click listener
        adapter.setOnItemClickListener(item -> {

            Intent intent;

            if ("lost".equalsIgnoreCase(item.getTag())) {
                intent = new Intent(requireContext(), LostDetails.class);
            } else {
                intent = new Intent(requireContext(), FoundDetails.class);
            }

            intent.putExtra("itemId", item.getItemId());
            intent.putExtra("tag", item.getTag());

            startActivity(intent);
        });


        // 🔹 Show user name
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        TextView userName = root.findViewById(R.id.userName);
        if (user != null) {
            String email = user.getEmail();
            CollectionReference usersRef = db.collection("users");
            usersRef.whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            String name = doc.getString("name");
                            if (name != null) userName.setText(name);
                        }
                    });
        }

        return root;
    }

    // 🔹 Load recent lost + found items
    private void loadRecentItems(RecyclerView recyclerView, TextView noRecentItems) {

        Query lostQuery = db.collection("lostItems");
        Query foundQuery = db.collection("foundItems");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        class TempItem {
            DashBoardViewModel item;
            Date date;
            TempItem(DashBoardViewModel i, Date d) {
                item = i;
                date = d;
            }
        }

        ArrayList<TempItem> tempList = new ArrayList<>();

        // 🔹 Load lost items
        lostQuery.get().addOnSuccessListener(lostSnap -> {
            for (DocumentSnapshot doc : lostSnap.getDocuments()) {
                DashBoardViewModel item = doc.toObject(DashBoardViewModel.class);
                if (item != null && item.getDateLost() != null) {
                    item.setItemId(doc.getId()); // Important: store Firestore ID
                    try {
                        tempList.add(new TempItem(item, sdf.parse(item.getDateLost())));
                    } catch (Exception ignored) {}
                }
            }

            // 🔹 Load found items
            foundQuery.get().addOnSuccessListener(foundSnap -> {
                for (DocumentSnapshot doc : foundSnap.getDocuments()) {
                    DashBoardViewModel item = doc.toObject(DashBoardViewModel.class);
                    if (item != null && item.getDateFound() != null) {
                        item.setItemId(doc.getId()); // Important: store Firestore ID
                        try {
                            tempList.add(new TempItem(item, sdf.parse(item.getDateFound())));
                        } catch (Exception ignored) {}
                    }
                }

                // 🔹 Sort by latest date
                Collections.sort(tempList, (a, b) -> b.date.compareTo(a.date));

                arr_recent_lofo.clear();
                for (int i = 0; i < Math.min(10, tempList.size()); i++) {
                    arr_recent_lofo.add(tempList.get(i).item);
                }

                if (arr_recent_lofo.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    noRecentItems.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    noRecentItems.setVisibility(View.GONE);
                }

                adapter.notifyDataSetChanged();
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
