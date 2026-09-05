import java.io.Serializable;
import java.time.LocalDateTime;

public class Payment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * المبلغ كما أدخله المستخدم.
     */
    private final double amount;

    private final LocalDateTime date;

    /**
     * USD أو SYP.
     */
    private final String currency;

    /**
     * سعر الدولار وقت تسجيل الدفعة.
     *
     * 1 USD = exchangeRate SYP
     */
    private final double exchangeRate;

    /**
     * القيمة المحاسبية للدفعة بالدولار.
     */
    private final double dollarAmount;


    /**
     * Constructor قديم.
     *
     * الدفعة القديمة تعتبر بالدولار.
     */
    public Payment(double amount, LocalDateTime date) {

        this(amount, date, "USD", 1.0, amount);
    }


    /**
     * Constructor جديد.
     */
    public Payment(
            double amount,
            LocalDateTime date,
            String currency,
            double exchangeRate) {

        this(
                amount,
                date,
                normalizeCurrency(currency),
                normalizeRate(currency, exchangeRate),
                calculateDollarAmount(
                        amount,
                        currency,
                        exchangeRate
                )
        );
    }


    private Payment(
            double amount,
            LocalDateTime date,
            String currency,
            double exchangeRate,
            double dollarAmount) {

        if (!Double.isFinite(amount) || amount < 0) {

            throw new IllegalArgumentException(
                    "قيمة الدفعة غير صالحة."
            );
        }

        this.amount = amount;
        this.date = date;
        this.currency = currency;
        this.exchangeRate = exchangeRate;
        this.dollarAmount = dollarAmount;
    }


    private static String normalizeCurrency(String currency) {

        return "SYP".equalsIgnoreCase(currency)
                ? "SYP"
                : "USD";
    }


    private static double normalizeRate(
            String currency,
            double rate) {

        String c = normalizeCurrency(currency);

        if ("USD".equals(c)) {
            return 1.0;
        }

        if (!Double.isFinite(rate) || rate <= 0) {

            throw new IllegalArgumentException(
                    "سعر الدولار غير صالح."
            );
        }

        return rate;
    }


    private static double calculateDollarAmount(
            double amount,
            String currency,
            double rate) {

        String c = normalizeCurrency(currency);

        if ("SYP".equals(c)) {

            return amount / normalizeRate(c, rate);
        }

        return amount;
    }


    public double getAmount() {
        return amount;
    }


    public LocalDateTime getDate() {
        return date;
    }


    public String getCurrency() {

        // الفواتير القديمة لا تحتوي currency
        // لذلك نعتبرها USD.

        return currency == null
                ? "USD"
                : normalizeCurrency(currency);
    }


    public boolean isSyrian() {

        return "SYP".equals(getCurrency());
    }


    public double getExchangeRate() {

        if ("SYP".equals(getCurrency())) {

            return Double.isFinite(exchangeRate)
                    && exchangeRate > 0
                    ? exchangeRate
                    : 1.0;
        }

        return 1.0;
    }


    public double getDollarAmount() {

        /*
         * الفواتير القديمة:
         *
         * لم يكن لديها dollarAmount
         * وكانت الدفعة أصلًا بالدولار.
         */

        if ("USD".equals(getCurrency())) {

            return amount;
        }

        if (Double.isFinite(dollarAmount)
                && dollarAmount > 0) {

            return dollarAmount;
        }

        return amount / getExchangeRate();
    }


    public double getSyrianAmount() {

        if (isSyrian()) {
            return amount;
        }

        return amount * getExchangeRate();
    }
}