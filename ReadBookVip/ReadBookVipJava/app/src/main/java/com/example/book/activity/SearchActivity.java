package com.example.book.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.book.R;
import com.example.book.adapter.BookAdapter;
import com.example.book.api.ApiCallback;
import com.example.book.api.ApiClient;
import com.example.book.api.BookApiService;
import com.example.book.constant.GlobalFunction;
import com.example.book.databinding.ActivitySearchBinding;
import com.example.book.listener.IOnClickBookListener;
import com.example.book.model.Book;
import com.example.book.model.BookText;
import com.example.book.model.Category;
import com.example.book.repository.BookRepository;
import com.example.book.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends BaseActivity {

    private ActivitySearchBinding mBinding;
    private List<Book> mListBook;
    private List<Book> mListAllBooks; // Store all books for filtering
    private BookAdapter mBookAdapter;
    private BookApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        apiService = ApiClient.getInstance().getBookApiService();
        initToolbar();
        initUi();
        initListener();
        loadAllBooksFromAPI("");
    }

    private void initToolbar() {
        mBinding.layoutToolbar.imgToolbar.setOnClickListener(view -> finish());
        mBinding.layoutToolbar.tvToolbarTitle.setText(getString(R.string.label_search));
    }

    private void initUi() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        mBinding.rcvSearchResult.setLayoutManager(gridLayoutManager);
        mListBook = new ArrayList<>();
        mListAllBooks = new ArrayList<>();
        mBookAdapter = new BookAdapter(mListBook, new IOnClickBookListener() {
            @Override
            public void onClickItemBook(Book book) {
                GlobalFunction.goToBookDetail(SearchActivity.this, book);
            }

            @Override
            public void onClickCategoryOfBook(Category category) {
                GlobalFunction.goToBookByCategory(SearchActivity.this, category);
            }

            @Override
            public void onClickFavoriteBook(Book book, boolean favorite) {
                GlobalFunction.onClickFavoriteBook(SearchActivity.this, book, favorite);
            }
        });
        mBinding.rcvSearchResult.setAdapter(mBookAdapter);
    }

    private void initListener() {
        mBinding.edtSearchName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String strKey = s.toString().trim();
                filterBooks(strKey);
            }
        });

        mBinding.imgSearch.setOnClickListener(view -> searchBook());

        mBinding.edtSearchName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchBook();
                return true;
            }
            return false;
        });
    }

    private void loadAllBooksFromAPI(String searchKey) {
        if (mListAllBooks.isEmpty()) {
            // Load all books from API first time
            showProgressDialog(true);
            BookRepository.getInstance().getAllBooks(new ApiCallback<List<Book>>() {
                @Override
                public void onSuccess(List<Book> books) {
                    showProgressDialog(false);
                    mListAllBooks.clear();
                    mListAllBooks.addAll(books);
                    filterBooks(searchKey);
                }

                @Override
                public void onError(String errorMessage) {
                    showProgressDialog(false);
                    Toast.makeText(SearchActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Already loaded, just filter
            filterBooks(searchKey);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterBooks(String searchKey) {
        resetListBookData();
        
        if (StringUtil.isEmpty(searchKey)) {
            // Show all books if search key is empty
            mListBook.addAll(mListAllBooks);
        } else {
            // Filter books by title
            String searchKeyLower = GlobalFunction.getTextSearch(searchKey).toLowerCase().trim();
            for (Book book : mListAllBooks) {
                String title = GlobalFunction.getTextSearch(book.getTitle()).toLowerCase().trim();
                if (title.contains(searchKeyLower)) {
                    mListBook.add(book);
                }
            }
        }
        
        if (mBookAdapter != null) {
            mBookAdapter.notifyDataSetChanged();
        }
        
        // Show/hide empty state
        if (mListBook.isEmpty()) {
            mBinding.rcvSearchResult.setVisibility(View.GONE);
            // You can add empty state view here if needed
        } else {
            mBinding.rcvSearchResult.setVisibility(View.VISIBLE);
        }
    }

    private void resetListBookData() {
        if (mListBook == null) {
            mListBook = new ArrayList<>();
        } else {
            mListBook.clear();
        }
    }

    private void searchBook() {
        String strKey = mBinding.edtSearchName.getText().toString().trim();
        filterBooks(strKey);
        GlobalFunction.hideSoftKeyboard(this);
    }
}
