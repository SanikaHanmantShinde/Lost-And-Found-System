package com.shruti.lofo.ui.Lost;

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

public class LostDetails extends AppCompatActivity {

    ImageView img;
    TextView title, address, dateLost, timeLost, mail, description, ownerName, category;
    Button backBtn, callBtn, smsBtn;

    String phone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lost_details);

        img = findViewById(R.id.img);
        title = findViewById(R.id.title);
        address = findViewById(R.id.address);
        dateLost = findViewById(R.id.dateLost);
        timeLost = findViewById(R.id.timeLost);   // 👈 ensure this ID exists in XML
        mail = findViewById(R.id.mail);
        description = findViewById(R.id.description);
        ownerName = findViewById(R.id.ownerName);
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
                .collection("lostItems")
                .document(itemId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Toast.makeText(this, "Lost item not found", Toast.LENGTH_LONG).show();
                        return;
                    }

                    title.setText(doc.getString("itemName"));
                    dateLost.setText(doc.getString("dateLost"));
                    mail.setText(doc.getString("email"));
                    description.setText(doc.getString("description"));
                    ownerName.setText(doc.getString("ownerName"));
                    category.setText(doc.getString("category"));

                    // ---------- TIME LOST (SAFE FALLBACK) ----------
                    String time =
                            doc.getString("timeLost") != null ? doc.getString("timeLost") :
                                    doc.getString("lostTime") != null ? doc.getString("lostTime") :
                                            doc.getString("time") != null ? doc.getString("time") :
                                                    "Not available";

                    timeLost.setText(time);


                    // ---------- PHONE (NUMBER OR STRING) ----------
                    Object phoneObj = doc.get("phnum");
                    if (phoneObj != null) {
                        phone = String.valueOf(phoneObj).trim();
                    } else {
                        phone = "";
                    }

                    Log.d("LOST_DETAILS", "Phone = " + phone);


                    // ---------- ADDRESS FALLBACK ----------
                    String addr =
                            doc.getString("address") != null ? doc.getString("address") :
                                    doc.getString("location") != null ? doc.getString("location") :
                                            doc.getString("place") != null ? doc.getString("place") :
                                                    "Not available";

                    address.setText(addr);


                    // ---------- LOAD IMAGE ----------
                    String imgUrl = doc.getString("imageURI");
                    if (imgUrl != null && !imgUrl.isEmpty()) {
                        Glide.with(this).load(imgUrl).into(img);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading details", Toast.LENGTH_SHORT).show()
                );


        // ---------- BUTTONS ----------
        backBtn.setOnClickListener(v -> finish());


        callBtn.setOnClickListener(v -> {

            if (phone != null && !phone.trim().isEmpty()) {

                String cleanPhone = phone.replaceAll("\\s+", "");

                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + cleanPhone));

                try {
                    startActivity(intent);
                } catch (Exception ex) {
                    Toast.makeText(this, "Unable to open dialer", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
            }

        });


        smsBtn.setOnClickListener(v -> {

            if (phone != null && !phone.trim().isEmpty()) {

                String cleanPhone = phone.replaceAll("\\s+", "");

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("sms:" + cleanPhone));
                intent.putExtra("sms_body", "Hello! I saw your lost item post.");

                try {
                    startActivity(intent);
                } catch (Exception ex) {
                    Toast.makeText(this, "Unable to open Messages", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
            }

        });
    }
}