import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 5000;
    private static final ExecutorService pool = Executors.newFixedThreadPool(10);
    private static final Map<String, ClientHandler> waitingPlayers = new ConcurrentHashMap<>();
    private static final Map<String, GameSession> activeSessions = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Serwer warcabów uruchomiony na porcie " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nowe połączenie: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket);
                pool.execute(handler);
            }
        } catch (IOException e) {
            System.err.println("Błąd serwera: " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }

    static class ClientHandler implements Runnable {

        private final Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private final String playerId;
        private GameSession gameSession;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.playerId = UUID.randomUUID().toString();
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    processCommand(inputLine);
                }
            } catch (IOException e) {
                System.err.println("Błąd obsługi klienta: " + e.getMessage());
            } finally {
                disconnect();
            }
        }

        private void processCommand(String command) {
            System.out.println("Otrzymano komendę: " + command);

            String[] parts = command.split(":");
            String action = parts[0];

            switch (action) {
                case "FIND_GAME":
                    findGame();
                    break;
                case "MOVE":
                    if (parts.length > 1) {
                        System.out.println("Przekazuję ruch: " + parts[1]);
                        sendMoveToOpponent(parts[1]);
                    }
                    break;
                case "CAPTURE_CONTINUED":
                    if (parts.length > 1) {
                        System.out.println("Przekazuję kontynuację bicia: " + parts[1]);
                        sendCaptureContinuedToOpponent(parts[1]);
                    }
                    break;
                case "CANCEL_SEARCH":
                    cancelSearch();
                    break;
                case "QUIT":
                    handleQuitRequest();
                    break;
                case "END_SESSION":
                    handleEndSessionRequest();
                    break;
                default:
                    System.out.println("Nieznana komenda: " + action);
            }
        }

        private void findGame() {
            sendMessage("WAITING");

            if (!waitingPlayers.isEmpty()) {
                Map.Entry<String, ClientHandler> entry = waitingPlayers.entrySet().iterator().next();
                String waitingPlayerId = entry.getKey();
                ClientHandler opponent = entry.getValue();

                waitingPlayers.remove(waitingPlayerId);

                boolean isWhite = new Random().nextBoolean();
                ClientHandler whitePlayer = isWhite ? this : opponent;
                ClientHandler blackPlayer = isWhite ? opponent : this;

                gameSession = new GameSession(whitePlayer, blackPlayer);
                opponent.gameSession = gameSession;

                String sessionId = UUID.randomUUID().toString();
                activeSessions.put(sessionId, gameSession);

                gameSession.startGameTimer();

                whitePlayer.sendMessage("GAME_FOUND:WHITE");
                blackPlayer.sendMessage("GAME_FOUND:BLACK");
            } else {
                waitingPlayers.put(playerId, this);
            }
        }

        private void sendMoveToOpponent(String moveData) {
            if (gameSession != null) {
                gameSession.updateTimeAfterMove();

                ClientHandler opponent = gameSession.getOpponent(this);
                if (opponent != null) {
                    System.out.println("Wysyłam ruch do przeciwnika: " + moveData);
                    opponent.sendMessage("OPPONENT_MOVE:" + moveData);
                } else {
                    System.out.println("Nie znaleziono przeciwnika!");
                }

                gameSession.sendTimeUpdate();
            } else {
                System.out.println("Brak aktywnej sesji gry!");
            }
        }

        private void sendCaptureContinuedToOpponent(String moveData) {
            if (gameSession != null) {

                ClientHandler opponent = gameSession.getOpponent(this);
                if (opponent != null) {
                    System.out.println("Wysyłam kontynuację bicia do przeciwnika: " + moveData);
                    opponent.sendMessage("OPPONENT_CAPTURE_CONTINUED:" + moveData);
                } else {
                    System.out.println("Nie znaleziono przeciwnika!");
                }
            } else {
                System.out.println("Brak aktywnej sesji gry!");
            }
        }

        private void cancelSearch() {
            waitingPlayers.remove(playerId);
            sendMessage("SEARCH_CANCELLED");
        }

        private void handleQuitRequest() {
            if (gameSession != null) {
                ClientHandler opponent = gameSession.getOpponent(this);
                if (opponent != null) {
                    opponent.sendMessage("OPPONENT_QUIT");
                }

                gameSession.stopGameTimer();

                for (Map.Entry<String, GameSession> entry : activeSessions.entrySet()) {
                    if (entry.getValue() == gameSession) {
                        activeSessions.remove(entry.getKey());
                        break;
                    }
                }

                gameSession = null;
            }
        }

        private void handleEndSessionRequest() {
            if (gameSession != null) {
                gameSession.stopGameTimer();

                sendMessage("SESSION_ENDED");

                ClientHandler opponent = gameSession.getOpponent(this);
                if (opponent != null) {
                    opponent.sendMessage("SESSION_ENDED");
                }

                for (Map.Entry<String, GameSession> entry : activeSessions.entrySet()) {
                    if (entry.getValue() == gameSession) {
                        activeSessions.remove(entry.getKey());
                        break;
                    }
                }

                gameSession = null;
            }
        }

        private void disconnect() {
            try {
                if (gameSession != null) {
                    handleQuitRequest();
                }

                waitingPlayers.remove(playerId);

                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                System.err.println("Błąd podczas rozłączania: " + e.getMessage());
            }
        }

        public void sendMessage(String message) {
            if (out != null) {
                out.println(message);
                System.out.println("Wysłano do " + playerId + ": " + message);
            }
        }
    }

    static class GameSession {
        private final ClientHandler whitePlayer;
        private final ClientHandler blackPlayer;

        private long whiteTime = 0;
        private long blackTime = 0;
        private long turnStartTime;
        private boolean whitesTurn = true;
        private Timer gameTimer;
        private boolean gameInProgress = false;

        public GameSession(ClientHandler whitePlayer, ClientHandler blackPlayer) {
            this.whitePlayer = whitePlayer;
            this.blackPlayer = blackPlayer;
        }

        public void startGameTimer() {
            whiteTime = 0;
            blackTime = 0;
            whitesTurn = true;
            gameInProgress = true;

            whitePlayer.sendMessage("GAME_STARTING:5");
            blackPlayer.sendMessage("GAME_STARTING:5");

            Timer startDelayTimer = new Timer();
            startDelayTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    turnStartTime = System.currentTimeMillis();

                    whitePlayer.sendMessage("GAME_STARTED");
                    blackPlayer.sendMessage("GAME_STARTED");

                    gameTimer = new Timer();
                    gameTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            if (gameInProgress) {
                                sendTimeUpdate();
                            }
                        }
                    }, 0, 1000);
                }
            }, 5000);
        }

        public void stopGameTimer() {
            if (gameTimer != null) {
                gameTimer.cancel();
                gameTimer = null;
            }
            gameInProgress = false;
        }

        public void updateTimeAfterMove() {
            long currentTime = System.currentTimeMillis();

            if (whitesTurn) {
                whiteTime += (currentTime - turnStartTime);
            } else {
                blackTime += (currentTime - turnStartTime);
            }

            whitesTurn = !whitesTurn;
            turnStartTime = currentTime;
        }

        public void sendTimeUpdate() {
            long currentWhiteTime = whiteTime;
            long currentBlackTime = blackTime;

            if (gameInProgress) {
                long currentTime = System.currentTimeMillis();
                if (whitesTurn) {
                    currentWhiteTime += (currentTime - turnStartTime);
                } else {
                    currentBlackTime += (currentTime - turnStartTime);
                }
            }

            long whiteSeconds = currentWhiteTime / 1000;
            long blackSeconds = currentBlackTime / 1000;

            String timeMessage = "TIME_UPDATE:" + whiteSeconds + ":" + blackSeconds + ":"
                    + (whitesTurn ? "WHITE" : "BLACK");
            whitePlayer.sendMessage(timeMessage);
            blackPlayer.sendMessage(timeMessage);
        }

        public ClientHandler getOpponent(ClientHandler player) {
            if (player == whitePlayer) {
                return blackPlayer;
            } else if (player == blackPlayer) {
                return whitePlayer;
            }
            return null;
        }
    }
}
