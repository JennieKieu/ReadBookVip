package com.example.book.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.book.MyApplication;
import com.example.book.R;
import com.example.book.adapter.BookAdapter;
import com.example.book.adapter.BookFeaturedAdapter;
import com.example.book.adapter.CategoryHomeAdapter;
import com.example.book.adapter.CategoryMenuAdapter;
import com.example.book.api.ApiCallback;
import com.example.book.constant.Constant;
import com.example.book.constant.GlobalFunction;
import com.example.book.databinding.ActivityMainBinding;
import com.example.book.listener.IOnClickBookListener;
import com.example.book.model.Book;
import com.example.book.model.Category;
import com.example.book.model.User;
import com.example.book.prefs.DataStoreManager;
import com.example.book.repository.BookRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("NonConstantResourceId")
public class MainActivity extends BaseActivity implements View.OnClickListener {

    private ActivityMainBinding mBinding;
    private CategoryMenuAdapter mCategoryMenuAdapter;
    private List<Category> mListCategory;
    private List<Category> mListCategoryHome;
    private List<Book> mListBook;
    private List<Book> mListBookFeatured;
    private final Handler mHandlerBanner = new Handler(Looper.getMainLooper());
    private final Runnable mRunnableBanner = new Runnable() {
        @Override
        public void run() {
            if (mListBookFeatured == null || mListBookFeatured.isEmpty()) return;
            if (mBinding.viewPager.getCurrentItem() == mListBookFeatured.size() - 1) {
                mBinding.viewPager.setCurrentItem(0);
                return;
            }
            mBinding.viewPager.setCurrentItem(mBinding.viewPager.getCurrentItem() + 1);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        initToolbar();
        initListener();
        initNavigationMenuLeft();
    }

    private void initToolbar() {
        mBinding.header.imgToolbar.setImageResource(R.drawable.ic_menu);
        mBinding.header.tvToolbarTitle.setText(getString(R.string.app_name));
        
        // Hide user icon if admin
        User user = DataStoreManager.getUser();
        if (user != null && user.isAdmin()) {
            mBinding.header.imgUser.setVisibility(View.GONE);
        } else {
            mBinding.header.imgUser.setVisibility(View.VISIBLE);
        }
    }

    private void initListener() {
        mBinding.header.imgToolbar.setOnClickListener(this);
        mBinding.header.imgUser.setOnClickListener(this);
        mBinding.layoutFavorite.setOnClickListener(this);
        mBinding.layoutHistory.setOnClickListener(this);
        mBinding.layoutFeedback.setOnClickListener(this);
        mBinding.layoutContact.setOnClickListener(this);
        mBinding.viewAllCategory.setOnClickListener(this);
        mBinding.viewAllBook.setOnClickListener(this);
        mBinding.layoutSearch.setOnClickListener(this);
        mBinding.layoutChangePassword.setOnClickListener(this);
        mBinding.layoutSignOut.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.img_toolbar) {
            mBinding.drawerLayout.openDrawer(GravityCompat.START);
        } else if (id == R.id.img_user) {
            // Open user info or drawer menu
            mBinding.drawerLayout.openDrawer(GravityCompat.START);
        } else if (id == R.id.layout_favorite) {
            GlobalFunction.startActivity(this, FavoriteActivity.class);
        } else if (id == R.id.layout_history) {
            GlobalFunction.startActivity(this, HistoryActivity.class);
        } else if (id == R.id.layout_feedback) {
            GlobalFunction.startActivity(this, FeedbackActivity.class);
        } else if (id == R.id.layout_contact) {
            GlobalFunction.startActivity(this, ContactActivity.class);
        } else if (id == R.id.view_all_category) {
            GlobalFunction.startActivity(this, ListCategoryActivity.class);
        } else if (id == R.id.view_all_book) {
            GlobalFunction.startActivity(this, ListBookActivity.class);
        } else if (id == R.id.layout_search) {
            GlobalFunction.startActivity(this, SearchActivity.class);
        } else if (id == R.id.layout_change_password) {
            GlobalFunction.startActivity(this, ChangePasswordActivity.class);
        } else if (id == R.id.layout_sign_out) {
            onClickSignOut();
        }
    }

    private void onClickSignOut() {
        FirebaseAuth.getInstance().signOut();
        DataStoreManager.setUser(null);
        GlobalFunction.startActivity(this, LoginActivity.class);
        finishAffinity();
    }

    private void initNavigationMenuLeft() {
        displayUserInformation();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mBinding.rcvCategory.setLayoutManager(linearLayoutManager);
        mListCategory = new ArrayList<>();
        mCategoryMenuAdapter = new CategoryMenuAdapter(mListCategory,
                category -> GlobalFunction.goToBookByCategory(MainActivity.this, category));
        mBinding.rcvCategory.setAdapter(mCategoryMenuAdapter);

        loadListCategoryFromAPI();
    }

    private void displayUserInformation() {
        User user = DataStoreManager.getUser();
        mBinding.tvUserEmail.setText(user.getEmail());
    }

