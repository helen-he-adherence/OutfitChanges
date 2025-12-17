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
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.bumptech.glide.Glide;
import com.example.outfitchanges.R;
import com.example.outfitchanges.ui.home.adapter.HomeAdapter;
import com.example.outfitchanges.ui.virtual.model.VirtualTryOnResponse;
import com.example.outfitchanges.utils.SharedPrefManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class VirtualTryOnFragment extends Fragment {
    private VirtualTryOnViewModel viewModel;
    private SharedPrefManager prefManager;
    
    private FrameLayout cardPersonImage;
    private ImageView imagePersonPreview;
    private FrameLayout btnUploadPerson;
    private TextView textUploadPerson;
    private TextView textPersonHint;
    private RecyclerView recyclerViewCollections;
    private ProgressBar progressTryOn;
    
    private Uri personImageUri;
    private File personImageFile;
    private HomeAdapter adapter;
    
    private ActivityResultLauncher<Intent> personImagePickerLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_virtual_tryon, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        prefManager = new SharedPrefManager(requireContext());
        viewModel = new ViewModelProvider(this).get(VirtualTryOnViewModel.class);
        
        initViews(view);
        setupImagePicker();
        setupRecyclerView();
        setupClickListeners();
        observeViewModel();
        loadCollections();
    }

    private void initViews(View view) {
        cardPersonImage = view.findViewById(R.id.card_person_image);
        imagePersonPreview = view.findViewById(R.id.image_person_preview);
        btnUploadPerson = view.findViewById(R.id.btn_upload_person);
        textUploadPerson = view.findViewById(R.id.text_upload_person);
        textPersonHint = view.findViewById(R.id.text_person_hint);
        recyclerViewCollections = view.findViewById(R.id.recycler_view_collections);
        progressTryOn = view.findViewById(R.id.progress_tryon);
    }

    private void setupImagePicker() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // 权限已授予
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
                        }
                    }
                }
        );
    }

    private void setupRecyclerView() {
        adapter = new HomeAdapter();
        adapter.setOnItemClickListener(outfit -> {
            if (personImageFile == null) {
                Toast.makeText(getContext(), "请先上传人像", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String token = prefManager.getToken();
            if (token == null || token.isEmpty()) {
                Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // TODO: 从outfit对象中获取ID
            // 暂时使用一个默认值，需要根据实际数据结构调整
            // 可能需要从API响应中获取outfitId，或者修改OutfitDisplayModel添加id字段
            Toast.makeText(getContext(), "功能开发中，需要outfitId", Toast.LENGTH_SHORT).show();
            // int outfitId = outfit.getId();
            // viewModel.submitTryOnWithOutfitId(personImageFile, outfitId, token);
        });
        
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerViewCollections.setLayoutManager(layoutManager);
        recyclerViewCollections.setAdapter(adapter);
    }

    private void setupClickListeners() {
        cardPersonImage.setOnClickListener(v -> {
            requestStoragePermissionAndPickImage();
        });
    }

    private void requestStoragePermissionAndPickImage() {
        String permission = android.Manifest.permission.READ_MEDIA_IMAGES;
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            permission = android.Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
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
        personImagePickerLauncher.launch(chooser);
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
            android.util.Log.e("VirtualTryOnFragment", "Error saving file", e);
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

    private void observeViewModel() {
        viewModel.getTryOnResult().observe(getViewLifecycleOwner(), response -> {
            if (response != null) {
                if (response.isSuccess()) {
                    if ("completed".equals(response.getStatus()) && response.getResultUrl() != null) {
                        // 跳转到结果展示界面
                        Intent intent = new Intent(getActivity(), TryOnResultActivity.class);
                        intent.putExtra("result_url", response.getResultUrl());
                        intent.putExtra("person_image_file", personImageFile != null ? personImageFile.getAbsolutePath() : null);
                        startActivity(intent);
                    } else if (response.getTaskId() != null) {
                        // 开始轮询任务状态
                        startPollingTaskStatus(response.getTaskId());
                    }
                } else {
                    Toast.makeText(getContext(), "换装失败: " + response.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressTryOn.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    private android.os.Handler pollingHandler;
    private Runnable pollingRunnable;
    private String currentTaskId;

    private void startPollingTaskStatus(String taskId) {
        String token = prefManager.getToken();
        if (token == null || token.isEmpty()) {
            return;
        }

        // 停止之前的轮询
        stopPolling();

        currentTaskId = taskId;
        pollingHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentTaskId != null && currentTaskId.equals(taskId)) {
                    viewModel.checkTaskStatus(taskId, token);
                    // 延迟后继续轮询
                    pollingHandler.postDelayed(this, 2000);
                }
            }
        };

        // 观察任务状态变化
        viewModel.getTaskStatus().observe(getViewLifecycleOwner(), status -> {
            if (status != null && taskId.equals(currentTaskId)) {
                if ("completed".equals(status)) {
                    stopPolling();
                } else if (!"processing".equals(status) && !"pending".equals(status)) {
                    stopPolling();
                }
            }
        });

        // 开始轮询
        pollingHandler.postDelayed(pollingRunnable, 2000);
    }

    private void stopPolling() {
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

    private void loadCollections() {
        // TODO: 从服务器加载收藏的穿搭
        // 暂时使用空列表
        adapter.setData(new ArrayList<>());
    }
}

