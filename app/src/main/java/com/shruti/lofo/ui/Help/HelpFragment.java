package com.shruti.lofo.ui.Help;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.shruti.lofo.R;

public class HelpFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_help, container, false);

        TextView que1 = root.findViewById(R.id.que1);
        TextView ans1 = root.findViewById(R.id.ans1);
        TextView que2 = root.findViewById(R.id.que2);
        TextView ans2 = root.findViewById(R.id.ans2);
        TextView que3 = root.findViewById(R.id.que3);
        TextView ans3 = root.findViewById(R.id.ans3);

        TextView email = root.findViewById(R.id.helpEmail);
        TextView phone = root.findViewById(R.id.helpPhone);

        que1.setOnClickListener(v -> toggle(ans1));
        que2.setOnClickListener(v -> toggle(ans2));
        que3.setOnClickListener(v -> toggle(ans3));

        email.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:campusguardian@adcet.in"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Help Request");
            startActivity(intent);
        });

        phone.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+919876543210"));
            startActivity(intent);
        });

        return root;
    }

    private void toggle(TextView tv) {
        tv.setVisibility(tv.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }
}
