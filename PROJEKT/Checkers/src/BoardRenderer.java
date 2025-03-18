import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

public class BoardRenderer {

    private final GameLogic gameLogic;
    private boolean boardFlipped;
    private Piece selectedPiece;
    private List<Point> possibleMoves;

    public BoardRenderer(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
        this.boardFlipped = false;
        this.possibleMoves = null;
    }

    public void setBoardFlipped(boolean flipped) {
        this.boardFlipped = flipped;
    }

    public boolean isBoardFlipped() {
        return boardFlipped;
    }

    public void setSelectedPiece(Piece piece) {
        this.selectedPiece = piece;
    }

    public void setPossibleMoves(List<Point> moves) {
        this.possibleMoves = moves;
    }

    public void drawBoard(Graphics g, int offset) {
        for (int row = 0; row < GameConstants.BOARD_SIZE; row++) {
            for (int col = 0; col < GameConstants.BOARD_SIZE; col++) {
                int drawRow = boardFlipped ? (GameConstants.BOARD_SIZE - 1 - row) : row;
                int drawCol = boardFlipped ? (GameConstants.BOARD_SIZE - 1 - col) : col;

                if ((row + col) % 2 == 0) {
                    g.setColor(GameConstants.LIGHT_SQUARE);
                } else {
                    g.setColor(GameConstants.DARK_SQUARE);
                }

                g.fillRect(offset + drawCol * GameConstants.SQUARE_SIZE,
                        offset + drawRow * GameConstants.SQUARE_SIZE,
                        GameConstants.SQUARE_SIZE,
                        GameConstants.SQUARE_SIZE);
            }
        }
    }

    public void drawPieces(Graphics g, int offset) {
        Piece[][] board = gameLogic.getBoard();

        for (int row = 0; row < GameConstants.BOARD_SIZE; row++) {
            for (int col = 0; col < GameConstants.BOARD_SIZE; col++) {
                int drawRow = boardFlipped ? (GameConstants.BOARD_SIZE - 1 - row) : row;
                int drawCol = boardFlipped ? (GameConstants.BOARD_SIZE - 1 - col) : col;
                Piece piece = board[row][col];

                if (piece != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int x = offset + drawCol * GameConstants.SQUARE_SIZE + GameConstants.SQUARE_SIZE / 2;
                    int y = offset + drawRow * GameConstants.SQUARE_SIZE + GameConstants.SQUARE_SIZE / 2;
                    int radius = GameConstants.SQUARE_SIZE / 2 - 10;

                    if (piece.isWhite()) {
                        g2d.setPaint(new GradientPaint(x - radius, y - radius, Color.WHITE, x + radius, y + radius,
                                new Color(220, 220, 220)));
                    } else {
                        g2d.setPaint(new GradientPaint(x - radius, y - radius, Color.BLACK, x + radius, y + radius,
                                new Color(50, 50, 50)));
                    }
                    g2d.fillOval(x - radius, y - radius, radius * 2, radius * 2);

                    g2d.setStroke(new BasicStroke(2));
                    g2d.setColor(piece.isWhite() ? new Color(100, 100, 100) : new Color(150, 150, 150));
                    g2d.drawOval(x - radius, y - radius, radius * 2, radius * 2);

                    if (piece.isKing()) {
                        g2d.setColor(piece.isWhite() ? Color.BLACK : Color.WHITE);
                        g2d.setFont(GameConstants.PIECE_KING_FONT);
                        FontMetrics fm = g2d.getFontMetrics();
                        g2d.drawString("K", x - fm.stringWidth("K") / 2, y + fm.getHeight() / 3);

                        int crownSize = radius - 10;
                        g2d.drawArc(x - crownSize / 2, y - crownSize / 2 - 5, crownSize, crownSize, 0, 180);
                    }

                    if (selectedPiece == piece) {
                        g2d.setColor(GameConstants.HIGHLIGHT_COLOR);
                        g2d.setStroke(new BasicStroke(4));
                        g2d.drawOval(x - radius - 5, y - radius - 5, (radius + 5) * 2, (radius + 5) * 2);
                    }
                }
            }
        }
    }

    public void highlightPossibleMoves(Graphics g, int offset) {
        if (possibleMoves == null) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(GameConstants.POSSIBLE_MOVE_COLOR);

        for (Point move : possibleMoves) {
            int drawX = boardFlipped ? (GameConstants.BOARD_SIZE - 1 - move.x) : move.x;
            int drawY = boardFlipped ? (GameConstants.BOARD_SIZE - 1 - move.y) : move.y;
            int x = offset + drawX * GameConstants.SQUARE_SIZE;
            int y = offset + drawY * GameConstants.SQUARE_SIZE;
            g2d.fillRect(x, y, GameConstants.SQUARE_SIZE, GameConstants.SQUARE_SIZE);
        }
    }

    public void highlightPiecesCanCapture(Graphics g, int offset) {
        List<Point> piecesCanCapture = gameLogic.getPiecesCanCapture();
        if (piecesCanCapture.isEmpty()) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(GameConstants.CAPTURE_HIGHLIGHT_COLOR);
        Piece[][] board = gameLogic.getBoard();

        for (Point p : piecesCanCapture) {
            Piece piece = board[p.y][p.x];
            if (piece != null) {
                int drawX = boardFlipped ? (GameConstants.BOARD_SIZE - 1 - p.x) : p.x;
                int drawY = boardFlipped ? (GameConstants.BOARD_SIZE - 1 - p.y) : p.y;
                int x = offset + drawX * GameConstants.SQUARE_SIZE + GameConstants.SQUARE_SIZE / 2;
                int y = offset + drawY * GameConstants.SQUARE_SIZE + GameConstants.SQUARE_SIZE / 2;
                int radius = GameConstants.SQUARE_SIZE / 2 - 5;
                g2d.fillOval(x - radius, y - radius, radius * 2, radius * 2);
            }
        }
    }

    public Point getBoardCoordinates(int x, int y, int offset) {
        x -= offset;
        y -= offset;

        if (x < 0 || y < 0 || x > GameConstants.SQUARE_SIZE * GameConstants.BOARD_SIZE ||
                y > GameConstants.SQUARE_SIZE * GameConstants.BOARD_SIZE) {
            return null;
        }

        int col = x / GameConstants.SQUARE_SIZE;
        int row = y / GameConstants.SQUARE_SIZE;

        if (boardFlipped) {
            col = GameConstants.BOARD_SIZE - 1 - col;
            row = GameConstants.BOARD_SIZE - 1 - row;
        }

        if (row < 0 || row >= GameConstants.BOARD_SIZE || col < 0 || col >= GameConstants.BOARD_SIZE) {
            return null;
        }

        return new Point(col, row);
    }
}
