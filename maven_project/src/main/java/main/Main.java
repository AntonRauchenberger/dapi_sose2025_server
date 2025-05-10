package main;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.checkerframework.checker.units.qual.s;
import org.eclipse.paho.client.mqttv3.*;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.gson.Gson;

import main.lib.helpers.GpsHelper;
import main.lib.helpers.MessageHandler;
import main.lib.services.FirestoreService;
import main.lib.services.MqttService;
import main.lib.services.RequestService;

public class Main {

    final static String USER_ID = "jEGrvfPcYMMuuMgMVCZeOhaSTz03";

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

        Thread t1 = new Thread(() -> {
            try {
                MqttService mqttService = new MqttService(new MessageHandler() {
                    @Override
                    public void handleMessage(String topic, MqttMessage message) {
                        // skip
                    }
                });
                Random random = new Random();

                while (true) {
                    double longitude = -180.0 + (random.nextDouble() * 360.0); // Bereich: 13.0 bis 13.8
                    double latitude = -90.0 + (random.nextDouble() * 180.0); // Bereich: 52.0 bis 52.8

                    Map<String, Object> coordinates = new HashMap<>();
                    coordinates.put("longitude", longitude);
                    coordinates.put("latitude", latitude);

                    Gson gson = new Gson();
                    String jsonPayload = gson.toJson(coordinates);

                    MqttMessage msg = new MqttMessage(jsonPayload.getBytes());
                    msg.setQos(1);
                    msg.setRetained(true);

                    MqttTopic tempTopic = mqttService.getPublisher()
                            .getTopic("dapi2025/" + USER_ID + "/data");
                    tempTopic.publish(msg);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (MqttException e) {
                e.printStackTrace();
            }
        });
        t1.setDaemon(true);
        t1.start();

        Thread t2 = new Thread(() -> {
            try {
                MqttService mqttService = new MqttService(new MessageHandler() {
                    @Override
                    public void handleMessage(String topic, MqttMessage message) {
                        String payload = new String(message.getPayload());
                        Gson gson = new Gson();

                        Map<String, Object> coordinates = gson.fromJson(payload, Map.class);
                        double longitude = (double) coordinates.get("longitude");
                        double latitude = (double) coordinates.get("latitude");

                        GpsHelper.updateGpsData(longitude, latitude, USER_ID);
                    }
                });
                mqttService.getSubscriber().subscribe("dapi2025/jEGrvfPcYMMuuMgMVCZeOhaSTz03/data");
            } catch (MqttException e) {
                e.printStackTrace();
            }
        });
        t2.setDaemon(true);
        t2.start();

        Thread t3 = new Thread(new RequestService());
        t3.start();

        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
