package net.crumb.lobbyParkour.systems;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ParkourSessionManager {
    private static final Map<UUID, ParkourSession> sessions = new HashMap<>();

    public static void startSession(UUID uuid, String parkourName) {
        sessions.put(uuid, new ParkourSession(parkourName));
    }

    public static void endSession(UUID uuid) {
        sessions.remove(uuid);
    }

    public static boolean isInSession(UUID uuid) {
        return sessions.containsKey(uuid);
    }

    public static ParkourSession getSession(UUID uuid) {
        return sessions.get(uuid);
    }

    public static Map<UUID, ParkourSession> getSessions() {
        return sessions;
    }

    public static void setTime(UUID uuid, float time) {
        ParkourSession session = sessions.get(uuid);
        if (session != null) {
            session.setTime(time);
        }
    }

    public static float getTime(UUID uuid) {
        ParkourSession session = sessions.get(uuid);
        return session != null ? session.getTime() : 0f;
    }
}
