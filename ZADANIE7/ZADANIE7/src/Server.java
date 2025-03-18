
import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {

    private static final int PORT = 5000;
    private static final ConcurrentHashMap<String, ClientConnection> clients = new ConcurrentHashMap<>();
    private static final PriorityBlockingQueue<Notyfikacja> kolejkaNotyfikacji
            = new PriorityBlockingQueue<>(100, Comparator.comparing(Notyfikacja::getCzasWyslania));

    public static void main(String[] args) {
        // Uruchom wątek do przetwarzania kolejki notyfikacji
        Thread notificationProcessor = new Thread(Server::processNotifications);
        notificationProcessor.setDaemon(true);
        notificationProcessor.start();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serwer uruchomiony na porcie " + PORT);

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    String clientId = UUID.randomUUID().toString();

                    System.out.println("Nowy klient połączony: ID=" + clientId
                            + ", Adres=" + clientSocket.getInetAddress());

                    // Uruchom nowy wątek dla każdego klienta
                    ClientHandler clientHandler = new ClientHandler(clientSocket, clientId);
                    new Thread(clientHandler).start();
                } catch (IOException e) {
                    System.err.println("Błąd podczas akceptowania połączenia: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Błąd podczas uruchamiania serwera: " + e.getMessage());
        }
    }

    // Metoda przetwarzająca kolejkę notyfikacji
    private static void processNotifications() {
        while (true) {
            try {
                // Sprawdź, czy są notyfikacje do wysłania
                Notyfikacja notyfikacja = kolejkaNotyfikacji.peek();

                if (notyfikacja != null) {
                    LocalDateTime now = LocalDateTime.now();

                    // Jeśli czas wysłania notyfikacji już minął
                    if (now.isAfter(notyfikacja.getCzasWyslania()) || now.isEqual(notyfikacja.getCzasWyslania())) {
                        // Usuń notyfikację z kolejki
                        kolejkaNotyfikacji.poll();

                        // Wyślij notyfikację do klienta
                        String clientId = notyfikacja.getKlientId();
                        ClientConnection clientConnection = clients.get(clientId);

                        if (clientConnection != null && clientConnection.isConnected()) {
                            try {
                                clientConnection.sendNotification(notyfikacja);
                                System.out.println("Wysłano notyfikację do klienta ID=" + clientId + ": " + notyfikacja);
                            } catch (IOException e) {
                                System.err.println("Błąd podczas wysyłania notyfikacji do klienta ID=" + clientId + ": " + e.getMessage());
                            }
                        } else {
                            System.out.println("Klient ID=" + clientId + " nie jest już połączony. Notyfikacja pominięta: " + notyfikacja);
                        }
                    } else {
                        // Oblicz czas do następnej notyfikacji
                        Duration timeToWait = Duration.between(now, notyfikacja.getCzasWyslania());
                        long millisToWait = Math.min(timeToWait.toMillis(), 1000); // Maksymalnie 1 sekunda
                        Thread.sleep(millisToWait);
                    }
                } else {
                    // Jeśli nie ma notyfikacji, poczekaj chwilę
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                System.err.println("Wątek przetwarzania notyfikacji przerwany: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Błąd podczas przetwarzania notyfikacji: " + e.getMessage());
            }
        }
    }

    // Klasa reprezentująca połączenie z klientem
    private static class ClientConnection {

        private final Socket socket;
        private final PrintWriter out;

        public ClientConnection(Socket socket, PrintWriter out) {
            this.socket = socket;
            this.out = out;
        }

        public boolean isConnected() {
            return socket != null && !socket.isClosed() && socket.isConnected();
        }

        public void sendNotification(Notyfikacja notyfikacja) throws IOException {
            if (isConnected()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                out.println("NOTYFIKACJA: " + notyfikacja.getTresc() + " (zaplanowana na: "
                        + notyfikacja.getCzasWyslania().format(formatter) + ")");
            } else {
                throw new IOException("Klient nie jest połączony");
            }
        }
    }

    // Klasa obsługująca komunikację z klientem
    private static class ClientHandler implements Runnable {

        private final Socket clientSocket;
        private final String clientId;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket, String id) {
            this.clientSocket = socket;
            this.clientId = id;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                // Zarejestruj klienta
                ClientConnection clientConnection = new ClientConnection(clientSocket, out);
                clients.put(clientId, clientConnection);

                // Wyślij ID klienta
                out.println("ID:" + clientId);

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Otrzymano od klienta ID=" + clientId + ": " + inputLine);

                    try {
                        if (inputLine.startsWith("NOTYFIKACJA:")) {
                            // Format: NOTYFIKACJA:treść|czas
                            String[] parts = inputLine.substring("NOTYFIKACJA:".length()).split("\\|");
                            if (parts.length != 2) {
                                out.println("BŁĄD: Niepoprawny format notyfikacji. Wymagany format: 'NOTYFIKACJA:treść|czas'");
                                continue;
                            }

                            String tresc = parts[0].trim();
                            String czasStr = parts[1].trim();

                            // Parsuj czas
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                            LocalDateTime czas = LocalDateTime.parse(czasStr, formatter);

                            // Utwórz i dodaj notyfikację do kolejki
                            Notyfikacja notyfikacja = new Notyfikacja(tresc, czas, clientId);
                            kolejkaNotyfikacji.add(notyfikacja);

                            System.out.println("Dodano notyfikację do kolejki: " + notyfikacja);
                            out.println("OK: Notyfikacja zaplanowana na " + czasStr);
                        } else {
                            out.println("BŁĄD: Nieznane polecenie. Użyj 'NOTYFIKACJA:treść|czas'");
                        }
                    } catch (DateTimeParseException e) {
                        out.println("BŁĄD: Niepoprawny format daty. Użyj formatu 'dd-MM-yyyy HH:mm:ss'");
                        System.out.println("Błąd parsowania daty od klienta ID=" + clientId + ": " + e.getMessage());
                    } catch (Exception e) {
                        out.println("BŁĄD: " + e.getMessage());
                        System.out.println("Błąd przetwarzania danych od klienta ID=" + clientId + ": " + e.getMessage());
                    }
                }

                // Klient się rozłączył
                System.out.println("Klient ID=" + clientId + " rozłączył się");
                clients.remove(clientId);

            } catch (IOException e) {
                System.out.println("Klient ID=" + clientId + " nieoczekiwanie rozłączył się: " + e.getMessage());
                clients.remove(clientId);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                    if (clientSocket != null) {
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    System.err.println("Błąd podczas zamykania zasobów: " + e.getMessage());
                }
            }
        }
    }
}
