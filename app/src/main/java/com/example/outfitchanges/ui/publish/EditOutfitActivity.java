package com.example.outfitchanges.ui.publish;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.outfitchanges.R;
import com.example.outfitchanges.ui.home.model.CreateOutfitResponse;
import com.example.outfitchanges.ui.home.model.OutfitClothingItem;
import com.example.outfitchanges.ui.home.model.OutfitTags;
import com.example.outfitchanges.ui.home.model.UpdateOutfitResponse;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditOutfitActivity extends AppCompatActivity {
    
    // Views
    private ImageView imagePreview;
    private MaterialCardView cardOverallInfo;
    private MaterialCardView cardItemsList;
    private ChipGroup chipGroupOverallStyle;
    private ChipGroup chipGroupOccasion;
    private ChipGroup chipGroupSeason;
    private ChipGroup chipGroupWeather;
    private ChipGroup chipGroupSex;
    private LinearLayout layoutItemsContainer;
    private LinearLayout btnAddItem;
    private ProgressBar progressUpload;
    
    // Data
    private PublishViewModel viewModel;
    private CreateOutfitResponse currentResponse;
    private OutfitTags rawTags;
    private OutfitTags modifiedTags;
    private File imageFile;
    private String imageUrl;
    
    // Item management
    private List<ItemViewHolder> itemViewHolders = new ArrayList<>();
    private Map<View, String> itemDetailsMap = new HashMap<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_outfit);
        
        // 隐藏默认ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        // 获取传递的数据
        String responseJson = getIntent().getStringExtra("response_json");
        imageUrl = getIntent().getStringExtra("image_url");
        String imageFilePath = getIntent().getStringExtra("image_file_path");
        
        if (imageFilePath != null) {
            imageFile = new File(imageFilePath);
        }
        
        if (responseJson != null) {
            Gson gson = new Gson();
            currentResponse = gson.fromJson(responseJson, CreateOutfitResponse.class);
            if (currentResponse != null) {
                rawTags = currentResponse.getRawTags();
                if (currentResponse.getRawTags() != null) {
                    modifiedTags = currentResponse.getRawTags();
                } else if (currentResponse.getTags() != null) {
                    modifiedTags = currentResponse.getTags();
                } else {
                    modifiedTags = new OutfitTags();
                }
            }
        }
        
        viewModel = new ViewModelProvider(this).get(PublishViewModel.class);
        
        initViews();
        setupClickListeners();
        observeViewModel();
        loadData();
    }
    
    private void showCancelDialog() {
        new AlertDialog.Builder(this)
                .setTitle("取消发布")
                .setMessage("确定要取消发布当前穿搭吗？未保存的修改将丢失。")
                .setPositiveButton("确定", (dialog, which) -> {
                    // 返回结果，通知PublishFragment重置状态
                    setResult(RESULT_CANCELED);
                    finish();
                })
                .setNegativeButton("取消", null)
                .show();
    }
    
    private void initViews() {
        imagePreview = findViewById(R.id.image_preview);
        cardOverallInfo = findViewById(R.id.card_overall_info);
        cardItemsList = findViewById(R.id.card_items_list);
        chipGroupOverallStyle = findViewById(R.id.chip_group_overall_style);
        chipGroupOccasion = findViewById(R.id.chip_group_occasion);
        chipGroupSeason = findViewById(R.id.chip_group_season);
        chipGroupWeather = findViewById(R.id.chip_group_weather);
        chipGroupSex = findViewById(R.id.chip_group_sex);
        layoutItemsContainer = findViewById(R.id.layout_items_container);
        btnAddItem = findViewById(R.id.btn_add_item);
        progressUpload = findViewById(R.id.progress_upload);
        
        // 加载图片
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(imagePreview);
        } else if (imageFile != null && imageFile.exists()) {
            Glide.with(this).load(imageFile).into(imagePreview);
        }
    }
    
    private void setupClickListeners() {
        // 添加单品按钮
        btnAddItem.setOnClickListener(v -> addNewItem());
        
        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> showCancelDialog());
        
        // 底部发布按钮
        findViewById(R.id.btn_publish).setOnClickListener(v -> publishOutfit());
    }
    
    private void observeViewModel() {
        // 监听更新穿搭结果（发布时）
        viewModel.getUpdateOutfitResult().observe(this, response -> {
            if (response != null && response.isSuccess()) {
                Toast.makeText(this, "发布成功！", Toast.LENGTH_SHORT).show();
                // 返回结果，通知PublishFragment重置
                setResult(RESULT_OK);
                finish();
            }
        });
        
        // 监听错误消息
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
        
        // 监听加载状态
        viewModel.getIsLoading().observe(this, isLoading -> {
            progressUpload.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            findViewById(R.id.btn_publish).setEnabled(!isLoading);
        });
    }
    
    private void loadData() {
        if (modifiedTags == null) {
            return;
        }
        
        // 显示整体信息和单品列表
        cardOverallInfo.setVisibility(View.VISIBLE);
        cardItemsList.setVisibility(View.VISIBLE);
        
        // 加载标签
        loadTags();
        
        // 加载单品列表
        loadItems();
    }
    
    private void loadTags() {
        if (modifiedTags == null) {
            return;
        }
        
        // 加载整体风格标签
        loadTagChips(chipGroupOverallStyle, modifiedTags.getOverallStyle(), true);
        
        // 加载适用场合标签
        loadTagChips(chipGroupOccasion, modifiedTags.getOccasion(), true);
        
        // 加载适用季节标签
        loadTagChips(chipGroupSeason, modifiedTags.getSeason(), true);
        
        // 加载适合的天气标签
        loadTagChips(chipGroupWeather, modifiedTags.getWeather(), true);
        
        // 加载性别标签
        loadSexChips();
    }
    
    private void loadSexChips() {
        chipGroupSex.removeAllViews();
        
        // 性别选项：male, female, unisex
        String[] sexOptions = {"男", "女", "中性"};
        String[] sexValues = {"male", "female", "unisex"};
        
        // 获取当前选中的性别值
        List<String> currentSex = modifiedTags.getSex();
        String selectedValue = null;
        if (currentSex != null && !currentSex.isEmpty()) {
            selectedValue = currentSex.get(0); // 取第一个值
        }
        
        for (int i = 0; i < sexOptions.length; i++) {
            Chip chip = new Chip(this);
            chip.setText(sexOptions[i]);
            chip.setChipBackgroundColorResource(R.color.pink_bg);
            chip.setTextColor(getResources().getColor(R.color.primary_color, null));
            chip.setCloseIconVisible(false); // 性别标签不可删除
            chip.setChipMinHeight(36);
            chip.setTextSize(12);
            chip.setPadding(8, 8, 8, 8);
            chip.setCheckable(true);
            
            // 设置选中状态
            if (selectedValue != null && selectedValue.equals(sexValues[i])) {
                chip.setChecked(true);
            }
            
            // 设置点击监听，确保只能选择一个
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // 取消其他选项
                    for (int j = 0; j < chipGroupSex.getChildCount(); j++) {
                        View child = chipGroupSex.getChildAt(j);
                        if (child instanceof Chip && child != buttonView) {
                            ((Chip) child).setChecked(false);
                        }
                    }
                }
            });
            
            ChipGroup.LayoutParams params = new ChipGroup.LayoutParams(
                    ChipGroup.LayoutParams.WRAP_CONTENT,
                    ChipGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 8, 8);
            chip.setLayoutParams(params);
            
            chipGroupSex.addView(chip);
        }
    }
    
    private void loadTagChips(ChipGroup chipGroup, List<String> tags, boolean canAdd) {
        chipGroup.removeAllViews();
        
        if (tags != null) {
            for (String tag : tags) {
                addTagChip(chipGroup, tag, true);
            }
        }
        
        // 添加"添加"标签
        if (canAdd) {
            addAddTagChip(chipGroup);
        }
    }
    
    private void addTagChip(ChipGroup chipGroup, String text, boolean canDelete) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setChipBackgroundColorResource(R.color.pink_bg);
        chip.setTextColor(getResources().getColor(R.color.primary_color, null));
        chip.setCloseIconVisible(canDelete);
        chip.setCloseIconTint(getResources().getColorStateList(R.color.primary_color, null));
        if (canDelete) {
            chip.setOnCloseIconClickListener(v -> chipGroup.removeView(chip));
        }
        chip.setChipMinHeight(36);
        chip.setTextSize(12);
        chip.setPadding(8, 8, 8, 8);
        
        ChipGroup.LayoutParams params = new ChipGroup.LayoutParams(
                ChipGroup.LayoutParams.WRAP_CONTENT,
                ChipGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 8, 8);
        chip.setLayoutParams(params);
        
        chipGroup.addView(chip);
    }
    
    private void addAddTagChip(ChipGroup chipGroup) {
        Chip chip = new Chip(this);
        chip.setText("+ 添加");
        chip.setChipBackgroundColorResource(R.color.white);
        chip.setTextColor(getResources().getColor(R.color.text_secondary, null));
        chip.setCloseIconVisible(false);
        chip.setChipStrokeWidth(1);
        chip.setChipStrokeColor(getResources().getColorStateList(R.color.text_secondary, null));
        chip.setChipMinHeight(36);
        chip.setTextSize(12);
        chip.setPadding(8, 8, 8, 8);
        
        chip.setOnClickListener(v -> showAddTagDialog(chipGroup));
        
        ChipGroup.LayoutParams params = new ChipGroup.LayoutParams(
                ChipGroup.LayoutParams.WRAP_CONTENT,
                ChipGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 8, 8);
        chip.setLayoutParams(params);
        
        chipGroup.addView(chip);
    }
    
    private void showAddTagDialog(ChipGroup chipGroup) {
        EditText input = new EditText(this);
        input.setHint("请输入标签");
        
        new AlertDialog.Builder(this)
                .setTitle("添加标签")
                .setView(input)
                .setPositiveButton("确定", (dialog, which) -> {
                    String text = input.getText().toString().trim();
                    if (!text.isEmpty()) {
                        // 在"添加"标签之前插入新标签
                        int addChipIndex = chipGroup.getChildCount() - 1;
                        addTagChip(chipGroup, text, true);
                        // 将"添加"标签移到最后
                        Chip addChip = (Chip) chipGroup.getChildAt(addChipIndex);
                        chipGroup.removeView(addChip);
                        chipGroup.addView(addChip);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
    
    private void loadItems() {
        layoutItemsContainer.removeAllViews();
        itemViewHolders.clear();
        
        if (modifiedTags != null && modifiedTags.getItems() != null) {
            for (OutfitClothingItem item : modifiedTags.getItems()) {
                addItemView(item);
            }
        }
    }
    
    private void addItemView(OutfitClothingItem item) {
        View itemView = LayoutInflater.from(this)
                .inflate(R.layout.item_clothing_edit_new, layoutItemsContainer, false);
        
        EditText editCategory = itemView.findViewById(R.id.edit_item_category);
        ChipGroup chipGroupTags = itemView.findViewById(R.id.chip_group_item_tags);
        Button btnEditDetails = itemView.findViewById(R.id.btn_edit_details);
        ImageView btnDeleteItem = itemView.findViewById(R.id.btn_delete_item);
        
        // 设置输入框，显示AI识别的category
        if (item != null && item.getCategory() != null && !item.getCategory().isEmpty()) {
            editCategory.setText(item.getCategory());
        } else {
            editCategory.setHint("请输入单品类型");
        }
        
        // 加载标签（材质、颜色等，不可删除）
        if (item != null) {
            // 材质标签
            for (String fabric : item.getFabric()) {
                addReadOnlyTagChip(chipGroupTags, "材质: " + fabric);
            }
            // 颜色标签
            for (String color : item.getColor()) {
                addReadOnlyTagChip(chipGroupTags, "颜色: " + color);
            }
            // 设计元素标签
            for (String element : item.getDesignElements()) {
                addReadOnlyTagChip(chipGroupTags, element);
            }
        }
        
        // 完善细节按钮
        btnEditDetails.setOnClickListener(v -> showItemDetailsDialog(itemView, item));
        
        // 删除按钮
        btnDeleteItem.setOnClickListener(v -> {
            layoutItemsContainer.removeView(itemView);
            itemViewHolders.removeIf(holder -> holder.view == itemView);
            itemDetailsMap.remove(itemView);
        });
        
        ItemViewHolder holder = new ItemViewHolder(itemView, editCategory, chipGroupTags, btnEditDetails);
        itemViewHolders.add(holder);
        layoutItemsContainer.addView(itemView);
    }
    
    private void addReadOnlyTagChip(ChipGroup chipGroup, String text) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setChipBackgroundColorResource(R.color.pink_bg);
        chip.setTextColor(getResources().getColor(R.color.primary_color, null));
        chip.setCloseIconVisible(false); // 不可删除
        chip.setChipMinHeight(36);
        chip.setTextSize(12);
        chip.setPadding(8, 8, 8, 8);
        
        ChipGroup.LayoutParams params = new ChipGroup.LayoutParams(
                ChipGroup.LayoutParams.WRAP_CONTENT,
                ChipGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 8, 8);
        chip.setLayoutParams(params);
        
        chipGroup.addView(chip);
    }
    
    private void showItemDetailsDialog(View itemView, OutfitClothingItem item) {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_item_details, null);
        
        EditText editDetails = dialogView.findViewById(R.id.edit_item_details);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSaveDetails = dialogView.findViewById(R.id.btn_save_details);
        
        // 加载已有的详细信息
        String existingDetails = itemDetailsMap.get(itemView);
        if (existingDetails != null) {
            editDetails.setText(existingDetails);
        }
        
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSaveDetails.setOnClickListener(v -> {
            String details = editDetails.getText().toString().trim();
            itemDetailsMap.put(itemView, details);
            dialog.dismiss();
        });
        
        dialog.show();
    }
    
    private void addNewItem() {
        addItemView(null);
    }
    
    private void publishOutfit() {
        if (currentResponse == null || currentResponse.getOutfitId() == 0) {
            Toast.makeText(this, "请先上传图片并等待AI识别完成", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (modifiedTags == null) {
            Toast.makeText(this, "请等待AI识别完成", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 收集修改后的标签
        OutfitTags tagsToSend = collectModifiedTags();
        
        // 使用PUT接口更新标签（is_public设为true表示公开发布）
        viewModel.updateOutfitTags(currentResponse.getOutfitId(), tagsToSend, true);
    }
    
    private OutfitTags collectModifiedTags() {
        OutfitTags tags = new OutfitTags();
        
        // 收集整体风格
        tags.setOverallStyle(getTagsFromChipGroup(chipGroupOverallStyle));
        
        // 收集适用场合
        tags.setOccasion(getTagsFromChipGroup(chipGroupOccasion));
        
        // 收集适用季节
        tags.setSeason(getTagsFromChipGroup(chipGroupSeason));
        
        // 收集适合的天气
        tags.setWeather(getTagsFromChipGroup(chipGroupWeather));
        
        // 收集性别（单选）
        List<String> sexList = new ArrayList<>();
        for (int i = 0; i < chipGroupSex.getChildCount(); i++) {
            View child = chipGroupSex.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.isChecked()) {
                    // 将UI显示的值转换为API需要的值
                    String uiValue = chip.getText().toString();
                    String apiValue = mapSexUIToAPI(uiValue);
                    if (apiValue != null) {
                        sexList.add(apiValue);
                    }
                    break; // 只能选一个
                }
            }
        }
        tags.setSex(sexList);
        
        // 收集单品列表
        List<OutfitClothingItem> items = new ArrayList<>();
        for (ItemViewHolder holder : itemViewHolders) {
            String category = holder.editCategory.getText().toString().trim();
            if (!category.isEmpty()) {
                OutfitClothingItem item = new OutfitClothingItem();
                item.setCategory(category);
                
                // 从标签中提取信息
                List<String> fabrics = new ArrayList<>();
                List<String> colors = new ArrayList<>();
                List<String> designElements = new ArrayList<>();
                
                for (int i = 0; i < holder.chipGroupTags.getChildCount(); i++) {
                    View child = holder.chipGroupTags.getChildAt(i);
                    if (child instanceof Chip) {
                        Chip chip = (Chip) child;
                        String text = chip.getText().toString();
                        if (text.startsWith("材质: ")) {
                            fabrics.add(text.substring(4));
                        } else if (text.startsWith("颜色: ")) {
                            colors.add(text.substring(4));
                        } else {
                            designElements.add(text);
                        }
                    }
                }
                
                item.setFabric(fabrics);
                item.setColor(colors);
                item.setDesignElements(designElements);
                
                items.add(item);
            }
        }
        tags.setItems(items);
        
        return tags;
    }
    
    /**
     * 将UI显示的性别值映射为API需要的值
     */
    private String mapSexUIToAPI(String uiValue) {
        if (uiValue == null) return null;
        switch (uiValue) {
            case "男":
                return "male";
            case "女":
                return "female";
            case "中性":
                return "unisex";
            default:
                return uiValue; // 如果已经是API格式，直接返回
        }
    }
    
    private List<String> getTagsFromChipGroup(ChipGroup chipGroup) {
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View child = chipGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                String text = chip.getText().toString();
                // 跳过"添加"标签
                if (!text.equals("+ 添加")) {
                    tags.add(text);
                }
            }
        }
        return tags;
    }
    
    private static class ItemViewHolder {
        View view;
        EditText editCategory;
        ChipGroup chipGroupTags;
        Button btnEditDetails;
        
        ItemViewHolder(View view, EditText editCategory, ChipGroup chipGroupTags, Button btnEditDetails) {
            this.view = view;
            this.editCategory = editCategory;
            this.chipGroupTags = chipGroupTags;
            this.btnEditDetails = btnEditDetails;
        }
    }
}

