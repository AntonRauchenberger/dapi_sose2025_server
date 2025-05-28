package main.lib.storers;

public class TrafficData {
    private double longitude;
    private double latitude;
    private double speed;

    public TrafficData(double longitude, double latitude, double speed) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.speed = speed;
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
}