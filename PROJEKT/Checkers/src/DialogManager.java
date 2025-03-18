import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

/**
 * Klasa zarządzająca dialogami w aplikacji Warcaby.
 */
public class DialogManager {

    /**
     * Tworzy dialog oczekiwania na przeciwnika z przyciskiem anulowania.
     *
     * @param parent       Okno rodzica dla dialogu
     * @param cancelAction Akcja wykonywana po kliknięciu przycisku anulowania
     * @return Dialog oczekiwania
     */
    public static JDialog createWaitingDialog(JFrame parent, ActionListener cancelAction) {
        JDialog waitingDialog = new JDialog(parent, "Oczekiwanie na przeciwnika", false);
        waitingDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(GameConstants.BACKGROUND_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(
                GameConstants.CONTENT_PANEL_VERTICAL_PADDING,
                GameConstants.CONTENT_PANEL_HORIZONTAL_PADDING,
                GameConstants.CONTENT_PANEL_VERTICAL_PADDING,
                GameConstants.CONTENT_PANEL_HORIZONTAL_PADDING));

        JLabel searchingLabel = new JLabel("Szukanie przeciwnika...", JLabel.CENTER);
        searchingLabel.setFont(GameConstants.DIALOG_CONTENT_FONT);
        contentPanel.add(searchingLabel, BorderLayout.CENTER);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setBorder(BorderFactory.createEmptyBorder(
                GameConstants.PROGRESS_BAR_VERTICAL_PADDING, 0,
                GameConstants.PROGRESS_BAR_VERTICAL_PADDING, 0));
        contentPanel.add(progressBar, BorderLayout.SOUTH);

        JButton cancelButton = new JButton(GameConstants.CANCEL_BUTTON_TEXT);
        cancelButton.addActionListener(cancelAction);

        waitingDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (cancelAction != null) {
                    cancelAction.actionPerformed(new java.awt.event.ActionEvent(
                            waitingDialog, java.awt.event.ActionEvent.ACTION_PERFORMED, "windowClosing"));
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(GameConstants.BACKGROUND_COLOR);
        buttonPanel.add(cancelButton);

        waitingDialog.add(contentPanel, BorderLayout.CENTER);
        waitingDialog.add(buttonPanel, BorderLayout.SOUTH);
        waitingDialog.setSize(GameConstants.WAITING_DIALOG_WIDTH, GameConstants.WAITING_DIALOG_HEIGHT);
        waitingDialog.setLocationRelativeTo(parent);

        return waitingDialog;
    }

    /**
     * Tworzy dialog powiadomienia o wyniku gry.
     *
     * @param parent      Okno rodzica dla dialogu
     * @param title       Tytuł dialogu
     * @param message     Wiadomość do wyświetlenia
     * @param isSuccess   Czy wynik jest sukcesem (wpływa na kolory)
     * @param closeDelay  Opóźnienie zamknięcia w milisekundach
     * @param closeAction Akcja wykonywana po zamknięciu dialogu
     * @return Dialog informacyjny
     */
    public static JDialog createGameResultDialog(JFrame parent, String title, String message,
                                                 boolean isSuccess, int closeDelay,
                                                 Runnable closeAction) {
        JDialog resultDialog = new JDialog(parent, title, false);
        resultDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new BorderLayout());

        if (isSuccess) {
            contentPanel.setBackground(GameConstants.WIN_DIALOG_BACKGROUND);
            JLabel resultLabel = new JLabel("<html><center>" + message + "</center></html>", JLabel.CENTER);
            resultLabel.setFont(GameConstants.DIALOG_TITLE_FONT);
            resultLabel.setForeground(GameConstants.WIN_TEXT_COLOR);
            contentPanel.add(resultLabel, BorderLayout.CENTER);
        } else {
            contentPanel.setBackground(GameConstants.LOSE_DIALOG_BACKGROUND);
            JLabel resultLabel = new JLabel("<html><center>" + message + "</center></html>", JLabel.CENTER);
            resultLabel.setFont(GameConstants.DIALOG_TITLE_FONT);
            resultLabel.setForeground(GameConstants.LOSE_TEXT_COLOR);
            contentPanel.add(resultLabel, BorderLayout.CENTER);
        }

        contentPanel.setBorder(BorderFactory.createEmptyBorder(
                GameConstants.DIALOG_CONTENT_VERTICAL_PADDING,
                GameConstants.DIALOG_CONTENT_HORIZONTAL_PADDING,
                GameConstants.DIALOG_CONTENT_VERTICAL_PADDING,
                GameConstants.DIALOG_CONTENT_HORIZONTAL_PADDING));
        resultDialog.add(contentPanel, BorderLayout.CENTER);
        resultDialog.setSize(GameConstants.DIALOG_WIDTH, GameConstants.DIALOG_HEIGHT);
        resultDialog.setLocationRelativeTo(parent);

        if (closeDelay > 0) {
            Timer closeTimer = new Timer(closeDelay, _ -> {
                resultDialog.dispose();
                if (closeAction != null) {
                    closeAction.run();
                }
            });
            closeTimer.setRepeats(false);
            closeTimer.start();
        }

        return resultDialog;
    }

