package com.example.book;

import android.app.Application;
import android.content.Context;

import com.example.book.prefs.DataStoreManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MyApplication extends Application {

    public static final String FIREBASE_URL = "https://readbookbasic-default-rtdb.firebaseio.com";
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseStorage mFirebaseStorage;

    public static MyApplication get(Context context) {
        return (MyApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        mFirebaseDatabase = FirebaseDatabase.getInstance(FIREBASE_URL);
        mFirebaseStorage = FirebaseStorage.getInstance();
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

    public DatabaseReference advertisementDatabaseReference() {
        return mFirebaseDatabase.getReference("/advertisement");
    }

    public DatabaseReference advertisementViewDatabaseReference() {
        return mFirebaseDatabase.getReference("/advertisementViews");
    }

    public StorageReference getAdvertisementStorageReference() {
        return mFirebaseStorage.getReference("advertisements");
    }
}
