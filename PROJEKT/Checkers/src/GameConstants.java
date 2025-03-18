import java.awt.Color;
import java.awt.Font;

/**
 * Klasa zawierająca wszystkie stałe używane w grze warcaby.
 */
public class GameConstants {
    // Stałe planszy
    public static final int BOARD_SIZE = 8;
    public static final int SQUARE_SIZE = 70;
    public static final int BOARD_OFFSET = 30;

    // Kolory planszy
    public static final Color LIGHT_SQUARE = new Color(255, 206, 158);
    public static final Color DARK_SQUARE = new Color(209, 139, 71);
    public static final Color HIGHLIGHT_COLOR = new Color(255, 255, 0, 100);
    public static final Color POSSIBLE_MOVE_COLOR = new Color(0, 255, 0, 100);
    public static final Color CAPTURE_HIGHLIGHT_COLOR = new Color(255, 0, 0, 100);

    // Czcionki
    public static final Font PIECE_KING_FONT = new Font("Arial", Font.BOLD, 24);
    public static final Font STATUS_FONT = new Font("Arial", Font.BOLD, 18);
    public static final Font TIMER_FONT = new Font("Arial", Font.BOLD, 18);
    public static final Font DIALOG_TITLE_FONT = new Font("Arial", Font.BOLD, 18);
    public static final Font DIALOG_CONTENT_FONT = new Font("Arial", Font.BOLD, 14);
    public static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 14);
    public static final Font NOTIFICATION_FONT = new Font("Arial", Font.BOLD, 14);

    // Stałe sieciowe
    public static final int SERVER_PORT = 5000;
    public static final String SERVER_ADDRESS = "localhost";

    // Kolory interfejsu
    public static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    public static final Color BUTTON_COLOR = new Color(230, 230, 230);
    public static final Color BUTTON_TEXT_COLOR = new Color(50, 50, 50);
    public static final Color BUTTON_HOVER_COLOR = new Color(210, 210, 210);
    public static final Color BUTTON_BORDER_COLOR = new Color(180, 180, 180);
    public static final Color SECONDARY_TEXT_COLOR = new Color(70, 70, 70);
    public static final Color ERROR_TEXT_COLOR = Color.RED;

    // Kolory dialogów
    public static final Color WIN_DIALOG_BACKGROUND = new Color(200, 255, 200);
    public static final Color LOSE_DIALOG_BACKGROUND = new Color(255, 200, 200);
    public static final Color WIN_TEXT_COLOR = new Color(0, 150, 0);
    public static final Color LOSE_TEXT_COLOR = new Color(200, 0, 0);

    // Kolory powiadomień
    public static final Color NOTIFICATION_BACKGROUND = new Color(40, 40, 40, 180);
    public static final Color NOTIFICATION_BORDER = new Color(200, 200, 200, 120);
    public static final Color NOTIFICATION_TEXT = Color.WHITE;

    // Czasy (w milisekundach)
    public static final int NOTIFICATION_DURATION = 3000;
    public static final int PROMOTION_NOTIFICATION_DURATION = 2000;
    public static final int AI_MOVE_DELAY = 500;
    public static final int VISUAL_MOVE_DELAY = 300;
    public static final int GAME_OVER_DIALOG_DURATION = 3000;
    public static final int CONNECT_WAIT_TIME = 1000;
    public static final int DISCONNECT_WAIT_TIME = 300;
    public static final int NOTIFICATION_FADE_START = 700;
    public static final int NOTIFICATION_FADE_STEP = 30;
    public static final int NOTIFICATION_FADE_ALPHA_STEP = 10;
    public static final int NOTIFICATION_FADE_MIN_ALPHA = 15;

    // Teksty UI
    public static final String START_BUTTON_TEXT = "Start";
    public static final String PAUSE_BUTTON_TEXT = "Pauza";
    public static final String NEW_GAME_BUTTON_TEXT = "Nowa gra";
    public static final String SURRENDER_BUTTON_TEXT = "Poddaj się";
    public static final String FLIP_BOARD_BUTTON_TEXT = "Obróć planszę";
    public static final String CANCEL_BUTTON_TEXT = "Anuluj";

    // Wymiary UI
    public static final int DIALOG_WIDTH = 300;
    public static final int DIALOG_HEIGHT = 150;
    public static final int CONNECTING_DIALOG_WIDTH = 300;
    public static final int CONNECTING_DIALOG_HEIGHT = 130;
    public static final int WAITING_DIALOG_WIDTH = 280;
    public static final int WAITING_DIALOG_HEIGHT = 150;
    public static final int BUTTON_PADDING = 8;
    public static final int BUTTON_HORIZONTAL_PADDING = 15;

    // Wymiary powiadomień
    public static final int NOTIFICATION_WIDTH = 250;
    public static final int NOTIFICATION_HEIGHT = 40;
    public static final int NOTIFICATION_RIGHT_OFFSET = 280;
    public static final int NOTIFICATION_TOP_POSITION = 38;
    public static final int NOTIFICATION_PADDING_VERTICAL = 8;
    public static final int NOTIFICATION_PADDING_HORIZONTAL = 12;

    // Marginesy i paddingi układu
    public static final int MAIN_PANEL_PADDING = 10;
    public static final int BOARD_EXTRA_SPACE = 60;
    public static final int STATUS_PANEL_SPACING = 20;
    public static final int STATUS_PANEL_PADDING = 5;
    public static final int BUTTON_PANEL_SPACING = 10;
    public static final int CONTENT_PANEL_VERTICAL_PADDING = 15;
    public static final int CONTENT_PANEL_HORIZONTAL_PADDING = 20;
    public static final int DIALOG_CONTENT_VERTICAL_PADDING = 20;
    public static final int DIALOG_CONTENT_HORIZONTAL_PADDING = 30;
    public static final int PROGRESS_BAR_VERTICAL_PADDING = 10;
}