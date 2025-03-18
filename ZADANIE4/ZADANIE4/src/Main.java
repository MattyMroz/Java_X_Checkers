//Napisz program umożliwiający przygotowanie listy zakupów. Praca z programem odbywa się w trybie tekstowym (bez interfejsu graficznego). Program wczytuje z pliku tekstowego listę możliwych do zakupienia produktów, które podzielone są na kategorie. Przykładowo w pliku mogą być zapisane następujące produkty:
//
//Spożywcze
//Chleb
//Masło
//Mleko
//Żółty ser
//Chemia
//Mydło
//Płyn do mycia naczyń
//Motoryzacja
//Odświeżacz powietrza
//Płyn do spryskiwaczy
//Zaproponuj odpowiedni format danych, tak aby był on prosty i wygodny dla użytkownika. Plik z listą produktów jest przygotowywany przez użytkownika przy pomocy edytora tekstowego. Nie jest on modyfikowany przez program.
//
//Podczas uruchamiania program wczytuje listę produktów. Program ma umożliwiać:
//
//1. dodanie produktu do listy zakupów zgodnie z poniższym scenariuszem:
//    - program wyświetla dostępne kategorie
//    - użytkownik wybiera kategorię
//    - program wyświetla dostępne produkty z danej kategorii
//    - użytkownik wybiera produkt z danej kategorii
//2. wyświetlenie wszystkich produktów z listy zakupów
//3. wyświetlenie wszystkich produktów z listy zakupów z danej kategorii (użytkownik wybiera kategorię)
//4. usunięcie wszystkich produktów z listy zakupów
//5. usunięcie wszystkich produktów z listy zakupów z danej kategorii (użytkownik wybiera kategorię)
//6. usunięcie produktu z listy zakupów (użytkownik wybiera kategorię, następnie produkt)
//7. zapis listy zakupów na dysku
//
//Przy kolejnym uruchomieniu program wczytuje do edycji ostatnio zapisaną listę zakupów (program nie umożliwia jednoczesnego zapisania kilku odrębnych list zakupów).
//
//W zadaniu można dla uproszczenia pominąć kategorie produktów. Tak uproszczone zadanie oceniane jest na ocenę 3.

//U:\MM\ZADANIE4\ZADANIE4\current_list.txt
//Spożywcze
//Chleb
//Masło
//Mleko
//Żółty ser
//
//Chemia
//Mydło
//Płyn do mycia naczyń
//
//Motoryzacja
//Odświeżacz powietrza
//Płyn do spryskiwaczy

import java.io.*;
import java.util.*;

public class Main {
    private static final String PRODUCTS_FILE = "current_list.txt";
    private static final String SHOPPING_LIST_FILE = "shopping_list.txt";

    // Mapa przechowująca kategorie i ich produkty
    private static Map<String, List<String>> availableProducts = new HashMap<>();

    // Mapa przechowująca listę zakupów
    private static Map<String, List<String>> shoppingList = new HashMap<>();

