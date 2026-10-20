
package com.katza.guyfirstfbapplication;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
public class FirestoreDAO {

    private static final String COLLECTION_NAME = "students";
    private ListenerRegistration listenerRegistration;

    public void createDocument(String name, String id, int grade) {
        Map<String, Object> studentRecord = new HashMap<>();

        studentRecord.put("fullname", name);
        studentRecord.put("id", id);
        studentRecord.put("grade", grade);

        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME)
                .document(id);

        docRef.set(studentRecord)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("DAO_FIREBASE", "SUCCESSFUL WRITE!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("DAO_FIREBASE", e.getMessage(), e);
                    }
                });
    }


    public void readDocumentById(String docID, IFFirestoreCallback callback) {
        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME)
                .document(docID);

        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    ArrayList<Student> students = new ArrayList<>();

                    if (documentSnapshot.exists()) {
                        Student student =
                                documentSnapshot.toObject(Student.class);

                        if (student != null) {
                            Log.d("DAO_FIREBASE",
                                    "SUCCESSFUL READ: "
                                            + student.getFullName()
                                            + ", " + student.getId()
                                            + ", " + student.getGrade());

                            students.add(student);
                        }
                    } else {
                        Log.d("DAO_FIREBASE", "Student not found");
                    }

                    callback.onDataReceived(students);
                })
                .addOnFailureListener(e ->
                        Log.e("DAO_FIREBASE", "READ FAILED", e));
    }
    public void stopListening() {

        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    public void readAllDocuments(IFFirestoreCallback callback) {

        CollectionReference colRef = FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME);

        colRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                ArrayList<Student> students = new ArrayList<>();

                for (DocumentSnapshot docSS : queryDocumentSnapshots.getDocuments()) {
                    Student student = docSS.toObject(Student.class);

                    if (student != null) {
                        Log.d("DAO_FIREBASE", "SUCCESSFUL READ: " + student);
                        students.add(student);
                    }
                }

                callback.onDataReceived(students);
            }
        }).addOnFailureListener(e ->
                Log.e("DAO_FIREBASE", "READ ALL FAILED", e));
    }
    public void readStudentsByGrade(IFFirestoreCallback callback) {

        CollectionReference colRef = FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME);

        colRef.whereGreaterThan("grade", 87)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    ArrayList<Student> students = new ArrayList<>();

                    for (DocumentSnapshot docSS :
                            queryDocumentSnapshots.getDocuments()) {

                        Student student = docSS.toObject(Student.class);

                        if (student != null) {
                            students.add(student);
                        }
                    }

                    callback.onDataReceived(students);
                })
                .addOnFailureListener(e ->
                        Log.e("DAO_FIREBASE", "READ BY GRADE FAILED", e));
    }

    public void updateDocument(String documentID, int newGrade) {

        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME)
                .document(documentID);

        docRef.update("grade", newGrade)
                .addOnSuccessListener(unused -> {
                    Log.d("DAO_FIREBASE", "SUCCESSFUL UPDATE!");
                })
                .addOnFailureListener(e ->
                        Log.e("DAO_FIREBASE", "UPDATE FAILED", e));
    }


    public void deleteDocument(String documentID) {

        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME)
                .document(documentID);

        docRef.delete()
                .addOnSuccessListener(unused -> {
                    Log.d("DAO_FIREBASE", "SUCCESSFUL DELETE!");
                })
                .addOnFailureListener(e -> {
                    Log.e("DAO_FIREBASE", "DELETE FAILED", e);
                });
    }


    public ListenerRegistration listenToAllDocumentsChange() {

        CollectionReference colRef = FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME);

        listenerRegistration = colRef.addSnapshotListener(
                new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(
                            @Nullable QuerySnapshot value,
                            @Nullable FirebaseFirestoreException error) {

                        if (error != null) {
                            Log.e("DAO_FIREBASE", "LISTEN FAILED", error);
                            return;
                        }

                        if (value != null) {
                            for (DocumentChange change : value.getDocumentChanges()) {

                                Student student =
                                        change.getDocument().toObject(Student.class);

                                if (change.getType() == DocumentChange.Type.ADDED) {
                                    Log.d("CHANGED_DAO", "ADDED: " + student);
                                } else if (change.getType() == DocumentChange.Type.MODIFIED) {
                                    Log.d("CHANGED_DAO", "MODIFIED: " + student);
                                } else if (change.getType() == DocumentChange.Type.REMOVED) {
                                    Log.d("CHANGED_DAO", "REMOVED: " + student);
                                }
                            }
                        }
                    }
                });

        return listenerRegistration;
    }






}
