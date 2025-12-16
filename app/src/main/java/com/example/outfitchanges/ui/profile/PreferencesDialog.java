package com.example.outfitchanges.ui.profile;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.example.outfitchanges.R;
import com.example.outfitchanges.utils.SharedPrefManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class PreferencesDialog extends Dialog {
    private final SharedPrefManager prefManager;
    private ChipGroup chipGroupGender;
    private ChipGroup chipGroupStyle;
    private EditText editAge;
    private EditText editOccupation;
    private OnPreferencesSavedListener listener;

    public interface OnPreferencesSavedListener {
        void onSaved();
    }

    public PreferencesDialog(@NonNull Context context, SharedPrefManager prefManager) {
        super(context);
        this.prefManager = prefManager;
    }

    public void setOnPreferencesSavedListener(OnPreferencesSavedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_preferences);
        setCancelable(true);

        initViews();
        setupChips();
        loadSavedPreferences();
        setupClickListeners();
    }

    private void initViews() {
        chipGroupGender = findViewById(R.id.chip_group_gender);
        chipGroupStyle = findViewById(R.id.chip_group_style);
        editAge = findViewById(R.id.edit_age);
        editOccupation = findViewById(R.id.edit_occupation);
    }

    private void setupChips() {
        // 性别选项
        String[] genders = {"男", "女", "其他"};
        for (String gender : genders) {
            Chip chip = new Chip(getContext());
            chip.setText(gender);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_bg_color);
            chip.setChipStrokeColorResource(R.color.gray_button_bg);
            chip.setChipStrokeWidth(1);
            chip.setTextColor(getContext().getColorStateList(R.color.chip_text_color));
            chipGroupGender.addView(chip);
        }

        // 风格选项
        String[] styles = {"简约", "甜美", "优雅", "街头", "复古", "通勤", "休闲"};
        for (String style : styles) {
            Chip chip = new Chip(getContext());
            chip.setText(style);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_bg_color);
            chip.setChipStrokeColorResource(R.color.gray_button_bg);
            chip.setChipStrokeWidth(1);
            chip.setTextColor(getContext().getColorStateList(R.color.chip_text_color));
            chipGroupStyle.addView(chip);
        }
    }

    private void loadSavedPreferences() {
        String savedGender = prefManager.getGender();
        String savedAge = prefManager.getAge();
        String savedOccupation = prefManager.getOccupation();
        String savedStyle = prefManager.getPreferredStyle();

        if (!savedGender.isEmpty()) {
            for (int i = 0; i < chipGroupGender.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupGender.getChildAt(i);
                if (chip.getText().toString().equals(savedGender)) {
                    chip.setChecked(true);
                    break;
                }
            }
        }

        if (!savedAge.isEmpty()) {
            editAge.setText(savedAge);
        }

        if (!savedOccupation.isEmpty()) {
            editOccupation.setText(savedOccupation);
        }

        if (!savedStyle.isEmpty()) {
            String[] styles = savedStyle.split(",");
            for (String style : styles) {
                for (int i = 0; i < chipGroupStyle.getChildCount(); i++) {
                    Chip chip = (Chip) chipGroupStyle.getChildAt(i);
                    if (chip.getText().toString().equals(style.trim())) {
                        chip.setChecked(true);
                        break;
                    }
                }
            }
        }
    }

    private void setupClickListeners() {
        Button btnCancel = findViewById(R.id.btn_cancel);
        Button btnSave = findViewById(R.id.btn_save_preferences);

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            savePreferences();
            if (listener != null) {
                listener.onSaved();
            }
            dismiss();
            Toast.makeText(getContext(), "设置已保存", Toast.LENGTH_SHORT).show();
        });
    }

    private void savePreferences() {
        // 保存性别
        for (int i = 0; i < chipGroupGender.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupGender.getChildAt(i);
            if (chip.isChecked()) {
                prefManager.setGender(chip.getText().toString());
                break;
            }
        }

        // 保存年龄
        String age = editAge.getText().toString().trim();
        if (!age.isEmpty()) {
            prefManager.setAge(age);
        }

        // 保存职业
        String occupation = editOccupation.getText().toString().trim();
        if (!occupation.isEmpty()) {
            prefManager.setOccupation(occupation);
        }

        // 保存偏好风格
        StringBuilder styles = new StringBuilder();
        for (int i = 0; i < chipGroupStyle.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupStyle.getChildAt(i);
            if (chip.isChecked()) {
                if (styles.length() > 0) {
                    styles.append(",");
                }
                styles.append(chip.getText().toString());
            }
        }
        if (styles.length() > 0) {
            prefManager.setPreferredStyle(styles.toString());
        }
    }
}

