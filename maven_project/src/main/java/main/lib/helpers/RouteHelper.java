package main.lib.helpers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Hilfsklasse zur Verwaltung von Routen, deren Aufzeichnung und zur Berechnung
 * von Routenstatistiken.
 */
public class RouteHelper {

    // thread-safe map
    private static Map<String, Map<String, Object>> routeData = new ConcurrentHashMap<>();
    private static ConcurrentLinkedQueue<String> runningRoutes = new ConcurrentLinkedQueue<>();

    /**
     * Fügt einen neuen GPS-Punkt mit Geschwindigkeit zu einer Route hinzu.
     */
    private static boolean addRoutePoint(String routeId, double latitude, double longitude, double speed) {
        Map<String, Object> route = routeData.get(routeId);
        if (route == null) {
            return false;
        }

        LinkedList<Map<String, Object>> routePoints = (LinkedList<Map<String, Object>>) route.get("routeData");

        Map<String, Object> point = new ConcurrentHashMap<>();
        point.put("latitude", latitude);
        point.put("longitude", longitude);
        point.put("timestamp", java.time.Instant.now().toString());
        point.put("speed", speed);

        routePoints.add(point);

        return true;
    }

    /**
     * Startet die Aufzeichnung einer neuen Route für einen Hund.
     */
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

        // Adds point data for the route every 10 seconds
        Thread thread = new Thread(() -> {
            try {
                while (runningRoutes.contains(routeId)) {
                    Map<String, Object> data = DataHelper.getCurrentData(userId);
                    if (data != null) {
                        double latitude = (double) data.get("latitude");
                        double longitude = (double) data.get("longitude");
                        double speed = (double) data.get("speed");

                        addRoutePoint(routeId, latitude, longitude, speed);
                    }

                    Thread.sleep(10000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    /**
     * Beendet die Aufzeichnung einer Route, berechnet Statistiken und gibt die
     * Routendaten zurück.
     */
    public static Map<String, Object> stopRoute(String routeId) {
        Map<String, Object> route = routeData.get(routeId);
        if (route == null) {
            return null;
        }

        // Calculate statistics
        // duration
        String startDateTime = (String) route.get("startDate");
        String duration = StatisticsHelper.calculateDuration(startDateTime);
        route.put("duration", duration);

        // distance
        String distance = StatisticsHelper.calculateTotalDistance(route);
        route.put("distance", distance);

        // avg and max speed
        Map<String, String> calculatedValues = new HashMap<>(StatisticsHelper.calculateMaxAndMinSpeed(route));
        route.put("avgSpeed", calculatedValues.get("avgSpeed"));
        route.put("maxSpeed", calculatedValues.get("maxSpeed"));

        runningRoutes.remove(routeId);
        return routeData.remove(routeId);
    }
}