package com.shruti.lofo.ui.MyProfile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shruti.lofo.Login;
import com.shruti.lofo.R;

public class MyProfileFragment extends Fragment {

    TextView profileName, profileEmail, profilePhone, titleName;
    FirebaseFirestore database;
    FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_my_profile, container, false);

        profileName = root.findViewById(R.id.profileName);
        profileEmail = root.findViewById(R.id.profileEmail);
        profilePhone = root.findViewById(R.id.profilephone);
        titleName = root.findViewById(R.id.titlename);

        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        fetchUserDataSafely();

        return root;
    }

    private void fetchUserDataSafely() {

        FirebaseUser currentUser = auth.getCurrentUser();

        // ✅ ABSOLUTELY REQUIRED CHECK
        if (currentUser == null) {
            Toast.makeText(getContext(), "Session expired. Please login again.", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getActivity(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }

        String userEmail = currentUser.getEmail();

        database.collection("users")
                .whereEqualTo("email", userEmail)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        DocumentSnapshot doc = snapshot.getDocuments().get(0);

                        titleName.setText(doc.getString("name"));
                        profileName.setText(doc.getString("name"));
                        profileEmail.setText(doc.getString("email"));
                        profilePhone.setText(doc.getString("phone"));
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
                );
    }
}
