import java.io.*;
import java.net.*;

public class CheckersClient {

    private static final Logger logger = new Logger(CheckersClient.class);

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final CheckersFrame frame;
    private boolean connected = false;

    public CheckersClient(CheckersFrame frame) {
        this.frame = frame;
    }

    public boolean connectToServer() {
        try {
            if (socket != null && !socket.isClosed()) {
                logger.info("Zamykanie istniejącego połączenia przed ponowną próbą...");
                disconnect();

                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            socket = null;
            out = null;
            in = null;
            connected = false;

            logger.info(
                    "Próba połączenia z serwerem: " + GameConstants.SERVER_ADDRESS + ":" + GameConstants.SERVER_PORT);
            socket = new Socket(GameConstants.SERVER_ADDRESS, GameConstants.SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;

            Thread receiverThread = new Thread(this::receiveMessages);
            receiverThread.setDaemon(true);
            receiverThread.start();

            logger.info("Połączono z serwerem");
            return true;
        } catch (IOException e) {
            logger.error("Nie można połączyć z serwerem", e);
            disconnect();
            return false;
        }
    }

    private void receiveMessages() {
        try {
            String message;
            while (connected && (message = in.readLine()) != null) {
                processServerMessage(message);
            }
        } catch (SocketException e) {
            if (connected) {
                logger.warning("Połączenie zostało nieoczekiwanie zamknięte: " + e.getMessage());
                disconnect();
            } else {
                logger.debug("Zamknięto połączenie z serwerem");
            }
        } catch (IOException e) {
            if (connected) {
                logger.error("Błąd odbierania wiadomości", e);
            }
        } finally {
            disconnect();
        }
    }

    private void processServerMessage(String message) {
        logger.debug("Otrzymano od serwera: " + message);

        String[] msgParts = NetworkProtocol.parseMessage(message);
        String action = msgParts[0];
        String data = msgParts.length > 1 ? msgParts[1] : "";

        switch (action) {
            case NetworkProtocol.RSP_WAITING:
                logger.debug("Serwer potwierdził oczekiwanie na przeciwnika");
                break;
            case NetworkProtocol.RSP_GAME_FOUND:
                frame.hideWaitingDialog();
                if (!data.isEmpty()) {
                    String color = data;
                    logger.info("Rozpoczynam grę jako kolor: " + color);
                    frame.startOnlineGame(color);
                }
                break;
            case NetworkProtocol.RSP_GAME_STARTING:
                if (!data.isEmpty()) {
                    int countdown = Integer.parseInt(data);
                    frame.showTemporaryNotification("Gra rozpocznie się za " + countdown + " sekund", 3000);
                }
                break;
            case NetworkProtocol.RSP_GAME_STARTED:
                frame.showTemporaryNotification("START!", 3000);
                frame.startGame();
                break;
            case NetworkProtocol.RSP_OPPONENT_MOVE:
                if (!data.isEmpty()) {
                    logger.debug("Otrzymano ruch przeciwnika: " + data);
                    frame.applyOpponentMove(data);
                }
                break;
            case NetworkProtocol.RSP_OPPONENT_CAPTURE_CONTINUED:
                if (!data.isEmpty()) {
                    logger.debug("Przeciwnik kontynuuje bicie: " + data);
                    frame.applyOpponentCapture(data);
                }
                break;
            case NetworkProtocol.RSP_TIME_UPDATE:
                Object[] timeData = NetworkProtocol.parseTimeUpdateMessage(message);
                if (timeData != null) {
                    long whiteSeconds = (long) timeData[0];
                    long blackSeconds = (long) timeData[1];
                    String currentTurn = (String) timeData[2];
                    frame.updateGameTime(whiteSeconds, blackSeconds, currentTurn);
                }
                break;
            case NetworkProtocol.RSP_SEARCH_CANCELLED:
                frame.hideWaitingDialog();
                break;
            case NetworkProtocol.RSP_OPPONENT_QUIT:
                frame.showOpponentQuitDialog();
                break;
            case NetworkProtocol.RSP_SESSION_ENDED:
                frame.showTemporaryNotification("Sesja gry zakończona", 2000);
                break;
            default:
                logger.warning("Nieznana wiadomość: " + action);
        }
    }

    public void findGame() {
        if (connected) {
            sendMessage(NetworkProtocol.CMD_FIND_GAME);
        }
    }

    public void cancelSearch() {
        if (connected) {
            sendMessage(NetworkProtocol.CMD_CANCEL_SEARCH);
        }
    }

    public void sendMove(String moveData) {
        if (connected) {
            sendMessage(NetworkProtocol.createMessage(NetworkProtocol.CMD_MOVE, moveData));
        }
    }

    public void sendCaptureContinued(String moveData) {
        if (connected) {
            sendMessage(NetworkProtocol.createMessage(NetworkProtocol.CMD_CAPTURE_CONTINUED, moveData));
        }
    }

    public void quitGame() {
        if (connected) {
            sendMessage(NetworkProtocol.CMD_QUIT);
        }
    }

    public void endSession() {
        if (connected) {
            sendMessage(NetworkProtocol.CMD_END_SESSION);
        }
    }

    public void disconnect() {
        if (!connected) {
            return;
        }

        logger.info("Zamykanie połączenia z serwerem");

        connected = false;

        try {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                }
                out = null;
            }

            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                }
                in = null;
            }

            if (socket != null) {
                try {
                    if (!socket.isClosed()) {
                        socket.close();
                    }
                } catch (Exception e) {
                }
                socket = null;
            }

            logger.info("Pomyślnie rozłączono z serwerem");
        } catch (Exception e) {
            logger.error("Błąd zamykania połączenia", e);
        }
    }

    private void sendMessage(String message) {
        if (out != null) {
            out.println(message);
            logger.debug("Wysłano do serwera: " + message);
        }
    }

    public boolean isConnected() {
        return connected;
    }
}
