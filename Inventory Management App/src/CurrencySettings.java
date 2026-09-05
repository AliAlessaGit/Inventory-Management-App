import java.io.Serializable;

/**
 * إعدادات العملة الخاصة بالتطبيق.
 *
 * السعر المخزن هو عدد الليرات السورية مقابل 1 دولار أمريكي.
 * مثال: إذا كان السعر 15000 فهذا يعني:
 * 1 USD = 15000 SYP
 */
public class CurrencySettings implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final double DEFAULT_DOLLAR_TO_SYRIAN_RATE = 1.0;

    private double dollarToSyrianRate;

    public CurrencySettings() {
        this.dollarToSyrianRate = DEFAULT_DOLLAR_TO_SYRIAN_RATE;
    }

    public CurrencySettings(double dollarToSyrianRate) {
        setDollarToSyrianRate(dollarToSyrianRate);
    }

    public double getDollarToSyrianRate() {
        return dollarToSyrianRate;
    }

    public void setDollarToSyrianRate(double dollarToSyrianRate) {

        if (!Double.isFinite(dollarToSyrianRate)
                || dollarToSyrianRate <= 0) {

            throw new IllegalArgumentException(
                    "سعر الدولار يجب أن يكون رقمًا أكبر من الصفر."
            );
        }

        this.dollarToSyrianRate = dollarToSyrianRate;
    }

    public double toSyrian(double usdAmount) {
        return usdAmount * dollarToSyrianRate;
    }

    public double toUsd(double syrianAmount) {
        return syrianAmount / dollarToSyrianRate;
    }
}