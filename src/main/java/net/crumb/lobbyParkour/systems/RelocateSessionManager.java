package net.crumb.lobbyParkour.systems;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RelocateSessionManager {
    private static final Map<UUID, RelocateCheckpoint> relocationSessions = new HashMap<>();

    public static Map<UUID, RelocateCheckpoint> getRelocationSessions() {
        return relocationSessions;
    }

    public static boolean isInSession(UUID uuid) {
        return relocationSessions.containsKey(uuid);
    }

    public static void removeRelocationSession(UUID uuid) {
        relocationSessions.remove(uuid);
    }
}
