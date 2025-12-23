package com.example.book;

import android.app.Application;
import android.content.Context;

import com.example.book.prefs.DataStoreManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {

    public static final String FIREBASE_URL = "https://readbookbasic-default-rtdb.firebaseio.com";
    private FirebaseDatabase mFirebaseDatabase;

    public static MyApplication get(Context context) {
        return (MyApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        mFirebaseDatabase = FirebaseDatabase.getInstance(FIREBASE_URL);
        DataStoreManager.init(getApplicationContext());
    }

    public DatabaseReference categoryDatabaseReference() {
        return mFirebaseDatabase.getReference("/category");
    }

    public DatabaseReference bookDatabaseReference() {
        return mFirebaseDatabase.getReference("/book");
    }

    public DatabaseReference feedbackDatabaseReference() {
        return mFirebaseDatabase.getReference("/feedback");
    }
}
