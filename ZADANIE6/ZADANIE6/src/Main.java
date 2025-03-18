//Napisz program proszący o podanie 2 wektorów (wektor to ciąg liczb). Koniec wektora oznacza się za pomocą wciśnięcia klawisza enter. Jeżeli podany ciąg nie jest liczbą, jest ignorowany. Następnie należy spróbować dodać wektory, jeżeli są równej długości (są równej długości jeśli mają tę samą liczbę elementów). Jeżeli nie są, rzucany jest własny wyjątek WektoryRoznejDlugosciException, za pomocą którego można podać a następnie odczytać długości tych wektorów (należy tak skonstruować wyjątek, aby możliwe było skonstruowanie zdania po jego przechwyceniu : "Długość pierwszego wektora to AA a drugiego to BB" lub dowolnego innego zdania wykorzystującego wartości AA i BB, np. określającego różnicę w długościach). Jeżeli są równej długości, wynik dodawania zapisywany jest do pliku. Jeżeli nie są równej długości, użytkownik jest proszony o ponowne wprowadzenie tych wektorów.

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class WektoryRoznejDlugosciException extends Exception {

    private final int dlugoscPierwszego;
    private final int dlugoscDrugiego;

    public WektoryRoznejDlugosciException(int dlugoscPierwszego, int dlugoscDrugiego) {
        this.dlugoscPierwszego = dlugoscPierwszego;
        this.dlugoscDrugiego = dlugoscDrugiego;
    }

    public int getDlugoscPierwszego() {
        return dlugoscPierwszego;
    }

    public int getDlugoscDrugiego() {
        return dlugoscDrugiego;
    }
}

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean poprawne = false;

        while (!poprawne) {
            try {
                List<Double> wektor1 = null;
                List<Double> wektor2 = null;

                // Pobieranie pierwszego wektora z walidacją
                while (wektor1 == null || wektor1.isEmpty()) {
                    System.out.println("Podaj pierwszy wektor (liczby oddzielone spacją, zakończ enterem):");
                    wektor1 = wczytajWektor(scanner);
                    if (wektor1.isEmpty()) {
                        System.out.println("Nie podano żadnych poprawnych liczb! Spróbuj ponownie.");
                    }
                }

                // Pobieranie drugiego wektora z walidacją
                while (wektor2 == null || wektor2.isEmpty()) {
                    System.out.println("Podaj drugi wektor (liczby oddzielone spacją, zakończ enterem):");
                    wektor2 = wczytajWektor(scanner);
                    if (wektor2.isEmpty()) {
                        System.out.println("Nie podano żadnych poprawnych liczb! Spróbuj ponownie.");
                    }
                }

                List<Double> wynik = dodajWektory(wektor1, wektor2);
                zapiszDoPliku(wynik);
                System.out.println("Wektory zostały dodane i wynik zapisano do pliku.");
                System.out.println("Wynik: " + wynikToString(wynik));
                poprawne = true;
            } catch (WektoryRoznejDlugosciException e) {
                System.out.println("Długość pierwszego wektora to " + e.getDlugoscPierwszego()
                        + " a drugiego to " + e.getDlugoscDrugiego());
                System.out.println("Różnica w długościach wynosi: "
                        + Math.abs(e.getDlugoscPierwszego() - e.getDlugoscDrugiego()));
                System.out.println("Spróbuj ponownie.\n");
            } catch (IOException e) {
                System.out.println("Błąd podczas zapisywania do pliku: " + e.getMessage());
                System.out.println("Spróbuję zapisać w innym miejscu...");
                try {
                    zapiszDoPliku(new ArrayList<>(), "wynik_awaryjny.txt");
                    System.out.println("Zapisano pusty plik awaryjny.");
                } catch (IOException ex) {
                    System.out.println("Nie udało się zapisać nawet pustego pliku. Kończę program.");
                }
                poprawne = true;
            }
        }
        scanner.close();
    }

    private static List<Double> wczytajWektor(Scanner scanner) {
        List<Double> wektor = new ArrayList<>();
        String linia = scanner.nextLine().trim();

        if (linia.isEmpty()) {
            return wektor; // Zwracamy pustą listę, jeśli nic nie wprowadzono
        }

        // Sprawdzamy, czy linia zawiera tylko liczby i spacje
        if (!linia.matches("^[0-9\\s.\\-]+$")) {
            System.out.println("Podany ciąg zawiera niedozwolone znaki! Wprowadź tylko liczby oddzielone spacjami.");
            return new ArrayList<>(); // Zwracamy pustą listę, aby wymusić ponowne wczytanie
        }

        String[] elementy = linia.split("\\s+");
        for (String element : elementy) {
            try {
                double liczba = Double.parseDouble(element);
                wektor.add(liczba);
            } catch (NumberFormatException e) {
                // Ten przypadek nie powinien wystąpić po walidacji regex, ale zostawiamy dla bezpieczeństwa
                System.out.println("Niepoprawna liczba: " + element);
                return new ArrayList<>(); // Zwracamy pustą listę, aby wymusić ponowne wczytanie
            }
        }

        return wektor;
    }

    private static List<Double> dodajWektory(List<Double> wektor1, List<Double> wektor2)
            throws WektoryRoznejDlugosciException {
        if (wektor1.size() != wektor2.size()) {
            throw new WektoryRoznejDlugosciException(wektor1.size(), wektor2.size());
        }

        List<Double> wynik = new ArrayList<>();
        for (int i = 0; i < wektor1.size(); i++) {
            wynik.add(wektor1.get(i) + wektor2.get(i));
        }
        return wynik;
    }

    private static void zapiszDoPliku(List<Double> wektor) throws IOException {
        zapiszDoPliku(wektor, "wynik.txt");
    }

    private static void zapiszDoPliku(List<Double> wektor, String nazwaPliku) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nazwaPliku))) {
            for (Double element : wektor) {
                writer.write(element.toString());
                writer.write(" ");
            }
        }
    }

    private static String wynikToString(List<Double> wektor) {
        StringBuilder sb = new StringBuilder();
        for (Double element : wektor) {
            sb.append(element).append(" ");
        }
        return sb.toString().trim();
    }
}
