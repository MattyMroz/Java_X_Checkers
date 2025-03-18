// Własny wyjątek do walidacji treści notyfikacji
public class NotyfikacjaException extends Exception {
    public NotyfikacjaException(String message) {
        super(message);
    }
}