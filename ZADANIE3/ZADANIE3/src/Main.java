// Napisz program losujący liczbę z zakresu 0-N.
// Limit zakresu N przekazywany jest z linii poleceń.
// Program pyta użytkownika, jaka jest wylosowana liczba.
// Jeżeli użytkownik nie zgadł, dowiaduje się czy wylosowana liczba jest większa czy mniejsza od podanej.
// Jeżeli zgadł, dowiaduje się ile wykonał prób i jest pytany czy chce kontynuować grę.
// Uwzględnij w programie wszelkie możliwe pomyłki użytkownika w przekazaniu parametru do programu.

import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        boolean continueGame = true;
        while (continueGame) { // Nieskończona pętelka gry
            int limit;
            while (true) { // Pętelka do podania poprawnego limitu
                System.out.print("Podaj limit zakresu N: ");
                try {
                    limit = Integer.parseInt(scanner.nextLine());
                    if (limit > 0) {
                        break;
                    } else {
                        System.out.println("Błąd: Limit N musi być większy od 0.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Błąd: Proszę wprowadzić poprawny limit N.");
                }
            }

            int randomNumber = random.nextInt(limit + 1);
            int attempts = 0;
            boolean guessed = false;

            while (!guessed) { // Pętelka do zgadywania liczby
                System.out.print("Zgadnij wylosowaną liczbę (0-" + limit + "): ");
                int userGuess;

                try {
                    userGuess = Integer.parseInt(scanner.nextLine());
                    attempts++;

                    // Czy liczba w dobrym zakresie
                    if (userGuess < 0 || userGuess > limit) {
                        System.out.println("Błąd: Proszę podać liczbę w zakresie 0-" + limit + ".");
                        continue;
                    }

                    // Nasza próba vs wylosowana liczba
                    if (userGuess < randomNumber) {
                        System.out.println("Wylosowana liczba jest większa.");
                    } else if (userGuess > randomNumber) {
                        System.out.println("Wylosowana liczba jest mniejsza.");
                    } else {
                        guessed = true; // Liczba została zgadnięta
                        System.out.println("Gratulacje! Zgadłeś liczbę w " + attempts + " próbach.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Błąd: Proszę wprowadzić poprawną liczbę całkowitą.");
                }
            }

            // Czy kontynuować grę
            String response;
            do {
                System.out.print("Czy chcesz kontynuować grę? (tak/nie): ");
                response = scanner.nextLine();
                continueGame = response.equalsIgnoreCase("tak"); // Resecik gry
            } while (!response.equalsIgnoreCase("tak") && !response.equalsIgnoreCase("nie"));
        }

        System.out.println("Dziękujemy za grę!");
        scanner.close();
    }
}