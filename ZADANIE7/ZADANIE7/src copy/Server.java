
import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

    private static final int PORT = 5000;
    private static final ConcurrentHashMap<Integer, Socket> clients = new ConcurrentHashMap<>();
    private static final AtomicInteger clientIdCounter = new AtomicInteger(0);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serwer uruchomiony na porcie " + PORT);

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    int clientId = clientIdCounter.incrementAndGet();
                    clients.put(clientId, clientSocket);

                    System.out.println("Nowy klient połączony: ID=" + clientId
                            + ", Adres=" + clientSocket.getInetAddress()
                            + ", Liczba klientów: " + clients.size());

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

    // Klasa obsługująca komunikację z klientem
    private static class ClientHandler implements Runnable {

        private final Socket clientSocket;
        private final int clientId;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket, int id) {
            this.clientSocket = socket;
            this.clientId = id;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("Otrzymano od klienta ID=" + clientId + ": " + inputLine);

                    try {
                        // Parsowanie danych od klienta (oczekiwany format: "liczba1 liczba2")
                        String[] numbers = inputLine.split(" ");
                        if (numbers.length != 2) {
                            out.println("BŁĄD: Niepoprawny format danych. Wymagany format: 'liczba1 liczba2'");
                            continue;
                        }

                        int num1 = Integer.parseInt(numbers[0]);
                        int num2 = Integer.parseInt(numbers[1]);
                        int result = num1 + num2;

                        System.out.println("Obliczono dla klienta ID=" + clientId + ": "
                                + num1 + " + " + num2 + " = " + result);

                        // Wysłanie wyniku do klienta
                        out.println("WYNIK: " + result);
                    } catch (NumberFormatException e) {
                        out.println("BŁĄD: Niepoprawne liczby. Podaj dwie liczby całkowite.");
                        System.out.println("Błąd przetwarzania danych od klienta ID=" + clientId
                                + ": " + e.getMessage());
                    }
                }

                // Klient się rozłączył
                System.out.println("Klient ID=" + clientId + " rozłączył się");
                clients.remove(clientId);
                System.out.println("Liczba pozostałych klientów: " + clients.size());

            } catch (IOException e) {
                System.out.println("Klient ID=" + clientId + " nieoczekiwanie rozłączył się: " + e.getMessage());
                clients.remove(clientId);
                System.out.println("Liczba pozostałych klientów: " + clients.size());
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
