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

import com.example.book.MyApplication;
import com.example.book.R;
import com.example.book.activity.admin.AdminAddAdvertisementActivity;
import com.example.book.adapter.admin.AdminAdvertisementAdapter;
import com.example.book.constant.Constant;
import com.example.book.constant.GlobalFunction;
import com.example.book.databinding.FragmentAdminAdvertisementListBinding;
import com.example.book.model.Advertisement;
import com.example.book.utils.StringUtil;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

public class AdminAdvertisementListFragment extends Fragment {

    private FragmentAdminAdvertisementListBinding binding;
    private List<Advertisement> mListAdvertisement;
    private AdminAdvertisementAdapter mAdapter;
    private ChildEventListener mChildEventListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminAdvertisementListBinding.inflate(inflater, container, false);

        initView();
        initListener();
        loadListAdvertisement("");

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
                    MyApplication.get(getActivity()).advertisementDatabaseReference()
                            .child(String.valueOf(advertisement.getId())).removeValue((error, ref) ->
                                    Toast.makeText(getActivity(),
                                            getString(R.string.msg_ad_delete_success),
                                            Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show();
    }

    private void toggleActive(Advertisement advertisement) {
        if (getActivity() == null) return;
        MyApplication.get(getActivity()).advertisementDatabaseReference()
                .child(String.valueOf(advertisement.getId()))
                .child("isActive")
                .setValue(!advertisement.isActive());
    }

    private void searchAdvertisement() {
        String strKey = binding.edtSearchName.getText().toString().trim();
        resetListAdvertisement();
        if (getActivity() != null) {
            MyApplication.get(getActivity()).advertisementDatabaseReference()
                    .removeEventListener(mChildEventListener);
        }
        loadListAdvertisement(strKey);
        GlobalFunction.hideSoftKeyboard(getActivity());
    }

    private void resetListAdvertisement() {
        if (mListAdvertisement != null) {
            mListAdvertisement.clear();
        } else {
            mListAdvertisement = new ArrayList<>();
        }
    }

    public void loadListAdvertisement(String keyword) {
        if (getActivity() == null) return;
        mChildEventListener = new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                Advertisement advertisement = dataSnapshot.getValue(Advertisement.class);
                if (advertisement == null || mListAdvertisement == null) return;
                if (StringUtil.isEmpty(keyword)) {
                    mListAdvertisement.add(0, advertisement);
                } else {
                    if (GlobalFunction.getTextSearch(advertisement.getTitle()).toLowerCase().trim()
                            .contains(GlobalFunction.getTextSearch(keyword).toLowerCase().trim())) {
                        mListAdvertisement.add(0, advertisement);
                    }
                }
                if (mAdapter != null) mAdapter.notifyDataSetChanged();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                Advertisement advertisement = dataSnapshot.getValue(Advertisement.class);
                if (advertisement == null || mListAdvertisement == null || mListAdvertisement.isEmpty()) return;
                for (int i = 0; i < mListAdvertisement.size(); i++) {
                    if (advertisement.getId() == mListAdvertisement.get(i).getId()) {
                        mListAdvertisement.set(i, advertisement);
                        break;
                    }
                }
                if (mAdapter != null) mAdapter.notifyDataSetChanged();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Advertisement advertisement = dataSnapshot.getValue(Advertisement.class);
                if (advertisement == null || mListAdvertisement == null || mListAdvertisement.isEmpty()) return;
                for (Advertisement ad : mListAdvertisement) {
                    if (advertisement.getId() == ad.getId()) {
                        mListAdvertisement.remove(ad);
                        break;
                    }
                }
                if (mAdapter != null) mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        MyApplication.get(getActivity()).advertisementDatabaseReference()
                .addChildEventListener(mChildEventListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null && mChildEventListener != null) {
            MyApplication.get(getActivity()).advertisementDatabaseReference()
                    .removeEventListener(mChildEventListener);
        }
    }
}



