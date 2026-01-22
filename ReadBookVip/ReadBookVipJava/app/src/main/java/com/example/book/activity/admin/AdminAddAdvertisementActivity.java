package com.example.book.activity.admin;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.book.R;
import com.example.book.activity.BaseActivity;
import com.example.book.api.AdvertisementApiService;
import com.example.book.api.ApiClient;
import com.example.book.constant.Constant;
import com.example.book.databinding.ActivityAdminAddAdvertisementBinding;
import com.example.book.model.Advertisement;
import com.example.book.utils.StringUtil;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminAddAdvertisementActivity extends BaseActivity {

    private ActivityAdminAddAdvertisementBinding binding;
    private boolean isUpdate;
    private Advertisement mAdvertisement;
    private AdvertisementApiService apiService;
    private Uri thumbnailImageUri;
    private ActivityResultLauncher<Intent> thumbnailPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAddAdvertisementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getInstance().getAdvertisementApiService();
        
        initThumbnailPicker();
        loadDataIntent();
        initToolbar();
        initView();
        initListener();
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
                binding.edtVideoUrl.setText(mAdvertisement.getVideoUrl());
            }
            if (mAdvertisement.getUrl() != null) {
                binding.edtUrl.setText(mAdvertisement.getUrl());
            }
            if (mAdvertisement.getThumbnailUrl() != null) {
                String thumbnailUrl = mAdvertisement.getThumbnailUrl();
                // Check if it's base64 or URL
                if (thumbnailUrl.startsWith("data:image")) {
                    // Base64 image - show upload section with preview
                    binding.rbUploadImage.setChecked(true);
                    binding.layoutUploadImage.setVisibility(View.VISIBLE);
                    binding.layoutEnterUrl.setVisibility(View.GONE);
                    binding.imgThumbnailPreview.setVisibility(View.VISIBLE);
                    com.example.book.utils.ImageUtils.loadImage(binding.imgThumbnailPreview, thumbnailUrl);
                    binding.edtThumbnail.setText("");
                } else {
                    // URL - show URL section
                    binding.rbEnterUrl.setChecked(true);
                    binding.layoutUploadImage.setVisibility(View.GONE);
                    binding.layoutEnterUrl.setVisibility(View.VISIBLE);
                    binding.edtThumbnail.setText(thumbnailUrl);
                }
            } else {
                // No thumbnail - default to upload
                binding.rbUploadImage.setChecked(true);
                binding.layoutUploadImage.setVisibility(View.VISIBLE);
                binding.layoutEnterUrl.setVisibility(View.GONE);
            }
            binding.chbActive.setChecked(mAdvertisement.isActive());
        } else {
            binding.layoutToolbar.tvToolbarTitle.setText(getString(R.string.label_add_advertisement));
            binding.btnAddOrEdit.setText(getString(R.string.action_add));
            // Default to upload image
            binding.rbUploadImage.setChecked(true);
            binding.layoutUploadImage.setVisibility(View.VISIBLE);
            binding.layoutEnterUrl.setVisibility(View.GONE);
        }
    }

    private void initThumbnailPicker() {
        thumbnailPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        thumbnailImageUri = result.getData().getData();
                        if (thumbnailImageUri != null) {
                            // Grant persistent permission
                            try {
                                int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                                getContentResolver().takePersistableUriPermission(thumbnailImageUri, flags);
                            } catch (SecurityException e) {
                                android.util.Log.d("AdminAddAd", "Persistable permission: " + e.getMessage());
                            }
                            
                            // Show preview
                            binding.imgThumbnailPreview.setVisibility(View.VISIBLE);
                            Glide.with(this)
                                    .load(thumbnailImageUri)
                                    .into(binding.imgThumbnailPreview);
                            
                            Toast.makeText(this, "Image selected. Click Add/Update to save.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void initListener() {
        binding.btnAddOrEdit.setOnClickListener(v -> addOrEditAdvertisement());
        binding.btnSelectThumbnail.setOnClickListener(v -> selectThumbnailImage());
        
        // RadioButton listeners to switch between upload and URL
        binding.rbUploadImage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.layoutUploadImage.setVisibility(View.VISIBLE);
                binding.layoutEnterUrl.setVisibility(View.GONE);
                // Clear URL field when switching to upload
                binding.edtThumbnail.setText("");
                // Clear selected image when switching to URL
                thumbnailImageUri = null;
            }
        });
        
        binding.rbEnterUrl.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.layoutUploadImage.setVisibility(View.GONE);
                binding.layoutEnterUrl.setVisibility(View.VISIBLE);
                // Clear selected image when switching to URL
                thumbnailImageUri = null;
                binding.imgThumbnailPreview.setVisibility(View.GONE);
            }
        });
    }

    private void selectThumbnailImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        thumbnailPickerLauncher.launch(Intent.createChooser(intent, "Select thumbnail image"));
    }

    private void addOrEditAdvertisement() {
        String strTitle = binding.edtTitle.getText().toString().trim();
        String strVideoUrl = binding.edtVideoUrl.getText().toString().trim();
        String strUrl = binding.edtUrl.getText().toString().trim();
        String strThumbnail = binding.edtThumbnail.getText().toString().trim();

        if (StringUtil.isEmpty(strTitle)) {
            Toast.makeText(this, getString(R.string.msg_ad_title_require), Toast.LENGTH_SHORT).show();
            return;
        }

        if (StringUtil.isEmpty(strVideoUrl)) {
            Toast.makeText(this, getString(R.string.msg_ad_video_require), Toast.LENGTH_SHORT).show();
            return;
        }

        // Check which option is selected
        String thumbnail = null;
        if (binding.rbUploadImage.isChecked()) {
            // Upload image option selected
            if (thumbnailImageUri != null) {
                // Convert to base64
                convertThumbnailToBase64AndSave(strTitle, strVideoUrl, strUrl);
                return; // Exit early, will be handled in callback
            } else {
                // No image selected, use null (optional field)
                thumbnail = null;
            }
        } else {
            // Enter URL option selected
            thumbnail = strThumbnail.isEmpty() ? null : strThumbnail;
        }
        
        // Save with thumbnail (URL or null)
        if (isUpdate) {
            updateAdvertisement(strTitle, strVideoUrl, strUrl, thumbnail);
        } else {
            createAdvertisement(strTitle, strVideoUrl, strUrl, thumbnail);
        }
    }

    private void convertThumbnailToBase64AndSave(String title, String videoUrl, String url) {
        showProgressDialog(true);
        
        final String finalTitle = title;
        final String finalVideoUrl = videoUrl;
        final String finalUrl = url;
        
        new Thread(() -> {
            try {
                String base64Thumbnail = convertImageUriToBase64(thumbnailImageUri, 800);
                
                runOnUiThread(() -> {
                    showProgressDialog(false);
                    if (isUpdate) {
                        updateAdvertisement(finalTitle, finalVideoUrl, finalUrl, base64Thumbnail);
                    } else {
                        createAdvertisement(finalTitle, finalVideoUrl, finalUrl, base64Thumbnail);
                    }
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showProgressDialog(false);
                    Toast.makeText(this, "Error processing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    android.util.Log.e("AdminAddAd", "Error converting image", e);
                });
            }
        }).start();
    }
    
    private String convertImageUriToBase64(Uri imageUri, int maxWidth) throws Exception {
        // Read image from URI
        InputStream inputStream = getContentResolver().openInputStream(imageUri);
        if (inputStream == null) {
            throw new Exception("Cannot read image file");
        }

        // Decode to Bitmap
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();
        
        if (bitmap == null) {
            throw new Exception("Cannot decode image");
        }

        // Resize image to reduce size (smaller for base64 to fit in database)
        int targetWidth = Math.min(maxWidth, 400); // Max 400px width for base64
        if (bitmap.getWidth() > targetWidth) {
            int newHeight = (int) ((float) targetWidth / bitmap.getWidth() * bitmap.getHeight());
            bitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, newHeight, true);
        }

        // Convert to base64 with lower quality to reduce size
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int quality = 70; // Lower quality (70%) to reduce base64 size
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        
        // Check size - if still too large, reduce quality further
        int maxBase64Length = 800; // Leave some room for "data:image/jpeg;base64," prefix
        while (byteArray.length > maxBase64Length && quality > 30) {
            quality -= 10;
            byteArrayOutputStream.reset();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream);
            byteArray = byteArrayOutputStream.toByteArray();
        }
        
        String base64 = "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP);
        
        // Log size for debugging
        android.util.Log.d("AdminAddAd", "Base64 length: " + base64.length());
        
        byteArrayOutputStream.close();
        return base64;
    }

    private void createAdvertisement(String title, String videoUrl, String url, String thumbnail) {
        showProgressDialog(true);
        
        Advertisement advertisement = new Advertisement();
        advertisement.setTitle(title);
        advertisement.setVideoUrl(videoUrl);
        advertisement.setUrl(StringUtil.isEmpty(url) ? null : url);
        advertisement.setThumbnailUrl(StringUtil.isEmpty(thumbnail) ? null : thumbnail);
        advertisement.setActive(binding.chbActive.isChecked());
        // Don't set id, createdAt, updatedAt, viewCount - backend will handle these

        apiService.createAdvertisement(advertisement).enqueue(new Callback<Advertisement>() {
            @Override
            public void onResponse(@NonNull Call<Advertisement> call, @NonNull Response<Advertisement> response) {
                showProgressDialog(false);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(AdminAddAdvertisementActivity.this,
                            getString(R.string.msg_ad_upload_success), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMsg = "Unknown error";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();
                        } catch (Exception e) {
                            errorMsg = response.message();
                        }
                    } else {
                        errorMsg = response.message();
                    }
                    android.util.Log.e("AdminAddAd", "Response error: " + errorMsg);
                    Toast.makeText(AdminAddAdvertisementActivity.this,
                            "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Advertisement> call, @NonNull Throwable t) {
                showProgressDialog(false);
                String errorMessage = t.getMessage();
                if (t.getCause() != null) {
                    errorMessage = t.getCause().getMessage();
                }
                android.util.Log.e("AdminAddAd", "Error creating advertisement", t);
                Toast.makeText(AdminAddAdvertisementActivity.this,
                        "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateAdvertisement(String title, String videoUrl, String url, String thumbnail) {
        if (mAdvertisement == null) return;
        
        showProgressDialog(true);
        
        Advertisement advertisement = new Advertisement();
        advertisement.setTitle(title);
        advertisement.setVideoUrl(videoUrl);
        advertisement.setUrl(StringUtil.isEmpty(url) ? null : url);
        advertisement.setThumbnailUrl(StringUtil.isEmpty(thumbnail) ? null : thumbnail);
        advertisement.setActive(binding.chbActive.isChecked());
        // Don't set id, createdAt, updatedAt, viewCount - backend will handle these

        apiService.updateAdvertisement(mAdvertisement.getId(), advertisement).enqueue(new Callback<Advertisement>() {
            @Override
            public void onResponse(@NonNull Call<Advertisement> call, @NonNull Response<Advertisement> response) {
                showProgressDialog(false);
                if (response.isSuccessful()) {
                    Toast.makeText(AdminAddAdvertisementActivity.this,
                            getString(R.string.msg_ad_edit_success), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AdminAddAdvertisementActivity.this,
                            "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Advertisement> call, @NonNull Throwable t) {
                showProgressDialog(false);
                String errorMessage = t.getMessage();
                if (t.getCause() != null) {
                    errorMessage = t.getCause().getMessage();
                }
                android.util.Log.e("AdminAddAd", "Error creating advertisement", t);
                Toast.makeText(AdminAddAdvertisementActivity.this,
                        "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
