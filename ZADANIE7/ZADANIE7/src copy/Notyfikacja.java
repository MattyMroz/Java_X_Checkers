import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Klasa reprezentująca notyfikację
public class Notyfikacja implements Serializable {
    private static final long serialVersionUID = 1L;
    private String tresc;
    private LocalDateTime czasWyslania;
    private String klientId;

    public Notyfikacja(String tresc, LocalDateTime czasWyslania, String klientId) {
        this.tresc = tresc;
        this.czasWyslania = czasWyslania;
        this.klientId = klientId;
    }

    public String getTresc() {
        return tresc;
    }

    public LocalDateTime getCzasWyslania() {
        return czasWyslania;
    }

    public String getKlientId() {
        return klientId;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return "Notyfikacja [tresc=" + tresc + ", czasWyslania=" + czasWyslania.format(formatter) + "]";
    }
}