    /**
     * Load categories from Firebase (KHÔNG thay đổi - giữ nguyên Firebase)
     */
    @SuppressLint("NotifyDataSetChanged")
    private void loadListCategoryFromAPI() {
        showProgressDialog(true);
        
        // Vẫn dùng Firebase cho Categories
        MyApplication.get(this).categoryDatabaseReference().addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                resetListCategory();
                for (com.google.firebase.database.DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Category category = dataSnapshot.getValue(Category.class);
                    if (category == null) continue;
                    mListCategory.add(category);
                }
                if (mCategoryMenuAdapter != null) mCategoryMenuAdapter.notifyDataSetChanged();
                displayListCategoryHome();
                
                // After categories loaded, load books from API
                loadListBookFromAPI();
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                showProgressDialog(false);
                Toast.makeText(MainActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayListCategoryHome() {
        LinearLayoutManager layoutManagerHorizontal = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        mBinding.rcvCategoryHome.setLayoutManager(layoutManagerHorizontal);
        CategoryHomeAdapter categoryHomeAdapter = new CategoryHomeAdapter(loadListCategoryHome(),
                category -> GlobalFunction.goToBookByCategory(MainActivity.this, category));
        mBinding.rcvCategoryHome.setAdapter(categoryHomeAdapter);
    }

    private List<Category> loadListCategoryHome() {
        resetListCategoryHome();
        for (Category category : mListCategory) {
            if (mListCategoryHome.size() < Constant.MAX_SIZE_LIST_CATEGORY) {
                mListCategoryHome.add(category);
            }
        }
        return mListCategoryHome;
    }

    private void resetListCategory() {
        if (mListCategory == null) {
            mListCategory = new ArrayList<>();
        } else {
            mListCategory.clear();
        }
    }

    private void resetListCategoryHome() {
        if (mListCategoryHome == null) {
            mListCategoryHome = new ArrayList<>();
        } else {
            mListCategoryHome.clear();
        }
    }

    private void resetListBook() {
        if (mListBook == null) {
            mListBook = new ArrayList<>();
        } else {
            mListBook.clear();
        }
    }

    private void resetListBookFeatured() {
        if (mListBookFeatured == null) {
            mListBookFeatured = new ArrayList<>();
        } else {
            mListBookFeatured.clear();
        }
    }

    /**
     * Load books from SQL API instead of Firebase
     */
    private void loadListBookFromAPI() {
        BookRepository.getInstance().getAllBooks(new ApiCallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> books) {
                showProgressDialog(false);
                resetListBook();
                mListBook.addAll(books);
                
                displayListBookFeatured();
                displayNewBooks();
                displayCountBookOfCategory();
            }

            @Override
            public void onError(String errorMessage) {
                showProgressDialog(false);
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void displayCountBookOfCategory() {
        if (mListCategory == null || mListCategory.isEmpty()) return;
        for (Category category : mListCategory) {
            category.setCount(loadCountBookOfCategory(category.getId()));
        }
        if (mCategoryMenuAdapter != null) mCategoryMenuAdapter.notifyDataSetChanged();
    }

    private int loadCountBookOfCategory(long categoryId) {
        if (mListBook == null || mListBook.isEmpty()) return 0;
        List<Book> listBooks = new ArrayList<>();
        for (Book book : mListBook) {
            if (categoryId == book.getCategoryId()) {
                listBooks.add(book);
            }
        }
        return listBooks.size();
    }

    private void displayListBookFeatured() {
        BookFeaturedAdapter bookFeaturedAdapter = new BookFeaturedAdapter(loadListBooks(), new IOnClickBookListener() {
            @Override
            public void onClickItemBook(Book book) {
                GlobalFunction.goToBookDetail(MainActivity.this, book);
            }

            @Override
            public void onClickCategoryOfBook(Category category) {}

            @Override
            public void onClickFavoriteBook(Book book, boolean favorite) {}
        });
        mBinding.viewPager.setAdapter(bookFeaturedAdapter);
        mBinding.indicator.setViewPager(mBinding.viewPager);

        mBinding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mHandlerBanner.removeCallbacks(mRunnableBanner);
                mHandlerBanner.postDelayed(mRunnableBanner, 3000);
            }
        });
    }

    private List<Book> loadListBooks() {
        resetListBookFeatured();
        for (Book book : mListBook) {
            if (book.isFeatured() && mListBookFeatured.size() < Constant.MAX_SIZE_LIST_FEATURED) {
                mListBookFeatured.add(book);
            }
        }
        return mListBookFeatured;
    }

    private void displayNewBooks() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        mBinding.rcvNewBooks.setLayoutManager(gridLayoutManager);

        BookAdapter bookAdapter = new BookAdapter(loadListNewBooks(), new IOnClickBookListener() {
            @Override
            public void onClickItemBook(Book book) {
                GlobalFunction.goToBookDetail(MainActivity.this, book);
            }

            @Override
            public void onClickCategoryOfBook(Category category) {
                GlobalFunction.goToBookByCategory(MainActivity.this, category);
            }

            @Override
            public void onClickFavoriteBook(Book book, boolean favorite) {
                GlobalFunction.onClickFavoriteBook(MainActivity.this, book, favorite);
            }
        });
        mBinding.rcvNewBooks.setAdapter(bookAdapter);
    }

    private List<Book> loadListNewBooks() {
        List<Book> list = new ArrayList<>();
        List<Book> allBooks = new ArrayList<>(mListBook);
        for (Book book : allBooks) {
            if (list.size() < Constant.MAX_SIZE_LIST_NEW_BOOKS) {
                list.add(book);
            }
        }
        return list;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        showConfirmExitApp();
    }

    private void showConfirmExitApp() {
        new MaterialDialog.Builder(this)
                .title(getString(R.string.app_name))
                .content(getString(R.string.msg_exit_app))
                .positiveText(getString(R.string.action_ok))
                .onPositive((dialog, which) -> finish())
                .negativeText(getString(R.string.action_cancel))
                .cancelable(false)
                .show();
    }

}
