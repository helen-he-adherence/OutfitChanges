package com.example.outfitchanges.ui.publish;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.example.outfitchanges.R;

public class PublishFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_publish, container, false);

        Button publishButton = view.findViewById(R.id.publish_button);
        publishButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "发布功能开发中...", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}