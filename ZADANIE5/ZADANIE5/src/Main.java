//Napisz klasę NrTelefoniczny, posiadającą dwa pola: nrkierunkowy i nrTelefonu i implementującą interfejs Comparable. Następnie utwórz abstrakcyjną klasę Wpis a następnie dziedziczące z niej klasy Osoba i Firma. Klasa Wpis ma abstrakcyjną metodę opis, która opisuje dany wpis. Być może ma również inne metody abstrakcyjne lub nie w miarę potrzeb. Klasa Osoba ma zawierać informacje o imieniu, nazwisku, adresie i (w tym nrTelefonu). Klasa Firma ma mieć nazwę i adres (w tym NrTelefonu). Utwórz kilka obiektów klasy Osoba i kilka obiektów klasy Firma i umieść je w kontenerze TreeMap, posługując się jako kluczem numerem telefonicznym. Następnie wypisz utworzoną w ten sposób książkę telefoniczną za pomocą iteratora. Następnie zaproponuj sposób eliminacji tych wpisów, które mają identyczną nazwę ulicy w adresie. Wypisz ponownie zawartość mapy.
import java.util.*;

class NrTelefoniczny implements Comparable<NrTelefoniczny> {
    private final String nrKierunkowy;
    private final String nrTelefonu;

    public NrTelefoniczny(String nrKierunkowy, String nrTelefonu) {
        this.nrKierunkowy = nrKierunkowy;
        this.nrTelefonu = nrTelefonu;
    }

    @Override
    public int compareTo(NrTelefoniczny other) {
        return (this.nrKierunkowy + this.nrTelefonu).compareTo(other.nrKierunkowy + other.nrTelefonu);
    }

    @Override
    public String toString() {
        return nrKierunkowy + " " + nrTelefonu;
    }
}

abstract class Wpis {
    protected NrTelefoniczny nrTelefonu;
    protected String adres;

    public abstract void opis();

    public String getAdres() {
        return adres;
    }
}

class Osoba extends Wpis {
    private final String imie;
    private final String nazwisko;

    public Osoba(String imie, String nazwisko, String adres, NrTelefoniczny nrTelefonu) {
        this.imie = imie;
        this.nazwisko = nazwisko;
        this.adres = adres;
        this.nrTelefonu = nrTelefonu;
    }

    @Override
    public void opis() {
        System.out.println("Osoba: " + imie + " " + nazwisko);
        System.out.println("Adres: " + adres);
        System.out.println("Telefon: " + nrTelefonu);
        System.out.println();
    }
}

class Firma extends Wpis {
    private final String nazwa;

    public Firma(String nazwa, String adres, NrTelefoniczny nrTelefonu) {
        this.nazwa = nazwa;
        this.adres = adres;
        this.nrTelefonu = nrTelefonu;
    }

    @Override
    public void opis() {
        System.out.println("Firma: " + nazwa);
        System.out.println("Adres: " + adres);
        System.out.println("Telefon: " + nrTelefonu);
        System.out.println();
    }
}

public class Main {
    public static void main(String[] args) {
        try {
            // Tworzenie książki telefonicznej
            TreeMap<NrTelefoniczny, Wpis> ksiazkaTelefoniczna = new TreeMap<>();

            // Dodawanie wpisów
            ksiazkaTelefoniczna.put(new NrTelefoniczny("+48", "123456789"), new Osoba("Jan", "Kowalski", "ul. Kwiatowa 1", new NrTelefoniczny("+48", "123456789")));

            ksiazkaTelefoniczna.put(new NrTelefoniczny("+48", "987654321"), new Firma("ABC Sp. z o.o.", "ul. Kwiatowa 2", new NrTelefoniczny("+48", "987654321")));

            ksiazkaTelefoniczna.put(new NrTelefoniczny("+48", "555555555"), new Osoba("Anna", "Nowak", "ul. Lipowa 10", new NrTelefoniczny("+48", "555555555")));

            // Wyświetlanie książki telefonicznej
            System.out.println("Książka telefoniczna przed usunięciem:");
            for (Wpis wpis : ksiazkaTelefoniczna.values()) {
                wpis.opis();
            }

            // Usuwanie wpisów z identyczną nazwą ulicy
            String ulicaDoUsuniecia = "ul. Kwiatowa";
            ksiazkaTelefoniczna.entrySet().removeIf(entry -> entry.getValue().getAdres().contains(ulicaDoUsuniecia));

            // Wyświetlanie książki telefonicznej po usunięciu
            System.out.println("\nKsiążka telefoniczna po usunięciu wpisów z ulicą " + ulicaDoUsuniecia + ":");
            for (Wpis wpis : ksiazkaTelefoniczna.values()) {
                wpis.opis();
            }

        } catch (Exception e) {
            System.out.println("Wystąpił błąd: " + e.getMessage());
            e.printStackTrace();
        }
    }
}