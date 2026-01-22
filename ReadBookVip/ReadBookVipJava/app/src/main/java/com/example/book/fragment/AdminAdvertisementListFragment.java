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
import com.example.book.activity.BaseActivity;
import com.example.book.activity.admin.AdminAddAdvertisementActivity;
import com.example.book.adapter.admin.AdminAdvertisementAdapter;
import com.example.book.api.AdvertisementApiService;
import com.example.book.api.ApiClient;
import com.example.book.constant.Constant;
import com.example.book.constant.GlobalFunction;
import com.example.book.databinding.FragmentAdminAdvertisementListBinding;
import com.example.book.model.Advertisement;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminAdvertisementListFragment extends Fragment {

    private FragmentAdminAdvertisementListBinding binding;
    private List<Advertisement> mListAdvertisement;
    private AdminAdvertisementAdapter mAdapter;
    private AdvertisementApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminAdvertisementListBinding.inflate(inflater, container, false);

        apiService = ApiClient.getInstance().getAdvertisementApiService();
        
        initView();
        initListener();
        loadListAdvertisement();

        return binding.getRoot();
    }

    private void initView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        binding.rcvAdvertisement.setLayoutManager(linearLayoutManager);
        mListAdvertisement = new ArrayList<>();
        mAdapter = new AdminAdvertisementAdapter(mListAdvertisement, new AdminAdvertisementAdapter.IOnAdminAdvertisementListener() {
            @Override
            public void onClickUpdate(Advertisement advertisement) {
                onClickEditAdvertisement(advertisement);
            }

            @Override
            public void onClickDelete(Advertisement advertisement) {
                deleteAdvertisementItem(advertisement);
            }

            @Override
            public void onClickToggleActive(Advertisement advertisement) {
                toggleActive(advertisement);
            }
        });
        binding.rcvAdvertisement.setAdapter(mAdapter);
        binding.rcvAdvertisement.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    binding.btnAddAdvertisement.hide();
                } else {
                    binding.btnAddAdvertisement.show();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void initListener() {
        binding.btnAddAdvertisement.setOnClickListener(v -> onClickAddAdvertisement());

        binding.imgSearch.setOnClickListener(view1 -> searchAdvertisement());

        binding.edtSearchName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchAdvertisement();
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
                    searchAdvertisement();
                }
            }
        });
    }

    private void onClickAddAdvertisement() {
        GlobalFunction.startActivity(getActivity(), AdminAddAdvertisementActivity.class);
    }

    private void onClickEditAdvertisement(Advertisement advertisement) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.OBJECT_ADVERTISEMENT, advertisement);
        GlobalFunction.startActivity(getActivity(), AdminAddAdvertisementActivity.class, bundle);
    }

    private void deleteAdvertisementItem(Advertisement advertisement) {
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.msg_delete_title))
                .setMessage(getString(R.string.msg_confirm_delete))
                .setPositiveButton(getString(R.string.action_ok), (dialogInterface, i) -> {
                    if (getActivity() == null) return;
                    showProgressDialog(true);
                    apiService.deleteAdvertisement(advertisement.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                            showProgressDialog(false);
                            if (response.isSuccessful()) {
                                Toast.makeText(getActivity(),
                                        getString(R.string.msg_ad_delete_success),
                                        Toast.LENGTH_SHORT).show();
                                loadListAdvertisement();
                            } else {
                                Toast.makeText(getActivity(),
                                        "Error: " + response.message(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                            showProgressDialog(false);
                            Toast.makeText(getActivity(),
                                    "Connection error: " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show();
    }

    private void toggleActive(Advertisement advertisement) {
        if (getActivity() == null) return;
        
        Advertisement updateAd = new Advertisement();
        updateAd.setTitle(advertisement.getTitle());
        updateAd.setVideoUrl(advertisement.getVideoUrl());
        updateAd.setUrl(advertisement.getUrl());
        updateAd.setThumbnailUrl(advertisement.getThumbnailUrl());
        updateAd.setActive(!advertisement.isActive());
        
        apiService.updateAdvertisement(advertisement.getId(), updateAd).enqueue(new Callback<Advertisement>() {
            @Override
            public void onResponse(@NonNull Call<Advertisement> call, @NonNull Response<Advertisement> response) {
                if (response.isSuccessful()) {
                    loadListAdvertisement();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Advertisement> call, @NonNull Throwable t) {
                // Silent fail
            }
        });
    }

    private void searchAdvertisement() {
        String strKey = binding.edtSearchName.getText().toString().trim();
        loadListAdvertisement();
        GlobalFunction.hideSoftKeyboard(getActivity());
    }

    public void loadListAdvertisement() {
        if (getActivity() == null) return;
        
        showProgressDialog(true);
        apiService.getAllAdvertisements().enqueue(new Callback<List<Advertisement>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<Advertisement>> call, @NonNull Response<List<Advertisement>> response) {
                showProgressDialog(false);
                if (response.isSuccessful() && response.body() != null) {
                    mListAdvertisement.clear();
                    String keyword = binding.edtSearchName.getText().toString().trim();
                    
                    for (Advertisement ad : response.body()) {
                        if (keyword.isEmpty() || 
                            GlobalFunction.getTextSearch(ad.getTitle()).toLowerCase().trim()
                                .contains(GlobalFunction.getTextSearch(keyword).toLowerCase().trim())) {
                            mListAdvertisement.add(ad);
                        }
                    }
                    
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(getActivity(),
                            "Error loading advertisements",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Advertisement>> call, @NonNull Throwable t) {
                showProgressDialog(false);
                Toast.makeText(getActivity(),
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showProgressDialog(boolean show) {
        if (getActivity() != null && getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).showProgressDialog(show);
        }
    }
}
