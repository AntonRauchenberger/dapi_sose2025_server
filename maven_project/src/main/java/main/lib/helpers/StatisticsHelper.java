package main.lib.helpers;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import main.lib.services.FirestoreService;

public class StatisticsHelper {

    public static String calculateDuration(String startDateTime) {
        java.time.LocalDateTime start = java.time.LocalDateTime.parse(startDateTime);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        long minutesBetween = java.time.Duration.between(start, now).toMinutes();
        return String.valueOf(minutesBetween);
    }

    public static String calculateTotalDistance(Map<String, Object> route) {
        @SuppressWarnings("unchecked")
        LinkedList<Map<String, Object>> points = (LinkedList<Map<String, Object>>) route.get("routeData");
        if (points == null || points.size() < 2) {
            return String.valueOf(0.0);
        }

        double totalDistance = 0.0;
        final double R = 6371.0;

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

    public static LinkedList<String> calculateDistanceDevelopment(String userId, FirestoreService firestoreService)
            throws IOException, ExecutionException, InterruptedException {
        LinkedList<String> distanceDevelopment = new LinkedList<>();

        Map<String, Object> data = firestoreService.getDocumentDataByPath("routes/" + userId);
        if (data == null) {
            return distanceDevelopment;
        }

        List<Map<String, Object>> routes = (List<Map<String, Object>>) data.get("routes");
        if (routes == null) {
            return distanceDevelopment;
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        for (Map<String, Object> route : routes) {
            if (distanceDevelopment.size() >= 14) {
                break;
            }

            Object startDateObj = route.get("startDate");
            Object distanceObj = route.get("distance");

            if (startDateObj == null || distanceObj == null) {
                continue;
            }

            String distanceStr = distanceObj.toString();

            java.time.LocalDateTime startDateTime = null;
            try {
                startDateTime = java.time.LocalDateTime.parse(startDateObj.toString());
            } catch (Exception e) {
                try {
                    java.time.LocalDate date = java.time.LocalDate.parse(startDateObj.toString());
                    startDateTime = date.atStartOfDay();
                } catch (Exception ex) {
                    continue;
                }
            }

            if (startDateTime != null && java.time.Duration.between(startDateTime, now).toDays() <= 14) {
                distanceDevelopment.add(distanceStr);
            }
        }

        return distanceDevelopment;
    }
}
