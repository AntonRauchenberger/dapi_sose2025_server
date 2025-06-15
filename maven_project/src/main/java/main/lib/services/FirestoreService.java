package main.lib.services;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.WriteResult;
import java.util.List;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Serviceklasse für das Speichern und Abrufen von Daten aus Firestore.
 */
public class FirestoreService {

    private Firestore db;

    /**
     * Initialisiert die Verbindung zur Firestore-Datenbank.
     */
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

    /**
     * Speichert Daten in einer Collection und einem Dokument.
     */
    public String saveData(String collection, String documentId, Map<String, Object> data)
            throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> result = db.collection(collection).document(documentId).set(data);
        return result.get().getUpdateTime().toString();
    }

    /**
     * Fügt ein neues Dokument zu einer Subcollection hinzu.
     */
    public String addToSubcollection(String collection, String documentId, String subcollection,
            Map<String, Object> data)
            throws ExecutionException, InterruptedException {
        ApiFuture<DocumentReference> future = db.collection(collection)
                .document(documentId)
                .collection(subcollection)
                .add(data);
        return future.get().getId();
    }

    /**
     * Holt die Daten eines Dokuments anhand des angegebenen Pfads.
     */
    public Map<String, Object> getDocumentDataByPath(String path) throws ExecutionException, InterruptedException {
        ApiFuture<DocumentSnapshot> future = db.document(path).get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return document.getData();
        } else {
            return null;
        }
    }

    /**
     * Holt alle Dokumentdaten aus einer angegebenen Collection (Pfad) als Liste.
     */
    public List<Map<String, Object>> getAllDocumentDataByPath(String path)
            throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = db.collection(path).get();
        QuerySnapshot snapshot = future.get();
        List<Map<String, Object>> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            result.add(doc.getData());
        }
        return result;
    }
}
