package com.example.outfitchanges.ui.publish;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.outfitchanges.R;
import com.example.outfitchanges.StartActivity;
import com.example.outfitchanges.ui.home.model.CreateOutfitResponse;
import com.example.outfitchanges.utils.SharedPrefManager;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PublishFragment extends Fragment {
    
    // Views
    private MaterialCardView cardImageContainer;
    private ImageView imagePreview;
    private LinearLayout layoutUploadHint;
    private ImageView iconUpload;
    private TextView textUploadHint;
    private ProgressBar progressUpload;
    
    // Data
    private PublishViewModel viewModel;
    private Uri selectedImageUri;
    private File imageFile;
    private SharedPrefManager prefManager;
    
    // Activity result launchers
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> editResultLauncher;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_publish, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        prefManager = new SharedPrefManager(requireContext());
        viewModel = new ViewModelProvider(this).get(PublishViewModel.class);
        
        // 从 SharedPreferences 恢复 token 到所有 NetworkClient
        com.example.outfitchanges.utils.TokenManager.getInstance(requireContext()).restoreToken();
        
        // 注册权限请求
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        pickImageFromGallery();
                    } else {
                        Toast.makeText(getContext(), "需要存储权限才能选择图片", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        
        // 注册图片选择器
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            displaySelectedImage();
                            uploadImage();
                        }
                    }
                }
        );
        
        // 注册编辑结果（发布成功或取消时重置界面）
        editResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // 无论是发布成功（RESULT_OK）还是取消（RESULT_CANCELED），都重置界面
                    resetToInitialState();
                }
        );
        
        initViews(view);
        setupClickListeners();
        observeViewModel();
    }
    
    private void initViews(View view) {
        cardImageContainer = view.findViewById(R.id.card_image_container);
        imagePreview = view.findViewById(R.id.image_preview);
        layoutUploadHint = view.findViewById(R.id.layout_upload_hint);
        iconUpload = view.findViewById(R.id.icon_upload);
        textUploadHint = view.findViewById(R.id.text_upload_hint);
        progressUpload = view.findViewById(R.id.progress_upload);
    }
    
    private void setupClickListeners() {
        // 点击图片区域选择图片
        cardImageContainer.setOnClickListener(v -> {
            if (prefManager.isGuestMode() || !prefManager.isLoggedIn()) {
                Toast.makeText(getContext(), "请先登录，才能发布穿搭", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), StartActivity.class);
                startActivity(intent);
                return;
            }
            
            if (selectedImageUri == null) {
                requestStoragePermissionAndPickImage();
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 检查登录状态，如果是游客，显示提示
        if (prefManager.isGuestMode() || !prefManager.isLoggedIn()) {
            // 显示默认页面（上传提示）
            resetToInitialState();
        }
    }
    
    private void observeViewModel() {
        // 监听创建穿搭结果（首次上传，获取AI识别结果）
        viewModel.getCreateOutfitResult().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.isSuccess()) {
                // 首次上传成功，跳转到编辑页面
                handleCreateOutfitSuccess(response);
            }
        });
        
        // 监听错误消息
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
        
        // 监听加载状态
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressUpload.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }
    
    private void requestStoragePermissionAndPickImage() {
        String permission;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        
        if (ContextCompat.checkSelfPermission(requireContext(), permission)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(permission);
        } else {
            pickImageFromGallery();
        }
    }
    
    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        
        Intent chooser = Intent.createChooser(intent, "选择图片");
        
        if (chooser.resolveActivity(requireContext().getPackageManager()) != null) {
            imagePickerLauncher.launch(chooser);
        } else {
            Toast.makeText(getContext(), "无法打开相册，请检查是否安装了图片查看应用", Toast.LENGTH_LONG).show();
        }
    }
    
    private void displaySelectedImage() {
        if (selectedImageUri != null) {
            Glide.with(this)
                    .load(selectedImageUri)
                    .into(imagePreview);
            imagePreview.setVisibility(View.VISIBLE);
            layoutUploadHint.setVisibility(View.GONE);
        }
    }
    
    private void uploadImage() {
        if (selectedImageUri == null) {
            return;
        }
        
        try {
            imageFile = uriToFile(selectedImageUri);
            if (imageFile != null && imageFile.exists()) {
                // 首次上传，不传modified_tags，让AI识别
                viewModel.createOutfit(imageFile, null);
            } else {
                Toast.makeText(getContext(), "无法读取图片文件", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(getContext(), "处理图片时出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private File uriToFile(Uri uri) throws IOException {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            return null;
        }
        
        File tempFile = new File(requireContext().getCacheDir(), "temp_image_" + System.currentTimeMillis() + ".jpg");
        FileOutputStream outputStream = new FileOutputStream(tempFile);
        
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        
        inputStream.close();
        outputStream.close();
        
        return tempFile;
    }
    
    private void handleCreateOutfitSuccess(CreateOutfitResponse response) {
        // 跳转到编辑页面
        Intent intent = new Intent(getActivity(), EditOutfitActivity.class);
        Gson gson = new Gson();
        intent.putExtra("response_json", gson.toJson(response));
        intent.putExtra("image_url", response.getImageUrl());
        if (imageFile != null && imageFile.exists()) {
            intent.putExtra("image_file_path", imageFile.getAbsolutePath());
        }
        
        // 使用ActivityResultLauncher启动Activity，以便在发布成功后重置界面
        editResultLauncher.launch(intent);
        
        Toast.makeText(getContext(), "AI识别完成，请检查并修改标签", Toast.LENGTH_SHORT).show();
    }
    
    private void resetToInitialState() {
        selectedImageUri = null;
        imageFile = null;
        
        imagePreview.setVisibility(View.GONE);
        layoutUploadHint.setVisibility(View.VISIBLE);
    }
}
