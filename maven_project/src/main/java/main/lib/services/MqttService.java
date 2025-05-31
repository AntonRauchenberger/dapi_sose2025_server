package main.lib.services;

import java.util.UUID;

import org.eclipse.paho.client.mqttv3.*;

import main.lib.helpers.MessageHandler;

/**
 * Handles mqtt communication
 */
public class MqttService implements MqttCallback {

    private MessageHandler messageHandler;
    private IMqttClient publisher;
    private IMqttClient subscriber;
    public static final String SERVER_URL = "tcp://test.mosquitto.org:1883";

    public MqttService(MessageHandler messageHandler) throws MqttException {
        // for dynamic data handling
        this.messageHandler = messageHandler;

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
        System.out.println("Connection lost: " + cause.getMessage());
        cause.printStackTrace();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if (messageHandler != null) {
            messageHandler.handleMessage(topic, message);
        }
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
