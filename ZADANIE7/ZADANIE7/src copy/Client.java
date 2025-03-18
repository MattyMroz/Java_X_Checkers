
import java.io.*;
import java.net.*;
import java.util.Scanner;
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

    public void start() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            scanner = new Scanner(System.in);
            running.set(true);

            System.out.println("Połączono z serwerem: " + SERVER_ADDRESS + ":" + SERVER_PORT);

            // Uruchom wątek do odbierania odpowiedzi z serwera
            responseThread = new Thread(() -> {
                try {
                    String response;
                    while (running.get() && (response = in.readLine()) != null) {
                        System.out.println("Odpowiedź serwera: " + response);
                        if (running.get()) {
                            System.out.println("\nWybierz opcję:");
                            System.out.println("1. Wprowadź dane");
                            System.out.println("2. Wyjdź");
                            System.out.print("> ");
                        }
                    }
                } catch (IOException e) {
                    if (running.get()) {
                        System.err.println("Utracono połączenie z serwerem: " + e.getMessage());
                    }
                }
            });
            responseThread.setDaemon(true); // Oznacz jako wątek daemon, aby nie blokował zakończenia programu
            responseThread.start();

            // Główna pętla interfejsu użytkownika
            while (running.get()) {
                System.out.println("\nWybierz opcję:");
                System.out.println("1. Wprowadź dane");
                System.out.println("2. Wyjdź");
                System.out.print("> ");

                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        System.out.print("Podaj pierwszą liczbę: ");
                        String num1 = scanner.nextLine();

                        System.out.print("Podaj drugą liczbę: ");
                        String num2 = scanner.nextLine();

                        // Wysłanie danych do serwera
                        out.println(num1 + " " + num2);
                        System.out.println("Wysłano dane do serwera. Oczekiwanie na odpowiedź...");
                        break;

                    case "2":
                        System.out.println("Zamykanie połączenia...");
                        stop();
                        return; // Wyjdź z metody start(), co zakończy program

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
            stop(); // Upewnij się, że zasoby są zwolnione
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
