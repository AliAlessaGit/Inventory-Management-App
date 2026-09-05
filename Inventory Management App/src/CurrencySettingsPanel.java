import javax.swing.*;
import java.awt.*;

public class CurrencySettingsPanel extends JPanel {

    private final CurrencySettingsService currencySettingsService;

    private JTextField rateField;

    private final Font LABEL_FONT =
            new Font("Tahoma", Font.BOLD, 20);

    private final Font FIELD_FONT =
            new Font("Tahoma", Font.PLAIN, 20);

    private final Font BUTTON_FONT =
            new Font("Tahoma", Font.BOLD, 18);

    private final Color PRIMARY_COLOR =
            new Color(63, 81, 181);

    public CurrencySettingsPanel(
            CurrencySettingsService currencySettingsService) {

        this.currencySettingsService = currencySettingsService;

        setLayout(new GridBagLayout());
        setBorder(
                BorderFactory.createEmptyBorder(
                        30, 30, 30, 30
                )
        );

        initUI();
    }

    private void initUI() {

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets =
                new Insets(10, 10, 10, 10);

        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel =
                new JLabel("إعدادات العملة");

        titleLabel.setFont(
                new Font(
                        "Tahoma",
                        Font.BOLD,
                        26
                )
        );

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        add(titleLabel, gbc);


        JLabel rateLabel =
                new JLabel(
                        "سعر الدولار مقابل الليرة السورية:"
                );

        rateLabel.setFont(LABEL_FONT);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;

        add(rateLabel, gbc);


        rateField = new JTextField();

        rateField.setFont(FIELD_FONT);
        rateField.setPreferredSize(
                new Dimension(250, 45)
        );

        rateField.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        rateField.setText(
                String.valueOf(
                        currencySettingsService
                                .getDollarToSyrianRate()
                )
        );

        gbc.gridx = 1;
        gbc.gridy = 1;

        add(rateField, gbc);


        JButton saveButton =
                new JButton("حفظ سعر الدولار");

        saveButton.setFont(BUTTON_FONT);
        saveButton.setBackground(PRIMARY_COLOR);
        saveButton.setForeground(Color.WHITE);
        saveButton.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        saveButton.addActionListener(
                e -> saveRate()
        );

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;

        add(saveButton, gbc);
    }

    private void saveRate() {

        String text =
                rateField.getText().trim();

        if (text.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "يرجى إدخال سعر الدولار.",
                    "تنبيه",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {

            double rate =
                    Double.parseDouble(text);

            if (!Double.isFinite(rate)
                    || rate <= 0) {

                throw new NumberFormatException();
            }

            currencySettingsService
                    .setDollarToSyrianRate(rate);

            JOptionPane.showMessageDialog(
                    this,
                    "تم حفظ سعر الدولار بنجاح.",
                    "تم الحفظ",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "يرجى إدخال سعر دولار صحيح.",
                    "خطأ",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}