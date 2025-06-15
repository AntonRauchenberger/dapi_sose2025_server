package main.lib.helpers;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Interface zur Verarbeitung von empfangenen MQTT-Nachrichten.
 */
public interface MessageHandler {
    /**
     * Wird aufgerufen, wenn eine neue MQTT-Nachricht empfangen wurde.
     *
     * @param topic   Das Topic, auf dem die Nachricht empfangen wurde.
     * @param message Die empfangene MQTT-Nachricht.
     */
    void handleMessage(String topic, MqttMessage message);
}
