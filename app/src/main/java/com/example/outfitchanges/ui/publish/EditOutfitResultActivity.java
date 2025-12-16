package com.example.outfitchanges.ui.publish;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.outfitchanges.R;
import com.example.outfitchanges.ui.publish.model.OutfitAnalysisTags;
import com.example.outfitchanges.ui.publish.model.UploadResponse;
import com.google.android.material.chip.Chip;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

public class EditOutfitResultActivity extends AppCompatActivity {
    private ImageView imageUploaded;
    private LinearLayout chipContainerOverallStyle;
    private LinearLayout chipContainerOccasion;
    private LinearLayout chipContainerSeason;
    private LinearLayout chipContainerWeather;
    private LinearLayout layoutItems;
    private Button btnSave;
    
    private UploadResponse uploadResponse;
    private OutfitAnalysisTags tags;
    private List<ItemViewHolder> itemViewHolders = new ArrayList<>();
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_outfit_result);

        // 获取传递的数据
        String responseJson = getIntent().getStringExtra("response_json");
        imageUrl = getIntent().getStringExtra("image_url");
        
        if (responseJson != null) {
            Gson gson = new Gson();
            uploadResponse = gson.fromJson(responseJson, UploadResponse.class);
            if (uploadResponse != null) {
                tags = uploadResponse.getTags();
            }
        }

        initViews();
        setupClickListeners();
        loadData();
    }

    private void initViews() {
        imageUploaded = findViewById(R.id.image_uploaded);
        chipContainerOverallStyle = findViewById(R.id.chip_container_overall_style);
        chipContainerOccasion = findViewById(R.id.chip_container_occasion);
        chipContainerSeason = findViewById(R.id.chip_container_season);
        chipContainerWeather = findViewById(R.id.chip_container_weather);
        layoutItems = findViewById(R.id.layout_items);
        btnSave = findViewById(R.id.btn_save);

        // 加载图片
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(imageUploaded);
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveAndFinish());

        findViewById(R.id.btn_add_overall_style).setOnClickListener(v -> showAddTagDialog(chipContainerOverallStyle, "整体风格"));
        findViewById(R.id.btn_add_occasion).setOnClickListener(v -> showAddTagDialog(chipContainerOccasion, "适用场合"));
        findViewById(R.id.btn_add_season).setOnClickListener(v -> showAddTagDialog(chipContainerSeason, "适用季节"));
        findViewById(R.id.btn_add_weather).setOnClickListener(v -> showAddTagDialog(chipContainerWeather, "适用天气"));
        findViewById(R.id.btn_add_item).setOnClickListener(v -> addNewItem());
    }

    private void loadData() {
        if (tags == null) {
            tags = new OutfitAnalysisTags();
        }

        // 加载整体风格
        loadTags(chipContainerOverallStyle, tags.getOverallStyle());

        // 加载适用场合
        loadTags(chipContainerOccasion, tags.getOccasion());

        // 加载适用季节
        loadTags(chipContainerSeason, tags.getSeason());

        // 加载适用天气
        loadTags(chipContainerWeather, tags.getWeather());

        // 加载单品列表
        loadItems();
    }

    private void loadTags(LinearLayout container, List<String> tagList) {
        container.removeAllViews();
        if (tagList != null) {
            for (String tag : tagList) {
                addTagChip(container, tag);
            }
        }
    }

    private void addTagChip(LinearLayout container, String text) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setChipBackgroundColorResource(R.color.pink_bg);
        chip.setTextColor(getResources().getColor(R.color.primary_color, null));
        chip.setCloseIconVisible(true);
        chip.setCloseIconTint(getResources().getColorStateList(R.color.primary_color, null));
        chip.setOnCloseIconClickListener(v -> container.removeView(chip));
        chip.setChipMinHeight(36);
        chip.setTextSize(12);
        chip.setPadding(8, 8, 8, 8);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 8, 0);
        chip.setLayoutParams(params);
        
        container.addView(chip);
    }

    private void showAddTagDialog(LinearLayout container, String title) {
        EditText input = new EditText(this);
        input.setHint("请输入" + title);
        
        new AlertDialog.Builder(this)
                .setTitle("添加" + title)
                .setView(input)
                .setPositiveButton("确定", (dialog, which) -> {
                    String text = input.getText().toString().trim();
                    if (!text.isEmpty()) {
                        addTagChip(container, text);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void loadItems() {
        layoutItems.removeAllViews();
        itemViewHolders.clear();

        if (tags != null && tags.getItems() != null) {
            for (OutfitAnalysisTags.ClothingItem item : tags.getItems()) {
                addItemView(item);
            }
        }
    }

    private void addItemView(OutfitAnalysisTags.ClothingItem item) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_clothing_edit, layoutItems, false);
        
        EditText editCategory = itemView.findViewById(R.id.edit_item_category);
        EditText editDetails = itemView.findViewById(R.id.edit_item_details);
        LinearLayout chipContainerFeatures = itemView.findViewById(R.id.chip_container_features);
        Button btnAddFeature = itemView.findViewById(R.id.btn_add_feature);
        Button btnDeleteItem = itemView.findViewById(R.id.btn_delete_item);

        // 设置单品名称
        if (item != null) {
            editCategory.setText(item.getCategory());
            
            // 加载特点标签（颜色、设计元素等）
            if (item.getColor() != null) {
                for (String color : item.getColor()) {
                    addTagChip(chipContainerFeatures, "颜色: " + color);
                }
            }
            if (item.getDesignElements() != null) {
                for (String element : item.getDesignElements()) {
                    addTagChip(chipContainerFeatures, element);
                }
            }
            if (item.getFabric() != null) {
                for (String fabric : item.getFabric()) {
                    addTagChip(chipContainerFeatures, "材质: " + fabric);
                }
            }
        }

        // 添加特点按钮
        btnAddFeature.setOnClickListener(v -> showAddTagDialog(chipContainerFeatures, "特点"));

        // 删除单品按钮
        btnDeleteItem.setOnClickListener(v -> {
            layoutItems.removeView(itemView);
            itemViewHolders.removeIf(holder -> holder.view == itemView);
        });

        ItemViewHolder holder = new ItemViewHolder(itemView, editCategory, editDetails, chipContainerFeatures);
        itemViewHolders.add(holder);
        layoutItems.addView(itemView);
    }

    private void addNewItem() {
        addItemView(null);
    }

    private void saveAndFinish() {
        // 收集整体信息
        List<String> overallStyle = getTagsFromContainer(chipContainerOverallStyle);
        List<String> occasion = getTagsFromContainer(chipContainerOccasion);
        List<String> season = getTagsFromContainer(chipContainerSeason);
        List<String> weather = getTagsFromContainer(chipContainerWeather);

        // 收集单品信息
        List<OutfitAnalysisTags.ClothingItem> items = new ArrayList<>();
        for (ItemViewHolder holder : itemViewHolders) {
            String category = holder.editCategory.getText().toString().trim();
            if (!category.isEmpty()) {
                OutfitAnalysisTags.ClothingItem item = new OutfitAnalysisTags.ClothingItem();
                item.setCategory(category);
                
                // 从标签中提取颜色、设计元素和材质
                List<String> colors = new ArrayList<>();
                List<String> designElements = new ArrayList<>();
                List<String> fabrics = new ArrayList<>();
                for (int i = 0; i < holder.chipContainer.getChildCount(); i++) {
                    View child = holder.chipContainer.getChildAt(i);
                    if (child instanceof Chip) {
                        Chip chip = (Chip) child;
                        String text = chip.getText().toString();
                        if (text.startsWith("颜色: ")) {
                            colors.add(text.substring(4));
                        } else if (text.startsWith("材质: ")) {
                            fabrics.add(text.substring(4));
                        } else {
                            designElements.add(text);
                        }
                    }
                }
                item.setColor(colors);
                item.setDesignElements(designElements);
                item.setFabric(fabrics);
                
                items.add(item);
            }
        }

        // 更新tags对象
        if (tags == null) {
            tags = new OutfitAnalysisTags();
        }
        tags.setOverallStyle(overallStyle);
        tags.setOccasion(occasion);
        tags.setSeason(season);
        tags.setWeather(weather);
        tags.setItems(items);

        // 转换为JSON（不格式化，直接转）
        Gson gson = new Gson();
        String jsonString = gson.toJson(tags);

        // 返回结果
        Intent resultIntent = new Intent();
        resultIntent.putExtra("modified_tags_json", jsonString);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private List<String> getTagsFromContainer(LinearLayout container) {
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof Chip) {
                tags.add(((Chip) child).getText().toString());
            }
        }
        return tags;
    }

    private static class ItemViewHolder {
        View view;
        EditText editCategory;
        EditText editDetails;
        LinearLayout chipContainer;

        ItemViewHolder(View view, EditText editCategory, EditText editDetails, LinearLayout chipContainer) {
            this.view = view;
            this.editCategory = editCategory;
            this.editDetails = editDetails;
            this.chipContainer = chipContainer;
        }
    }
}

