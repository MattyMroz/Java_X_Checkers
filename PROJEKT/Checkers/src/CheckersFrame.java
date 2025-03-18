import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class CheckersFrame extends JFrame {

    private final CheckersClient client;
    private boolean isOnlineGame = false;
    private String playerColor;
    private final CheckersPanel warcabyPanel;
    private JDialog waitingDialog = null;

    public CheckersFrame() {
        setTitle("Warcaby");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        client = new CheckersClient(this);

        warcabyPanel = new CheckersPanel(this);
        add(warcabyPanel, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();

                new Thread(() -> {
                    try {
                        if (isOnlineGame) {
                            client.endSession();
                        }
                        client.disconnect();
                    } catch (Exception ex) {
                        System.err.println("Błąd podczas zamykania połączenia: " + ex.getMessage());
                    } finally {
                        System.exit(0);
                    }
                }).start();
            }
        });

        pack();
        setLocationRelativeTo(null);
    }

    public boolean isOnlineGame() {
        return isOnlineGame;
    }

    public String getPlayerColor() {
        return playerColor;
    }

    public CheckersClient getClient() {
        return client;
    }

    public void showWaitingForOpponentDialog() {
        if (waitingDialog != null) {
            return;
        }

        waitingDialog = DialogManager.createWaitingDialog(this, e -> {
            client.cancelSearch();
            hideWaitingDialog();
            isOnlineGame = false;
            playerColor = null;
            client.disconnect();

            SwingUtilities.invokeLater(() -> {
                warcabyPanel.resetGame();
                warcabyPanel.resetButtonsFromOnlineGame();
                warcabyPanel.showGameModeDialog();
            });
        });

        SwingUtilities.invokeLater(() -> {
            if (waitingDialog != null && !waitingDialog.isVisible()) {
                waitingDialog.setVisible(true);
            }
        });
    }

    public void hideWaitingDialog() {
        if (waitingDialog != null) {
            SwingUtilities.invokeLater(() -> {
                if (waitingDialog != null) {
                    waitingDialog.dispose();
                    waitingDialog = null;
                }
            });
        }

        warcabyPanel.resetSearchingState();
    }

    public void startOnlineGame(String color) {
        hideWaitingDialog();
        this.playerColor = color;
        this.isOnlineGame = true;

        warcabyPanel.resetGame();

        showTemporaryNotification(
                "Grasz kolorem: " + (color.equals(NetworkProtocol.COLOR_WHITE) ? "BIAŁYM" : "CZARNYM"), 3000);

        if (color.equals(NetworkProtocol.COLOR_BLACK)) {
            warcabyPanel.flipBoard();
        }

        warcabyPanel.updateButtonsForOnlineGame();
    }

    public void showTemporaryNotification(String message, int durationMs) {
        DialogManager.showTemporaryNotification(this, message, durationMs);
    }

    public void applyOpponentMove(String moveData) {
        try {
            System.out.println("Przetwarzam ruch przeciwnika: " + moveData);
            int[] moveCoords = NetworkProtocol.parseMoveMessage(moveData);
            if (moveCoords == null) {
                System.err.println("Nieprawidłowy format danych ruchu: " + moveData);
                return;
            }

            int fromX = moveCoords[0];
            int fromY = moveCoords[1];
            int toX = moveCoords[2];
            int toY = moveCoords[3];

            System.out.println("Ruch przeciwnika z (" + fromX + "," + fromY + ") do (" + toX + "," + toY + ")");

            warcabyPanel.makeOpponentMove(fromX, fromY, toX, toY);
        } catch (Exception e) {
            System.err.println("Błąd przetwarzania ruchu przeciwnika: " + moveData);
            e.printStackTrace();
        }
    }

    public void applyOpponentCapture(String moveData) {
        try {
            System.out.println("Przetwarzam kontynuację bicia przez przeciwnika: " + moveData);
            int[] moveCoords = NetworkProtocol.parseMoveMessage(moveData);
            if (moveCoords == null) {
                System.err.println("Nieprawidłowy format danych kontynuacji bicia: " + moveData);
                return;
            }

            int fromX = moveCoords[0];
            int fromY = moveCoords[1];
            int toX = moveCoords[2];
            int toY = moveCoords[3];

            System.out.println("Kontynuacja bicia z (" + fromX + "," + fromY + ") do (" + toX + "," + toY + ")");

            warcabyPanel.makeOpponentCapture(fromX, fromY, toX, toY);
        } catch (Exception e) {
            System.err.println("Błąd przetwarzania kontynuacji bicia przez przeciwnika: " + moveData);
            e.printStackTrace();
        }
    }

    public void showOpponentQuitDialog() {
        JDialog quitDialog = DialogManager.createGameResultDialog(
                this,
                "Wygrana",
                "WYGRAŁEŚ!\nPrzeciwnik się poddał!",
                true,
                2000,
                this::resetGameToMenu);
        quitDialog.setVisible(true);
    }

    public void showGameOverDialog(String winner) {
        boolean isWinner = (playerColor != null && playerColor.equals(NetworkProtocol.COLOR_WHITE)
                && winner.startsWith("Białe")) ||
                (playerColor != null && playerColor.equals(NetworkProtocol.COLOR_BLACK) && winner.startsWith("Czarne"));

        JDialog gameOverDialog = DialogManager.createGameResultDialog(
                this,
                "Koniec gry",
                isWinner ? "WYGRAŁEŚ!\n" + winner : "PRZEGRAŁEŚ!\n" + winner,
                isWinner,
                3000,
                this::resetGameToMenu);
        gameOverDialog.setVisible(true);
    }

    public void resetGameToMenu() {
        if (isOnlineGame) {
            client.endSession();
        }

        isOnlineGame = false;
        playerColor = null;

        warcabyPanel.resetGame();
        warcabyPanel.resetButtonsFromOnlineGame();
        warcabyPanel.showGameModeDialog();
    }

    public void updateGameTime(long whiteSeconds, long blackSeconds, String currentTurn) {
        warcabyPanel.updateServerTime(whiteSeconds, blackSeconds, currentTurn);
    }

    public void startGame() {
        warcabyPanel.startGame();
    }

    public void showMandatoryMoveNotification(String message) {
        showTemporaryNotification(message, 3000);
    }
}
