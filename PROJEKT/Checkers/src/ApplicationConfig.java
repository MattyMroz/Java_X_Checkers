/**
 * Klasa do prostej konfiguracji logowania aplikacji.
 */
public class ApplicationConfig {
    private static final Logger logger = new Logger(ApplicationConfig.class);

    /**
     * Włącza lub wyłącza logowanie.
     *
     * @param enabled Czy logowanie jest włączone
     */
    public static void configureLogging(boolean enabled) {
        Logger.setLoggingEnabled(enabled);

        if (enabled) {
            Logger.setMinimumLevel(Logger.Level.DEBUG); // Logowanie wszystkiego
            logger.info("Włączono logowanie");
        }
    }

    /**
     * Inicjalizuje domyślne ustawienia logowania (włączone)
     */
    public static void initialize() {
        configureLogging(true);
    }
}