package com.shruti.lofo.ui.Found;

import static android.app.Activity.RESULT_OK;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.shruti.lofo.OnImageUploadCallback;
import com.shruti.lofo.R;
import com.shruti.lofo.Utility;
import com.shruti.lofo.ui.Lost.LostItems;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FoundItemsFragment extends DialogFragment {

    private ImageButton datePickerButton;
    private TextView dateEdit;
    private Spinner categorySpinner;
    private EditText description, location;
    private Button upload, submitButton;
    private Uri imageUri;

    private FirebaseAuth auth;

    private final int REQ_CODE = 1000;
    private String date = null;
    private int mYear, mMonth, mDay;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_found_items, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();

        description = view.findViewById(R.id.description);
        datePickerButton = view.findViewById(R.id.datePickerButton);
        dateEdit = view.findViewById(R.id.selectedDateEditText);
        location = view.findViewById(R.id.location);
        categorySpinner = view.findViewById(R.id.categorySpinner);
        upload = view.findViewById(R.id.uploadImageButton);
        submitButton = view.findViewById(R.id.submit_button);

        datePickerButton.setOnClickListener(v -> showDatePicker());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.categories_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        final String[] selectedCategory = new String[1];
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory[0] = categorySpinner.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategory[0] = null;
            }
        });

        upload.setOnClickListener(v -> {
            Intent iGallery = new Intent(Intent.ACTION_PICK);
            iGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(iGallery, REQ_CODE);
        });

        submitButton.setOnClickListener(v -> {

            EditText item = view.findViewById(R.id.item_name_edittext);
            String itemName = item.getText().toString().trim();
            String loc = location.getText().toString().trim();
            String desc = description.getText().toString().trim();

            if (itemName.isEmpty()) {
                Utility.showToast(getContext(), "Name cannot be empty");
                return;
            }
            if (selectedCategory[0] == null) {
                Utility.showToast(getContext(), "Please select a category");
                return;
            }
            if (date == null) {
                showDatePicker();
                return;
            }
            if (loc.isEmpty()) {
                Utility.showToast(getContext(), "Please provide location");
                return;
            }
            if (desc.isEmpty()) {
                Utility.showToast(getContext(), "Please add description");
                return;
            }

            FoundItems foundItem = new FoundItems();
            foundItem.setItemName(itemName);
            foundItem.setCategory(selectedCategory[0]);
            foundItem.setDateFound(date);
            foundItem.setLocation(loc);
            foundItem.setDescription(desc);

            // ✅ Add created timestamp here
            foundItem.setCreatedAt(Timestamp.now());

            FirebaseUser currentUser = auth.getCurrentUser();

            if (currentUser == null) {
                Utility.showToast(getContext(), "Please login again");
                dismiss();
                return;
            }

            String userEmail = currentUser.getEmail();
            String userID = currentUser.getUid();

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .whereEqualTo("email", userEmail)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (!snapshot.isEmpty()) {
                            DocumentSnapshot doc = snapshot.getDocuments().get(0);
                            foundItem.setfinderName(doc.getString("name"));
                            foundItem.setPhnum(doc.getString("phone"));

                            foundItem.setEmail(userEmail);
                            foundItem.setfinderId(userID);
                        }
                        saveItemAndDismiss(foundItem);
                    })
                    .addOnFailureListener(e -> saveItemAndDismiss(foundItem));
        });

    }

    private void saveItemAndDismiss(FoundItems item) {
        if (imageUri != null) {
            saveImageToFirebaseStorage(imageUri, new OnImageUploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    item.setImageURI(imageUrl);
                    saveToFirestoreAndDismiss(item);
                }

                @Override
                public void onFailure() {
                    saveToFirestoreAndDismiss(item);
                }
            });
        } else {
            saveToFirestoreAndDismiss(item);
        }
    }

    private void saveToFirestoreAndDismiss(FoundItems item) {
        DocumentReference docRef = Utility.getCollectionReferrenceForFound().document();
        docRef.set(item).addOnCompleteListener(task -> {
            new Handler(Looper.getMainLooper()).post(() -> {
                if (task.isSuccessful()) {
                    Utility.showToast(getContext(), "Item added successfully");
                    checkForMatchingLostItems(item);
                } else {
                    Utility.showToast(getContext(), "Failed to add item");
                }
                dismiss();
            });
        });
    }

    private void saveImageToFirebaseStorage(Uri imageUri, OnImageUploadCallback callback) {
        String imageName = "image_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        .format(new Date()) + ".jpg";

        StorageReference ref = FirebaseStorage.getInstance()
                .getReference()
                .child("foundImages/" + imageName);

        ref.putFile(imageUri)
                .addOnSuccessListener(task ->
                        ref.getDownloadUrl().addOnSuccessListener(uri ->
                                callback.onSuccess(uri.toString())))
                .addOnFailureListener(e -> callback.onFailure());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQ_CODE && data != null) {
            imageUri = data.getData();
            upload.setText("Image added");
        }
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(requireContext(),
                (view, year, month, day) -> {
                    mYear = year;
                    mMonth = month;
                    mDay = day;
                    updateDateButton();
                }, mYear, mMonth, mDay).show();
    }

    private void updateDateButton() {
        date = mDay + "/" + (mMonth + 1) + "/" + mYear;
        dateEdit.setText(date);
    }

    private void checkForMatchingLostItems(FoundItems foundItem) {
        FirebaseFirestore.getInstance()
                .collection("lostItems")
                .whereEqualTo("category", foundItem.getCategory())
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) {
                        LostItems lost = doc.toObject(LostItems.class);
                        if (lost.getItemName().equalsIgnoreCase(foundItem.getItemName())
                                && lost.getLocation().toLowerCase()
                                .contains(foundItem.getLocation().toLowerCase())) {
                            Utility.showToast(getContext(), "Match found");
                            saveMatch(foundItem, lost);
                            break;
                        }
                    }
                });
    }

    private void saveMatch(FoundItems found, LostItems lost) {
        FirebaseFirestore.getInstance()
                .collection("matches")
                .add(new com.shruti.lofo.ui.Matches.Matches(lost, found));
    }
}