    public static void main(String[] args) {
        Scanner scanner = null;
        try {
            loadAvailableProducts(); // Wczytanie produktów
            loadShoppingList(); // Wczytanie listy zakupów

            scanner = new Scanner(System.in);
            int choice = -1;

            while (choice != 0) {
                displayMenu();
                try {
                    System.out.print("Wybierz opcję: ");
                    choice = Integer.parseInt(scanner.nextLine().trim());

                    switch (choice) {
                        case 0:
                            // Wyjście z programu
                            System.out.println("Koniec programu.");
                            break;
                        case 1:
                            // Dodaj produkt do listy zakupów
                            addProductToShoppingList(scanner);
                            break;
                        case 2:
                            // Wyświetl wszystkie produkty z listy zakupów
                            displayAllShoppingListProducts();
                            break;
                        case 3:
                            // Wyświetl produkty z listy zakupów z danej kategorii
                            displayShoppingListProductsByCategory(scanner);
                            break;
                        case 4:
                            // Usuń wszystkie produkty z listy zakupów
                            removeAllProductsFromShoppingList();
                            break;
                        case 5:
                            // Usuń wszystkie produkty z listy zakupów z danej kategorii
                            removeProductsByCategoryFromShoppingList(scanner);
                            break;
                        case 6:
                            // Usuń produkt z listy zakupów
                            removeProductFromShoppingList(scanner);
                            break;
                        case 7:
                            // Zapisz listę zakupów na dysku
                            saveShoppingList();
                            break;
                        default:
                            System.out.println("Nieprawidłowa opcja. Wybierz ponownie.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Błąd: Wprowadź liczbę.");
                } catch (Exception e) {
                    System.out.println("Wystąpił błąd: " + e.getMessage());
                }

                if (choice != 0) {
                    System.out.println("\nNaciśnij Enter, aby kontynuować...");
                    scanner.nextLine();
                }
            }
        } catch (Exception e) {
            System.out.println("Krytyczny błąd programu: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    // Wyświetlenie menu
    private static void displayMenu() {
        System.out.println("\n===== LISTA ZAKUPÓW =====");
        System.out.println("1. Dodaj produkt do listy zakupów");
        System.out.println("2. Wyświetl wszystkie produkty z listy zakupów");
        System.out.println("3. Wyświetl produkty z listy zakupów z danej kategorii");
        System.out.println("4. Usuń wszystkie produkty z listy zakupów");
        System.out.println("5. Usuń wszystkie produkty z listy zakupów z danej kategorii");
        System.out.println("6. Usuń produkt z listy zakupów");
        System.out.println("7. Zapisz listę zakupów na dysku");
        System.out.println("0. Wyjście");
    }

    // Wczytanie dostępnych produktów z pliku
    private static void loadAvailableProducts() throws IOException {
        File file = new File(PRODUCTS_FILE);
        if (!file.exists()) {
            throw new FileNotFoundException("Nie znaleziono pliku z produktami: " + PRODUCTS_FILE);
        }

        // NP.:
        // Klucz "Spożywcze" będzie miał listę ["Chleb", "Masło"]
        // Klucz "Chemia" będzie miał listę ["Mydło"]
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String currentCategory = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) {
                    currentCategory = null;
                    continue;
                }

                if (currentCategory == null) {
                    // Nowa kategoria
                    currentCategory = line;
                    availableProducts.put(currentCategory, new ArrayList<>());
                } else {
                    // Produkt w bieżącej kategorii
                    availableProducts.get(currentCategory).add(line);
                }
            }
        }

        System.out.println("Wczytano " + availableProducts.size() + " kategorii produktów.");
    }

    // Wczytanie listy zakupów z pliku
    private static void loadShoppingList() {
        File file = new File(SHOPPING_LIST_FILE);
        if (!file.exists()) {
            System.out.println("Nie znaleziono pliku listy zakupów. Tworzenie nowej listy.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String currentCategory = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) {
                    currentCategory = null;
                    continue;
                }

                if (currentCategory == null) {
                    // Nowa kategoria
                    currentCategory = line;
                    shoppingList.put(currentCategory, new ArrayList<>());
                } else {
                    // Produkt w bieżącej kategorii
                    shoppingList.get(currentCategory).add(line);
                }
            }

            System.out.println("Wczytano zapisaną listę zakupów.");
        } catch (IOException e) {
            System.out.println("Błąd podczas wczytywania listy zakupów: " + e.getMessage());
            System.out.println("Tworzenie nowej listy zakupów.");
        }
    }

