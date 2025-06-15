package main;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import org.eclipse.paho.client.mqttv3.*;
import com.google.gson.Gson;
import main.lib.helpers.DataHelper;
import main.lib.helpers.MessageHandler;
import main.lib.helpers.ActivityAnalyseHelper;
import main.lib.services.MqttService;
import main.lib.services.RequestService;
import main.lib.storers.TrafficData;

/**
 * Hauptklasse zum Starten des Servers und Initialisieren der Threads für MQTT,
 * HTTP und Aktivitätsanalyse.
 */
public class Main {

    final static String USER_ID = "jEGrvfPcYMMuuMgMVCZeOhaSTz03";

    public static void main(String[] args) throws MqttException, InterruptedException, IOException, ExecutionException {
        // Thread für den Empfang und die Verarbeitung von MQTT-Nachrichten starten
        Thread thread1 = new Thread(() -> {
            try {
                MqttService mqttService = new MqttService(new MessageHandler() {
                    @Override
                    public void handleMessage(String topic, MqttMessage message) {
                        String payload = new String(message.getPayload());
                        System.out.println(message.toString());

                        Gson gson = new Gson();

                        Map<String, Object> data = gson.fromJson(payload, Map.class);
                        double longitude = (double) data.get("longitude");
                        double latitude = (double) data.get("latitude");
                        double speed = (double) data.get("speed_kmph");
                        int battery = ((Double) data.get("akku")).intValue();
                        String status = (String) data.get("aktion");

                        DataHelper.updateCurrentData(new TrafficData(longitude, latitude, speed,
                                battery, status),
                                USER_ID);

                    }
                });
                mqttService.getSubscriber().subscribe("dapi2025/jEGrvfPcYMMuuMgMVCZeOhaSTz03/test");
            } catch (MqttException e) {
                e.printStackTrace();
            }
        });
        thread1.start();

        // Thread für den HTTP-Server (RequestService) starten
        Thread thread2 = new Thread(new RequestService());
        thread2.start();

        // Thread für die zyklische Aktivitätsanalyse starten
        Thread thread3 = new Thread(new ActivityAnalyseHelper(USER_ID));
        thread3.start();

        try {
            thread1.join();
            thread2.join();
            thread3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
