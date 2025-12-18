package com.example.outfitchanges.ui.profile;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import com.example.outfitchanges.R;
import com.example.outfitchanges.auth.model.PreferencesRequest;
import com.example.outfitchanges.utils.SharedPrefManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;

public class PreferencesDialog extends Dialog {
    private final SharedPrefManager prefManager;
    private ProfileViewModel profileViewModel;
    private LifecycleOwner lifecycleOwner;
    private ChipGroup chipGroupGender;
    private ChipGroup chipGroupStyle;
    private ChipGroup chipGroupColors;
    private EditText editAge;
    private EditText editOccupation;
    private OnPreferencesSavedListener listener;
    private boolean isSaving = false;

    public interface OnPreferencesSavedListener {
        void onSaved();
    }

    public PreferencesDialog(@NonNull Context context, SharedPrefManager prefManager) {
        super(context);
        this.prefManager = prefManager;
    }
    
    public void setProfileViewModel(ProfileViewModel viewModel, LifecycleOwner lifecycleOwner) {
        this.profileViewModel = viewModel;
        this.lifecycleOwner = lifecycleOwner;
    }

    public void setOnPreferencesSavedListener(OnPreferencesSavedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_preferences);
        setCancelable(true);
        
        // 设置弹窗宽度接近屏幕宽度
        if (getWindow() != null) {
            android.view.WindowManager.LayoutParams params = getWindow().getAttributes();
            params.width = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.9);
            getWindow().setAttributes(params);
        }

        initViews();
        setupChips();
        loadSavedPreferences();
        setupClickListeners();
    }

    private void initViews() {
        chipGroupGender = findViewById(R.id.chip_group_gender);
        chipGroupStyle = findViewById(R.id.chip_group_style);
        chipGroupColors = findViewById(R.id.chip_group_colors);
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
        String[] styles = {"休闲", "通勤", "街头", "优雅", "甜美", "复古", "运动", "民族风", "简约", "奢华", "森系", "朋克", "商务", "度假风"};
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
        
        // 颜色选项
        String[] colors = {"黑色", "白色", "灰色", "红色", "蓝色", "绿色", "黄色", "粉色", "紫色", "橙色", "棕色", "青色"};
        for (String color : colors) {
            Chip chip = new Chip(getContext());
            chip.setText(color);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_bg_color);
            chip.setChipStrokeColorResource(R.color.gray_button_bg);
            chip.setChipStrokeWidth(1);
            chip.setTextColor(getContext().getColorStateList(R.color.chip_text_color));
            chipGroupColors.addView(chip);
        }
    }

    private void loadSavedPreferences() {
        // 如果设置了 ViewModel 且已登录，尝试从服务器加载
        if (profileViewModel != null && prefManager.isLoggedIn()) {
            // 先检查是否已有缓存的profile数据
            com.example.outfitchanges.auth.model.ProfileResponse.Profile cachedProfile = 
                profileViewModel.getProfile().getValue();
            if (cachedProfile != null && cachedProfile.getPreferences() != null) {
                loadPreferencesFromProfile(cachedProfile);
            } else {
                // 如果没有缓存，观察一次并加载
                profileViewModel.getProfile().observe(lifecycleOwner, profile -> {
                    if (profile != null && profile.getPreferences() != null) {
                        loadPreferencesFromProfile(profile);
                    } else {
                        // 如果服务器没有数据，从本地加载
                        loadPreferencesFromLocal();
                    }
                });
                // 触发加载个人资料
                profileViewModel.loadProfile();
            }
        } else {
            // 未登录或没有 ViewModel，从本地加载
            loadPreferencesFromLocal();
        }
    }
    
    private void loadPreferencesFromProfile(com.example.outfitchanges.auth.model.ProfileResponse.Profile profile) {
        // 先清空所有选择
        clearAllSelections();
        
        // 加载性别
        if (profile.getGender() != null && !profile.getGender().isEmpty()) {
            for (int i = 0; i < chipGroupGender.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupGender.getChildAt(i);
                if (chip.getText().toString().equals(profile.getGender())) {
                    chip.setChecked(true);
                    break;
                }
            }
        }
        
        // 加载年龄
        if (profile.getAge() != null) {
            editAge.setText(String.valueOf(profile.getAge()));
        } else {
            editAge.setText("");
        }
        
        // 加载职业
        if (profile.getOccupation() != null && !profile.getOccupation().isEmpty()) {
            editOccupation.setText(profile.getOccupation());
        } else {
            editOccupation.setText("");
        }
        
        // 加载偏好风格
        if (profile.getPreferences() != null && profile.getPreferences().getPreferredStyles() != null) {
            for (String style : profile.getPreferences().getPreferredStyles()) {
                for (int i = 0; i < chipGroupStyle.getChildCount(); i++) {
                    Chip chip = (Chip) chipGroupStyle.getChildAt(i);
                    if (chip.getText().toString().equals(style)) {
                        chip.setChecked(true);
                        break;
                    }
                }
            }
        }
        
        // 加载喜欢的颜色
        if (profile.getPreferences() != null && profile.getPreferences().getPreferredColors() != null) {
            for (String color : profile.getPreferences().getPreferredColors()) {
                for (int i = 0; i < chipGroupColors.getChildCount(); i++) {
                    Chip chip = (Chip) chipGroupColors.getChildAt(i);
                    if (chip.getText().toString().equals(color)) {
                        chip.setChecked(true);
                        break;
                    }
                }
            }
        }
    }
    
    private void clearAllSelections() {
        // 清空性别选择
        for (int i = 0; i < chipGroupGender.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupGender.getChildAt(i);
            chip.setChecked(false);
        }
        
        // 清空风格选择
        for (int i = 0; i < chipGroupStyle.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupStyle.getChildAt(i);
            chip.setChecked(false);
        }
        
        // 清空颜色选择
        for (int i = 0; i < chipGroupColors.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupColors.getChildAt(i);
            chip.setChecked(false);
        }
    }
    
    private void loadPreferencesFromLocal() {
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
            if (isSaving) {
                return; // 防止重复点击
            }
            savePreferences();
        });
        
        // 如果设置了 ViewModel，观察更新结果
        if (profileViewModel != null && lifecycleOwner != null) {
            profileViewModel.getUpdateSuccessMessage().observe(lifecycleOwner, message -> {
                if (message != null && !message.isEmpty() && isSaving) {
                    isSaving = false;
                    // 保存成功后，ProfileViewModel.updatePreferences() 已经会自动调用 loadProfile()
                    // 所以这里不需要再次调用 loadProfile()，只需要等待 profile 更新即可
                    // ProfileFragment 的观察者会自动检测到 profile 更新并应用到穿搭广场
                    if (listener != null) {
                        listener.onSaved();
                    }
                    dismiss();
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
            
            profileViewModel.getErrorMessage().observe(lifecycleOwner, errorMessage -> {
                if (errorMessage != null && !errorMessage.isEmpty() && isSaving) {
                    isSaving = false;
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void savePreferences() {
        // 收集性别
        String gender = null;
        for (int i = 0; i < chipGroupGender.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupGender.getChildAt(i);
            if (chip.isChecked()) {
                gender = chip.getText().toString();
                break;
            }
        }

        // 收集年龄
        String ageStr = editAge.getText().toString().trim();
        Integer age = null;
        if (!ageStr.isEmpty()) {
            try {
                age = Integer.parseInt(ageStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "年龄格式不正确", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // 收集职业
        String occupation = editOccupation.getText().toString().trim();
        if (occupation.isEmpty()) {
            occupation = null;
        }

        // 收集偏好风格
        List<String> preferredStyles = new ArrayList<>();
        for (int i = 0; i < chipGroupStyle.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupStyle.getChildAt(i);
            if (chip.isChecked()) {
                preferredStyles.add(chip.getText().toString());
            }
        }
        
        // 收集偏好颜色
        List<String> preferredColors = new ArrayList<>();
        for (int i = 0; i < chipGroupColors.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupColors.getChildAt(i);
            if (chip.isChecked()) {
                preferredColors.add(chip.getText().toString());
            }
        }

        // 如果设置了 ViewModel，使用 API 保存
        if (profileViewModel != null && prefManager.isLoggedIn()) {
            isSaving = true;
            PreferencesRequest request = new PreferencesRequest();
            request.setGender(gender);
            request.setAge(age);
            request.setOccupation(occupation);
            request.setPreferredStyles(preferredStyles.toArray(new String[0]));
            request.setPreferredColors(preferredColors.toArray(new String[0]));
            
            profileViewModel.updatePreferences(request);
        } else {
            // 否则只保存到本地
            if (gender != null) {
                prefManager.setGender(gender);
            }
            if (age != null) {
                prefManager.setAge(String.valueOf(age));
            }
            if (occupation != null) {
                prefManager.setOccupation(occupation);
            }
            
            StringBuilder styles = new StringBuilder();
            for (String style : preferredStyles) {
                if (styles.length() > 0) {
                    styles.append(",");
                }
                styles.append(style);
            }
            if (styles.length() > 0) {
                prefManager.setPreferredStyle(styles.toString());
            }
            
            if (listener != null) {
                listener.onSaved();
            }
            dismiss();
            Toast.makeText(getContext(), "设置已保存", Toast.LENGTH_SHORT).show();
        }
    }
}

