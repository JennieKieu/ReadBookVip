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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
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
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import jp.wasabeef.richeditor.RichEditor;

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
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    
    // Inline chapters
    private List<Chapter> inlineChapterList;
    private InlineChapterAdapter inlineChapterAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAddBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initImagePicker();
        loadDataIntent();
        initToolbar();
        initView();
        initInlineChapters();

        apiService = ApiClient.getApiService();
        
        binding.btnSelectCover.setOnClickListener(v -> selectCoverImage());
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
                            binding.tvCoverFileName.setText(fileName != null ? fileName : "Đã chọn ảnh");
                            
                            // Show preview
                            binding.imgCoverPreview.setVisibility(View.VISIBLE);
                            Glide.with(this)
                                    .load(coverImageUri)
                                    .into(binding.imgCoverPreview);
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
        imagePickerLauncher.launch(Intent.createChooser(intent, "Chọn ảnh bìa"));
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
        book.setCategoryId(bookText.getCategoryId());
        book.setCategoryName(bookText.getCategoryName());
        book.setFeatured(bookText.isFeatured());
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
            binding.edtBanner.setText(mBookText.getBanner());
            binding.edtDescription.setText(mBookText.getDescription());
            binding.edtTags.setText(mBookText.getTags());
            binding.chbFeatured.setChecked(mBookText.isFeatured());
            
            // Load existing cover image (base64 or URL)
            if (mBookText.getImage() != null && !mBookText.getImage().isEmpty()) {
                binding.tvCoverFileName.setText("Ảnh hiện tại");
                binding.imgCoverPreview.setVisibility(View.VISIBLE);
                ImageUtils.loadImage(binding.imgCoverPreview, mBookText.getImage());
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
        statusList.add(new SelectObject(0, getString(R.string.status_ongoing))); // "Đang viết"
        statusList.add(new SelectObject(1, getString(R.string.status_completed))); // "Hoàn thành"
        
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
                    inlineChapterAdapter.updateData(inlineChapterList);
                    updateChapterListVisibility();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Chapter>> call, @NonNull Throwable t) {
                showProgressDialog(false);
                Toast.makeText(AdminAddBookActivity.this, 
                        "Lỗi khi tải danh sách chương: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        // Show dialog to add chapter
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_chapter_inline, null);
        
        EditText edtChapterNumber = dialogView.findViewById(R.id.edt_chapter_number);
        EditText edtChapterTitle = dialogView.findViewById(R.id.edt_chapter_title);
        RichEditor edtChapterContent = dialogView.findViewById(R.id.editor_chapter_content);
        
        // Set next chapter number
        int nextChapterNumber = inlineChapterList.size() + 1;
        edtChapterNumber.setText(String.valueOf(nextChapterNumber));
        
        builder.setView(dialogView)
                .setTitle("Thêm chương mới")
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String chapterNumberStr = edtChapterNumber.getText().toString().trim();
                    String chapterTitle = edtChapterTitle.getText().toString().trim();
                    String chapterContent = edtChapterContent.getHtml();
                    
                    if (chapterNumberStr.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập số chương", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    if (chapterContent == null || chapterContent.trim().isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập nội dung chương", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    int chapterNumber = Integer.parseInt(chapterNumberStr);
                    
                    Chapter newChapter = new Chapter();
                    newChapter.setChapterNumber(chapterNumber);
                    newChapter.setTitle(chapterTitle);
                    newChapter.setContent(chapterContent);
                    
                    // If updating and book exists, save to API immediately
                    if (isUpdate && mBookText != null) {
                        newChapter.setBookId(mBookText.getId());
                        saveChapterToApi(newChapter, -1); // -1 means add new
                    } else {
                        // Otherwise, add to temp list
                        inlineChapterList.add(newChapter);
                        inlineChapterAdapter.updateData(inlineChapterList);
                        updateChapterListVisibility();
                    }
                })
                .setNegativeButton("Hủy", null)
                .create()
                .show();
    }

    private void editInlineChapter(Chapter chapter, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_chapter_inline, null);
        
        EditText edtChapterNumber = dialogView.findViewById(R.id.edt_chapter_number);
        EditText edtChapterTitle = dialogView.findViewById(R.id.edt_chapter_title);
        RichEditor edtChapterContent = dialogView.findViewById(R.id.editor_chapter_content);
        
        // Fill existing data
        edtChapterNumber.setText(String.valueOf(chapter.getChapterNumber()));
        edtChapterTitle.setText(chapter.getTitle());
        edtChapterContent.setHtml(chapter.getContent());
        
        builder.setView(dialogView)
                .setTitle("Sửa chương")
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    String chapterNumberStr = edtChapterNumber.getText().toString().trim();
                    String chapterTitle = edtChapterTitle.getText().toString().trim();
                    String chapterContent = edtChapterContent.getHtml();
                    
                    if (chapterNumberStr.isEmpty() || chapterContent == null || chapterContent.trim().isEmpty()) {
                        Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    chapter.setChapterNumber(Integer.parseInt(chapterNumberStr));
                    chapter.setTitle(chapterTitle);
                    chapter.setContent(chapterContent);
                    
                    // If chapter has ID, update via API
                    if (chapter.getId() > 0) {
                        saveChapterToApi(chapter, position);
                    } else {
                        inlineChapterAdapter.updateData(inlineChapterList);
                    }
                })
                .setNegativeButton("Hủy", null)
                .create()
                .show();
    }

    private void deleteInlineChapter(Chapter chapter, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa chương này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
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
                .setNegativeButton("Hủy", null)
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
                        Toast.makeText(AdminAddBookActivity.this, "Cập nhật chương thành công", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Chapter> call, @NonNull Throwable t) {
                    showProgressDialog(false);
                    Toast.makeText(AdminAddBookActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(AdminAddBookActivity.this, "Thêm chương thành công", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Chapter> call, @NonNull Throwable t) {
                    showProgressDialog(false);
                    Toast.makeText(AdminAddBookActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(AdminAddBookActivity.this, "Xóa chương thành công", Toast.LENGTH_SHORT).show();
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
        String strBanner = binding.edtBanner.getText().toString().trim();
        String strDescription = binding.edtDescription.getText().toString().trim();
        String strTags = binding.edtTags.getText().toString().trim();

        if (StringUtil.isEmpty(strName)) {
            Toast.makeText(this, getString(R.string.msg_input_name_require), Toast.LENGTH_SHORT).show();
            return;
        }

        // Cover image is required only for new books (unless already has image URL)
        if (!isUpdate && coverImageUri == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh bìa", Toast.LENGTH_SHORT).show();
            return;
        }

        if (StringUtil.isEmpty(strBanner)) {
            Toast.makeText(this, getString(R.string.msg_input_banner_require), Toast.LENGTH_SHORT).show();
            return;
        }

        if (mCategorySelected == null) {
            Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mStatusSelected == null) {
            Toast.makeText(this, "Vui lòng chọn trạng thái", Toast.LENGTH_SHORT).show();
            return;
        }

        // If has new cover image, convert to base64
        if (coverImageUri != null) {
            convertImageToBase64AndSaveBook(strName, strBanner, strDescription, strTags);
        } else {
            // No new image, just save book
            saveBookToApi(strName, mBookText != null ? mBookText.getImage() : null, strBanner, strDescription, strTags);
        }
    }

    private void convertImageToBase64AndSaveBook(String title, String banner, String description, String tags) {
        showProgressDialog(true);
        
        try {
            // Read image from URI
            InputStream inputStream = getContentResolver().openInputStream(coverImageUri);
            if (inputStream == null) {
                showProgressDialog(false);
                Toast.makeText(this, "Không thể đọc file ảnh", Toast.LENGTH_SHORT).show();
                return;
            }

            // Decode to Bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            
            if (bitmap == null) {
                showProgressDialog(false);
                Toast.makeText(this, "Không thể decode ảnh", Toast.LENGTH_SHORT).show();
                return;
            }

            // Resize image to reduce size (max 800px width)
            int maxWidth = 800;
            if (bitmap.getWidth() > maxWidth) {
                int newHeight = (int) ((float) maxWidth / bitmap.getWidth() * bitmap.getHeight());
                bitmap = Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true);
            }

            // Convert to base64
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream); // 80% quality
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String base64Image = "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP);
            
            byteArrayOutputStream.close();
            
            // Save book with base64 image
            saveBookToApi(title, base64Image, banner, description, tags);
            
        } catch (Exception e) {
            showProgressDialog(false);
            Toast.makeText(this, "Lỗi khi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void saveBookToApi(String title, String imageBase64, String banner, String description, String tags) {
        BookText bookText = new BookText();
        bookText.setTitle(title);
        bookText.setImage(imageBase64); // Base64 string hoặc URL cũ
        bookText.setBanner(banner);
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
                                "Lỗi khi cập nhật sách", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<BookText> call, @NonNull Throwable t) {
                    showProgressDialog(false);
                    Toast.makeText(AdminAddBookActivity.this,
                            "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                                "Lỗi khi thêm sách", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<BookText> call, @NonNull Throwable t) {
                    showProgressDialog(false);
                    Toast.makeText(AdminAddBookActivity.this,
                            "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Thêm sách và chương thành công", Toast.LENGTH_SHORT).show();
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
                            "Lỗi khi lưu chương " + (index + 1), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Chapter> call, @NonNull Throwable t) {
                showProgressDialog(false);
                Toast.makeText(AdminAddBookActivity.this,
                        "Lỗi kết nối khi lưu chương: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