    /**
     * Wyświetla tymczasowe powiadomienie w panelu głównym.
     *
     * @param parent     Okno rodzica dla powiadomienia
     * @param message    Wiadomość do wyświetlenia
     * @param durationMs Czas wyświetlania w milisekundach
     */
    public static void showTemporaryNotification(JFrame parent, String message, int durationMs) {
        JPanel notificationPanel = new JPanel();
        notificationPanel.setLayout(new BorderLayout());
        notificationPanel.setBackground(GameConstants.NOTIFICATION_BACKGROUND);
        notificationPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GameConstants.NOTIFICATION_BORDER, 1),
                BorderFactory.createEmptyBorder(
                        GameConstants.NOTIFICATION_PADDING_VERTICAL,
                        GameConstants.NOTIFICATION_PADDING_HORIZONTAL,
                        GameConstants.NOTIFICATION_PADDING_VERTICAL,
                        GameConstants.NOTIFICATION_PADDING_HORIZONTAL)));

        JLabel notificationLabel = new JLabel(message);
        notificationLabel.setForeground(GameConstants.NOTIFICATION_TEXT);
        notificationLabel.setFont(GameConstants.NOTIFICATION_FONT);
        notificationPanel.add(notificationLabel, BorderLayout.CENTER);

        parent.getLayeredPane().add(notificationPanel, JLayeredPane.POPUP_LAYER);

        notificationPanel.setBounds(
                parent.getWidth() - GameConstants.NOTIFICATION_RIGHT_OFFSET,
                GameConstants.NOTIFICATION_TOP_POSITION,
                GameConstants.NOTIFICATION_WIDTH,
                GameConstants.NOTIFICATION_HEIGHT);
        notificationPanel.setVisible(true);
        parent.validate();
        parent.repaint();

        Timer fadeTimer = createFadeTimer(parent, notificationPanel, durationMs);
        fadeTimer.start();
    }

    /**
     * Tworzy timer odpowiedzialny za płynne zanikanie powiadomienia.
     *
     * @param parent            Okno rodzica dla powiadomienia
     * @param notificationPanel Panel powiadomienia do wygaszenia
     * @param durationMs        Czas wyświetlania powiadomienia w milisekundach
     * @return Timer kontrolujący animację zanikania
     */
    private static Timer createFadeTimer(JFrame parent, JPanel notificationPanel, int durationMs) {
        Timer fadeTimer = new Timer(durationMs - GameConstants.NOTIFICATION_FADE_START, _ -> {
            final Timer fadeOutTimer = new Timer(GameConstants.NOTIFICATION_FADE_STEP, fade -> {
                Color bg = notificationPanel.getBackground();
                int alpha = bg.getAlpha();

                if (alpha <= GameConstants.NOTIFICATION_FADE_MIN_ALPHA) {
                    parent.getLayeredPane().remove(notificationPanel);
                    parent.validate();
                    parent.repaint();
                    ((Timer) fade.getSource()).stop();
                } else {
                    alpha -= GameConstants.NOTIFICATION_FADE_ALPHA_STEP;
                    Color baseColor = GameConstants.NOTIFICATION_BACKGROUND;
                    notificationPanel.setBackground(new Color(
                            baseColor.getRed(),
                            baseColor.getGreen(),
                            baseColor.getBlue(),
                            alpha));
                    parent.validate();
                    parent.repaint();
                }
            });
            fadeOutTimer.setRepeats(true);
            fadeOutTimer.start();
        });
        fadeTimer.setRepeats(false);
        return fadeTimer;
    }
}