package main.lib.helpers;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Handles route tracking and statistics
 */
public class RouteHelper {

    // thread-safe map
    private static Map<String, Map<String, Object>> routeData = new ConcurrentHashMap<>();
    private static ConcurrentLinkedQueue<String> runningRoutes = new ConcurrentLinkedQueue<>();

    private static boolean addRoutePoint(String routeId, double latitude, double longitude) {
        Map<String, Object> route = routeData.get(routeId);
        if (route == null) {
            return false;
        }

        LinkedList<Map<String, Object>> routePoints = (LinkedList<Map<String, Object>>) route.get("routeData");
        Map<String, Object> point = new ConcurrentHashMap<>();
        point.put("latitude", latitude);
        point.put("longitude", longitude);
        point.put("timestamp", java.time.Instant.now().toString());
        routePoints.add(point);

        return true;
    }

    public static void startRoute(String userId, String routeId) {
        String currentDate = java.time.LocalDate.now().toString();
        Map<String, Object> tempRouteData = new ConcurrentHashMap<>();
        tempRouteData.put("userId", userId);
        tempRouteData.put("startDate", currentDate);
        tempRouteData.put("duration", "");
        tempRouteData.put("routeData", new LinkedList<Map<String, Object>>());
        tempRouteData.put("avgSpeed", "");
        tempRouteData.put("maxSpeed", "");

        routeData.put(routeId, tempRouteData);
        runningRoutes.add(routeId);

        Thread thread = new Thread(() -> {
            try {
                while (runningRoutes.contains(routeId)) {
                    Map<String, Object> data = GpsHelper.getCurrentGpsData(userId);
                    if (data != null) {
                        double latitude = (double) data.get("latitude");
                        double longitude = (double) data.get("longitude");

                        addRoutePoint(routeId, latitude, longitude);
                    }

                    Thread.sleep(10000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public static Map<String, Object> stopRoute(String routeId) {
        Map<String, Object> route = routeData.get(routeId);
        if (route == null) {
            return null;
        }

        // Calculate statistics
        // TODO avgSpeed, maxSpeed, duration

        runningRoutes.remove(routeId);
        return routeData.remove(routeId);
    }
}