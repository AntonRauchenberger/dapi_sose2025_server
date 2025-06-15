package main.lib.storers;

/**
 * Datenklasse zur Speicherung des aktuellen Aktivitätszustands eines Hundes
 * (Ruhezeit, Status, Zeitstempel).
 */
public class ActivityStateData {
    private double restingTime;
    private String status;
    private String timestamp;

    public ActivityStateData(double restingTime, String status, String timestamp) {
        this.restingTime = restingTime;
        this.status = status;
        this.timestamp = timestamp;
    }

    public double getRestingTime() {
        return this.restingTime;
    }

    public void setRestingTime(double restingTime) {
        this.restingTime = restingTime;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setRestingTime(String timestamp) {
        this.timestamp = timestamp;
    }
}