    // Dodanie produktu do listy zakupów
    private static void addProductToShoppingList(Scanner scanner) {
        if (availableProducts.isEmpty()) {
            System.out.println("Brak dostępnych produktów.");
            return;
        }

        // Wyświetl dostępne kategorie
        System.out.println("\nDostępne kategorie:");
        List<String> categories = new ArrayList<>(availableProducts.keySet());
        for (int i = 0; i < categories.size(); i++) {
            System.out.println((i + 1) + ". " + categories.get(i));
        }

        // Wybór kategorii
        int categoryIndex = -1;
        while (categoryIndex < 0 || categoryIndex >= categories.size()) {
            try {
                System.out.print("Wybierz kategorię (1-" + categories.size() + "): ");
                categoryIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;

                if (categoryIndex < 0 || categoryIndex >= categories.size()) {
                    System.out.println("Nieprawidłowy numer kategorii.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Wprowadź poprawny numer.");
            }
        }

        String selectedCategory = categories.get(categoryIndex);
        List<String> products = availableProducts.get(selectedCategory);

        // Wyświetl dostępne produkty
        System.out.println("\nDostępne produkty w kategorii " + selectedCategory + ":");
        for (int i = 0; i < products.size(); i++) {
            System.out.println((i + 1) + ". " + products.get(i));
        }

        // Wybór produktu
        int productIndex = -1;
        while (productIndex < 0 || productIndex >= products.size()) {
            try {
                System.out.print("Wybierz produkt (1-" + products.size() + "): ");
                productIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;

                if (productIndex < 0 || productIndex >= products.size()) {
                    System.out.println("Nieprawidłowy numer produktu.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Wprowadź poprawny numer.");
            }
        }

        String selectedProduct = products.get(productIndex);

        // Dodaj produkt do listy zakupów
        if (!shoppingList.containsKey(selectedCategory)) {
            shoppingList.put(selectedCategory, new ArrayList<>());
        }

        List<String> shoppingListProducts = shoppingList.get(selectedCategory);
        if (shoppingListProducts.contains(selectedProduct)) {
            System.out.println("Produkt " + selectedProduct + " już znajduje się na liście zakupów.");
        } else {
            shoppingListProducts.add(selectedProduct);
            System.out.println("Dodano " + selectedProduct + " do listy zakupów.");
        }
    }

    // Wyświetlenie wszystkich produktów z listy zakupów
    private static void displayAllShoppingListProducts() {
        if (shoppingList.isEmpty()) {
            System.out.println("Lista zakupów jest pusta.");
            return;
        }

        System.out.println("\n===== TWOJA LISTA ZAKUPÓW =====");
        for (Map.Entry<String, List<String>> entry : shoppingList.entrySet()) {
            String category = entry.getKey();
            List<String> products = entry.getValue();

            if (!products.isEmpty()) {
                System.out.println("\n" + category + ":");
                for (String product : products) {
                    System.out.println("- " + product);
                }
            }
        }
    }

    // Wyświetlenie produktów z listy zakupów z danej kategorii
    private static void displayShoppingListProductsByCategory(Scanner scanner) {
        if (shoppingList.isEmpty()) {
            System.out.println("Lista zakupów jest pusta.");
            return;
        }

        // Wyświetl dostępne kategorie z listy zakupów
        System.out.println("\nKategorie na liście zakupów:");
        List<String> categories = new ArrayList<>(shoppingList.keySet());
        for (int i = 0; i < categories.size(); i++) {
            System.out.println((i + 1) + ". " + categories.get(i));
        }

        // Wybór kategorii
        int categoryIndex = -1;
        while (categoryIndex < 0 || categoryIndex >= categories.size()) {
            try {
                System.out.print("Wybierz kategorię (1-" + categories.size() + "): ");
                categoryIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;

                if (categoryIndex < 0 || categoryIndex >= categories.size()) {
                    System.out.println("Nieprawidłowy numer kategorii.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Wprowadź poprawny numer.");
            }
        }

        String selectedCategory = categories.get(categoryIndex);
        List<String> products = shoppingList.get(selectedCategory);

        if (products.isEmpty()) {
            System.out.println("Brak produktów w kategorii " + selectedCategory + " na liście zakupów.");
        } else {
            System.out.println("\nProdukty z kategorii " + selectedCategory + " na liście zakupów:");
            for (String product : products) {
                System.out.println("- " + product);
            }
        }
    }

    // Usunięcie wszystkich produktów z listy zakupów
    private static void removeAllProductsFromShoppingList() {
        if (shoppingList.isEmpty()) {
            System.out.println("Lista zakupów jest już pusta.");
        } else {
            shoppingList.clear();
            System.out.println("Usunięto wszystkie produkty z listy zakupów.");
        }
    }

    // Usunięcie wszystkich produktów z listy zakupów z danej kategorii
    private static void removeProductsByCategoryFromShoppingList(Scanner scanner) {
        if (shoppingList.isEmpty()) {
            System.out.println("Lista zakupów jest pusta.");
            return;
        }

        // Wyświetl dostępne kategorie z listy zakupów
        System.out.println("\nKategorie na liście zakupów:");
        List<String> categories = new ArrayList<>(shoppingList.keySet());
        for (int i = 0; i < categories.size(); i++) {
            System.out.println((i + 1) + ". " + categories.get(i));
        }

        // Wybór kategorii
        int categoryIndex = -1;
        while (categoryIndex < 0 || categoryIndex >= categories.size()) {
            try {
                System.out.print("Wybierz kategorię (1-" + categories.size() + "): ");
                categoryIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;

                if (categoryIndex < 0 || categoryIndex >= categories.size()) {
                    System.out.println("Nieprawidłowy numer kategorii.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Wprowadź poprawny numer.");
            }
        }

        String selectedCategory = categories.get(categoryIndex);
        shoppingList.remove(selectedCategory);
        System.out.println("Usunięto wszystkie produkty z kategorii " + selectedCategory + " z listy zakupów.");
    }

    // Usunięcie produktu z listy zakupów
    private static void removeProductFromShoppingList(Scanner scanner) {
        if (shoppingList.isEmpty()) {
            System.out.println("Lista zakupów jest pusta.");
            return;
        }

        // Wyświetl dostępne kategorie z listy zakupów
        System.out.println("\nKategorie na liście zakupów:");
        List<String> categories = new ArrayList<>(shoppingList.keySet());
        for (int i = 0; i < categories.size(); i++) {
            System.out.println((i + 1) + ". " + categories.get(i));
        }

        // Wybór kategorii
        int categoryIndex = -1;
        while (categoryIndex < 0 || categoryIndex >= categories.size()) {
            try {
                System.out.print("Wybierz kategorię (1-" + categories.size() + "): ");
                categoryIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;

                if (categoryIndex < 0 || categoryIndex >= categories.size()) {
                    System.out.println("Nieprawidłowy numer kategorii.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Wprowadź poprawny numer.");
            }
        }

        String selectedCategory = categories.get(categoryIndex);
        List<String> products = shoppingList.get(selectedCategory);

        if (products.isEmpty()) {
            System.out.println("Brak produktów w kategorii " + selectedCategory + " na liście zakupów.");
            return;
        }

        // Wyświetl produkty z wybranej kategorii
        System.out.println("\nProdukty z kategorii " + selectedCategory + " na liście zakupów:");
        for (int i = 0; i < products.size(); i++) {
            System.out.println((i + 1) + ". " + products.get(i));
        }

        // Wybór produktu do usunięcia
        int productIndex = -1;
        while (productIndex < 0 || productIndex >= products.size()) {
            try {
                System.out.print("Wybierz produkt do usunięcia (1-" + products.size() + "): ");
                productIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;

                if (productIndex < 0 || productIndex >= products.size()) {
                    System.out.println("Nieprawidłowy numer produktu.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Wprowadź poprawny numer.");
            }
        }

        String removedProduct = products.remove(productIndex);
        System.out.println("Usunięto " + removedProduct + " z listy zakupów.");

        // Jeśli kategoria jest pusta, usuń ją
        if (products.isEmpty()) {
            shoppingList.remove(selectedCategory);
        }
    }

    // Zapis listy zakupów na dysku
    private static void saveShoppingList() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SHOPPING_LIST_FILE))) {
            for (Map.Entry<String, List<String>> entry : shoppingList.entrySet()) {
                String category = entry.getKey();
                List<String> products = entry.getValue();

                if (!products.isEmpty()) {
                    writer.println(category);
                    for (String product : products) {
                        writer.println(product);
                    }
                    writer.println(); // Pusta linia między kategoriami
                }
            }
            System.out.println("Lista zakupów została zapisana do pliku " + SHOPPING_LIST_FILE);
        } catch (IOException e) {
            System.out.println("Błąd podczas zapisywania listy zakupów: " + e.getMessage());
        }
    }
}
