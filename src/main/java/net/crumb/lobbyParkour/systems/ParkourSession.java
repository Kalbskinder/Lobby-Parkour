package net.crumb.lobbyParkour.systems;

public class ParkourSession {
    private final String parkourName;
    private long startTimeMillis;
    private long finalTime;

    public ParkourSession(String parkourName) {
        this.parkourName = parkourName;
        this.startTimeMillis = System.currentTimeMillis();
    }

    public long getElapsedTimeMillis() {
        return System.currentTimeMillis() - startTimeMillis;
    }

    public float getElapsedSeconds() {
        return getElapsedTimeMillis() / 1000f;
    }

    public String getParkourName() {
        return parkourName;
    }

    public void resetTime() {
        this.startTimeMillis = System.currentTimeMillis();
    }

    public long getFinalTime() {
        return finalTime;
    }

    public void setFinalTime() {
        this.finalTime = getElapsedTimeMillis();
    }
}
