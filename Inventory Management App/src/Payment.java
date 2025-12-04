import java.io.Serializable;
import java.time.LocalDateTime;

public class Payment implements Serializable {
    private final double amount;
    private final LocalDateTime date;

    public Payment(double amount, LocalDateTime date) {
        this.amount = amount;
        this.date = date;
    }

    public double getAmount() { return amount; }

    public LocalDateTime getDate() { return date; }
}