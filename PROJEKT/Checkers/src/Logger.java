import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Klasa Logger służy do spójnego logowania informacji w aplikacji.
 * Umożliwia włączanie/wyłączanie logowania i filtrowanie wiadomości według
 * poziomu ważności.
 */
public class Logger {

    // Poziomy logowania
    public enum Level {
        DEBUG(1, "DEBUG"),
        INFO(2, "INFO"),
        WARNING(3, "WARNING"),
        ERROR(4, "ERROR"),
        NONE(5, "NONE");

        private final int value;
        private final String name;

        Level(int value, String name) {
            this.value = value;
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public String getName() {
            return name;
        }
    }

    // Czy logowanie jest włączone
    private static boolean loggingEnabled = true;

    // Minimalny poziom logowania
    private static Level minimumLevel = Level.INFO;

    // Format daty i czasu
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // Źródło (klasa) logów
    private final String source;

    /**
     * Tworzy nowy logger dla określonego źródła.
     *
     * @param source Nazwa klasy źródłowej
     */
    public Logger(String source) {
        this.source = source;
    }

    /**
     * Tworzy nowy logger dla klasy.
     *
     * @param clazz Klasa źródłowa
     */
    public Logger(Class<?> clazz) {
        this.source = clazz.getSimpleName();
    }

    /**
     * Włącza lub wyłącza logowanie globalnie.
     *
     * @param enabled Czy logowanie jest włączone
     */
    public static void setLoggingEnabled(boolean enabled) {
        loggingEnabled = enabled;
    }

    /**
     * Ustawia minimalny poziom logowania.
     *
     * @param level Minimalny poziom
     */
    public static void setMinimumLevel(Level level) {
        minimumLevel = level;
    }

    /**
     * Sprawdza, czy logowanie na danym poziomie jest włączone.
     *
     * @param level Poziom do sprawdzenia
     * @return true jeśli logowanie na tym poziomie jest włączone
     */
    public static boolean isLevelEnabled(Level level) {
        return loggingEnabled && level.getValue() >= minimumLevel.getValue();
    }

    /**
     * Loguje wiadomość na poziomie DEBUG.
     *
     * @param message Wiadomość do zalogowania
     */
    public void debug(String message) {
        log(Level.DEBUG, message);
    }

    /**
     * Loguje wiadomość na poziomie INFO.
     *
     * @param message Wiadomość do zalogowania
     */
    public void info(String message) {
        log(Level.INFO, message);
    }

    /**
     * Loguje wiadomość na poziomie WARNING.
     *
     * @param message Wiadomość do zalogowania
     */
    public void warning(String message) {
        log(Level.WARNING, message);
    }

    /**
     * Loguje wiadomość na poziomie ERROR.
     *
     * @param message Wiadomość do zalogowania
     */
    public void error(String message) {
        log(Level.ERROR, message);
    }

    /**
     * Loguje wiadomość na poziomie ERROR z wyjątkiem.
     *
     * @param message Wiadomość do zalogowania
     * @param e       Wyjątek
     */
    public void error(String message, Exception e) {
        error(message + ": " + e.getMessage());
        if (isLevelEnabled(Level.DEBUG)) {
            e.printStackTrace();
        }
    }

    /**
     * Loguje wiadomość na określonym poziomie.
     *
     * @param level   Poziom logowania
     * @param message Wiadomość do zalogowania
     */
    private void log(Level level, String message) {
        if (!isLevelEnabled(level)) {
            return;
        }

        String timestamp = LocalDateTime.now().format(formatter);
        String formattedMessage = String.format("[%s] [%s] [%s] %s",
                timestamp,
                level.getName(),
                source,
                message);

        if (level == Level.ERROR) {
            System.err.println(formattedMessage);
        } else {
            System.out.println(formattedMessage);
        }
    }
}