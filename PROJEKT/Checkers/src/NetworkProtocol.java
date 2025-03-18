/**
 * Klasa definiująca protokół komunikacyjny dla warcabów online.
 * Zawiera stałe i metody wspólne dla klienta i serwera.
 */
public class NetworkProtocol {

    // Stałe komunikatów od klienta do serwera
    public static final String CMD_FIND_GAME = "FIND_GAME";
    public static final String CMD_MOVE = "MOVE";
    public static final String CMD_CAPTURE_CONTINUED = "CAPTURE_CONTINUED";
    public static final String CMD_CANCEL_SEARCH = "CANCEL_SEARCH";
    public static final String CMD_QUIT = "QUIT";
    public static final String CMD_END_SESSION = "END_SESSION";

    // Stałe komunikatów od serwera do klienta
    public static final String RSP_WAITING = "WAITING";
    public static final String RSP_GAME_FOUND = "GAME_FOUND";
    public static final String RSP_GAME_STARTING = "GAME_STARTING";
    public static final String RSP_GAME_STARTED = "GAME_STARTED";
    public static final String RSP_OPPONENT_MOVE = "OPPONENT_MOVE";
    public static final String RSP_OPPONENT_CAPTURE_CONTINUED = "OPPONENT_CAPTURE_CONTINUED";
    public static final String RSP_TIME_UPDATE = "TIME_UPDATE";
    public static final String RSP_SEARCH_CANCELLED = "SEARCH_CANCELLED";
    public static final String RSP_OPPONENT_QUIT = "OPPONENT_QUIT";
    public static final String RSP_SESSION_ENDED = "SESSION_ENDED";

    // Stałe dla kolorów graczy
    public static final String COLOR_WHITE = "WHITE";
    public static final String COLOR_BLACK = "BLACK";

    // Separator używany w komunikatach
    public static final String SEPARATOR = ":";

    /**
     * Parsuje komunikat o ruchu.
     *
     * @param moveMessage Komunikat o ruchu
     * @return Tablica [fromX, fromY, toX, toY] lub null jeśli format jest
     * niepoprawny
     */
    public static int[] parseMoveMessage(String moveMessage) {
        try {
            String[] positions = moveMessage.split("->");
            if (positions.length != 2) {
                return null;
            }

            String[] from = positions[0].split(",");
            String[] to = positions[1].split(",");

            if (from.length != 2 || to.length != 2) {
                return null;
            }

            int[] result = new int[4];
            result[0] = Integer.parseInt(from[0]); // fromX
            result[1] = Integer.parseInt(from[1]); // fromY
            result[2] = Integer.parseInt(to[0]); // toX
            result[3] = Integer.parseInt(to[1]); // toY

            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Tworzy pełny komunikat z kodem i danymi.
     *
     * @param command Kod komendy
     * @param data    Dane (opcjonalne)
     * @return Pełny komunikat
     */
    public static String createMessage(String command, String data) {
        if (data != null && !data.isEmpty()) {
            return command + SEPARATOR + data;
        }
        return command;
    }

    /**
     * Parsuje komunikat na kod i dane.
     *
     * @param message Komunikat do sparsowania
     * @return Tablica [kod, dane] lub [kod, ""] jeśli brak danych
     */
    public static String[] parseMessage(String message) {
        String[] parts = message.split(SEPARATOR, 2);
        if (parts.length == 1) {
            return new String[]{parts[0], ""};
        }
        return parts;
    }

    /**
     * Tworzy komunikat aktualizacji czasu.
     *
     * @param whiteSeconds Czas białego gracza w sekundach
     * @param blackSeconds Czas czarnego gracza w sekundach
     * @param currentTurn  Aktualny gracz ("WHITE" lub "BLACK")
     * @return Komunikat aktualizacji czasu
     */
    public static String createTimeUpdateMessage(long whiteSeconds, long blackSeconds, String currentTurn) {
        return RSP_TIME_UPDATE + SEPARATOR + whiteSeconds + SEPARATOR + blackSeconds + SEPARATOR + currentTurn;
    }

    /**
     * Parsuje komunikat aktualizacji czasu.
     *
     * @param message Komunikat aktualizacji czasu
     * @return Tablica [whiteSeconds, blackSeconds, currentTurn] lub null jeśli
     * format jest niepoprawny
     */
    public static Object[] parseTimeUpdateMessage(String message) {
        try {
            String[] parts = message.split(SEPARATOR);
            if (parts.length != 4 || !parts[0].equals(RSP_TIME_UPDATE)) {
                return null;
            }

            long whiteSeconds = Long.parseLong(parts[1]);
            long blackSeconds = Long.parseLong(parts[2]);
            String currentTurn = parts[3];

            return new Object[]{whiteSeconds, blackSeconds, currentTurn};
        } catch (Exception e) {
            return null;
        }
    }
}