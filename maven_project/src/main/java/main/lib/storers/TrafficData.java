package main.lib.storers;

public class TrafficData {
    private double longitude;
    private double latitude;
    private double speed;
    private int battery;
    private String status;

    public TrafficData(double longitude, double latitude, double speed, int battery, String status) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.speed = speed;
        this.speed = speed;
        this.battery = battery;
        this.status = status;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}