
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.Timer;

public class ComputerPlayer {

    private final GameLogic gameLogic;
    private final Random random = new Random();
    private Timer moveTimer;
    private ComputerMoveListener listener;
    private List<Point> currentPossibleMoves = new ArrayList<>();
    private Piece currentSelectedPiece = null;

    public ComputerPlayer(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
    }

    public List<Point> getCurrentPossibleMoves() {
        return currentPossibleMoves;
    }

    public Piece getCurrentSelectedPiece() {
        return currentSelectedPiece;
    }

    public void setMoveListener(ComputerMoveListener listener) {
        this.listener = listener;
    }

    public void makeMove(int delay) {
        stopTimer();

        moveTimer = new Timer(delay, _ -> {
            if (gameLogic.isContinuedCapture() && gameLogic.getCapturingPiece() != null) {
                handleContinuedCapture();
            } else if (!gameLogic.getPiecesCanCapture().isEmpty()) {
                handleRequiredCapture();
            } else {
                handleRegularMove();
            }
        });
        moveTimer.setRepeats(false);
        moveTimer.start();
    }

    public void stopTimer() {
        if (moveTimer != null && moveTimer.isRunning()) {
            moveTimer.stop();
        }
    }

    private void handleContinuedCapture() {
        Piece capturingPiece = gameLogic.getCapturingPiece();
        List<Point> captures = new ArrayList<>();
        gameLogic.checkCaptures(capturingPiece, captures);

        if (!captures.isEmpty()) {
            selectAndMove(capturingPiece, captures);
        }
    }

    private void handleRequiredCapture() {
        List<Point> piecesCanCapture = gameLogic.getPiecesCanCapture();
        Point capturePoint = piecesCanCapture.get(random.nextInt(piecesCanCapture.size()));
        Piece piece = gameLogic.getBoard()[capturePoint.y][capturePoint.x];

        List<Point> captures = new ArrayList<>();
        gameLogic.checkCaptures(piece, captures);

        if (!captures.isEmpty()) {
            selectAndMove(piece, captures);
        }
    }

    private void handleRegularMove() {
        List<ComputerMove> allMoves = new ArrayList<>();
        Piece[][] board = gameLogic.getBoard();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece != null && !piece.isWhite()) {
                    List<Point> moves = gameLogic.calculatePossibleMoves(piece);
                    for (Point move : moves) {
                        allMoves.add(new ComputerMove(piece, move));
                    }
                }
            }
        }

        if (!allMoves.isEmpty()) {
            ComputerMove selectedMove = allMoves.get(random.nextInt(allMoves.size()));
            currentSelectedPiece = selectedMove.piece;
            currentPossibleMoves = gameLogic.calculatePossibleMoves(selectedMove.piece);

            if (listener != null) {
                listener.onMoveSelected(selectedMove.piece, selectedMove.move.y, selectedMove.move.x);
            }
        }
    }

    private void selectAndMove(Piece piece, List<Point> moves) {
        currentSelectedPiece = piece;
        currentPossibleMoves = new ArrayList<>(moves);

        Point movePoint = moves.get(random.nextInt(moves.size()));
        if (listener != null) {
            listener.onMoveSelected(piece, movePoint.y, movePoint.x);
        }
    }

    public void clearSelection() {
        currentSelectedPiece = null;
        currentPossibleMoves.clear();
    }

    private static class ComputerMove {

        Piece piece;
        Point move;

        ComputerMove(Piece piece, Point move) {
            this.piece = piece;
            this.move = move;
        }
    }

    public interface ComputerMoveListener {

        void onMoveSelected(Piece piece, int row, int col);
    }
}
