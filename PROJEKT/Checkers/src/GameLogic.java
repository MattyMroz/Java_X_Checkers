import java.util.ArrayList;
import java.util.List;

public class GameLogic {

    private static final Logger logger = new Logger(GameLogic.class);

    private final Piece[][] board;
    private boolean isWhiteTurn;
    private boolean continuedCapture;
    private Piece capturingPiece;
    private final List<Point> piecesCanCapture;
    private boolean gameOver;

    public GameLogic() {
        board = new Piece[GameConstants.BOARD_SIZE][GameConstants.BOARD_SIZE];
        piecesCanCapture = new ArrayList<>();
        resetGame();
    }

    public void resetGame() {
        logger.info("Resetowanie gry");
        initializeBoard();
        isWhiteTurn = true;
        continuedCapture = false;
        capturingPiece = null;
        gameOver = false;
        updatePiecesCanCapture();
    }

    public Piece[][] getBoard() {
        return board;
    }

    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    public void setWhiteTurn(boolean isWhiteTurn) {
        this.isWhiteTurn = isWhiteTurn;
    }

    public boolean isContinuedCapture() {
        return continuedCapture;
    }

    public Piece getCapturingPiece() {
        return capturingPiece;
    }

    public List<Point> getPiecesCanCapture() {
        return piecesCanCapture;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    private void initializeBoard() {
        logger.debug("Inicjalizacja planszy");
        for (int row = 0; row < GameConstants.BOARD_SIZE; row++) {
            for (int col = 0; col < GameConstants.BOARD_SIZE; col++) {
                board[row][col] = null;
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < GameConstants.BOARD_SIZE; col++) {
                if ((row + col) % 2 == 1) {
                    board[row][col] = new Piece(false, row, col);
                }
            }
        }

        for (int row = 5; row < 8; row++) {
            for (int col = 0; col < GameConstants.BOARD_SIZE; col++) {
                if ((row + col) % 2 == 1) {
                    board[row][col] = new Piece(true, row, col);
                }
            }
        }
        logger.debug("Plansza zainicjalizowana");
    }

    public List<Point> calculatePossibleMoves(Piece piece) {
        List<Point> possibleMoves = new ArrayList<>();
        if (piece == null) {
            return possibleMoves;
        }

        int row = piece.getRow();
        int col = piece.getCol();
        boolean isWhite = piece.isWhite();
        boolean isKing = piece.isKing();

        List<Point> captures = new ArrayList<>();
        checkCaptures(piece, captures);

        if (!captures.isEmpty()) {
            possibleMoves.addAll(captures);
            return possibleMoves;
        }

        if (!piecesCanCapture.isEmpty()) {
            return possibleMoves;
        }

        if (!isKing) {
            int direction = isWhite ? -1 : 1;

            if (col > 0 && row + direction >= 0 && row + direction < GameConstants.BOARD_SIZE) {
                if (board[row + direction][col - 1] == null) {
                    possibleMoves.add(new Point(col - 1, row + direction));
                }
            }

            if (col < GameConstants.BOARD_SIZE - 1 && row + direction >= 0
                    && row + direction < GameConstants.BOARD_SIZE) {
                if (board[row + direction][col + 1] == null) {
                    possibleMoves.add(new Point(col + 1, row + direction));
                }
            }
        } else {
            checkKingMoves(row, col, possibleMoves);
        }

        return possibleMoves;
    }

    public void checkCaptures(Piece piece, List<Point> captures) {
        if (piece == null) {
            return;
        }

        int row = piece.getRow();
        int col = piece.getCol();
        boolean isKing = piece.isKing();

        if (!isKing) {
            checkCapture(piece, row + 1, col - 1, row + 2, col - 2, captures);
            checkCapture(piece, row + 1, col + 1, row + 2, col + 2, captures);
            checkCapture(piece, row - 1, col - 1, row - 2, col - 2, captures);
            checkCapture(piece, row - 1, col + 1, row - 2, col + 2, captures);
        } else {
            checkKingCaptures(piece, row, col, captures);
        }
    }

    private void checkCapture(Piece piece, int midRow, int midCol, int endRow, int endCol, List<Point> captures) {
        if (endRow >= 0 && endRow < GameConstants.BOARD_SIZE && endCol >= 0 && endCol < GameConstants.BOARD_SIZE) {
            if (midRow >= 0 && midRow < GameConstants.BOARD_SIZE && midCol >= 0 && midCol < GameConstants.BOARD_SIZE) {
                Piece midPiece = board[midRow][midCol];
                if (midPiece != null && midPiece.isWhite() != piece.isWhite() && board[endRow][endCol] == null) {
                    captures.add(new Point(endCol, endRow));
                }
            }
        }
    }

    private void checkKingMoves(int row, int col, List<Point> possibleMoves) {
        checkDiagonalMoves(row, col, -1, -1, possibleMoves);
        checkDiagonalMoves(row, col, -1, 1, possibleMoves);
        checkDiagonalMoves(row, col, 1, -1, possibleMoves);
        checkDiagonalMoves(row, col, 1, 1, possibleMoves);
    }

    private void checkDiagonalMoves(int row, int col, int rowDir, int colDir, List<Point> possibleMoves) {
        int r = row + rowDir;
        int c = col + colDir;
        while (r >= 0 && r < GameConstants.BOARD_SIZE && c >= 0 && c < GameConstants.BOARD_SIZE) {
            if (board[r][c] == null) {
                possibleMoves.add(new Point(c, r));
            } else {
                break;
            }
            r += rowDir;
            c += colDir;
        }
    }

    private void checkKingCaptures(Piece piece, int row, int col, List<Point> captures) {
        checkDiagonalCaptures(piece, row, col, -1, -1, captures);
        checkDiagonalCaptures(piece, row, col, -1, 1, captures);
        checkDiagonalCaptures(piece, row, col, 1, -1, captures);
        checkDiagonalCaptures(piece, row, col, 1, 1, captures);
    }

    private void checkDiagonalCaptures(Piece piece, int row, int col, int rowDir, int colDir, List<Point> captures) {
        int r = row + rowDir;
        int c = col + colDir;
        boolean foundOpponent = false;

        while (r >= 0 && r < GameConstants.BOARD_SIZE && c >= 0 && c < GameConstants.BOARD_SIZE) {
            if (board[r][c] == null) {
                if (foundOpponent) {
                    captures.add(new Point(c, r));
                    r += rowDir;
                    c += colDir;
                    continue;
                }
                r += rowDir;
                c += colDir;
            } else if (!foundOpponent && board[r][c].isWhite() != piece.isWhite()) {
                foundOpponent = true;
                r += rowDir;
                c += colDir;
            } else {
                break;
            }
        }
    }

    public boolean movePiece(Piece piece, int newRow, int newCol) {
        if (piece == null) {
            logger.error("Próba ruchu null pionka");
            return false;
        }

        int oldRow = piece.getRow();
        int oldCol = piece.getCol();

        logger.info("Wykonuję ruch pionka z (" + oldCol + "," + oldRow + ") do (" + newCol + "," + newRow + ")");

        if (newRow < 0 || newRow >= GameConstants.BOARD_SIZE || newCol < 0 || newCol >= GameConstants.BOARD_SIZE) {
            logger.error("Ruch poza planszą");
            return false;
        }

        if (board[newRow][newCol] != null) {
            logger.error("Miejsce docelowe jest już zajęte");
            return false;
        }

        boolean capturePerformed = false;

        if (Math.abs(newRow - oldRow) == 2 && Math.abs(newCol - oldCol) == 2) {
            int capturedRow = (newRow + oldRow) / 2;
            int capturedCol = (newCol + oldCol) / 2;
            if (board[capturedRow][capturedCol] != null
                    && board[capturedRow][capturedCol].isWhite() != piece.isWhite()) {
                logger.info("Bicie pionka na (" + capturedCol + "," + capturedRow + ")");
                board[capturedRow][capturedCol] = null;
                capturePerformed = true;
            }
        } else if (piece.isKing() && Math.abs(newRow - oldRow) > 1) {
            int rowDir = Integer.compare(newRow, oldRow);
            int colDir = Integer.compare(newCol, oldCol);
            int r = oldRow + rowDir;
            int c = oldCol + colDir;

            boolean foundOpponent = false;
            while (r != newRow || c != newCol) {
                if (board[r][c] != null) {
                    if (!foundOpponent && board[r][c].isWhite() != piece.isWhite()) {
                        board[r][c] = null;
                        capturePerformed = true;
                        foundOpponent = true;
                    } else {
                        return false;
                    }
                }
                r += rowDir;
                c += colDir;
            }
        }

        board[oldRow][oldCol] = null;
        piece.setPosition(newRow, newCol);
        board[newRow][newCol] = piece;

        if (!piece.isKing()) {
            if ((piece.isWhite() && newRow == 0) || (!piece.isWhite() && newRow == GameConstants.BOARD_SIZE - 1)) {
                piece.setKing(true);
                logger.info("Pionek został promowany do damy");
            }
        }

        continuedCapture = false;
        capturingPiece = null;

        if (capturePerformed) {
            List<Point> furtherCaptures = new ArrayList<>();
            checkCaptures(piece, furtherCaptures);

            if (!furtherCaptures.isEmpty()) {
                logger.info("Możliwe dalsze bicie");
                continuedCapture = true;
                capturingPiece = piece;
                return true;
            }
        }

        isWhiteTurn = !isWhiteTurn;
        updatePiecesCanCapture();
        checkGameOver();

        return true;
    }

    public void updatePiecesCanCapture() {
        piecesCanCapture.clear();

        for (int row = 0; row < GameConstants.BOARD_SIZE; row++) {
            for (int col = 0; col < GameConstants.BOARD_SIZE; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.isWhite() == isWhiteTurn) {
                    List<Point> captures = new ArrayList<>();
                    checkCaptures(piece, captures);

                    if (!captures.isEmpty()) {
                        piecesCanCapture.add(new Point(col, row));
                    }
                }
            }
        }

        if (!piecesCanCapture.isEmpty()) {
            logger.debug("Znaleziono " + piecesCanCapture.size() + " pionków, które mogą wykonać bicie");
        }
    }

