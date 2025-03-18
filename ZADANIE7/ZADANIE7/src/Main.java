// Napisz program sieciowy (aplikacja kliencka + aplikacja serwerowa) służący do kolejkowania i wysyłania notyfikacji. Program powinien obsługiwać domyślne wyjątki oraz implementację przynajmniej jednego wyjątku związanego z walidacją treści wprowadzanych przez użytkownika w aplikacji klienckiej.

// Funkcje aplikacji klienckiej:
// połączenie z serwerem wraz z walidacją i obsługą wyjątków
// pobranie od użytkownika treści notyfikacji (wiadomość tekstowa) oraz czasu odesłania notyfikacji do użytkownika
// wyświetlenie otrzymanej przez serwer notyfikacji
// Funkcje aplikacji serwerowej:
// obsługa wielu klientów jednocześnie
// przyjmowanie wysłanych z aplikacji klienckiej notyfikacji i kolejkowanie ich na serwerze
// wysyłanie notyfikacji do klienta który ją zapisał o podanym przez niego czasie

public class Main {

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("server")) {
            System.out.println("Uruchamianie serwera...");
            Server.main(new String[0]);
        } else if (args.length > 0 && args[0].equalsIgnoreCase("client")) {
            System.out.println("Uruchamianie klienta...");
            Client.main(new String[0]);
        } else {
            System.out.println("Użycie: java Main [server|client]");
            System.out.println("  server - uruchamia serwer");
            System.out.println("  client - uruchamia klienta");
        }
    }
}

// javac src/*.java -d out
// java -cp out Main server
// java -cp out Main client
