package main;

import org.eclipse.paho.client.mqttv3.*;

import main.lib.services.MqttService;

public class Main {

    public static void main(String[] args) throws MqttException, InterruptedException {
        MqttService mqttService = new MqttService();
        mqttService.getSubscriber().subscribe("dapi2025/test");

        Thread.sleep(3000);
        MqttMessage msg = new MqttMessage("Hallo Welt".getBytes());
        msg.setQos(1);
        msg.setRetained(true);

        MqttTopic tempTopic = mqttService.getPublisher().getTopic("dapi2025/test");
        tempTopic.publish(msg);
        System.out.println("data sent");
    }
}