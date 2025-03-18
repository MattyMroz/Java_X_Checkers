public class Piece {
    private final boolean isWhite;
    private boolean isKing;
    private int row;
    private int col;

    public Piece(boolean isWhite, int row, int col) {
        this.isWhite = isWhite;
        this.isKing = false;
        this.row = row;
        this.col = col;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public boolean isKing() {
        return isKing;
    }

    public void setKing(boolean isKing) {
        this.isKing = isKing;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }
}