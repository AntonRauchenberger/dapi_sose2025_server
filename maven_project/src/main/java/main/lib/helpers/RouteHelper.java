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
        String currentDate = java.time.LocalDateTime.now().toString();
        Map<String, Object> tempRouteData = new ConcurrentHashMap<>();
        tempRouteData.put("userId", userId);
        tempRouteData.put("startDate", currentDate);
        tempRouteData.put("duration", "");
        tempRouteData.put("distance", "");
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

    private static String calculateDuration(String startDateTime) {
        java.time.LocalDateTime start = java.time.LocalDateTime.parse(startDateTime);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        long minutesBetween = java.time.Duration.between(start, now).toMinutes();
        return String.valueOf(minutesBetween);
    }

    private static String calculateTotalDistance(Map<String, Object> route) {
        @SuppressWarnings("unchecked")
        LinkedList<Map<String, Object>> points = (LinkedList<Map<String, Object>>) route.get("routeData");
        if (points == null || points.size() < 2) {
            return String.valueOf(0.0);
        }

        double totalDistance = 0.0;
        final double R = 6371.0; // eaarth radius in kilometers

        for (int i = 1; i < points.size(); i++) {
            Map<String, Object> prev = points.get(i - 1);
            Map<String, Object> curr = points.get(i);

            double lat1 = (double) prev.get("latitude");
            double lon1 = (double) prev.get("longitude");
            double lat2 = (double) curr.get("latitude");
            double lon2 = (double) curr.get("longitude");

            // Haversine formula
            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lon2 - lon1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                            * Math.sin(dLon / 2) * Math.sin(dLon / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double distance = R * c;

            totalDistance += distance;
        }

        return String.valueOf(Math.round(totalDistance * 100.0) / 100.0);
    }

    public static Map<String, Object> stopRoute(String routeId) {
        Map<String, Object> route = routeData.get(routeId);
        if (route == null) {
            return null;
        }

        // Calculate statistics
        // TODO avgSpeed, maxSpeed
        // duration
        String startDateTime = (String) route.get("startDate");
        String duration = calculateDuration(startDateTime);
        route.put("duration", duration);

        // distance
        String distance = calculateTotalDistance(route);
        route.put("distance", distance);

        runningRoutes.remove(routeId);
        return routeData.remove(routeId);
    }
}