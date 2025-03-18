import javax.swing.SwingUtilities;

public class Main {
    private static final Logger logger = new Logger(Main.class);

    public static void main(String[] args) {
        configureApplication(args);

        logger.info("Uruchamianie aplikacji Warcaby");

        if (args.length > 0 && args[0].equalsIgnoreCase("server")) {
            logger.info("Uruchamianie serwera...");
            Server.main(new String[0]);
        } else {
            logger.info("Uruchamianie klienta...");
            SwingUtilities.invokeLater(() -> {
                CheckersFrame frame = new CheckersFrame();
                frame.setVisible(true);
            });
        }
    }

    /**
     * Konfiguruje aplikację, w tym logowanie.
     *
     * @param args Argumenty wiersza poleceń
     */
    private static void configureApplication(String[] args) {
        // Domyślnie włącz logowanie
        ApplicationConfig.initialize();

        // Sprawdź, czy jest flaga do wyłączenia logowania
        for (String arg : args) {
            if (arg.equals("--no-log")) {
                ApplicationConfig.configureLogging(false);
                break;
            }
        }
    }
}

// javac src/*.java -d out
// java -cp out Main server (z logowaniem)
// java -cp out Main client (z logowaniem)
// java -cp out Main server --no-log (bez logowania)
// java -cp out Main client --no-log (bez logowania)