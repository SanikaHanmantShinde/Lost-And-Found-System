package com.shruti.lofo.ui.Lost;

import static android.app.Activity.RESULT_OK;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import com.google.firebase.Timestamp; // ✅ ADDED
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.shruti.lofo.OnImageUploadCallback;
import com.shruti.lofo.R;
import com.shruti.lofo.Utility;
import com.shruti.lofo.notifications.NotificationHelper;
import com.shruti.lofo.ui.Found.FoundItems;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LostItemsFragment extends DialogFragment {

    private ImageButton datePickerButton, timePickerButton;
    private TextView dateEdit, timeEdit;
    private Spinner categorySpinner;
    private EditText description, location;
    private Button upload, submitButton;
    private Uri imageUri;
    private int REQ_CODE = 1000;
    private String date = null, time = null;
    private int mYear, mMonth, mDay, mHour, mMinute;

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
        return inflater.inflate(R.layout.fragment_lost_items, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        description = view.findViewById(R.id.description);
        datePickerButton = view.findViewById(R.id.datePickerButton);
        timePickerButton = view.findViewById(R.id.timePickerButton);
        dateEdit = view.findViewById(R.id.selectedDateEditText);
        timeEdit = view.findViewById(R.id.selectedTimeEditText);
        location = view.findViewById(R.id.location);
        categorySpinner = view.findViewById(R.id.categorySpinner);
        upload = view.findViewById(R.id.uploadImageButton);
        submitButton = view.findViewById(R.id.submit_button);

        datePickerButton.setOnClickListener(v -> showDatePicker());
        timePickerButton.setOnClickListener(v -> showTimePicker());

        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(
                        requireContext(),
                        R.array.categories_array,
                        android.R.layout.simple_spinner_item
                );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        final String[] selectedCategory = new String[1];
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view,
                                       int position,
                                       long id) {
                selectedCategory[0] =
                        categorySpinner.getItemAtPosition(position).toString();
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
            String itemName = item.getText().toString();
            String loc = location.getText().toString();
            String desc = description.getText().toString();

            if (itemName.isEmpty()) {
                Utility.showToast(getContext(), "Name cannot be empty");
                return;
            }
            if (selectedCategory[0] == null) {
                Utility.showToast(getContext(), "Please select a category");
                return;
            }
            if (date == null) { showDatePicker(); return; }
            if (time == null) { showTimePicker(); return; }
            if (loc.isEmpty()) {
                Utility.showToast(getContext(), "Please provide location");
                return;
            }
            if (desc.isEmpty()) {
                Utility.showToast(getContext(), "Please add description");
                return;
            }

            LostItems lostItem = new LostItems();
            lostItem.setItemName(itemName);
            lostItem.setCategory(selectedCategory[0]);
            lostItem.setDateLost(date);
            lostItem.setTimeLost(time);
            lostItem.setLocation(loc);
            lostItem.setDescription(desc);

            // ✅ THIS IS THE KEY LINE (RECENT ITEMS FIX)
            lostItem.setCreatedAt(Timestamp.now());

            FirebaseUser currentUser =
                    FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null) {
                String userEmail = currentUser.getEmail();
                String userID = currentUser.getUid();

                FirebaseFirestore.getInstance()
                        .collection("users")
                        .whereEqualTo("email", userEmail)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document :
                                        task.getResult()) {

                                    lostItem.setOwnerName(
                                            document.getString("name"));
                                    lostItem.setPhnum(
                                            Long.valueOf(
                                                    document.getString("phone")));
                                    lostItem.setEmail(userEmail);
                                    lostItem.setUserId(userID);
                                }
                            }
                            saveItemAndDismiss(lostItem);
                        });
            } else {
                saveItemAndDismiss(lostItem);
            }
        });
    }

    private void saveItemAndDismiss(LostItems item) {
        if (imageUri != null) {
            saveImageToFirebaseStorage(imageUri,
                    new OnImageUploadCallback() {
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

    private void saveToFirestoreAndDismiss(LostItems item) {

        DocumentReference documentReference =
                Utility.getCollectionReferrenceForItems2().document();

        documentReference.set(item).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Utility.showToast(getContext(), "Item added successfully");
                checkForMatchingFoundItems(item);
            } else {
                Utility.showToast(getContext(), "Failed to add item");
            }
            dismiss();
        });
    }

    private void saveImageToFirebaseStorage(Uri imageUri,
                                            OnImageUploadCallback callback) {

        String imageName =
                "image_" + new SimpleDateFormat(
                        "yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date()) + ".jpg";

        StorageReference storageReference =
                FirebaseStorage.getInstance()
                        .getReference()
                        .child("images/" + imageName);

        storageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        storageReference.getDownloadUrl()
                                .addOnSuccessListener(uri ->
                                        callback.onSuccess(uri.toString())))
                .addOnFailureListener(e -> callback.onFailure());
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK &&
                requestCode == REQ_CODE &&
                data != null) {
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
                    dateEdit.setText(
                            mDay + "/" + (mMonth + 1) + "/" + mYear);
                    date = dateEdit.getText().toString();
                },
                mYear, mMonth, mDay).show();
    }

    private void showTimePicker() {
        Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        new TimePickerDialog(requireContext(),
                (view, hour, minute) -> {
                    String ampm = hour < 12 ? "AM" : "PM";
                    int h = hour % 12;
                    if (h == 0) h = 12;
                    time = h + ":" + minute + " " + ampm;
                    timeEdit.setText(time);
                },
                mHour, mMinute, false).show();
    }

    private void checkForMatchingFoundItems(LostItems lostItem) {
        FirebaseFirestore.getInstance()
                .collection("foundItems")
                .whereEqualTo("category", lostItem.getCategory())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot document : querySnapshot) {

                        FoundItems found =
                                document.toObject(FoundItems.class);

                        if (found.getItemName() == null ||
                                found.getLocation() == null) continue;

                        if (found.getItemName()
                                .equalsIgnoreCase(lostItem.getItemName()) &&
                                found.getLocation()
                                        .toLowerCase()
                                        .contains(lostItem.getLocation()
                                                .toLowerCase())) {

                            NotificationHelper.showNotification(
                                    getContext(),
                                    "Match Found!",
                                    "Your lost item matches a found item."
                            );
                            break;
                        }
                    }
                });
    }
}
