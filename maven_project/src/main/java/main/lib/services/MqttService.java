package main.lib.services;

import java.util.UUID;

import org.eclipse.paho.client.mqttv3.*;

public class MqttService implements MqttCallback {
    private IMqttClient publisher;
    private IMqttClient subscriber;
    public static final String SERVER_URL = "tcp://broker.mqttdashboard.com:1883";

    public MqttService() throws MqttException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true); // discard unsent messages from a previous run
        options.setConnectionTimeout(10);
        String clientId1 = UUID.randomUUID().toString();
        String clientId2 = UUID.randomUUID().toString();

        this.publisher = new MqttClient(SERVER_URL, clientId1);
        publisher.connect(options); // connect publisher
        System.out.println("publisher connected to broker");

        this.subscriber = new MqttClient(SERVER_URL, clientId2);
        subscriber.setCallback(this);
        subscriber.connect(options); // connect subscriber
        System.out.println("subscriber connected to broker");
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Connection lost");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.out.println("Message: " + message.toString());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    public IMqttClient getPublisher() {
        return this.publisher;
    }

    public IMqttClient getSubscriber() {
        return this.subscriber;
    }
}
