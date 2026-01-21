package com.example.book.activity.admin;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.book.R;
import com.example.book.activity.BaseActivity;
import com.example.book.api.ApiClient;
import com.example.book.api.BookApiService;
import com.example.book.constant.Constant;
import com.example.book.databinding.ActivityAdminAddChapterBinding;
import com.example.book.model.BookText;
import com.example.book.model.Chapter;
import com.example.book.utils.StringUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminAddChapterActivity extends BaseActivity {

    private ActivityAdminAddChapterBinding binding;
    private BookText mBook;
    private Chapter mChapter;
    private boolean isUpdate;
    private BookApiService apiService;
    private int suggestedChapterNumber = 1; // Default to 1

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAddChapterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadDataIntent();
        initToolbar();
        initView();
    }

    private void loadDataIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mBook = (BookText) bundle.getSerializable(Constant.OBJECT_BOOK);
            if (bundle.containsKey("chapter")) {
                isUpdate = true;
                mChapter = (Chapter) bundle.getSerializable("chapter");
            }
            if (bundle.containsKey("suggested_chapter_number")) {
                suggestedChapterNumber = bundle.getInt("suggested_chapter_number", 1);
            }
        }
    }

    private void initToolbar() {
        binding.layoutToolbar.imgToolbar.setOnClickListener(view -> finish());
        if (isUpdate) {
            binding.layoutToolbar.tvToolbarTitle.setText("Sửa chương");
            binding.btnSaveChapter.setText("Cập nhật");
        } else {
            binding.layoutToolbar.tvToolbarTitle.setText("Thêm chương");
            binding.btnSaveChapter.setText("Thêm");
        }
    }

    private void initView() {
        apiService = ApiClient.getInstance().getBookApiService();

        // Setup RichEditor
        binding.richEditor.setPlaceholder("Bắt đầu viết nội dung chương của bạn...");
        binding.richEditor.setEditorFontSize(16);
        binding.richEditor.setEditorFontColor(getResources().getColor(R.color.colorPrimaryDark, null));
        binding.richEditor.setPadding(10, 10, 10, 10);

        // Setup formatting toolbar
        setupFormattingToolbar();

        if (isUpdate && mChapter != null) {
            // Edit existing chapter
            binding.edtChapterNumber.setText(String.valueOf(mChapter.getChapterNumber()));
            binding.edtChapterTitle.setText(mChapter.getTitle());
            binding.richEditor.setHtml(mChapter.getContent());
        } else {
            // Add new chapter - auto-fill chapter number (but allow editing)
            binding.edtChapterNumber.setText(String.valueOf(suggestedChapterNumber));
        }

        binding.btnSaveChapter.setOnClickListener(v -> saveOrUpdateChapter());
    }

    private void setupFormattingToolbar() {
        // Bold
        binding.btnBold.setOnClickListener(v -> binding.richEditor.setBold());
        
        // Italic
        binding.btnItalic.setOnClickListener(v -> binding.richEditor.setItalic());
        
        // Underline
        binding.btnUnderline.setOnClickListener(v -> binding.richEditor.setUnderline());
        
        // Heading 1
        binding.btnH1.setOnClickListener(v -> binding.richEditor.setHeading(1));
        
        // Heading 2
        binding.btnH2.setOnClickListener(v -> binding.richEditor.setHeading(2));
        
        // Bullet list
        binding.btnBullet.setOnClickListener(v -> binding.richEditor.setBullets());
        
        // Quote
        binding.btnQuote.setOnClickListener(v -> binding.richEditor.setBlockquote());
    }

    private void saveOrUpdateChapter() {
        if (mBook == null) {
            Toast.makeText(this, "Lỗi: Không có thông tin sách", Toast.LENGTH_SHORT).show();
            return;
        }

        String strChapterNumber = binding.edtChapterNumber.getText().toString().trim();
        String strTitle = binding.edtChapterTitle.getText().toString().trim();
        String strContent = binding.richEditor.getHtml();

        if (StringUtil.isEmpty(strChapterNumber)) {
            Toast.makeText(this, "Vui lòng nhập số chương", Toast.LENGTH_SHORT).show();
            return;
        }

        if (StringUtil.isEmpty(strContent)) {
            Toast.makeText(this, "Vui lòng nhập nội dung chương", Toast.LENGTH_SHORT).show();
            return;
        }

        int chapterNumber;
        try {
            chapterNumber = Integer.parseInt(strChapterNumber);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số chương không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgressDialog(true);

        if (isUpdate && mChapter != null) {
            // Update chapter
            mChapter.setChapterNumber(chapterNumber);
            mChapter.setTitle(strTitle);
            mChapter.setContent(strContent);

            apiService.updateChapter(mChapter.getId(), mChapter).enqueue(new Callback<Chapter>() {
                @Override
                public void onResponse(@NonNull Call<Chapter> call, @NonNull Response<Chapter> response) {
                    showProgressDialog(false);
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminAddChapterActivity.this,
                                "Cập nhật chương thành công", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(AdminAddChapterActivity.this,
                                "Lỗi khi cập nhật chương", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Chapter> call, @NonNull Throwable t) {
                    showProgressDialog(false);
                    Toast.makeText(AdminAddChapterActivity.this,
                            "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Create new chapter
            Chapter newChapter = new Chapter();
            newChapter.setBookId(mBook.getId());
            newChapter.setChapterNumber(chapterNumber);
            newChapter.setTitle(strTitle);
            newChapter.setContent(strContent);

            apiService.createChapter(newChapter).enqueue(new Callback<Chapter>() {
                @Override
                public void onResponse(@NonNull Call<Chapter> call, @NonNull Response<Chapter> response) {
                    showProgressDialog(false);
                    if (response.isSuccessful()) {
                        Toast.makeText(AdminAddChapterActivity.this,
                                "Thêm chương thành công", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(AdminAddChapterActivity.this,
                                "Lỗi khi thêm chương", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Chapter> call, @NonNull Throwable t) {
                    showProgressDialog(false);
                    Toast.makeText(AdminAddChapterActivity.this,
                            "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}

