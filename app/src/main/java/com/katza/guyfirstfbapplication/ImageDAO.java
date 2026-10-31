
package com.katza.guyfirstfbapplication;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ImageDAO {

    private static final String COLLECTION_IMAGE_NAME = "image.labs";

    public void save(
            String name,
            String data,
            String type,
            IFFirestoreImageCallback callback) {

        Map<String, Object> item = new HashMap<>();
        item.put("type", type);
        item.put("data", data);

        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection(COLLECTION_IMAGE_NAME)
                .document(name);

        docRef.set(item)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e);
                    }
                });
    }
    public void read(
            String name,
            IFFirestoreImageCallback callback) {

        FirebaseFirestore.getInstance()
                .collection(COLLECTION_IMAGE_NAME)
                .document(name)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String type = document.getString("type");
                        String data = document.getString("data");

                        callback.onSuccess(data);
                    } else {
                        callback.onFailure(
                                new Exception("התמונה לא נמצאה")
                        );
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }
}
