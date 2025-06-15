package main.lib.helpers;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import main.lib.storers.ActivityStateData;
import main.lib.storers.TrafficData;

/**
 * Hilfsklasse zum Speichern und Abrufen der aktuellen GPS- und Aktivitätsdaten
 * eines Hundes.
 */
public class DataHelper {

    // thread-safe map
    private static Map<String, Map<String, Object>> currentData = new ConcurrentHashMap<>();
    private static Map<String, Object> currentActivityStates = new ConcurrentHashMap<>();

    /**
     * Speichert die aktuellen GPS-Daten eines Hundes in einer thread-sicheren Map.
     */
    public static void updateCurrentData(TrafficData data, String userId) {
        if (data == null) {
            System.err.println("TrafficData ist null!");
            return;
        }
        Map<String, Object> tempData = new ConcurrentHashMap<>();
        tempData.put("longitude", data.getLongitude());
        tempData.put("latitude", data.getLatitude());
        tempData.put("speed", data.getSpeed());
        tempData.put("battery", data.getBattery());
        tempData.put("status", data.getStatus() != null ? data.getStatus() : "unknown");
        tempData.put("timestamp", Instant.now().toString());

        currentData.put(userId, tempData);
    }

    public static Map<String, Object> getCurrentData(String userId) {
        return currentData.get(userId);
    }

    /**
     * Speichert den aktuellen Aktivitätszustand eines Hundes in einer
     * thread-sicheren Map.
     */
    public static void updateCurrentActivityState(ActivityStateData data, String userId) {
        if (data == null) {
            System.err.println("ActivityStateData ist null!");
            return;
        }
        Map<String, Object> tempData = new ConcurrentHashMap<>();
        tempData.put("restingTime", data.getRestingTime());
        tempData.put("status", data.getStatus());
        tempData.put("timestamp", data.getTimestamp());

        currentActivityStates.put(userId, tempData);
    }

    public static Object getCurrentActivityState(String userId) {
        return currentActivityStates.get(userId);
    }
}