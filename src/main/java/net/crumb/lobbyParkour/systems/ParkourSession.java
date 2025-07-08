package net.crumb.lobbyParkour.systems;

public class ParkourSession {
    private final String parkourName;
    private float time;

    public ParkourSession(String parkourName) {
        this.parkourName = parkourName;
        this.time = 0f;
    }

    public String getParkourName() {
        return parkourName;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }
}
