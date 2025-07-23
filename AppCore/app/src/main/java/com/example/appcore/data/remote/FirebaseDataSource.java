package com.example.appcore.data.remote;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseDataSource {
    private static FirebaseDataSource instance;
    private final FirebaseDatabase firebaseDatabase;
    private final FirebaseStorage storage;

    // Constructor private để ngăn khởi tạo từ bên ngoài
    private FirebaseDataSource() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
    }
    // Singleton pattern: Lấy instance duy nhất
    public static synchronized FirebaseDataSource getInstance() {
        if (instance == null) {
            instance = new FirebaseDataSource();
        }
        return instance;
    }
    // Lấy DatabaseReference cho một node cụ thể
    public DatabaseReference getReference(String node) {
        return firebaseDatabase.getReference(node);
    }

    public StorageReference getStorageReference(String path) {
        return storage.getReference(path);
    }

    public StorageReference getStorageReferenceFromUrl(String url) {
        return storage.getReferenceFromUrl(url);
    }
}
