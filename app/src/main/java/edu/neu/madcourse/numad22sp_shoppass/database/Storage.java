package edu.neu.madcourse.numad22sp_shoppass.database;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Storage {
    public FirebaseStorage storage;
    public StorageReference storageRef;

    public Storage() {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }
}
