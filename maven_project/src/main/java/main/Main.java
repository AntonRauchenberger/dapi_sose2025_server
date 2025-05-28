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
import main.lib.services.MqttService;
import main.lib.services.RequestService;
import main.lib.storers.TrafficData;

public class Main {

    final static String USER_ID = "jEGrvfPcYMMuuMgMVCZeOhaSTz03";

    public static void main(String[] args) throws MqttException, InterruptedException, IOException, ExecutionException {
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
                    double longitude = -180.0 + (random.nextDouble() * 360.0);
                    double latitude = -90.0 + (random.nextDouble() * 180.0);
                    double speed = 3.0 + (random.nextDouble() * 20.0);
                    int battery = (int) (1 + random.nextDouble() * 100);
                    String status = "Schüttelt sich";

                    Map<String, Object> data = new HashMap<>();
                    data.put("longitude", longitude);
                    data.put("latitude", latitude);
                    data.put("speed", speed);
                    data.put("battery", battery);
                    data.put("status", status);

                    Gson gson = new Gson();
                    String jsonPayload = gson.toJson(data);

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

                        Map<String, Object> data = gson.fromJson(payload, Map.class);
                        double longitude = (double) data.get("longitude");
                        double latitude = (double) data.get("latitude");
                        double speed = (double) data.get("speed");
                        int battery = ((Double) data.get("battery")).intValue();
                        String status = (String) data.get("status");

                        DataHelper.updateCurrentData(new TrafficData(longitude, latitude, speed, battery, status),
                                USER_ID);
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
