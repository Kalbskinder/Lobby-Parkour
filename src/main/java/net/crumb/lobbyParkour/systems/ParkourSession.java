package net.crumb.lobbyParkour.systems;

public class ParkourSession {
    private final String parkourName;
    private long startTimeMillis;
    private long finalTime;
    private int maxCheckpoints;
    private int lastReachedCheckpointIndex;
    private int completedCheckpoints;

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

    public int getMaxCheckpoints() {
        return maxCheckpoints;
    }

    public void setMaxCheckpoints(int maxCheckpoints) {
        this.maxCheckpoints = maxCheckpoints;
    }

    public int getCompletedCheckpoints() {
        return completedCheckpoints;
    }

    public void setCompletedCheckpoints(int completedCheckpoints) {
        this.completedCheckpoints = completedCheckpoints;
    }

    public int getLastReachedCheckpointIndex() {
        return lastReachedCheckpointIndex;
    }

    public void setLastReachedCheckpointIndex(int lastReachedCheckpointIndex) {
        this.lastReachedCheckpointIndex = lastReachedCheckpointIndex;
    }
}
