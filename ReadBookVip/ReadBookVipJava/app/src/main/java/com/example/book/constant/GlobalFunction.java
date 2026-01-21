package com.example.book.constant;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.book.MyApplication;
import com.example.book.activity.BookByCategoryActivity;
import com.example.book.activity.BookDetailActivity;
import com.example.book.activity.BookInfoActivity;
import com.example.book.api.ApiClient;
import com.example.book.api.BookApiService;
import com.example.book.model.Book;
import com.example.book.model.BookText;
import com.example.book.model.Category;
import com.example.book.model.UserInfo;
import com.example.book.prefs.DataStoreManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GlobalFunction {

    public static void startActivity(Context context, Class<?> clz) {
        Intent intent = new Intent(context, clz);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, Class<?> clz, Bundle bundle) {
        Intent intent = new Intent(context, clz);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.
                    getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public static void onClickOpenGmail(Context context) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", AboutUsConfig.GMAIL, null));
        context.startActivity(Intent.createChooser(emailIntent, "Send Email"));
    }

    public static void onClickOpenSkype(Context context) {
        try {
            Uri skypeUri = Uri.parse("skype:" + AboutUsConfig.SKYPE_ID + "?chat");
            context.getPackageManager().getPackageInfo("com.skype.raider", 0);
            Intent skypeIntent = new Intent(Intent.ACTION_VIEW, skypeUri);
            skypeIntent.setComponent(new ComponentName("com.skype.raider", "com.skype.raider.Main"));
            context.startActivity(skypeIntent);
        } catch (Exception e) {
            openSkypeWebView(context);
        }
    }

    private static void openSkypeWebView(Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("skype:" + AboutUsConfig.SKYPE_ID + "?chat")));
        } catch (Exception exception) {
            String skypePackageName = "com.skype.raider";
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + skypePackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                context.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + skypePackageName)));
            }
        }
    }

    public static void onClickOpenFacebook(Context context) {
        Intent intent;
        try {
            String urlFacebook = AboutUsConfig.PAGE_FACEBOOK;
            PackageManager packageManager = context.getPackageManager();
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) {
                urlFacebook = "fb://facewebmodal/f?href=" + AboutUsConfig.LINK_FACEBOOK;
            }
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlFacebook));
        } catch (Exception e) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(AboutUsConfig.LINK_FACEBOOK));
        }
        context.startActivity(intent);
    }

    public static void onClickOpenYoutubeChannel(Context context) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(AboutUsConfig.LINK_YOUTUBE)));
    }

    public static void onClickOpenZalo(Context context) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(AboutUsConfig.ZALO_LINK)));
    }

    public static void callPhoneNumber(Activity activity) {
        try {
            if (ActivityCompat.checkSelfPermission(activity,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.CALL_PHONE}, 101);
                return;
            }

            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + AboutUsConfig.PHONE_NUMBER));
            activity.startActivity(callIntent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void showToastMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static String getTextSearch(String input) {
        String nfdNormalizedString = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

    public static void goToBookDetail(Context context, Book book) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.OBJECT_BOOK, book);
        UserInfo userInfoHistory = loadUserInfoHistory(book);
        if (userInfoHistory != null) {
            bundle.putSerializable(Constant.OBJECT_USER_INFO, userInfoHistory);
        }

        startActivity(context, BookInfoActivity.class, bundle);
    }

    private static UserInfo loadUserInfoHistory(Book book) {
        if (book.getHistory() == null || book.getHistory().isEmpty()) return null;

        List<UserInfo> listUsersHistory = new ArrayList<>(book.getHistory().values());
        if (listUsersHistory.isEmpty()) return null;
        for (UserInfo userInfo : listUsersHistory) {
            if (DataStoreManager.getUser().getEmail().equals(userInfo.getEmailUser())) {
                return userInfo;
            }
        }
        return null;
    }

    public static void goToBookByCategory(Context context, Category category) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.OBJECT_CATEGORY, category);
        startActivity(context, BookByCategoryActivity.class, bundle);
    }

    public static boolean isFavoriteBook(Book book) {
        if (book.getFavorite() == null || book.getFavorite().isEmpty()) return false;
        List<UserInfo> listUsersFavorite = new ArrayList<>(book.getFavorite().values());
        if (listUsersFavorite.isEmpty()) return false;
        for (UserInfo userInfo : listUsersFavorite) {
            if (DataStoreManager.getUser().getEmail().equals(userInfo.getEmailUser())) {
                return true;
            }
        }
        return false;
    }

    public static void onClickFavoriteBook(Context context, Book book, boolean isFavorite) {
        if (context == null) return;
        if (isFavorite) {
            String userEmail = DataStoreManager.getUser().getEmail();
            UserInfo userInfo = new UserInfo(System.currentTimeMillis(), userEmail, 0);
            MyApplication.get(context).bookDatabaseReference()
                    .child(String.valueOf(book.getId()))
                    .child("favorite")
                    .child(String.valueOf(userInfo.getId()))
                    .setValue(userInfo);
        } else {
            UserInfo userInfo = getUserFavoriteBook(book);
            if (userInfo != null) {
                MyApplication.get(context).bookDatabaseReference()
                        .child(String.valueOf(book.getId()))
                        .child("favorite")
                        .child(String.valueOf(userInfo.getId()))
                        .removeValue();
            }
        }
    }

    private static UserInfo getUserFavoriteBook(Book book) {
        UserInfo userInfo = null;
        if (book.getFavorite() == null || book.getFavorite().isEmpty()) return null;
        List<UserInfo> listUsersFavorite = new ArrayList<>(book.getFavorite().values());
        for (UserInfo userObject : listUsersFavorite) {
            if (DataStoreManager.getUser().getEmail().equals(userObject.getEmailUser())) {
                userInfo = userObject;
                break;
            }
        }
        return userInfo;
    }

    // New API-based methods for BookText (SQL Server)
    public static void onClickFavoriteBookText(Context context, BookText book, boolean isFavorite) {
        if (context == null || book == null) return;
        BookApiService apiService = ApiClient.getInstance().getBookApiService();
        String userEmail = DataStoreManager.getUser().getEmail();
        
        if (isFavorite) {
            apiService.addFavorite(book.getId(), userEmail).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    // Favorite added
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    // Silent fail
                }
            });
        } else {
            apiService.removeFavorite(book.getId(), userEmail).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    // Favorite removed
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    // Silent fail
                }
            });
        }
    }

    public static void checkFavoriteBookText(Context context, BookText book, 
                                             OnFavoriteCheckedListener listener) {
        if (context == null || book == null) return;
        BookApiService apiService = ApiClient.getInstance().getBookApiService();
        String userEmail = DataStoreManager.getUser().getEmail();
        
        apiService.getFavorites(userEmail).enqueue(new Callback<List<Long>>() {
            @Override
            public void onResponse(Call<List<Long>> call, Response<List<Long>> response) {
                boolean isFavorite = false;
                if (response.isSuccessful() && response.body() != null) {
                    isFavorite = response.body().contains(book.getId());
                }
                if (listener != null) {
                    listener.onFavoriteChecked(isFavorite);
                }
            }

            @Override
            public void onFailure(Call<List<Long>> call, Throwable t) {
                if (listener != null) {
                    listener.onFavoriteChecked(false);
                }
            }
        });
    }

    public interface OnFavoriteCheckedListener {
        void onFavoriteChecked(boolean isFavorite);
    }
}
