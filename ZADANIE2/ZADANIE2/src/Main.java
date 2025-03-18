public class Main {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Błąd: Należy podać co najmniej 3 argumenty: łańcuch oraz dwie liczby.");
            return;
        }

        String inputString = args[0];
        int startIndex;
        int endIndex;

        try {
            startIndex = Integer.parseInt(args[1]);
            endIndex = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.out.println("Błąd: Wprowadzone liczby muszą być całkowite.");
            return;
        }

        if (startIndex < 0) {
            System.out.println("Błąd: startIndex nie może być mniejszy od 0.");
            return;
        }

        if (endIndex < startIndex) {
            System.out.println("Błąd: endIndex nie może być mniejszy od startIndex.");
            return;
        }

        if (endIndex >= inputString.length()) {
            endIndex = inputString.length() - 1; // Ustawienie endIndex na ostatni indeks łańcucha
        }

        String substring = inputString.substring(startIndex, endIndex + 1); // Zmiana na endIndex + 1
        System.out.println(substring);
    }
}