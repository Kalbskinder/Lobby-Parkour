package net.crumb.lobbyParkour.utils;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigManager {
    private static FileConfiguration config;

    public static void loadConfig(FileConfiguration config) {
        ConfigManager.config = config;
    }

    public static Format getFormat() {
        return new Format();
    }

    public static class Format {
        private final String path = "formatting.";

        public String getStartPlate() {
            return config.getString(path + "start-plate");
        }

        public String getEndPlate() {
            return config.getString(path + "end-plate");
        }

        public String getCheckpointPlate() {
            return config.getString(path + "checkpoint-plate");
        }

        public String getTimer() {
            return config.getString(path + "timer");
        }

        public String getStartMessage() {
            return config.getString(path + "start-message");
        }

        public String getCancelMessage() {
            return config.getString(path + "cancel-message");
        }

        public String getEndMessage() {
            return config.getString(path + "end-message");
        }

        public String getResetMessage() {
            return config.getString(path + "reset-message");
        }

        public String getTpMessage() {
            return config.getString(path + "tp-message");
        }

        public String getCheckpointMessage() {
            return config.getString(path + "checkpoint-message");
        }

        public String getActionBar() {
            return config.getString(path + "action-bar");
        }

        public Leaderboard getLeaderboard() {
            return new Leaderboard(path + "leaderboard.");
        }

        public static class Leaderboard {
            private final String path;

            public Leaderboard(String basePath) {
                this.path = basePath;
            }

            public String getTitle() {
                return config.getString(path + "title");
            }

            public int getMaximumDisplayed() {
                return config.getInt(path + "maximum-displayed");
            }

            public boolean isPersonalBestEnabled() {
                return config.getBoolean(path + "personal-best-enabled");
            }

            public String getDefaultLineStyle() {
                return config.getString(path + "default-line-style");
            }

            public String getPersonalBestStyle() {
                return config.getString(path + "personal-best-style");
            }

            public String getEmptyLineStyle() {
                return config.getString(path + "empty-line-style");
            }

            public List<String> getLines() {
                return config.getStringList(path + "lines");
            }
        }
    }
}
