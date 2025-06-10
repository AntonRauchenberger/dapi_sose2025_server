package main.lib.helpers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.List;
import java.time.Instant;
import java.time.Duration;

import main.lib.services.FirestoreService;

public class ActivityAnalyseHelper implements Runnable {

    private String userId;
    private FirestoreService firestoreService;
    private static HashMap<String, Object> currentActivityStates = new HashMap<>();

    /*
     * TODO
     * firestore getLog anpassen
     * Algorithmen durchgehen
     * testen
     */

    public ActivityAnalyseHelper(String userId) throws IOException {
        this.userId = userId;
        this.firestoreService = new FirestoreService();
    }

    private void saveCurrentDataToLog() {
        HashMap<String, Object> currentData = new HashMap<>(DataHelper.getCurrentData(userId));
        try {
            firestoreService.addToSubcollection("logs", userId, "dogLogs", currentData);
        } catch (ExecutionException | InterruptedException e) {
            System.out.println("Error saving current data to log");
            e.printStackTrace();
        }
    }

    private static double calculateAverageRestingTime() {
        try {
            List<Map<String, Object>> logs = firestoreService.getDataByPath("logs/" + userId + "/dogLogs");
            if (logs == null || logs.isEmpty())
                return 0.0;

            Instant now = Instant.now();
            long restingCount = 0;
            long totalCount = 0;

            for (Map<String, Object> entry : logs) {
                String status = (String) entry.get("status");
                String timestampStr = (String) entry.get("timestamp");
                if (timestampStr == null)
                    continue;
                Instant entryTime = Instant.parse(timestampStr);
                long daysAgo = Duration.between(entryTime, now).toDays();
                if (daysAgo <= 7) {
                    totalCount++;
                    if ("ruht".equalsIgnoreCase(status)) {
                        restingCount++;
                    }
                }
            }
            // Jeder Log steht für 30 Minuten
            double totalRestingMinutes = restingCount * 30.0;
            return Math.round(totalRestingMinutes * 100.0) / 100.0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    private static String calculateAverageStateOfMind() {
        try {
            List<Map<String, Object>> logs = firestoreService.getDataByPath("logs/" + userId + "/dogLogs");
            if (logs == null || logs.isEmpty())
                return "Keine Daten";

            Instant now = Instant.now();
            int sum = 0;
            int count = 0;

            // Gewichtung: rennt = -1, ruht = +2, schüttelt sich = 0
            for (Map<String, Object> entry : logs) {
                String status = (String) entry.get("status");
                String timestampStr = (String) entry.get("timestamp");
                if (timestampStr == null)
                    continue;
                Instant entryTime = Instant.parse(timestampStr);
                long daysAgo = Duration.between(entryTime, now).toDays();
                if (daysAgo <= 7) {
                    count++;
                    if ("rennt".equalsIgnoreCase(status))
                        sum += -1;
                    else if ("ruht".equalsIgnoreCase(status))
                        sum += 2;
                    else if ("schüttelt sich".equalsIgnoreCase(status))
                        sum += 0;
                }
            }
            if (count == 0)
                return "Keine Daten";
            double avg = (double) sum / count;

            if (avg >= 1.5)
                return "sehr erholt";
            else if (avg >= 0.5)
                return "erholt";
            else if (avg >= -0.5)
                return "aktiv";
            else
                return "erschöpft";
        } catch (Exception e) {
            e.printStackTrace();
            return "Fehler";
        }
    }

    private static void calculateCurrentState(String userId) {
        String status = calculateAverageStateOfMind();
        double restingTime = calculateAverageRestingTime();
        HashMap<String, Object> currentState = new HashMap<>();
        currentState.put("status", status);
        currentState.put("restingTime", restingTime);
        currentState.put("timestamp", Instant.now().toString());
        currentActivityStates.put(userId, currentState);
    }

    public static Object getCurrentActivityStates(String userId) {
        if (!currentActivityStates.containsKey(userId)) {
            ActivityAnalyseHelper.calculateCurrentState(userId);
        }
        return currentActivityStates.get(userId);
    }

    @Override
    public void run() {
        while (true) {
            saveCurrentDataToLog();
            calculateCurrentState(userId);
            try {

                // TODO remove comment and seccond sleep for production
                // Thread.sleep(1800000); // wait 30 minutes
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