    public void checkGameOver() {
        if (gameOver) {
            return;
        }

        boolean[] gameState = checkGameState();
        boolean whiteExists = gameState[0];
        boolean blackExists = gameState[1];
        boolean canMove = gameState[2];

        if (!whiteExists || !blackExists || !canMove) {
            gameOver = true;
            logger.info("Koniec gry! Zwycięzca: " + getWinner());
        }
    }

    public String getWinner() {
        boolean[] gameState = checkGameState();
        boolean whiteExists = gameState[0];
        boolean blackExists = gameState[1];
        boolean canMove = gameState[2];

        if (!whiteExists) {
            return "Czarne";
        } else if (!blackExists) {
            return "Białe";
        } else if (!canMove) {
            return isWhiteTurn ? "Czarne" : "Białe";
        }

        return null;
    }

    private boolean[] checkGameState() {
        boolean whiteExists = false;
        boolean blackExists = false;
        boolean canMove = false;

        for (int row = 0; row < GameConstants.BOARD_SIZE; row++) {
            for (int col = 0; col < GameConstants.BOARD_SIZE; col++) {
                Piece piece = board[row][col];
                if (piece != null) {
                    if (piece.isWhite()) {
                        whiteExists = true;
                    } else {
                        blackExists = true;
                    }

                    if (piece.isWhite() == isWhiteTurn) {
                        List<Point> moves = calculatePossibleMoves(piece);
                        if (!moves.isEmpty()) {
                            canMove = true;
                        }
                    }
                }
            }
        }

        return new boolean[] { whiteExists, blackExists, canMove };
    }
}
