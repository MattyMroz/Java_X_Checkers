import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class CheckersPanel extends JPanel implements ComputerPlayer.ComputerMoveListener {

    private final GameLogic gameLogic;
    private final BoardRenderer boardRenderer;
    private final ComputerPlayer computerPlayer;
    private final CheckersFrame parentFrame;

    private Piece selectedPiece = null;
    private List<Point> possibleMoves = new ArrayList<>();

    private final JLabel statusLabel;
    private final JLabel timerLabel;
    private final JPanel boardPanel;
    private final JButton startButton;
    private final JButton newGameButton;

    private long whiteTime = 0;
    private long blackTime = 0;
    private long turnStartTime;
    private final Timer timer;
    private boolean timerStarted = false;

    private long serverWhiteTime = 0;
    private long serverBlackTime = 0;
    private boolean useServerTime = false;
    private String serverCurrentTurn = "WHITE";

    private boolean playingWithComputer = false;

    private enum GameMode {
        SINGLE_PLAYER, TWO_PLAYERS, ONLINE
    }

    private GameMode currentGameMode = GameMode.TWO_PLAYERS;
    private boolean isSearchingGame = false;

    public CheckersPanel(CheckersFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(GameConstants.BACKGROUND_COLOR);

        gameLogic = new GameLogic();
        boardRenderer = new BoardRenderer(gameLogic);
        computerPlayer = new ComputerPlayer(gameLogic);
        computerPlayer.setMoveListener(this);

        boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                boardRenderer.drawBoard(g, GameConstants.BOARD_OFFSET);
                boardRenderer.drawPieces(g, GameConstants.BOARD_OFFSET);
                boardRenderer.highlightPossibleMoves(g, GameConstants.BOARD_OFFSET);
                boardRenderer.highlightPiecesCanCapture(g, GameConstants.BOARD_OFFSET);
            }
        };

        boardPanel.setPreferredSize(new Dimension(GameConstants.BOARD_SIZE * GameConstants.SQUARE_SIZE + 60,
                GameConstants.BOARD_SIZE * GameConstants.SQUARE_SIZE + 60));
        boardPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel statusPanel = new JPanel(new BorderLayout(20, 0));
        statusPanel.setBackground(GameConstants.BACKGROUND_COLOR);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        statusLabel = new JLabel("Runda: Białe");
        statusLabel.setFont(GameConstants.STATUS_FONT);
        statusLabel.setForeground(Color.BLACK);

        timerLabel = new JLabel("Czas: Białe 00:00 | Czarne 00:00");
        timerLabel.setFont(GameConstants.TIMER_FONT);
        timerLabel.setForeground(GameConstants.SECONDARY_TEXT_COLOR);

        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(timerLabel, BorderLayout.EAST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(GameConstants.BACKGROUND_COLOR);

        JButton flipBoardButton = createStyledButton(GameConstants.FLIP_BOARD_BUTTON_TEXT);
        newGameButton = createStyledButton(GameConstants.NEW_GAME_BUTTON_TEXT);
        startButton = createStyledButton(GameConstants.START_BUTTON_TEXT);

        flipBoardButton.addActionListener(_ -> {
            boardRenderer.setBoardFlipped(!boardRenderer.isBoardFlipped());
            repaint();
        });

        newGameButton.addActionListener(_ -> {
            if (parentFrame.isOnlineGame()) {
                int option = JOptionPane.showConfirmDialog(this,
                        "Czy na pewno chcesz poddać grę?",
                        "Potwierdzenie",
                        JOptionPane.YES_NO_OPTION);

                if (option == JOptionPane.YES_OPTION) {
                    parentFrame.getClient().quitGame();
                    parentFrame.resetGameToMenu();
                }
            } else {
                resetGame();
                startButton.setText(GameConstants.START_BUTTON_TEXT);
                showGameModeDialog();
            }
        });

        startButton.addActionListener(_ -> {
            if (!parentFrame.isOnlineGame()) {
                toggleGameState();
            }
        });

        buttonPanel.add(flipBoardButton);
        buttonPanel.add(newGameButton);
        buttonPanel.add(startButton);

        add(boardPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(statusPanel, BorderLayout.NORTH);

        timer = new Timer(1000, _ -> {
            if (timerStarted) {
                updateTimer();
            }
        });

        setupMouseListeners();

        showGameModeDialog();
    }

    private void setupMouseListeners() {
        boardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (timerStarted) {
                    if (playingWithComputer && !gameLogic.isWhiteTurn()) {
                        return;
                    }
                    handleMouseClick(e.getX(), e.getY());
                } else {
                    parentFrame.showTemporaryNotification("Kliknij Start, aby rozpocząć grę!",
                            GameConstants.NOTIFICATION_DURATION);
                }
            }
        });

        boardPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateCursor(e.getX(), e.getY());
            }
        });
    }

    public void toggleGameState() {
        if (!timerStarted) {
            timerStarted = true;
            turnStartTime = System.currentTimeMillis();
            timer.start();
            startButton.setText(GameConstants.PAUSE_BUTTON_TEXT);

            if (playingWithComputer && !gameLogic.isWhiteTurn()) {
                computerPlayer.makeMove(GameConstants.AI_MOVE_DELAY);
            }
        } else {
            timerStarted = false;
            long currentTime = System.currentTimeMillis();

            if (gameLogic.isWhiteTurn()) {
                whiteTime += (currentTime - turnStartTime);
            } else {
                blackTime += (currentTime - turnStartTime);
            }

            timer.stop();
            computerPlayer.stopTimer();
            startButton.setText(GameConstants.START_BUTTON_TEXT);
        }
    }

    public void showGameModeDialog() {
        String[] options = { "Gra z komputerem", "Gra na jednym komputerze", "Gra online" };
        int choice = JOptionPane.showOptionDialog(
                this,
                "Wybierz tryb gry:",
                "Tryb gry",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]);

        switch (choice) {
            case 0:
                currentGameMode = GameMode.SINGLE_PLAYER;
                playingWithComputer = true;
                break;
            case 1:
                currentGameMode = GameMode.TWO_PLAYERS;
                playingWithComputer = false;
                break;
            case 2:
                if (isSearchingGame) {
                    JOptionPane.showMessageDialog(this,
                            "Już trwa wyszukiwanie przeciwnika. Poczekaj na zakończenie.",
                            "Informacja",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                currentGameMode = GameMode.ONLINE;
                playingWithComputer = false;
                isSearchingGame = true;

                JDialog connectingDialog = new JDialog(JOptionPane.getFrameForComponent(this), "Łączenie z serwerem",
                        false);
                connectingDialog.setLayout(new BorderLayout());

                JPanel contentPanel = new JPanel(new BorderLayout());
                contentPanel.setBackground(GameConstants.BACKGROUND_COLOR);
                contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

                JLabel connectingLabel = new JLabel("Próba połączenia z serwerem...", JLabel.CENTER);
                connectingLabel.setFont(new Font("Arial", Font.BOLD, 14));
                contentPanel.add(connectingLabel, BorderLayout.CENTER);

                JProgressBar progressBar = new JProgressBar();
                progressBar.setIndeterminate(true);
                contentPanel.add(progressBar, BorderLayout.SOUTH);

                connectingDialog.add(contentPanel, BorderLayout.CENTER);
                connectingDialog.setSize(GameConstants.CONNECTING_DIALOG_WIDTH, GameConstants.CONNECTING_DIALOG_HEIGHT);
                connectingDialog.setLocationRelativeTo(this);
                connectingDialog.setVisible(true);

                new Thread(() -> {
                    try {
                        Thread.sleep(GameConstants.CONNECT_WAIT_TIME);

                        SwingUtilities.invokeLater(() -> {
                            CheckersClient client = parentFrame.getClient();

                            if (client.isConnected()) {
                                client.disconnect();
                                try {
                                    Thread.sleep(GameConstants.DISCONNECT_WAIT_TIME);
                                } catch (InterruptedException ex) {
                                    Thread.currentThread().interrupt();
                                }
                            }

                            boolean connected = client.connectToServer();
                            connectingDialog.dispose();

                            if (connected) {
                                parentFrame.showWaitingForOpponentDialog();
                                client.findGame();
                            } else {
                                JOptionPane.showMessageDialog(this,
                                        "Nie można połączyć z serwerem. Spróbuj ponownie później.",
                                        "Błąd połączenia",
                                        JOptionPane.ERROR_MESSAGE);
                                currentGameMode = GameMode.TWO_PLAYERS;
                                playingWithComputer = false;
                                isSearchingGame = false;
                                resetSearchingState();
                            }
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
                break;
            default:
                currentGameMode = GameMode.TWO_PLAYERS;
                playingWithComputer = false;
                break;
        }
    }

    private void handleMouseClick(int x, int y) {
        Point boardPoint = boardRenderer.getBoardCoordinates(x, y, GameConstants.BOARD_OFFSET);
        if (boardPoint == null) {
            return;
        }

        int clickRow = boardPoint.y;
        int clickCol = boardPoint.x;

        if (parentFrame.isOnlineGame()) {
            boolean isWhitePlayer = "WHITE".equals(parentFrame.getPlayerColor());
            if (isWhitePlayer != gameLogic.isWhiteTurn()) {
                parentFrame.showMandatoryMoveNotification("Teraz jest ruch przeciwnika!");
                return;
            }
        }

        if (gameLogic.isContinuedCapture()) {
            if (selectedPiece != gameLogic.getCapturingPiece()) {
                selectedPiece = gameLogic.getCapturingPiece();
                calculatePossibleMoves();
                repaint();
                return;
            }

            for (Point move : possibleMoves) {
                if (move.x == clickCol && move.y == clickRow) {
                    executeMove(selectedPiece, clickRow, clickCol);
                    return;
                }
            }
            return;
        }

        if (!gameLogic.getPiecesCanCapture().isEmpty() && selectedPiece == null) {
            boolean canCapture = false;
            for (Point p : gameLogic.getPiecesCanCapture()) {
                if (p.x == clickCol && p.y == clickRow) {
                    canCapture = true;
                    break;
                }
            }

            if (!canCapture) {
                parentFrame.showMandatoryMoveNotification("Musisz wykonać bicie!");
                return;
            }
        }

        Piece clickedPiece = gameLogic.getBoard()[clickRow][clickCol];
        if (clickedPiece != null) {
            if (clickedPiece.isWhite() == gameLogic.isWhiteTurn()) {
                selectedPiece = clickedPiece;
                calculatePossibleMoves();
                repaint();
                return;
            }
        }

        if (selectedPiece != null) {
            for (Point move : possibleMoves) {
                if (move.x == clickCol && move.y == clickRow) {
                    executeMove(selectedPiece, clickRow, clickCol);
                    return;
                }
            }

            selectedPiece = null;
            possibleMoves.clear();
            repaint();
        }
    }

    private void executeMove(Piece piece, int newRow, int newCol) {
        boolean wasKingBefore = piece.isKing();
        int oldRow = piece.getRow();
        int oldCol = piece.getCol();

        System.out.println("Wykonuję ruch z (" + oldCol + "," + oldRow + ") do (" + newCol + "," + newRow + ")");

        boolean success = gameLogic.movePiece(piece, newRow, newCol);

        if (success) {
            if (!wasKingBefore && piece.isKing()) {
                showPiecePromotionNotification(piece.isWhite());
            }

            if (timerStarted) {
                long currentTime = System.currentTimeMillis();
                if (gameLogic.isWhiteTurn()) {
                    blackTime += (currentTime - turnStartTime);
                } else {
                    whiteTime += (currentTime - turnStartTime);
                }
                turnStartTime = currentTime;
            }

            if (gameLogic.isContinuedCapture() && !possibleMoves.isEmpty()) {
                parentFrame.showTemporaryNotification("Możesz kontynuować bicie!", GameConstants.NOTIFICATION_DURATION);
            }

            if (parentFrame.isOnlineGame()) {
                String moveData = oldCol + "," + oldRow + "->" + newCol + "," + newRow;

                if (gameLogic.isContinuedCapture()) {
                    System.out.println("Wysyłam kontynuację bicia: " + moveData);
                    parentFrame.getClient().sendCaptureContinued(moveData);
                } else {
                    System.out.println("Wysyłam ruch do przeciwnika: " + moveData);
                    parentFrame.getClient().sendMove(moveData);
                }
            }

            if (!gameLogic.isContinuedCapture()) {
                selectedPiece = null;
                possibleMoves.clear();
                boardRenderer.setSelectedPiece(null);
                boardRenderer.setPossibleMoves(null);

                if (playingWithComputer && !gameLogic.isWhiteTurn() && !gameLogic.isGameOver()) {
                    computerPlayer.makeMove(GameConstants.AI_MOVE_DELAY);
                }
            } else {
                selectedPiece = gameLogic.getCapturingPiece();
                calculatePossibleMoves();

                if (playingWithComputer && !gameLogic.isWhiteTurn()) {
                    boardRenderer.setSelectedPiece(gameLogic.getCapturingPiece());
                    boardRenderer.setPossibleMoves(possibleMoves);
                    repaint();
                }
            }

            updateStatusLabel();
            updateTimer();

            if (gameLogic.isGameOver()) {
                Timer endGameTimer = new Timer(500, _ -> handleGameOver());
                endGameTimer.setRepeats(false);
                endGameTimer.start();
            } else if (playingWithComputer && !gameLogic.isWhiteTurn() && timerStarted) {
                computerPlayer.makeMove(GameConstants.AI_MOVE_DELAY);
            }
        }

        repaint();
    }

    public void makeOpponentMove(int fromX, int fromY, int toX, int toY) {
        System.out.println("Wykonuję ruch przeciwnika: z (" + fromX + "," + fromY + ") do (" + toX + "," + toY + ")");

        System.out.println("Stan planszy przed ruchem przeciwnika:");
        printBoardState();

        Piece piece = gameLogic.getBoard()[fromY][fromX];
        if (piece != null) {
            boolean wasKingBefore = piece.isKing();
            System.out.println("Znaleziono pionek: " + (piece.isWhite() ? "biały" : "czarny") + " na pozycji " + fromX
                    + "," + fromY);
            boolean success = gameLogic.movePiece(piece, toY, toX);
            System.out.println("Wykonanie ruchu przeciwnika: " + (success ? "udane" : "nieudane"));

            if (success && !wasKingBefore && piece.isKing()) {
                showPiecePromotionNotification(piece.isWhite());
            }

            System.out.println("Stan planszy po ruchu przeciwnika:");
            printBoardState();

            selectedPiece = null;
            possibleMoves.clear();
            boardRenderer.setSelectedPiece(null);
            boardRenderer.setPossibleMoves(null);

            updateStatusLabel();
            updateTimer();
            repaint();

            if (gameLogic.isGameOver()) {
                Timer endGameTimer = new Timer(500, _ -> handleGameOver());
                endGameTimer.setRepeats(false);
                endGameTimer.start();
            }
        } else {
            System.err.println("BŁĄD: Nie znaleziono pionka na pozycji " + fromX + "," + fromY);
            printBoardState();
        }
    }

    public void makeOpponentCapture(int fromX, int fromY, int toX, int toY) {
        System.out.println("Wykonuję kontynuację bicia przez przeciwnika: z (" + fromX + "," + fromY + ") do (" + toX
                + "," + toY + ")");

        System.out.println("Stan planszy przed kontynuacją bicia:");
        printBoardState();

        Piece piece = gameLogic.getBoard()[fromY][fromX];
        if (piece != null) {
            boolean wasKingBefore = piece.isKing();
            System.out.println("Znaleziono pionek: " + (piece.isWhite() ? "biały" : "czarny") + " na pozycji " + fromX
                    + "," + fromY);
            boolean success = gameLogic.movePiece(piece, toY, toX);
            System.out.println("Wykonanie kontynuacji bicia: " + (success ? "udane" : "nieudane"));

            if (success && !wasKingBefore && piece.isKing()) {
                showPiecePromotionNotification(piece.isWhite());
            }

            if (success && gameLogic.isContinuedCapture()) {
                parentFrame.showTemporaryNotification("Przeciwnik kontynuuje bicie",
                        GameConstants.NOTIFICATION_DURATION);
            }

            System.out.println("Stan planszy po kontynuacji bicia:");
            printBoardState();

            if (!gameLogic.isContinuedCapture()) {
                selectedPiece = null;
                possibleMoves.clear();
                boardRenderer.setSelectedPiece(null);
                boardRenderer.setPossibleMoves(null);
            }

            updateStatusLabel();
            updateTimer();
            repaint();

            if (gameLogic.isGameOver()) {
                Timer endGameTimer = new Timer(500, _ -> handleGameOver());
                endGameTimer.setRepeats(false);
                endGameTimer.start();
            }
        } else {
            System.err.println("BŁĄD: Nie znaleziono pionka na pozycji " + fromX + "," + fromY);
            printBoardState();
        }
    }

    private void printBoardState() {
        Piece[][] board = gameLogic.getBoard();
        StringBuilder boardStr = new StringBuilder();

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                Piece p = board[y][x];
                if (p != null) {
                    boardStr.append(String.format("%s(%d,%d) ", (p.isWhite() ? "B" : "C"), x, y));
                }
            }
        }

        System.out.println(boardStr);
    }

    public void flipBoard() {
        boardRenderer.setBoardFlipped(!boardRenderer.isBoardFlipped());
        repaint();
    }

    public void updateButtonsForOnlineGame() {
        startButton.setEnabled(false);
        newGameButton.setText(GameConstants.SURRENDER_BUTTON_TEXT);
    }

    public void resetButtonsFromOnlineGame() {
        startButton.setEnabled(true);
        newGameButton.setText(GameConstants.NEW_GAME_BUTTON_TEXT);
    }

    private void showGameOverDialog() {
        String winner = gameLogic.getWinner();
        if (winner != null) {
            boolean isLocalGame = !parentFrame.isOnlineGame();

            JDialog gameOverDialog = new JDialog(parentFrame, "Koniec gry", false);
            gameOverDialog.setLayout(new BorderLayout());

            JPanel contentPanel = new JPanel(new BorderLayout());

            if (winner.startsWith("Białe")) {
                contentPanel.setBackground(GameConstants.WIN_DIALOG_BACKGROUND);
                JLabel resultLabel = new JLabel("<html><center>Koniec gry!<br>" + winner + " wygrały!</center></html>",
                        JLabel.CENTER);
                resultLabel.setFont(new Font("Arial", Font.BOLD, 18));
                resultLabel.setForeground(GameConstants.WIN_TEXT_COLOR);
                contentPanel.add(resultLabel, BorderLayout.CENTER);
            } else {
                contentPanel.setBackground(GameConstants.LOSE_DIALOG_BACKGROUND);
                JLabel resultLabel = new JLabel("<html><center>Koniec gry!<br>" + winner + " wygrały!</center></html>",
                        JLabel.CENTER);
                resultLabel.setFont(new Font("Arial", Font.BOLD, 18));
                resultLabel.setForeground(GameConstants.LOSE_TEXT_COLOR);
                contentPanel.add(resultLabel, BorderLayout.CENTER);
            }

            contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
            gameOverDialog.add(contentPanel, BorderLayout.CENTER);
            gameOverDialog.setSize(GameConstants.DIALOG_WIDTH, GameConstants.DIALOG_HEIGHT);
            gameOverDialog.setLocationRelativeTo(parentFrame);
            gameOverDialog.setVisible(true);

            Timer closeTimer = new Timer(GameConstants.GAME_OVER_DIALOG_DURATION, _ -> {
                gameOverDialog.dispose();
                if (isLocalGame) {
                    resetGame();
                    startButton.setText(GameConstants.START_BUTTON_TEXT);
                    showGameModeDialog();
                }
            });
            closeTimer.setRepeats(false);
            closeTimer.start();
        }
    }

    @Override
    public void onMoveSelected(Piece piece, int row, int col) {
        boardRenderer.setSelectedPiece(computerPlayer.getCurrentSelectedPiece());
        boardRenderer.setPossibleMoves(computerPlayer.getCurrentPossibleMoves());
        repaint();

        Timer visualTimer = new Timer(GameConstants.VISUAL_MOVE_DELAY, _ -> {
            executeMove(piece, row, col);
            computerPlayer.clearSelection();
        });
        visualTimer.setRepeats(false);
        visualTimer.start();
    }

    private void calculatePossibleMoves() {
        possibleMoves = gameLogic.calculatePossibleMoves(selectedPiece);
        boardRenderer.setSelectedPiece(selectedPiece);
        boardRenderer.setPossibleMoves(possibleMoves);
    }

    private void updateCursor(int x, int y) {
        Point boardPoint = boardRenderer.getBoardCoordinates(x, y, GameConstants.BOARD_OFFSET);
        if (boardPoint == null) {
            boardPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            return;
        }

        int row = boardPoint.y;
        int col = boardPoint.x;

        if (selectedPiece != null) {
            for (Point move : possibleMoves) {
                if (move.x == col && move.y == row) {
                    boardPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    return;
                }
            }
        }

        Piece piece = gameLogic.getBoard()[row][col];
        if (piece != null && piece.isWhite() == gameLogic.isWhiteTurn()) {
            if (!gameLogic.getPiecesCanCapture().isEmpty()) {
                for (Point p : gameLogic.getPiecesCanCapture()) {
                    if (p.x == col && p.y == row) {
                        boardPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        return;
                    }
                }
                boardPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            } else {
                boardPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        } else {
            boardPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private void updateStatusLabel() {
        if (!gameLogic.getPiecesCanCapture().isEmpty() && selectedPiece == null) {
            statusLabel.setText("Runda: " + (gameLogic.isWhiteTurn() ? "Białe" : "Czarne") + " - WYMAGANE BICIE!");
            statusLabel.setForeground(Color.RED);
        } else {
            statusLabel.setText("Runda: " + (gameLogic.isWhiteTurn() ? "Białe" : "Czarne"));
            statusLabel.setForeground(Color.BLACK);
        }
    }

    private void updateTimer() {
        long whiteSeconds;
        long blackSeconds;

        if (useServerTime && parentFrame.isOnlineGame()) {
            whiteSeconds = serverWhiteTime;
            blackSeconds = serverBlackTime;

            gameLogic.setWhiteTurn(serverCurrentTurn.equals("WHITE"));

            updateStatusLabel();
        } else {
            whiteSeconds = whiteTime / 1000;
            blackSeconds = blackTime / 1000;

            if (timerStarted) {
                long currentTime = System.currentTimeMillis();
                if (gameLogic.isWhiteTurn()) {
                    whiteSeconds = (whiteTime + (currentTime - turnStartTime)) / 1000;
                } else {
                    blackSeconds = (blackTime + (currentTime - turnStartTime)) / 1000;
                }
            }
        }

        String whiteTimeStr = String.format("%02d:%02d", whiteSeconds / 60, whiteSeconds % 60);
        String blackTimeStr = String.format("%02d:%02d", blackSeconds / 60, blackSeconds % 60);
        timerLabel.setText("Czas: Białe " + whiteTimeStr + " | Czarne " + blackTimeStr);
    }

    private void handleGameOver() {
        timerStarted = false;
        timer.stop();
        computerPlayer.stopTimer();

        if (parentFrame.isOnlineGame()) {
            String winner = gameLogic.getWinner();
            parentFrame.showGameOverDialog(winner + " wygrały!");
        } else {
            showGameOverDialog();
        }
    }

    public void startGame() {
        timerStarted = true;
        if (parentFrame.isOnlineGame()) {
            useServerTime = true;
        } else {
            turnStartTime = System.currentTimeMillis();
            timer.start();
        }
        repaint();
    }

    public void resetGame() {
        gameLogic.resetGame();
        selectedPiece = null;
        possibleMoves.clear();
        boardRenderer.setSelectedPiece(null);
        boardRenderer.setPossibleMoves(null);
        whiteTime = 0;
        blackTime = 0;
        timerStarted = false;
        timer.stop();

        useServerTime = false;
        serverWhiteTime = 0;
        serverBlackTime = 0;

        if (!parentFrame.isOnlineGame() ||
                (parentFrame.isOnlineGame() && "WHITE".equals(parentFrame.getPlayerColor()))) {
            boardRenderer.setBoardFlipped(false);
        }

        isSearchingGame = false;

        updateStatusLabel();
        updateTimer();
        repaint();
    }

    public void resetSearchingState() {
        isSearchingGame = false;
    }

    public void updateServerTime(long whiteSeconds, long blackSeconds, String currentTurn) {
        if (parentFrame.isOnlineGame()) {
            serverWhiteTime = whiteSeconds;
            serverBlackTime = blackSeconds;
            serverCurrentTurn = currentTurn;
            updateTimer();
            repaint();
        }
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(GameConstants.BUTTON_COLOR);
        button.setForeground(GameConstants.BUTTON_TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GameConstants.BUTTON_BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(GameConstants.BUTTON_PADDING, GameConstants.BUTTON_HORIZONTAL_PADDING,
                        GameConstants.BUTTON_PADDING, GameConstants.BUTTON_HORIZONTAL_PADDING)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(GameConstants.BUTTON_HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(GameConstants.BUTTON_COLOR);
            }
        });

        return button;
    }

    private void showPiecePromotionNotification(boolean isWhite) {
        String color = isWhite ? "BIAŁY" : "CZARNY";
        parentFrame.showTemporaryNotification("Pion " + color + " stał się damą!",
                GameConstants.PROMOTION_NOTIFICATION_DURATION);
    }
}
