public class Main {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Błąd: Należy podać dokładnie 3 współczynniki a, b, c.");
            return;
        }

        double a = Double.parseDouble(args[0]);
        double b = Double.parseDouble(args[1]);
        double c = Double.parseDouble(args[2]);

        if (a == 0.0) {
            System.out.println("Parametr a == 0");
            return;
        }

        double delta = b * b - 4.0 * a * c;

        if (delta < 0.0) {
            double realPart = -b / (2.0 * a);
            double imaginaryPart = Math.sqrt(-delta) / (2.0 * a);
            System.out.printf("Istnieją dwa zespolone rozwiązania: x1 = %f + %fi oraz x2 = %f - %fi%n", realPart,
                    imaginaryPart, realPart, imaginaryPart);
        } else {
            double sqrtDelta = Math.sqrt(delta);
            if (delta == 0.0) {
                System.out.printf("Jedno podwójne rozwiązanie x = %f%n", -b / (2.0 * a));
            } else {
                double x1 = (-b + sqrtDelta) / (2.0 * a);
                double x2 = (-b - sqrtDelta) / (2.0 * a);
                System.out.printf("Istnieją dwa rozwiązania x1 = %f oraz x2 = %f%n", x1, x2);
            }
        }
    }
}
