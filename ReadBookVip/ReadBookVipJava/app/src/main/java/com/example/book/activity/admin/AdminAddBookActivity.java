package com.example.book.activity.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.book.MyApplication;
import com.example.book.R;
import com.example.book.activity.BaseActivity;
import com.example.book.adapter.admin.AdminSelectAdapter;
import com.example.book.adapter.admin.InlineChapterAdapter;
import com.example.book.api.ApiClient;
import com.example.book.api.BookApiService;
import com.example.book.constant.Constant;
import com.example.book.constant.GlobalFunction;
import com.example.book.databinding.ActivityAdminAddBookBinding;
import com.example.book.model.Book;
import com.example.book.model.BookText;
import com.example.book.model.Category;
import com.example.book.model.Chapter;
import com.example.book.model.SelectObject;
import com.example.book.utils.ImageUtils;
import com.example.book.utils.StringUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminAddBookActivity extends BaseActivity {

    private ActivityAdminAddBookBinding binding;
    private boolean isUpdate;
    private Book mBook; // Keep for compatibility
    private BookText mBookText;
    private SelectObject mCategorySelected;
    private SelectObject mStatusSelected;
    private BookApiService apiService;
    
    // Image upload
    private Uri coverImageUri;
    private Uri bannerImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> bannerPickerLauncher;
    private ActivityResultLauncher<Intent> addChapterLauncher;
    
    // Inline chapters
    private List<Chapter> inlineChapterList;
    private InlineChapterAdapter inlineChapterAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAddBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize apiService first (needed by other methods)
        apiService = ApiClient.getInstance().getBookApiService();
        
        initImagePicker();
        loadDataIntent();
        initToolbar();
        initView();
        initInlineChapters();
        
        binding.btnSelectCover.setOnClickListener(v -> selectCoverImage());
        binding.btnSelectBanner.setOnClickListener(v -> selectBannerImage());
        binding.btnAddChapter.setOnClickListener(v -> addInlineChapter());
        binding.btnAddOrEdit.setOnClickListener(v -> validateAndSaveBook());
    }

    private void initImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        coverImageUri = result.getData().getData();
                        if (coverImageUri != null) {
                            // Grant persistent permission
                            try {
                                int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                                getContentResolver().takePersistableUriPermission(coverImageUri, flags);
                            } catch (SecurityException e) {
                                e.printStackTrace();
                            }
                            
                            // Display file name
                            String fileName = getFileName(coverImageUri);
                            binding.tvCoverFileName.setText(fileName != null ? fileName : "Image selected");
                            
                            // Show preview
                            binding.imgCoverPreview.setVisibility(View.VISIBLE);
                            Glide.with(this)
                                    .load(coverImageUri)
                                    .into(binding.imgCoverPreview);
                        }
                    }
                });
        
        bannerPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        bannerImageUri = result.getData().getData();
                        if (bannerImageUri != null) {
                            // Grant persistent permission
                            try {
                                int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                                getContentResolver().takePersistableUriPermission(bannerImageUri, flags);
                            } catch (SecurityException e) {
                                e.printStackTrace();
                            }
                            
                            // Display file name
                            String fileName = getFileName(bannerImageUri);
                            binding.tvBannerFileName.setText(fileName != null ? fileName : "Banner selected");
                            
                            // Show preview
                            binding.imgBannerPreview.setVisibility(View.VISIBLE);
                            Glide.with(this)
                                    .load(bannerImageUri)
                                    .into(binding.imgBannerPreview);
                        }
                    }
                });
        
        // Add/Edit chapter launcher
        addChapterLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Refresh chapter list after add/edit
                        if (isUpdate && mBookText != null) {
                            loadChaptersForBook(mBookText.getId());
                        }
                    }
                });
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void selectCoverImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select cover image"));
    }

    private void selectBannerImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        bannerPickerLauncher.launch(Intent.createChooser(intent, "Select banner"));
    }

    private void loadDataIntent() {
        Bundle bundleReceived = getIntent().getExtras();
        if (bundleReceived != null) {
            isUpdate = true;
            Object bookObj = bundleReceived.get(Constant.OBJECT_BOOK);
            if (bookObj instanceof BookText) {
                mBookText = (BookText) bookObj;
                // Create Book for compatibility
                mBook = convertBookTextToBook(mBookText);
            } else if (bookObj instanceof Book) {
                mBook = (Book) bookObj;
                // Convert to BookText
                mBookText = convertBookToBookText(mBook);
            }
        }
    }

    private Book convertBookTextToBook(BookText bookText) {
        Book book = new Book();
        book.setId(bookText.getId());
        book.setTitle(bookText.getTitle());
        book.setImage(bookText.getImage());
        book.setBanner(bookText.getBanner());
        book.setCategoryId(bookText.getCategoryId() != null ? bookText.getCategoryId() : 0);
        book.setCategoryName(bookText.getCategoryName());
        book.setFeatured(bookText.isFeatured());
        book.setDescription(bookText.getDescription()); // Include description
        return book;
    }

    private BookText convertBookToBookText(Book book) {
        BookText bookText = new BookText();
        bookText.setId(book.getId());
        bookText.setTitle(book.getTitle());
        bookText.setImage(book.getImage());
        bookText.setBanner(book.getBanner());
        bookText.setCategoryId(book.getCategoryId());
        bookText.setCategoryName(book.getCategoryName());
        bookText.setFeatured(book.isFeatured());
        bookText.setDescription(book.getDescription()); // Include description
        return bookText;
    }

    private void initToolbar() {
        binding.layoutToolbar.imgToolbar.setOnClickListener(view -> finish());
    }

    private void initView() {
        if (isUpdate) {
            binding.layoutToolbar.tvToolbarTitle.setText(getString(R.string.label_update_book));
            binding.btnAddOrEdit.setText(getString(R.string.action_edit));

            binding.edtName.setText(mBookText.getTitle());
            binding.edtDescription.setText(mBookText.getDescription());
            binding.edtTags.setText(mBookText.getTags());
            binding.chbFeatured.setChecked(mBookText.isFeatured());
            
            // Load existing cover image (base64 or URL)
            if (mBookText.getImage() != null && !mBookText.getImage().isEmpty()) {
                binding.tvCoverFileName.setText("Current image");
                binding.imgCoverPreview.setVisibility(View.VISIBLE);
                ImageUtils.loadImage(binding.imgCoverPreview, mBookText.getImage());
            }
            
            // Load existing banner image (base64 or URL)
            if (mBookText.getBanner() != null && !mBookText.getBanner().isEmpty()) {
                binding.tvBannerFileName.setText("Current banner");
                binding.imgBannerPreview.setVisibility(View.VISIBLE);
                ImageUtils.loadImage(binding.imgBannerPreview, mBookText.getBanner());
            }
        } else {
            binding.layoutToolbar.tvToolbarTitle.setText(getString(R.string.label_add_book));
            binding.btnAddOrEdit.setText(getString(R.string.action_add));
        }
        
        loadListCategory();
        loadStatusSpinner();
    }

    private void loadListCategory() {
        MyApplication.get(this).categoryDatabaseReference()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<SelectObject> list = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Category category = dataSnapshot.getValue(Category.class);
                            if (category == null) return;
                            list.add(0, new SelectObject(category.getId(), category.getName()));
                        }
                        AdminSelectAdapter adapter = new AdminSelectAdapter(AdminAddBookActivity.this,
                                R.layout.item_choose_option, list);
                        binding.spnCategory.setAdapter(adapter);
                        binding.spnCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                mCategorySelected = adapter.getItem(position);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {}
                        });

                        if (mBookText != null && mBookText.getCategoryId() != null && mBookText.getCategoryId() > 0) {
                            binding.spnCategory.setSelection(getPositionSelected(list, mBookText.getCategoryId()));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadStatusSpinner() {
        List<SelectObject> statusList = new ArrayList<>();
        statusList.add(new SelectObject(0, getString(R.string.status_ongoing))); // "Ongoing"
        statusList.add(new SelectObject(1, getString(R.string.status_completed))); // "Completed"
        
        AdminSelectAdapter adapter = new AdminSelectAdapter(this, R.layout.item_choose_option, statusList);
        binding.spnStatus.setAdapter(adapter);
        binding.spnStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mStatusSelected = adapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Set current status if updating
        if (isUpdate && mBookText != null && mBookText.getStatus() != null) {
            int statusPosition = mBookText.getStatus().equals("completed") ? 1 : 0;
            binding.spnStatus.setSelection(statusPosition);
        }
    }

    private int getPositionSelected(List<SelectObject> list, long id) {
        int position = 0;
        for (int i = 0; i < list.size(); i++) {
            if (id == list.get(i).getId()) {
                position = i;
                break;
            }
        }
        return position;
    }

    private void initInlineChapters() {
        inlineChapterList = new ArrayList<>();
        inlineChapterAdapter = new InlineChapterAdapter(inlineChapterList, new InlineChapterAdapter.IClickListener() {
            @Override
            public void onClickEdit(Chapter chapter, int position) {
                editInlineChapter(chapter, position);
            }

            @Override
            public void onClickDelete(Chapter chapter, int position) {
                deleteInlineChapter(chapter, position);
            }
        });
        
        binding.rcvChapters.setLayoutManager(new LinearLayoutManager(this));
        binding.rcvChapters.setAdapter(inlineChapterAdapter);
        
        // Load chapters if editing existing book
        if (isUpdate && mBookText != null) {
            loadChaptersForBook(mBookText.getId());
            binding.btnAddChapter.setVisibility(View.VISIBLE);
        } else {
            // Hide add chapter button for new books
            binding.btnAddChapter.setVisibility(View.GONE);
        }
        
        updateChapterListVisibility();
    }

    private void loadChaptersForBook(long bookId) {
        showProgressDialog(true);
        apiService.getChaptersByBook(bookId).enqueue(new Callback<List<Chapter>>() {
            @Override
            public void onResponse(@NonNull Call<List<Chapter>> call, @NonNull Response<List<Chapter>> response) {
                showProgressDialog(false);
                if (response.isSuccessful() && response.body() != null) {
                    inlineChapterList.clear();
                    inlineChapterList.addAll(response.body());
                    
                    // Sort chapters by chapter number (ascending)
                    Collections.sort(inlineChapterList, new Comparator<Chapter>() {
                        @Override
                        public int compare(Chapter c1, Chapter c2) {
                            return Integer.compare(c1.getChapterNumber(), c2.getChapterNumber());
                        }
                    });
                    
                    inlineChapterAdapter.updateData(inlineChapterList);
                    updateChapterListVisibility();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Chapter>> call, @NonNull Throwable t) {
                showProgressDialog(false);
                Toast.makeText(AdminAddBookActivity.this, 
                        "Error loading chapters: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateChapterListVisibility() {
        if (inlineChapterList.isEmpty()) {
            binding.tvNoChapters.setVisibility(View.VISIBLE);
            binding.rcvChapters.setVisibility(View.GONE);
        } else {
            binding.tvNoChapters.setVisibility(View.GONE);
            binding.rcvChapters.setVisibility(View.VISIBLE);
        }
    }

    private void addInlineChapter() {
        // Check if book is saved first
        if (!isUpdate || mBookText == null) {
            Toast.makeText(this, "Please save the book before adding chapters", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Calculate next chapter number (max + 1)
        int nextChapterNumber = 1; // Default: start from 1
        if (!inlineChapterList.isEmpty()) {
            // Find max chapter number
            int maxChapterNumber = 0;
            for (Chapter chapter : inlineChapterList) {
                if (chapter.getChapterNumber() > maxChapterNumber) {
                    maxChapterNumber = chapter.getChapterNumber();
                }
            }
            nextChapterNumber = maxChapterNumber + 1;
        }
        
        // Open AdminAddChapterActivity
        Intent intent = new Intent(this, AdminAddChapterActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.OBJECT_BOOK, mBookText);
        bundle.putInt("suggested_chapter_number", nextChapterNumber);
        intent.putExtras(bundle);
        addChapterLauncher.launch(intent);
    }

    private void editInlineChapter(Chapter chapter, int position) {
        // Open AdminAddChapterActivity for editing
        Intent intent = new Intent(this, AdminAddChapterActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.OBJECT_BOOK, mBookText);
        bundle.putSerializable("chapter", chapter);
        intent.putExtras(bundle);
        addChapterLauncher.launch(intent);
    }

    private void deleteInlineChapter(Chapter chapter, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this chapter?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // If chapter has ID (saved to DB), delete via API
                    if (chapter.getId() > 0) {
                        deleteChapterFromApi(chapter.getId(), position);
                    } else {
                        // Just remove from list
                        inlineChapterList.remove(position);
                        inlineChapterAdapter.updateData(inlineChapterList);
                        updateChapterListVisibility();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveChapterToApi(Chapter chapter, int position) {
        showProgressDialog(true);
        
        if (chapter.getId() > 0) {
            // Update existing chapter
            apiService.updateChapter(chapter.getId(), chapter).enqueue(new Callback<Chapter>() {
                @Override
                public void onResponse(@NonNull Call<Chapter> call, @NonNull Response<Chapter> response) {
                    showProgressDialog(false);
                    if (response.isSuccessful() && response.body() != null) {
                        if (position >= 0) {
                            inlineChapterList.set(position, response.body());
                            inlineChapterAdapter.updateData(inlineChapterList);
                        }
                        Toast.makeText(AdminAddBookActivity.this, "Chapter updated successfully", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Chapter> call, @NonNull Throwable t) {
                    showProgressDialog(false);
                    Toast.makeText(AdminAddBookActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Create new chapter
            apiService.createChapter(chapter).enqueue(new Callback<Chapter>() {
                @Override
                public void onResponse(@NonNull Call<Chapter> call, @NonNull Response<Chapter> response) {
                    showProgressDialog(false);
                    if (response.isSuccessful() && response.body() != null) {
                        inlineChapterList.add(response.body());
                        inlineChapterAdapter.updateData(inlineChapterList);
                        updateChapterListVisibility();
                        Toast.makeText(AdminAddBookActivity.this, "Chapter added successfully", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Chapter> call, @NonNull Throwable t) {
                    showProgressDialog(false);
                    Toast.makeText(AdminAddBookActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void deleteChapterFromApi(long chapterId, int position) {
        showProgressDialog(true);
        apiService.deleteChapter(chapterId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                showProgressDialog(false);
                if (response.isSuccessful()) {
                    inlineChapterList.remove(position);
                    inlineChapterAdapter.updateData(inlineChapterList);
                    updateChapterListVisibility();
                    Toast.makeText(AdminAddBookActivity.this, "Chapter deleted successfully", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                showProgressDialog(false);
                Toast.makeText(AdminAddBookActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validateAndSaveBook() {
        String strName = binding.edtName.getText().toString().trim();
        String strDescription = binding.edtDescription.getText().toString().trim();
        String strTags = binding.edtTags.getText().toString().trim();

        if (StringUtil.isEmpty(strName)) {
            Toast.makeText(this, getString(R.string.msg_input_name_require), Toast.LENGTH_SHORT).show();
            return;
        }

        // Cover image is required only for new books (unless already has image URL)
        if (!isUpdate && coverImageUri == null) {
            Toast.makeText(this, "Please select cover image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Banner image is required only for new books (unless already has banner)
        if (!isUpdate && bannerImageUri == null) {
            Toast.makeText(this, "Please select banner", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mCategorySelected == null) {
            Toast.makeText(this, "Please select category", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mStatusSelected == null) {
            Toast.makeText(this, "Please select status", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert images to base64 if needed
        if (coverImageUri != null || bannerImageUri != null) {
            convertImagesToBase64AndSaveBook(strName, strDescription, strTags);
        } else {
            // No new images, just save book with existing images
            String existingImage = mBookText != null ? mBookText.getImage() : null;
            String existingBanner = mBookText != null ? mBookText.getBanner() : null;
            saveBookToApi(strName, existingImage, existingBanner, strDescription, strTags);
        }
    }

    private void convertImagesToBase64AndSaveBook(String title, String description, String tags) {
        showProgressDialog(true);
        
        final String finalTitle = title;
        final String finalDescription = description;
        final String finalTags = tags;
        
        new Thread(() -> {
            try {
                String base64Image = null;
                String base64Banner = null;
                
                // Convert cover image if selected
                if (coverImageUri != null) {
                    base64Image = convertImageUriToBase64(coverImageUri, 800);
                } else if (mBookText != null && mBookText.getImage() != null) {
                    base64Image = mBookText.getImage(); // Keep existing
                }
                
                // Convert banner image if selected
                if (bannerImageUri != null) {
                    base64Banner = convertImageUriToBase64(bannerImageUri, 1200); // Banner can be wider
                } else if (mBookText != null && mBookText.getBanner() != null) {
                    base64Banner = mBookText.getBanner(); // Keep existing
                }
                
                // Create final variables for lambda
                final String finalBase64Image = base64Image;
                final String finalBase64Banner = base64Banner;
                
                // Save on main thread
                runOnUiThread(() -> {
                    showProgressDialog(false);
                    saveBookToApi(finalTitle, finalBase64Image, finalBase64Banner, finalDescription, finalTags);
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showProgressDialog(false);
                    Toast.makeText(this, "Error processing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
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

        // Resize image to reduce size
        if (bitmap.getWidth() > maxWidth) {
            int newHeight = (int) ((float) maxWidth / bitmap.getWidth() * bitmap.getHeight());
            bitmap = Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true);
        }

        // Convert to base64
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream); // 80% quality
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String base64 = "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP);
        
        byteArrayOutputStream.close();
        return base64;
    }

    private void saveBookToApi(String title, String imageBase64, String bannerBase64, String description, String tags) {
        BookText bookText = new BookText();
        bookText.setTitle(title);
        bookText.setImage(imageBase64); // Base64 string hoặc URL cũ
        bookText.setBanner(bannerBase64); // Base64 string hoặc URL cũ
        bookText.setDescription(description);
        bookText.setTags(tags);
        bookText.setStatus(mStatusSelected.getId() == 1 ? "completed" : "ongoing");
        bookText.setFeatured(binding.chbFeatured.isChecked());
        bookText.setCategoryId(mCategorySelected.getId());
        bookText.setCategoryName(mCategorySelected.getName());

        showProgressDialog(true);

        if (isUpdate && mBookText != null) {
            // Update existing book
            bookText.setId(mBookText.getId());
            apiService.updateBook(mBookText.getId(), bookText).enqueue(new Callback<BookText>() {
                @Override
                public void onResponse(@NonNull Call<BookText> call, @NonNull Response<BookText> response) {
                    showProgressDialog(false);
                    if (response.isSuccessful()) {
                        GlobalFunction.hideSoftKeyboard(AdminAddBookActivity.this);
                        Toast.makeText(AdminAddBookActivity.this,
                                getString(R.string.msg_edit_book_success), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AdminAddBookActivity.this,
                                "Error updating book", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<BookText> call, @NonNull Throwable t) {
                    showProgressDialog(false);
                    Toast.makeText(AdminAddBookActivity.this,
                            "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Create new book
            apiService.createBook(bookText).enqueue(new Callback<BookText>() {
                @Override
                public void onResponse(@NonNull Call<BookText> call, @NonNull Response<BookText> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        BookText createdBook = response.body();
                        // Save chapters if any
                        if (!inlineChapterList.isEmpty()) {
                            saveChaptersForNewBook(createdBook.getId());
                        } else {
                            showProgressDialog(false);
                            Toast.makeText(AdminAddBookActivity.this,
                                    getString(R.string.msg_add_book_success), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        showProgressDialog(false);
                        Toast.makeText(AdminAddBookActivity.this,
                                "Error adding book", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<BookText> call, @NonNull Throwable t) {
                    showProgressDialog(false);
                    Toast.makeText(AdminAddBookActivity.this,
                            "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveChaptersForNewBook(long bookId) {
        // Save all chapters sequentially
        saveChapterSequentially(bookId, 0);
    }

    private void saveChapterSequentially(long bookId, int index) {
        if (index >= inlineChapterList.size()) {
            // All chapters saved
            showProgressDialog(false);
            Toast.makeText(this, "Book and chapters added successfully", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Chapter chapter = inlineChapterList.get(index);
        chapter.setBookId(bookId);
        
        apiService.createChapter(chapter).enqueue(new Callback<Chapter>() {
            @Override
            public void onResponse(@NonNull Call<Chapter> call, @NonNull Response<Chapter> response) {
                if (response.isSuccessful()) {
                    // Save next chapter
                    saveChapterSequentially(bookId, index + 1);
                } else {
                    showProgressDialog(false);
                    Toast.makeText(AdminAddBookActivity.this,
                            "Error saving chapter " + (index + 1), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Chapter> call, @NonNull Throwable t) {
                showProgressDialog(false);
                Toast.makeText(AdminAddBookActivity.this,
                        "Connection error while saving chapter: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
