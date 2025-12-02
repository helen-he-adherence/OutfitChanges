package com.example.outfitchanges.ui.weather;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.outfitchanges.R;

public class WeatherFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);

        TextView weatherText = view.findViewById(R.id.weather_text);
        weatherText.setText("今日天气：晴\n温度：15°C ~ 25°C\n适合穿搭：轻薄外套");

        return view;
    }
}