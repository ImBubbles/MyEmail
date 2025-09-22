package dev.haydenholmes.log;

public final class Logger {

    public enum LogLevel {
        DEBUG(0),
        INFO(1),
        WARNING(2),
        EXCEPTION(3),
        ERROR(4);

        private final int weight;

        LogLevel(int weight) {
            this.weight = weight;
        }

        public int getWeight() {
            return weight;
        }
    }

    private static int filter = -1;

    public static int getFilter() {
        return filter;
    }

    public static void log(LogLevel logLevel, String message) {
        if(logLevel.getWeight() < filter) {
            return;
        }
        String prefix = logLevel.name();
        System.out.printf("[%s] %s%n", prefix, message);
    }

    public static void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    public static void info(String message) {
        log(LogLevel.INFO, message);
    }

    public static void warning(String message) {
        log(LogLevel.WARNING, message);
    }

    public static void exception(Exception exception) {
        // TODO make better bruh
        exception.printStackTrace();
    }

    public static void error(String message) {
        log(LogLevel.ERROR, message);
    }

    public static void setFilter(int val) {
        filter=val;
    }

}

