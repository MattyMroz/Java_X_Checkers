
import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5000;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Scanner scanner;
    private AtomicBoolean running = new AtomicBoolean(false);
    private Thread responseThread;
    private boolean alreadyStopped = false;
    private String clientId;
    private CountDownLatch idReceived = new CountDownLatch(1);

    public void start() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            scanner = new Scanner(System.in);
            running.set(true);

            System.out.println("Połączono z serwerem: " + SERVER_ADDRESS + ":" + SERVER_PORT);
            System.out.println("Oczekiwanie na przydzielenie ID klienta...");

            // Uruchom wątek do odbierania odpowiedzi z serwera
            responseThread = new Thread(() -> {
                try {
                    String response;
                    while (running.get() && (response = in.readLine()) != null) {
                        if (response.startsWith("ID:")) {
                            clientId = response.substring(3);
                            System.out.println("Otrzymano ID klienta: " + clientId);
                            idReceived.countDown(); // Oznacz, że ID zostało otrzymane
                        } else if (response.startsWith("NOTYFIKACJA:")) {
                            System.out.println("\n🔔 " + response);
                            if (running.get()) {
                                wyswietlMenu();
                            }
                        } else {
                            System.out.println("Odpowiedź serwera: " + response);
                            if (running.get()) {
                                wyswietlMenu();
                            }
                        }
                    }
                } catch (IOException e) {
                    if (running.get()) {
                        System.err.println("Utracono połączenie z serwerem: " + e.getMessage());
                    }
                }
            });
            responseThread.setDaemon(true);
            responseThread.start();

            // Poczekaj na otrzymanie ID klienta
            try {
                idReceived.await();
            } catch (InterruptedException e) {
                System.err.println("Przerwano oczekiwanie na ID klienta: " + e.getMessage());
                stop();
                return;
            }

            // Główna pętla interfejsu użytkownika
            while (running.get()) {
                wyswietlMenu();

                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        utworzNotyfikacje();
                        break;

                    case "2":
                        System.out.println("Zamykanie połączenia...");
                        stop();
                        return;

                    default:
                        System.out.println("Nieprawidłowa opcja. Wybierz 1 lub 2.");
                        break;
                }
            }

        } catch (UnknownHostException e) {
            System.err.println("Nie można znaleźć serwera: " + SERVER_ADDRESS);
        } catch (IOException e) {
            System.err.println("Błąd połączenia: " + e.getMessage());
        } finally {
            stop();
        }
    }

    private void wyswietlMenu() {
        System.out.println("\nWybierz opcję:");
        System.out.println("1. Utwórz notyfikację");
        System.out.println("2. Wyjdź");
        System.out.print("> ");
    }

    private void utworzNotyfikacje() {
        try {
            System.out.print("Podaj treść notyfikacji: ");
            String tresc = scanner.nextLine();

            if (tresc.isEmpty()) {
                throw new NotyfikacjaException("Treść notyfikacji nie może być pusta");
            }

            if (tresc.length() > 100) {
                throw new NotyfikacjaException("Treść notyfikacji nie może przekraczać 100 znaków");
            }

            System.out.println("Podaj datę i czas wysłania notyfikacji (format: DD-MM-YYYY HH:mm:ss)");
            System.out.print("> ");
            String czasStr = scanner.nextLine();

            // Walidacja formatu daty
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            LocalDateTime czas = LocalDateTime.parse(czasStr, formatter);

            // Sprawdź, czy data jest w przyszłości
            if (czas.isBefore(LocalDateTime.now())) {
                throw new NotyfikacjaException("Data musi być w przyszłości");
            }

            // Wyślij notyfikację do serwera
            out.println("NOTYFIKACJA:" + tresc + "|" + czasStr);
            System.out.println("Wysłano żądanie utworzenia notyfikacji. Oczekiwanie na odpowiedź...");

        } catch (NotyfikacjaException e) {
            System.err.println("Błąd walidacji: " + e.getMessage());
        } catch (DateTimeParseException e) {
            System.err.println("Błąd formatu daty: Podaj datę w formacie dd-MM-yyyy HH:mm:ss");
        }
    }

    public void stop() {
        if (alreadyStopped) {
            return;
        }

        alreadyStopped = true;
        running.set(false);
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (scanner != null) {
                scanner.close();
            }
            System.out.println("Klient zakończył działanie.");
        } catch (IOException e) {
            System.err.println("Błąd podczas zamykania połączenia: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }
}
