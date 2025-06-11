package main.lib.helpers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.List;
import java.time.Instant;
import java.time.Duration;

import main.lib.services.FirestoreService;
import main.lib.storers.ActivityStateData;

public class ActivityAnalyseHelper implements Runnable {

    private String userId;
    private FirestoreService firestoreService;

    /*
     * TODO
     * Algorithmen durchgehen
     * testen
     */

    public ActivityAnalyseHelper(String userId) throws IOException {
        this.userId = userId;
        this.firestoreService = new FirestoreService();
    }

    private void saveCurrentDataToLog() {
        Map<String, Object> data = DataHelper.getCurrentData(userId);
        if (data == null || data.isEmpty()) {
            return;
        }
        HashMap<String, Object> currentData = new HashMap<>(data);
        try {
            firestoreService.addToSubcollection("logs", userId, "dogLogs", currentData);
        } catch (ExecutionException | InterruptedException e) {
            System.out.println("Error saving current data to log");
            e.printStackTrace();
        }
    }

    private double calculateAverageRestingTime() {
        try {
            List<Map<String, Object>> logs = firestoreService.getAllDocumentDataByPath("logs/" + userId + "/dogLogs");
            if (logs == null || logs.isEmpty())
                return 0.0;

            Instant now = Instant.now();
            long restingCount = 0;

            for (Map<String, Object> entry : logs) {
                String status = (String) entry.get("status");
                String timestampStr = (String) entry.get("timestamp");
                if (timestampStr == null)
                    continue;
                Instant entryTime = Instant.parse(timestampStr);
                long daysAgo = Duration.between(entryTime, now).toDays();
                if (daysAgo >= 0 && daysAgo < 7) {
                    if ("Ruht".equalsIgnoreCase(status)) {
                        restingCount++;
                    }
                }
            }
            // Jeder Log steht für 30 Minuten
            double totalRestingMinutes = restingCount * 30.0;
            // Durchschnitt pro Tag
            double avgRestingPerDay = totalRestingMinutes / 7.0;
            return Math.round(avgRestingPerDay * 100.0) / 100.0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    private String calculateAverageStateOfMind() {
        try {
            List<Map<String, Object>> logs = firestoreService.getAllDocumentDataByPath("logs/" + userId + "/dogLogs");
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
                if (daysAgo >= 0 && daysAgo < 7) {
                    count++;
                    if ("Läuft".equalsIgnoreCase(status))
                        sum += -1;
                    else if ("Ruht".equalsIgnoreCase(status))
                        sum += 2;
                    else if ("Schüttelt sich".equalsIgnoreCase(status))
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

    private void calculateCurrentState(String userId) {
        String status = calculateAverageStateOfMind();
        double restingTime = calculateAverageRestingTime();
        DataHelper.updateCurrentActivityState(new ActivityStateData(restingTime, status, Instant.now().toString()),
                userId);
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
