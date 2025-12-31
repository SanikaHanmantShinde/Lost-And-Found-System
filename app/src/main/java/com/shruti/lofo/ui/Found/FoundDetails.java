package com.shruti.lofo.ui.Found;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shruti.lofo.R;

public class FoundDetails extends AppCompatActivity {

    ImageView img;
    TextView title, address, dateFound, mail, description, finderName, category;
    Button backBtn, callBtn, smsBtn;

    String phone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.found_details);

        img = findViewById(R.id.img);
        title = findViewById(R.id.title);
        address = findViewById(R.id.address);
        dateFound = findViewById(R.id.dateFound);
        mail = findViewById(R.id.mail);
        description = findViewById(R.id.description);
        finderName = findViewById(R.id.finderName);
        category = findViewById(R.id.category);

        backBtn = findViewById(R.id.backBtn);
        callBtn = findViewById(R.id.call);
        smsBtn = findViewById(R.id.sms);

        String itemId = getIntent().getStringExtra("itemId");
        if (itemId == null) {
            Toast.makeText(this, "Item ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("foundItems")
                .document(itemId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Toast.makeText(this, "Found item not found", Toast.LENGTH_LONG).show();
                        return;
                    }

                    title.setText(doc.getString("itemName"));
                    dateFound.setText(doc.getString("dateFound"));
                    mail.setText(doc.getString("email"));
                    description.setText(doc.getString("description"));
                    finderName.setText(doc.getString("finderName"));
                    category.setText(doc.getString("category"));
                    phone = doc.getString("phnum");

                    // ✅ ADDRESS FIX (IMPORTANT)
                    String addr =
                            doc.getString("address") != null ? doc.getString("address") :
                                    doc.getString("location") != null ? doc.getString("location") :
                                            doc.getString("place") != null ? doc.getString("place") :
                                                    "Not available";

                    address.setText(addr);

                    // Debug log (VERY IMPORTANT)
                    Log.d("FOUND_DETAILS", "Address value = " + addr);

                    String imgUrl = doc.getString("imageURI");
                    if (imgUrl != null && !imgUrl.isEmpty()) {
                        Glide.with(this).load(imgUrl).into(img);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading details", Toast.LENGTH_SHORT).show()
                );

        backBtn.setOnClickListener(v -> finish());

        callBtn.setOnClickListener(v -> {
            if (phone != null && !phone.isEmpty()) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
            }
        });

        smsBtn.setOnClickListener(v -> {
            if (phone != null && !phone.isEmpty()) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phone)));
            }
        });
    }
}
