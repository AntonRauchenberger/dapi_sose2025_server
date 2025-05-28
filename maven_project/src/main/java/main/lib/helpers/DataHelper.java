package main.lib.helpers;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import main.lib.storers.TrafficData;

/**
 * Saves the current GPS data
 */
public class DataHelper {

    // thread-safe map
    private static Map<String, Map<String, Object>> currentData = new ConcurrentHashMap<>();

    public static void updateCurrentData(TrafficData data, String userId) {
        Map<String, Object> tempGpsData = new ConcurrentHashMap<>();
        tempGpsData.put("longitude", data.getLongitude());
        tempGpsData.put("latitude", data.getLatitude());
        tempGpsData.put("speed", data.getSpeed());
        tempGpsData.put("timestamp", Instant.now().toString());

        currentData.put(userId, tempGpsData);
    }

    public static Map<String, Object> getCurrentData(String userId) {
        return currentData.get(userId);
    }
}