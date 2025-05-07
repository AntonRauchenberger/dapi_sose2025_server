package main.lib.helpers;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Saves the current GPS data
 */
public class GpsHelper {

    // thread-safe map
    private static Map<String, Map<String, Object>> currentGpsData = new ConcurrentHashMap<>();

    public static void updateGpsData(double longitude, double latitude, String userId) {
        Map<String, Object> tempGpsData = new ConcurrentHashMap<>();
        tempGpsData.put("longitude", longitude);
        tempGpsData.put("latitude", latitude);
        tempGpsData.put("timestamp", Instant.now().toString());

        currentGpsData.put(userId, tempGpsData);
    }

    public static Map<String, Object> getCurrentGpsData(String userId) {
        return currentGpsData.get(userId);
    }
}