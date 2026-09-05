/**
 * خدمة حفظ وتحميل إعدادات العملة.
 */
public class CurrencySettingsService {

    private final String dataFile;

    private CurrencySettings settings;

    public CurrencySettingsService(String dataFile) {
        this.dataFile = dataFile;
        load();
    }

    public synchronized CurrencySettings getSettings() {
        return settings;
    }

    public synchronized double getDollarToSyrianRate() {
        return settings.getDollarToSyrianRate();
    }

    public synchronized void setDollarToSyrianRate(double rate) {

        settings.setDollarToSyrianRate(rate);

        save();
    }

    public synchronized void save() {
        JSONUtil.writeObject(dataFile, settings);
    }

    private synchronized void load() {

        CurrencySettings loaded =
                JSONUtil.readObject(dataFile, CurrencySettings.class);

        settings = loaded != null
                ? loaded
                : new CurrencySettings();

        if (!Double.isFinite(settings.getDollarToSyrianRate())
                || settings.getDollarToSyrianRate() <= 0) {

            settings = new CurrencySettings();
        }
    }
}