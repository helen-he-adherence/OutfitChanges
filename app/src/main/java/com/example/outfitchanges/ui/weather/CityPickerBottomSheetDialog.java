package com.example.outfitchanges.ui.weather;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.outfitchanges.R;
import android.widget.Toast;
import com.example.outfitchanges.ui.weather.database.CityEntity;
import com.example.outfitchanges.ui.weather.repository.WeatherRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import java.util.ArrayList;
import java.util.List;

public class CityPickerBottomSheetDialog extends BottomSheetDialogFragment {
    private Spinner provinceSpinner;
    private Spinner citySpinner;
    private Spinner districtSpinner;
    private Button confirmButton;
    private TextView titleText;

    private WeatherRepository repository;
    private List<String> provinces = new ArrayList<>();
    private List<String> cities = new ArrayList<>();
    private List<String> districts = new ArrayList<>();

    private String selectedProvince = "";
    private String selectedCity = "";
    private String selectedDistrict = "";

    private OnCitySelectedListener listener;

    public interface OnCitySelectedListener {
        void onCitySelected(String province, String city, String district);
    }

    public void setOnCitySelectedListener(OnCitySelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onStart() {
        super.onStart();
        // 设置BottomSheet的行为，确保可以展开显示所有内容
        if (getDialog() != null) {
            BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                // 设置最大高度为屏幕高度的85%，确保按钮可见
                if (getContext() != null) {
                    int screenHeight = getResources().getDisplayMetrics().heightPixels;
                    behavior.setMaxHeight((int) (screenHeight * 0.85));
                }
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_city_picker, container, false);

        repository = new WeatherRepository(requireContext());

        titleText = view.findViewById(R.id.title_text);
        provinceSpinner = view.findViewById(R.id.province_spinner);
        citySpinner = view.findViewById(R.id.city_spinner);
        districtSpinner = view.findViewById(R.id.district_spinner);
        confirmButton = view.findViewById(R.id.confirm_button);

        titleText.setText("切换城市");
        
        // 强制设置按钮可见和可用
        if (confirmButton != null) {
            // 立即设置按钮为完全可见
            confirmButton.setVisibility(View.VISIBLE);
            confirmButton.setAlpha(1.0f);
            confirmButton.setEnabled(false);
            confirmButton.setText("确认选择");
            
            // 初始状态使用灰色背景，保持可见
            confirmButton.setBackgroundColor(getResources().getColor(R.color.gray_button_bg, null));
            confirmButton.setTextColor(getResources().getColor(R.color.text_secondary, null));
            
            // 确保按钮在视图层次的最前面
            if (confirmButton.getParent() instanceof ViewGroup) {
                ((ViewGroup) confirmButton.getParent()).bringChildToFront(confirmButton);
            }
            
            // 立即更新状态
            updateConfirmButtonState();
            
            // 延迟再次检查，确保按钮可见
            view.postDelayed(() -> {
                if (confirmButton != null) {
                    confirmButton.setVisibility(View.VISIBLE);
                    confirmButton.setAlpha(1.0f);
                    updateConfirmButtonState();
                    android.util.Log.d("CityPicker", "Button final check - visibility: " + confirmButton.getVisibility() 
                        + ", enabled: " + confirmButton.isEnabled()
                        + ", alpha: " + confirmButton.getAlpha()
                        + ", text: " + confirmButton.getText());
                }
            }, 300);
        }

        // 先检查数据是否已加载
        repository.getCityCount(count -> {
            if (count == 0) {
                // 数据未加载，提示用户
                if (getContext() != null) {
                    Toast.makeText(getContext(), "正在加载城市数据，请稍候...", Toast.LENGTH_LONG).show();
                }
                // 延迟一下再加载，给用户看到提示
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    loadProvinces();
                }, 500);
            } else {
                // 数据已加载，直接加载省份列表
                loadProvinces();
            }
        });

        provinceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedProvince = provinces.get(position - 1);
                    loadCities(selectedProvince);
                } else {
                    selectedProvince = "";
                    cities.clear();
                    districts.clear();
                    updateCitySpinner();
                    updateDistrictSpinner();
                }
                updateConfirmButtonState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && !selectedProvince.isEmpty()) {
                    selectedCity = cities.get(position - 1);
                    loadDistricts(selectedProvince, selectedCity);
                } else {
                    selectedCity = "";
                    districts.clear();
                    updateDistrictSpinner();
                }
                updateConfirmButtonState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedDistrict = districts.get(position - 1);
                } else {
                    selectedDistrict = "";
                }
                updateConfirmButtonState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 初始状态禁用确认按钮
        confirmButton.setEnabled(false);
        
        confirmButton.setOnClickListener(v -> {
            if (!selectedProvince.isEmpty() && !selectedCity.isEmpty() && !selectedDistrict.isEmpty()) {
                // 显示加载提示
                confirmButton.setEnabled(false);
                confirmButton.setText("正在切换...");
                
                if (listener != null) {
                    listener.onCitySelected(selectedProvince, selectedCity, selectedDistrict);
                }
                // 延迟关闭对话框，让用户看到"正在切换"的提示
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    dismiss();
                }, 500);
            } else {
                // 如果选择不完整，提示用户
                if (getContext() != null) {
                    String message = "请";
                    if (selectedProvince.isEmpty()) {
                        message += "选择省份";
                    } else if (selectedCity.isEmpty()) {
                        message += "选择城市";
                    } else if (selectedDistrict.isEmpty()) {
                        message += "选择区县";
                    }
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void loadProvinces() {
        repository.getAllProvinces(provinces -> {
            if (provinces != null && !provinces.isEmpty()) {
                this.provinces = provinces;
                updateProvinceSpinner();
            } else {
                // 如果数据为空，检查是否需要加载数据
                repository.getCityCount(count -> {
                    if (getContext() != null) {
                        if (count == 0) {
                            // 数据未加载，提示用户并触发加载
                            Toast.makeText(getContext(), "城市数据正在加载中，请稍候再试", Toast.LENGTH_LONG).show();
                            // 可以在这里触发数据加载，但最好在Fragment中处理
                        } else {
                            // 数据已加载但查询结果为空，可能是数据格式问题
                            Toast.makeText(getContext(), "未找到城市数据，共 " + count + " 条记录", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void loadCities(String province) {
        repository.getCitiesByProvince(province, cities -> {
            this.cities = cities;
            updateCitySpinner();
        });
    }

    private void loadDistricts(String province, String city) {
        repository.getDistrictsByProvinceAndCity(province, city, districts -> {
            this.districts = districts;
            updateDistrictSpinner();
        });
    }

    private void updateProvinceSpinner() {
        List<String> items = new ArrayList<>();
        items.add("请选择省份");
        items.addAll(provinces);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        provinceSpinner.setAdapter(adapter);
    }

    private void updateCitySpinner() {
        List<String> items = new ArrayList<>();
        items.add("请选择城市");
        items.addAll(cities);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(adapter);
    }

    private void updateDistrictSpinner() {
        List<String> items = new ArrayList<>();
        items.add("请选择区县");
        items.addAll(districts);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        districtSpinner.setAdapter(adapter);
    }

    private void updateConfirmButtonState() {
        // 只有当省份、城市、区县都选择后，才启用确认按钮
        boolean isComplete = !selectedProvince.isEmpty() 
                && !selectedCity.isEmpty() 
                && !selectedDistrict.isEmpty();
        
        if (confirmButton != null) {
            // 确保按钮始终可见
            confirmButton.setVisibility(View.VISIBLE);
            confirmButton.setAlpha(1.0f); // 始终完全不透明
            confirmButton.setEnabled(isComplete);
            confirmButton.setText("确认选择");
            
            // 根据状态设置不同的背景色
            if (isComplete) {
                // 选择完整时，使用粉红色背景
                confirmButton.setBackgroundResource(R.drawable.rounded_button_bg);
                confirmButton.setTextColor(getResources().getColor(android.R.color.white, null));
            } else {
                // 选择不完整时，使用灰色背景但保持可见
                confirmButton.setBackgroundColor(getResources().getColor(R.color.gray_button_bg, null));
                confirmButton.setTextColor(getResources().getColor(R.color.text_secondary, null));
            }
            
            // 强制刷新按钮显示
            confirmButton.invalidate();
            confirmButton.requestLayout();
            
            // 打印调试信息
            android.util.Log.d("CityPicker", "Button state updated - visible: " + (confirmButton.getVisibility() == View.VISIBLE)
                + ", enabled: " + confirmButton.isEnabled()
                + ", alpha: " + confirmButton.getAlpha()
                + ", complete: " + isComplete);
        }
    }
}
