package com.example.book.activity.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.example.book.MyApplication;
import com.example.book.R;
import com.example.book.activity.BaseActivity;
import com.example.book.constant.Constant;
import com.example.book.constant.GlobalFunction;
import com.example.book.databinding.ActivityAdminAddAdvertisementBinding;
import com.example.book.model.Advertisement;
import com.example.book.utils.StringUtil;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class AdminAddAdvertisementActivity extends BaseActivity {

    private ActivityAdminAddAdvertisementBinding binding;
    private boolean isUpdate;
    private Advertisement mAdvertisement;
    private Uri videoUri;
    private ActivityResultLauncher<Intent> videoPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAddAdvertisementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initVideoPicker();
        loadDataIntent();
        initToolbar();
        initView();
        initListener();
    }

    private void initVideoPicker() {
        videoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        videoUri = result.getData().getData();
                        if (videoUri != null) {
                            String fileName = getFileName(videoUri);
                            binding.tvVideoName.setText(fileName != null ? fileName : "Video selected");
                        }
                    }
                });
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result != null ? result.lastIndexOf('/') : -1;
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void loadDataIntent() {
        Bundle bundleReceived = getIntent().getExtras();
        if (bundleReceived != null) {
            isUpdate = true;
            mAdvertisement = (Advertisement) bundleReceived.getSerializable(Constant.OBJECT_ADVERTISEMENT);
        }
    }

    private void initToolbar() {
        binding.layoutToolbar.imgToolbar.setOnClickListener(view -> finish());
    }

    private void initView() {
        if (isUpdate && mAdvertisement != null) {
            binding.layoutToolbar.tvToolbarTitle.setText(getString(R.string.label_edit_advertisement));
            binding.btnAddOrEdit.setText(getString(R.string.action_edit));

            binding.edtTitle.setText(mAdvertisement.getTitle());
            if (mAdvertisement.getVideoUrl() != null) {
                binding.tvVideoName.setText("Current: " + mAdvertisement.getVideoUrl());
            }
            if (mAdvertisement.getThumbnailUrl() != null) {
                binding.edtThumbnail.setText(mAdvertisement.getThumbnailUrl());
            }
            binding.chbActive.setChecked(mAdvertisement.isActive());
        } else {
            binding.layoutToolbar.tvToolbarTitle.setText(getString(R.string.label_add_advertisement));
            binding.btnAddOrEdit.setText(getString(R.string.action_add));
        }
    }

    private void initListener() {
        binding.btnSelectVideo.setOnClickListener(v -> selectVideo());
        binding.btnAddOrEdit.setOnClickListener(v -> addOrEditAdvertisement());
    }

    private void selectVideo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        videoPickerLauncher.launch(Intent.createChooser(intent, "Select Video"));
    }

    private void addOrEditAdvertisement() {
        String strTitle = binding.edtTitle.getText().toString().trim();
        String strThumbnail = binding.edtThumbnail.getText().toString().trim();

        if (StringUtil.isEmpty(strTitle)) {
            Toast.makeText(this, getString(R.string.msg_ad_title_require), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isUpdate && videoUri == null) {
            Toast.makeText(this, getString(R.string.msg_ad_video_require), Toast.LENGTH_SHORT).show();
            return;
        }

        if (isUpdate) {
            updateAdvertisement(strTitle, strThumbnail);
        } else {
            uploadAndAddAdvertisement(strTitle, strThumbnail);
        }
    }

    private void uploadAndAddAdvertisement(String title, String thumbnail) {
        showProgressDialog(true);
        long advertisementId = System.currentTimeMillis();
        StorageReference videoRef = MyApplication.get(this).getAdvertisementStorageReference()
                .child(String.valueOf(advertisementId))
                .child("video.mp4");

        UploadTask uploadTask = videoRef.putFile(videoUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            videoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String videoUrl = uri.toString();
                saveAdvertisementToDatabase(advertisementId, title, videoUrl, thumbnail);
            }).addOnFailureListener(e -> {
                showProgressDialog(false);
                Toast.makeText(this, "Failed to get video URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            showProgressDialog(false);
            Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateAdvertisement(String title, String thumbnail) {
        showProgressDialog(true);
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("thumbnailUrl", thumbnail);
        map.put("isActive", binding.chbActive.isChecked());
        map.put("updatedAt", System.currentTimeMillis());

        // If new video is selected, upload it
        if (videoUri != null) {
            StorageReference videoRef = MyApplication.get(this).getAdvertisementStorageReference()
                    .child(String.valueOf(mAdvertisement.getId()))
                    .child("video.mp4");

            UploadTask uploadTask = videoRef.putFile(videoUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                videoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    map.put("videoUrl", uri.toString());
                    updateAdvertisementInDatabase(map);
                }).addOnFailureListener(e -> {
                    showProgressDialog(false);
                    Toast.makeText(this, "Failed to get video URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                showProgressDialog(false);
                Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            // No new video, just update other fields
            updateAdvertisementInDatabase(map);
        }
    }

    private void updateAdvertisementInDatabase(Map<String, Object> map) {
        MyApplication.get(this).advertisementDatabaseReference()
                .child(String.valueOf(mAdvertisement.getId()))
                .updateChildren(map, new com.google.firebase.database.DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError error, @NonNull com.google.firebase.database.DatabaseReference ref) {
                        showProgressDialog(false);
                        GlobalFunction.hideSoftKeyboard(AdminAddAdvertisementActivity.this);
                        if (error == null) {
                            Toast.makeText(AdminAddAdvertisementActivity.this,
                                    getString(R.string.msg_ad_edit_success), Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AdminAddAdvertisementActivity.this,
                                    "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveAdvertisementToDatabase(long advertisementId, String title, String videoUrl, String thumbnail) {
        Advertisement advertisement = new Advertisement(advertisementId, title, videoUrl);
        advertisement.setThumbnailUrl(thumbnail);
        advertisement.setActive(binding.chbActive.isChecked());

        MyApplication.get(this).advertisementDatabaseReference()
                .child(String.valueOf(advertisementId))
                .setValue(advertisement, new com.google.firebase.database.DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError error, @NonNull com.google.firebase.database.DatabaseReference ref) {
                        showProgressDialog(false);
                        binding.edtTitle.setText("");
                        binding.edtThumbnail.setText("");
                        binding.tvVideoName.setText("No video selected");
                        binding.chbActive.setChecked(true);
                        videoUri = null;
                        GlobalFunction.hideSoftKeyboard(AdminAddAdvertisementActivity.this);
                        if (error == null) {
                            Toast.makeText(AdminAddAdvertisementActivity.this,
                                    getString(R.string.msg_ad_upload_success), Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AdminAddAdvertisementActivity.this,
                                    "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}




