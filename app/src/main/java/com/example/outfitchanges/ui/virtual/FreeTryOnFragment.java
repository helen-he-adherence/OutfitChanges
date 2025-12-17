package com.example.outfitchanges.ui.virtual;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.bumptech.glide.request.RequestListener;
import com.example.outfitchanges.R;
import com.example.outfitchanges.ui.virtual.model.VirtualTryOnResponse;
import com.example.outfitchanges.utils.SharedPrefManager;
import com.google.android.material.button.MaterialButton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FreeTryOnFragment extends Fragment {
    private VirtualTryOnViewModel viewModel;
    private SharedPrefManager prefManager;
    
    private FrameLayout cardPersonImage;
    private FrameLayout cardClothImage;
    private ImageView imagePersonPreview;
    private ImageView imageClothPreview;
    private FrameLayout btnUploadPerson;
    private FrameLayout btnUploadCloth;
    private TextView textUploadPerson;
    private TextView textUploadCloth;
    private TextView textPersonHint;
    private MaterialButton btnTryOn;
    private ImageView imageResult;
    private ProgressBar progressTryOn;
    
    private Uri personImageUri;
    private Uri clothImageUri;
    private File personImageFile;
    private File clothImageFile;
    
    private ActivityResultLauncher<Intent> personImagePickerLauncher;
    private ActivityResultLauncher<Intent> clothImagePickerLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_free_tryon, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        prefManager = new SharedPrefManager(requireContext());
        viewModel = new ViewModelProvider(this).get(VirtualTryOnViewModel.class);
        
        initViews(view);
        setupImagePickers();
        setupClickListeners();
        observeViewModel();
    }

    private void initViews(View view) {
        cardPersonImage = view.findViewById(R.id.card_person_image);
        cardClothImage = view.findViewById(R.id.card_cloth_image);
        imagePersonPreview = view.findViewById(R.id.image_person_preview);
        imageClothPreview = view.findViewById(R.id.image_cloth_preview);
        btnUploadPerson = view.findViewById(R.id.btn_upload_person);
        btnUploadCloth = view.findViewById(R.id.btn_upload_cloth);
        textUploadPerson = view.findViewById(R.id.text_upload_person);
        textUploadCloth = view.findViewById(R.id.text_upload_cloth);
        textPersonHint = view.findViewById(R.id.text_person_hint);
        btnTryOn = view.findViewById(R.id.btn_tryon);
        imageResult = view.findViewById(R.id.image_result);
        progressTryOn = view.findViewById(R.id.progress_tryon);
    }

    private void setupImagePickers() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // 权限已授予，可以继续选择图片
                    } else {
                        Toast.makeText(getContext(), "需要存储权限才能选择图片", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        personImagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        personImageUri = result.getData().getData();
                        if (personImageUri != null) {
                            personImageFile = saveUriToFile(personImageUri, "person_image");
                            displayPersonImage();
                            updateTryOnButtonState();
                        }
                    }
                }
        );

        clothImagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        clothImageUri = result.getData().getData();
                        if (clothImageUri != null) {
                            clothImageFile = saveUriToFile(clothImageUri, "cloth_image");
                            displayClothImage();
                            updateTryOnButtonState();
                        }
                    }
                }
        );
    }

    private void setupClickListeners() {
        cardPersonImage.setOnClickListener(v -> {
            if (personImageUri != null) {
                // 如果已有图片，点击可以重新选择
                requestStoragePermissionAndPickImage(true);
            } else {
                requestStoragePermissionAndPickImage(true);
            }
        });

        cardClothImage.setOnClickListener(v -> {
            if (clothImageUri != null) {
                // 如果已有图片，点击可以重新选择
                requestStoragePermissionAndPickImage(false);
            } else {
                requestStoragePermissionAndPickImage(false);
            }
        });

        btnTryOn.setOnClickListener(v -> {
            android.util.Log.d("FreeTryOnFragment", "=== 一键换装按钮被点击 ===");
            if (personImageFile != null && clothImageFile != null) {
                android.util.Log.d("FreeTryOnFragment", "图片文件检查通过");
                android.util.Log.d("FreeTryOnFragment", "Person image: " + (personImageFile != null ? personImageFile.getAbsolutePath() : "null") + ", exists: " + (personImageFile != null && personImageFile.exists()));
                android.util.Log.d("FreeTryOnFragment", "Cloth image: " + (clothImageFile != null ? clothImageFile.getAbsolutePath() : "null") + ", exists: " + (clothImageFile != null && clothImageFile.exists()));
                
                String token = com.example.outfitchanges.utils.TokenManager.getInstance(requireContext()).getToken();
                android.util.Log.d("FreeTryOnFragment", "Token: " + (token != null && !token.isEmpty() ? "存在（长度: " + token.length() + "）" : "不存在"));
                if (token == null || token.isEmpty()) {
                    Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
                    return;
                }
                android.util.Log.d("FreeTryOnFragment", "开始提交换装任务...");
                viewModel.submitTryOnWithSourceImage(personImageFile, clothImageFile, token);
            } else {
                android.util.Log.e("FreeTryOnFragment", "图片文件检查失败: personImageFile=" + (personImageFile != null) + ", clothImageFile=" + (clothImageFile != null));
            }
        });
    }

    private void requestStoragePermissionAndPickImage(boolean isPerson) {
        String permission = android.Manifest.permission.READ_MEDIA_IMAGES;
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            permission = android.Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(permission);
        } else {
            pickImageFromGallery(isPerson);
        }
    }

    private void pickImageFromGallery(boolean isPerson) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        Intent chooser = Intent.createChooser(intent, "选择图片");

        if (isPerson) {
            personImagePickerLauncher.launch(chooser);
        } else {
            clothImagePickerLauncher.launch(chooser);
        }
    }

    private File saveUriToFile(Uri uri, String prefix) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File tempFile = File.createTempFile(prefix + "_", ".jpg", requireContext().getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return tempFile;
        } catch (Exception e) {
            android.util.Log.e("FreeTryOnFragment", "Error saving file", e);
            return null;
        }
    }

    private void displayPersonImage() {
        if (personImageUri != null) {
            Glide.with(this)
                    .load(personImageUri)
                    .into(imagePersonPreview);
            imagePersonPreview.setVisibility(View.VISIBLE);
            btnUploadPerson.setVisibility(View.GONE);
            textUploadPerson.setVisibility(View.GONE);
            textPersonHint.setVisibility(View.GONE);
        }
    }

    private void displayClothImage() {
        if (clothImageUri != null) {
            Glide.with(this)
                    .load(clothImageUri)
                    .into(imageClothPreview);
            imageClothPreview.setVisibility(View.VISIBLE);
            btnUploadCloth.setVisibility(View.GONE);
            textUploadCloth.setVisibility(View.GONE);
        }
    }

    private void updateTryOnButtonState() {
        boolean canTryOn = personImageFile != null && clothImageFile != null;
        btnTryOn.setEnabled(canTryOn);
        if (canTryOn) {
            btnTryOn.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.primary_color));
        } else {
            btnTryOn.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.gray_button_bg));
        }
    }

    private void observeViewModel() {
        viewModel.getTryOnResult().observe(getViewLifecycleOwner(), response -> {
            android.util.Log.d("FreeTryOnFragment", "=== tryOnResult观察者被触发 ===");
            if (response != null) {
                android.util.Log.d("FreeTryOnFragment", "TryOnResult: success=" + response.isSuccess() + ", status=" + response.getStatus() + ", taskId=" + response.getTaskId() + ", resultUrl=" + response.getResultUrl());
                
                // 如果状态是completed且有resultUrl，直接显示结果（无论success字段是什么）
                if ("completed".equals(response.getStatus()) && response.getResultUrl() != null) {
                    android.util.Log.d("FreeTryOnFragment", "任务已完成，显示结果");
                    // 显示结果
                    stopPolling();
                    displayResult(response.getResultUrl());
                } else if (response.isSuccess()) {
                    // 如果success为true，检查是否有taskId需要轮询
                    if (response.getTaskId() != null && !response.getTaskId().isEmpty()) {
                        // 开始轮询任务状态
                        android.util.Log.d("FreeTryOnFragment", "任务已提交，开始轮询，taskId: " + response.getTaskId());
                        startPollingTaskStatus(response.getTaskId());
                    } else {
                        android.util.Log.w("FreeTryOnFragment", "响应成功但没有taskId也没有completed状态");
                    }
                } else {
                    android.util.Log.e("FreeTryOnFragment", "任务提交失败: " + response.getMessage());
                    stopPolling();
                    Toast.makeText(getContext(), "换装失败: " + (response.getMessage() != null ? response.getMessage() : "未知错误"), Toast.LENGTH_SHORT).show();
                }
            } else {
                android.util.Log.w("FreeTryOnFragment", "tryOnResult为null");
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                stopPolling();
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressTryOn.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnTryOn.setEnabled(!isLoading && personImageFile != null && clothImageFile != null);
            // 如果正在加载，隐藏结果图片
            if (isLoading) {
                imageResult.setVisibility(View.GONE);
            }
        });
    }

    private android.os.Handler pollingHandler;
    private Runnable pollingRunnable;
    private String currentTaskId;
    private boolean isPolling = false;

    private void startPollingTaskStatus(String taskId) {
        android.util.Log.d("FreeTryOnFragment", "=== startPollingTaskStatus 开始 ===");
        android.util.Log.d("FreeTryOnFragment", "TaskId: " + taskId);
        
        String token = com.example.outfitchanges.utils.TokenManager.getInstance(requireContext()).getToken();
        if (token == null || token.isEmpty()) {
            android.util.Log.e("FreeTryOnFragment", "Token is empty, cannot poll");
            return;
        }
        android.util.Log.d("FreeTryOnFragment", "Token存在，长度: " + token.length());

        // 停止之前的轮询
        stopPolling();

        currentTaskId = taskId;
        isPolling = true;
        pollingHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        
        // 确保加载动画显示
        android.util.Log.d("FreeTryOnFragment", "设置加载状态为true");
        viewModel.setLoading(true);
        
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPolling && currentTaskId != null && currentTaskId.equals(taskId)) {
                    android.util.Log.d("FreeTryOnFragment", "=== 轮询任务状态 ===");
                    android.util.Log.d("FreeTryOnFragment", "TaskId: " + taskId);
                    viewModel.checkTaskStatus(taskId, token);
                    // 延迟后继续轮询（每2秒一次）
                    pollingHandler.postDelayed(this, 2000);
                } else {
                    android.util.Log.d("FreeTryOnFragment", "轮询已停止，不再继续");
                }
            }
        };

        // 观察任务状态变化（只观察一次，避免重复）
        viewModel.getTaskStatus().observe(getViewLifecycleOwner(), status -> {
            android.util.Log.d("FreeTryOnFragment", "=== taskStatus观察者被触发 ===");
            android.util.Log.d("FreeTryOnFragment", "Status: " + status);
            android.util.Log.d("FreeTryOnFragment", "CurrentTaskId: " + currentTaskId);
            android.util.Log.d("FreeTryOnFragment", "IsPolling: " + isPolling);
            
            if (status != null && taskId.equals(currentTaskId) && isPolling) {
                android.util.Log.d("FreeTryOnFragment", "Task status changed: " + status);
                if ("completed".equals(status)) {
                    android.util.Log.d("FreeTryOnFragment", "任务完成，停止轮询");
                    // 状态变为completed，停止轮询
                    // 结果会通过tryOnResult返回
                    stopPolling();
                } else if (!"processing".equals(status) && !"pending".equals(status)) {
                    android.util.Log.e("FreeTryOnFragment", "任务状态异常: " + status + "，停止轮询");
                    // 其他状态（如failed），停止轮询
                    stopPolling();
                    viewModel.setLoading(false);
                } else {
                    android.util.Log.d("FreeTryOnFragment", "任务状态: " + status + "，继续轮询");
                }
            } else {
                android.util.Log.w("FreeTryOnFragment", "状态更新被忽略: status=" + status + ", taskId匹配=" + (taskId.equals(currentTaskId)) + ", isPolling=" + isPolling);
            }
        });

        // 立即开始第一次查询
        android.util.Log.d("FreeTryOnFragment", "立即开始第一次状态查询");
        viewModel.checkTaskStatus(taskId, token);
        // 然后每2秒轮询一次
        android.util.Log.d("FreeTryOnFragment", "设置2秒后继续轮询");
        pollingHandler.postDelayed(pollingRunnable, 2000);
    }

    private void stopPolling() {
        isPolling = false;
        if (pollingHandler != null && pollingRunnable != null) {
            pollingHandler.removeCallbacks(pollingRunnable);
        }
        currentTaskId = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopPolling();
    }

    private void displayResult(String resultUrl) {
        android.util.Log.d("FreeTryOnFragment", "Displaying result: " + resultUrl);
        
        // 确保ImageView可见
        imageResult.setVisibility(View.VISIBLE);
        progressTryOn.setVisibility(View.GONE);
        viewModel.setLoading(false);
        
        // 使用Glide加载图片，添加错误处理和占位符
        Glide.with(this)
                .load(resultUrl)
                .placeholder(R.color.gray_button_bg)
                .error(R.color.gray_button_bg)
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                        android.util.Log.e("FreeTryOnFragment", "Glide加载失败", e);
                        Toast.makeText(getContext(), "图片加载失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        android.util.Log.d("FreeTryOnFragment", "图片加载成功");
                        return false;
                    }
                })
                .into(imageResult);
        
        Toast.makeText(getContext(), "换装完成！", Toast.LENGTH_SHORT).show();
    }
}

