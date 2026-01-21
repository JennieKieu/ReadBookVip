package com.example.book.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book.R;
import com.example.book.activity.admin.AdminAddBookActivity;
import com.example.book.activity.admin.AdminBookDetailActivity;
import com.example.book.adapter.admin.AdminBookAdapter;
import com.example.book.api.ApiClient;
import com.example.book.api.BookApiService;
import com.example.book.constant.Constant;
import com.example.book.constant.GlobalFunction;
import com.example.book.databinding.FragmentAdminBookBinding;
import com.example.book.listener.IOnAdminManagerBookListener;
import com.example.book.model.Book;
import com.example.book.model.BookText;
import com.example.book.utils.StringUtil;
// Firebase code - commented for SQL Server migration
// import com.example.book.MyApplication;
// import com.google.firebase.database.ChildEventListener;
// import com.google.firebase.database.DataSnapshot;
// import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminBookFragment extends Fragment {

    private FragmentAdminBookBinding binding;
    private List<Book> mListBook;
    private AdminBookAdapter mAdminBookAdapter;
    // Firebase code - commented for SQL Server migration
    // private ChildEventListener mChildEventListener;
    private BookApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminBookBinding.inflate(inflater, container, false);

        initView();
        initListener();
        apiService = ApiClient.getInstance().getBookApiService();
        loadListBook("");

        return binding.getRoot();
    }

    private void initView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        binding.rcvBook.setLayoutManager(linearLayoutManager);
        mListBook = new ArrayList<>();
        mAdminBookAdapter = new AdminBookAdapter(mListBook, new IOnAdminManagerBookListener() {
            @Override
            public void onClickUpdateBook(Book book) {
                onClickEditBook(book);
            }

            @Override
            public void onClickDeleteBook(Book book) {
                deleteBookItem(book);
            }

            @Override
            public void onClickDetailBook(Book book) {
                goToBookDetail(book);
            }
        });
        binding.rcvBook.setAdapter(mAdminBookAdapter);
        binding.rcvBook.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    binding.btnAddBook.hide();
                } else {
                    binding.btnAddBook.show();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void initListener() {
        binding.btnAddBook.setOnClickListener(v -> onClickAddBook());

        binding.imgSearch.setOnClickListener(view1 -> searchBook());

        binding.edtSearchName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchBook();
                return true;
            }
            return false;
        });

        binding.edtSearchName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String strKey = s.toString().trim();
                if (strKey.isEmpty()) {
                    searchBook();
                }
            }
        });
    }

    private void goToBookDetail(@NonNull Book book) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.OBJECT_BOOK, book);
        GlobalFunction.startActivity(getActivity(), AdminBookDetailActivity.class, bundle);
    }

    private void onClickAddBook() {
        GlobalFunction.startActivity(getActivity(), AdminAddBookActivity.class);
    }

    private void onClickEditBook(Book book) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.OBJECT_BOOK, book);
        GlobalFunction.startActivity(getActivity(), AdminAddBookActivity.class, bundle);
    }

    private void deleteBookItem(Book book) {
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.msg_delete_title))
                .setMessage(getString(R.string.msg_confirm_delete))
                .setPositiveButton(getString(R.string.action_ok), (dialogInterface, i) -> {
                    if (getActivity() == null) return;
                    
                    // New API-based delete
                    showProgressDialog(true);
                    apiService.deleteBook(book.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                            showProgressDialog(false);
                            if (response.isSuccessful()) {
                                Toast.makeText(getActivity(),
                                        getString(R.string.msg_delete_book_successfully),
                                        Toast.LENGTH_SHORT).show();
                                // Reload list after delete
                                loadListBook(binding.edtSearchName.getText().toString().trim());
                            } else {
                                Toast.makeText(getActivity(),
                                        "Error deleting book", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                            showProgressDialog(false);
                            Toast.makeText(getActivity(),
                                    "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    
                    // Firebase code - commented for SQL Server migration
                    /*
                    MyApplication.get(getActivity()).bookDatabaseReference()
                            .child(String.valueOf(book.getId())).removeValue((error, ref) ->
                                    Toast.makeText(getActivity(),
                                            getString(R.string.msg_delete_book_successfully),
                                            Toast.LENGTH_SHORT).show());
                    */
                })
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show();
    }

    private void showProgressDialog(boolean show) {
        if (getActivity() != null && getActivity() instanceof com.example.book.activity.BaseActivity) {
            ((com.example.book.activity.BaseActivity) getActivity()).showProgressDialog(show);
        }
    }

    private void searchBook() {
        String strKey = binding.edtSearchName.getText().toString().trim();
        resetListBook();
        loadListBook(strKey);
        GlobalFunction.hideSoftKeyboard(getActivity());
    }

    private void resetListBook() {
        if (mListBook != null) {
            mListBook.clear();
        } else {
            mListBook = new ArrayList<>();
        }
    }

    public void loadListBook(String keyword) {
        if (getActivity() == null) return;
        
        // New API-based load
        showProgressDialog(true);
        apiService.getAllBooks().enqueue(new Callback<List<BookText>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<BookText>> call, @NonNull Response<List<BookText>> response) {
                showProgressDialog(false);
                if (response.isSuccessful() && response.body() != null) {
                    mListBook.clear();
                    for (BookText bookText : response.body()) {
                        // Filter by keyword if provided
                        if (StringUtil.isEmpty(keyword)) {
                            mListBook.add(convertBookTextToBook(bookText));
                        } else {
                            String searchText = GlobalFunction.getTextSearch(bookText.getTitle()).toLowerCase().trim();
                            String keywordLower = GlobalFunction.getTextSearch(keyword).toLowerCase().trim();
                            if (searchText.contains(keywordLower)) {
                                mListBook.add(convertBookTextToBook(bookText));
                            }
                        }
                    }
                    if (mAdminBookAdapter != null) {
                        mAdminBookAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(getActivity(),
                            "Error loading books", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<BookText>> call, @NonNull Throwable t) {
                showProgressDialog(false);
                Toast.makeText(getActivity(),
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        
        // Firebase code - commented for SQL Server migration
        /*
        mChildEventListener = new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                Book book = dataSnapshot.getValue(Book.class);
                if (book == null || mListBook == null) return;
                if (StringUtil.isEmpty(keyword)) {
                    mListBook.add(0, book);
                } else {
                    if (GlobalFunction.getTextSearch(book.getTitle()).toLowerCase().trim()
                            .contains(GlobalFunction.getTextSearch(keyword).toLowerCase().trim())) {
                        mListBook.add(0, book);
                    }
                }
                if (mAdminBookAdapter != null) mAdminBookAdapter.notifyDataSetChanged();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                Book book = dataSnapshot.getValue(Book.class);
                if (book == null || mListBook == null || mListBook.isEmpty()) return;
                for (int i = 0; i < mListBook.size(); i++) {
                    if (book.getId() == mListBook.get(i).getId()) {
                        mListBook.set(i, book);
                        break;
                    }
                }
                if (mAdminBookAdapter != null) mAdminBookAdapter.notifyDataSetChanged();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Book book = dataSnapshot.getValue(Book.class);
                if (book == null || mListBook == null || mListBook.isEmpty()) return;
                for (Book bookObject : mListBook) {
                    if (book.getId() == bookObject.getId()) {
                        mListBook.remove(bookObject);
                        break;
                    }
                }
                if (mAdminBookAdapter != null) mAdminBookAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        MyApplication.get(getActivity()).bookDatabaseReference()
                .addChildEventListener(mChildEventListener);
        */
    }

    // Helper method to convert BookText to Book (for compatibility with existing adapter)
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
        // Note: url field is not used anymore (PDF migration)
        book.setUrl("");
        return book;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Firebase code - commented for SQL Server migration
        /*
        if (getActivity() != null && mChildEventListener != null) {
            MyApplication.get(getActivity()).bookDatabaseReference()
                    .removeEventListener(mChildEventListener);
        }
        */
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload list when fragment resumes (to show newly added books)
        loadListBook(binding.edtSearchName.getText().toString().trim());
    }
}
