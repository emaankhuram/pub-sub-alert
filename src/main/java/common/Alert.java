package common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Alert {
    public final AlertType type;
    public final String message;
    public final String timestamp;
    public final String publisher;

    public Alert(AlertType type, String message, String publisher) {
        this.type = type;
        this.message = message;
        this.publisher = publisher;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] [" + type + "] " + publisher + ": " + message;
    }
}
