package com.example.outfitchanges.ui.publish;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.outfitchanges.R;
import com.example.outfitchanges.StartActivity;
import com.example.outfitchanges.ui.publish.model.UploadResponse;
import com.example.outfitchanges.utils.SharedPrefManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PublishFragment extends Fragment {
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1002;

    private android.widget.FrameLayout cardUpload;
    private ImageView imagePreview;
    private ImageView iconUpload;
    private android.widget.TextView textUploadHint;
    private LinearLayout layoutResult;
    private EditText editJsonResult;
    private Button btnSave;
    private ProgressBar progressUpload;
    private PublishViewModel viewModel;
    private Uri selectedImageUri;
    private File imageFile;
    private SharedPrefManager prefManager;
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
                    android.util.Log.d("PublishFragment", "Permission request result: " + isGranted);
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
                    android.util.Log.d("PublishFragment", "Image picker result: " + result.getResultCode());
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            displaySelectedImage();
                            uploadImage();
                        }
                    }
                }
        );
        
        // 注册编辑结果
        editResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    android.util.Log.d("PublishFragment", "Edit result: " + result.getResultCode());
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        String modifiedTagsJson = result.getData().getStringExtra("modified_tags_json");
                        android.util.Log.d("PublishFragment", "Modified tags JSON length: " + (modifiedTagsJson != null ? modifiedTagsJson.length() : 0));
                        android.util.Log.d("PublishFragment", "Modified tags JSON preview: " + (modifiedTagsJson != null && modifiedTagsJson.length() > 0 ? modifiedTagsJson.substring(0, Math.min(200, modifiedTagsJson.length())) : "null"));
                        if (modifiedTagsJson != null && imageFile != null && imageFile.exists()) {
                            android.util.Log.d("PublishFragment", "Saving modified tags to server");
                            android.util.Log.d("PublishFragment", "Image file path: " + imageFile.getAbsolutePath());
                            // 保存修改后的标签到服务器
                            viewModel.uploadImageWithModifiedTags(imageFile, modifiedTagsJson);
                        } else {
                            android.util.Log.e("PublishFragment", "Cannot save: imageFile=" + (imageFile != null ? imageFile.exists() : "null") + ", json=" + (modifiedTagsJson != null ? "exists" : "null"));
                            Toast.makeText(getContext(), "保存失败：图片文件不存在", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
        
        initViews(view);
        setupClickListeners();
        observeViewModel();
        
        // 检查登录状态
        checkLoginStatus();
    }
    
    private void checkLoginStatus() {
        // 不强制退出，只是提示用户需要登录
        // 用户可以在需要时点击上传按钮，然后会提示登录
    }

    private void initViews(View view) {
        cardUpload = view.findViewById(R.id.card_upload);
        imagePreview = view.findViewById(R.id.image_preview);
        iconUpload = view.findViewById(R.id.icon_upload);
        textUploadHint = view.findViewById(R.id.text_upload_hint);
        layoutResult = view.findViewById(R.id.layout_result);
        editJsonResult = view.findViewById(R.id.edit_json_result);
        btnSave = view.findViewById(R.id.btn_save);
        progressUpload = view.findViewById(R.id.progress_upload);
    }

    private void setupClickListeners() {
        cardUpload.setOnClickListener(v -> {
            if (!prefManager.isLoggedIn()) {
                Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), StartActivity.class);
                startActivity(intent);
                return;
            }
            
            if (selectedImageUri == null) {
                requestStoragePermissionAndPickImage();
            }
        });

        btnSave.setOnClickListener(v -> {
            if (imageFile == null) {
                Toast.makeText(getContext(), "请先上传图片", Toast.LENGTH_SHORT).show();
                return;
            }

            String modifiedJson = editJsonResult.getText().toString().trim();
            if (modifiedJson.isEmpty()) {
                Toast.makeText(getContext(), "请先等待解析完成", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.uploadImageWithModifiedTags(imageFile, modifiedJson);
        });
    }

    private void observeViewModel() {
        // 首次上传成功，跳转到编辑界面
        viewModel.getUploadResult().observe(getViewLifecycleOwner(), response -> {
            if (response != null) {
                handleUploadSuccess(response);
            }
        });

        // 保存修改后的标签成功，只显示提示，不跳转
        viewModel.getSaveResult().observe(getViewLifecycleOwner(), response -> {
            if (response != null) {
                if (response.isSuccess()) {
                    Toast.makeText(getContext(), "保存成功！", Toast.LENGTH_SHORT).show();
                    // 清空已选择的图片，允许用户上传新图片
                    selectedImageUri = null;
                    imageFile = null;
                    imagePreview.setVisibility(View.GONE);
                    textUploadHint.setVisibility(View.VISIBLE);
                    iconUpload.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getContext(), "保存失败: " + response.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressUpload.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnSave.setEnabled(!isLoading);
        });
    }

    private void requestStoragePermissionAndPickImage() {
        android.util.Log.d("PublishFragment", "requestStoragePermissionAndPickImage called");
        // Android 13+ (API 33+) 使用 READ_MEDIA_IMAGES，否则使用 READ_EXTERNAL_STORAGE
        String permission;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        
        if (ContextCompat.checkSelfPermission(requireContext(), permission)
                != PackageManager.PERMISSION_GRANTED) {
            android.util.Log.d("PublishFragment", "Requesting permission: " + permission);
            requestPermissionLauncher.launch(permission);
        } else {
            android.util.Log.d("PublishFragment", "Permission already granted, picking image");
            pickImageFromGallery();
        }
    }

    private void pickImageFromGallery() {
        android.util.Log.d("PublishFragment", "pickImageFromGallery called");
        
        // 使用 ACTION_GET_CONTENT，这是最通用的方法
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        
        // 创建选择器，让用户选择使用哪个应用
        Intent chooser = Intent.createChooser(intent, "选择图片");
        
        if (chooser.resolveActivity(requireContext().getPackageManager()) != null) {
            android.util.Log.d("PublishFragment", "Launching image picker");
            imagePickerLauncher.launch(chooser);
        } else {
            android.util.Log.e("PublishFragment", "Cannot resolve image picker activity");
            Toast.makeText(getContext(), "无法打开相册，请检查是否安装了图片查看应用", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        android.util.Log.d("PublishFragment", "onRequestPermissionsResult called: requestCode=" + requestCode);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            android.util.Log.d("PublishFragment", "Permission result: " + (grantResults.length > 0 ? grantResults[0] : "empty"));
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限授予后，立即选择图片
                android.util.Log.d("PublishFragment", "Permission granted, picking image");
                pickImageFromGallery();
            } else {
                Toast.makeText(getContext(), "需要存储权限才能选择图片", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void displaySelectedImage() {
        if (selectedImageUri != null) {
            Glide.with(this)
                    .load(selectedImageUri)
                    .into(imagePreview);
            imagePreview.setVisibility(View.VISIBLE);
            iconUpload.setVisibility(View.GONE);
            if (textUploadHint != null) {
                textUploadHint.setVisibility(View.GONE);
            }
        }
    }

    private void uploadImage() {
        if (selectedImageUri == null) {
            return;
        }

        try {
            imageFile = uriToFile(selectedImageUri);
            if (imageFile != null && imageFile.exists()) {
                viewModel.uploadImage(imageFile);
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

    private void handleUploadSuccess(UploadResponse response) {
        if (response.isSuccess()) {
            // 跳转到编辑界面
            Intent intent = new Intent(getActivity(), EditOutfitResultActivity.class);
            Gson gson = new Gson();
            intent.putExtra("response_json", gson.toJson(response));
            intent.putExtra("image_url", response.getImageUrl());
            editResultLauncher.launch(intent);
            
            Toast.makeText(getContext(), "解析成功！请编辑信息后保存", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "解析失败: " + response.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
