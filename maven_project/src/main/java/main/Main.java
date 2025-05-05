package main;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.paho.client.mqttv3.*;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import main.lib.services.FirestoreService;
import main.lib.services.MqttService;
import main.lib.services.RequestService;

public class Main {

    public static void main(String[] args) throws MqttException, InterruptedException, IOException, ExecutionException {
        // MqttService mqttService = new MqttService();
        // mqttService.getSubscriber().subscribe("dapi2025/test");

        // Thread.sleep(3000);
        // MqttMessage msg = new MqttMessage("Hallo Welt".getBytes());
        // msg.setQos(1);
        // msg.setRetained(true);

        // MqttTopic tempTopic = mqttService.getPublisher().getTopic("dapi2025/test");
        // tempTopic.publish(msg);
        // System.out.println("data sent");

        // FirestoreService firestoreService = new FirestoreService();
        // List<QueryDocumentSnapshot> documents = firestoreService.getData("dogs");

        // for (DocumentSnapshot doc : documents) {
        // String id = doc.getId();
        // Map<String, Object> daten = doc.getData();

        // System.out.println("Dokument-ID: " + id);
        // System.out.println("Daten: " + daten);
        // }

        // Map<String, Object> daten = new HashMap<>();
        // daten.put("name", "Anna");
        // daten.put("alter", 28);
        // daten.put("online", true);

        // String time = firestoreService.saveData("test", "test0", daten);
        // System.out.println("Gespeichert um: " + time);

        Thread thread = new Thread(new RequestService());
        thread.start();
    }
}
