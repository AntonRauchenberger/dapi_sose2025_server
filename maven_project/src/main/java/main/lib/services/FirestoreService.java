package main.lib.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.WriteResult;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Get and saves data to firestore
 */
public class FirestoreService {

    private Firestore db;

    public FirestoreService() throws IOException {
        FileInputStream serviceAccount = new FileInputStream(
                "dapi-sose2025-app-firebase-adminsdk-fbsvc-d44312696b.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }

        this.db = FirestoreClient.getFirestore();
    }

    public Firestore getDb() {
        return db;
    }

    public String saveData(String collection, String documentId, Map<String, Object> data)
            throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> result = db.collection(collection).document(documentId).set(data);
        return result.get().getUpdateTime().toString();
    }

    public Map<String, Object> getDocumentDataByPath(String path) throws ExecutionException, InterruptedException {
        ApiFuture<DocumentSnapshot> future = db.document(path).get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return document.getData();
        } else {
            return null;
        }
    }
}
