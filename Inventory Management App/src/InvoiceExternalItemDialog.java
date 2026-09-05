import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InvoiceExternalItemDialog {

    private final Window owner;
    private final Invoice invoice;
    private final InvoiceService invoiceService;

    private final Runnable reloadItemTables;
    private final Runnable refreshTotals;

    public InvoiceExternalItemDialog(
            Window owner,
            Invoice invoice,
            InvoiceService invoiceService,
            Runnable reloadItemTables,
            Runnable refreshTotals) {

        this.owner = owner;
        this.invoice = invoice;
        this.invoiceService = invoiceService;

        this.reloadItemTables = reloadItemTables;
        this.refreshTotals = refreshTotals;
    }

    public void show() {

        JDialog dlg =
                new JDialog(
                        owner,
                        "إضافة عنصر خارجي",
                        Dialog.ModalityType.APPLICATION_MODAL
                );

        dlg.setLayout(
                new BorderLayout(
                        10,
                        10
                )
        );

        dlg.setResizable(false);

        final DateTimeFormatter dtf =
                DateTimeFormatter.ofPattern(
                        "yyyy/MM/dd HH:mm"
                );

        // =====================================================
        // الحقول
        // =====================================================

        JTextField nameField =
                new JTextField(20);

        JTextField priceField =
                new JTextField(20);

        JTextField quantityField =
                new JTextField(20);

        JTextField dateField =
                new JTextField(
                        dtf.format(
                                LocalDateTime.now()
                        ),
                        20
                );

        // =====================================================
        // اتجاه النموذج RTL
        //
        // في كل صف:
        // اسم الحقل في اليمين
        // حقل الكتابة في اليسار
        // =====================================================

        JPanel formPanel =
                new JPanel(
                        new GridLayout(
                                4,
                                2,
                                8,
                                8
                        )
                );

        formPanel.setComponentOrientation(
                ComponentOrientation.RIGHT_TO_LEFT
        );

        formPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        15,
                        15,
                        10,
                        15
                )
        );

        JLabel nameLabel =
                new JLabel(
                        "اسم العنصر:",
                        SwingConstants.RIGHT
                );

        JLabel priceLabel =
                new JLabel(
                        "السعر:",
                        SwingConstants.RIGHT
                );

        JLabel quantityLabel =
                new JLabel(
                        "العدد:",
                        SwingConstants.RIGHT
                );

        JLabel dateLabel =
                new JLabel(
                        "التاريخ:",
                        SwingConstants.RIGHT
                );

        formPanel.add(nameLabel);
        formPanel.add(nameField);

        formPanel.add(priceLabel);
        formPanel.add(priceField);

        formPanel.add(quantityLabel);
        formPanel.add(quantityField);

        formPanel.add(dateLabel);
        formPanel.add(dateField);

        dlg.add(
                formPanel,
                BorderLayout.CENTER
        );

        // =====================================================
        // الأزرار
        // =====================================================

        JButton addBtn =
                new JButton(
                        "إضافة إلى الفاتورة"
                );

        JButton cancelBtn =
                new JButton(
                        "إلغاء"
                );

        JPanel buttonsPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER,
                                10,
                                8
                        )
                );

        buttonsPanel.setComponentOrientation(
                ComponentOrientation.RIGHT_TO_LEFT
        );

        buttonsPanel.add(addBtn);
        buttonsPanel.add(cancelBtn);

        dlg.add(
                buttonsPanel,
                BorderLayout.SOUTH
        );

        // =====================================================
        // إلغاء
        // =====================================================

        cancelBtn.addActionListener(
                e -> dlg.dispose()
        );

        // =====================================================
        // إضافة العنصر الخارجي
        // =====================================================

        addBtn.addActionListener(
                e -> {

                    String name =
                            nameField
                                    .getText()
                                    .trim();

                    String priceText =
                            priceField
                                    .getText()
                                    .trim()
                                    .replace(
                                            ',',
                                            '.'
                                    );

                    String quantityText =
                            quantityField
                                    .getText()
                                    .trim()
                                    .replace(
                                            ',',
                                            '.'
                                    );

                    String dateText =
                            dateField
                                    .getText()
                                    .trim();

                    // -----------------------------------------
                    // الاسم
                    // -----------------------------------------

                    if (name.isEmpty()) {

                        JOptionPane.showMessageDialog(
                                dlg,
                                "اسم العنصر لا يمكن أن يكون فارغًا.",
                                "خطأ",
                                JOptionPane.ERROR_MESSAGE
                        );

                        nameField.requestFocusInWindow();

                        return;
                    }

                    try {

                        double price =
                                Double.parseDouble(
                                        priceText
                                );

                        double quantity =
                                Double.parseDouble(
                                        quantityText
                                );

                        LocalDateTime date =
                                LocalDateTime.parse(
                                        dateText,
                                        dtf
                                );

                        // -------------------------------------
                        // السعر
                        // -------------------------------------

                        if (price < 0) {

                            JOptionPane.showMessageDialog(
                                    dlg,
                                    "السعر لا يمكن أن يكون سالبًا.",
                                    "خطأ",
                                    JOptionPane.ERROR_MESSAGE
                            );

                            priceField.requestFocusInWindow();

                            return;
                        }

                        // -------------------------------------
                        // الكمية
                        // -------------------------------------

                        if (quantity <= 0) {

                            JOptionPane.showMessageDialog(
                                    dlg,
                                    "العدد يجب أن يكون أكبر من صفر.",
                                    "خطأ",
                                    JOptionPane.ERROR_MESSAGE
                            );

                            quantityField.requestFocusInWindow();

                            return;
                        }

                        // -------------------------------------
                        // إنشاء العنصر الخارجي
                        // -------------------------------------

                        InvoiceItem externalItem =
                                new InvoiceItem(
                                        name,
                                        quantity,
                                        price,
                                        date
                                );

                        invoice.addItem(
                                externalItem
                        );

                        // -------------------------------------
                        // حفظ
                        // -------------------------------------

                        invoiceService.saveToFile();

                        // -------------------------------------
                        // تحديث الجداول
                        // -------------------------------------

                        if (reloadItemTables != null) {
                            reloadItemTables.run();
                        }

                        if (refreshTotals != null) {
                            refreshTotals.run();
                        }

                        dlg.dispose();

                    } catch (NumberFormatException ex) {

                        JOptionPane.showMessageDialog(
                                dlg,
                                "السعر أو العدد غير صحيح.",
                                "خطأ",
                                JOptionPane.ERROR_MESSAGE
                        );

                    } catch (Exception ex) {

                        JOptionPane.showMessageDialog(
                                dlg,
                                "التاريخ غير صحيح.\n"
                                        + "استخدم الصيغة: yyyy/MM/dd HH:mm",
                                "خطأ",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
        );

        // =====================================================
        // Enter للتنقل بين الحقول
        //
        // الاسم -> السعر
        // السعر -> العدد
        // العدد -> التاريخ
        // التاريخ -> إضافة
        // =====================================================

        nameField.addActionListener(
                e ->
                        priceField.requestFocusInWindow()
        );

        priceField.addActionListener(
                e ->
                        quantityField.requestFocusInWindow()
        );

        quantityField.addActionListener(
                e ->
                        dateField.requestFocusInWindow()
        );

        dateField.addActionListener(
                e ->
                        addBtn.doClick()
        );

        // =====================================================
        // الأسهم ↑ ↓ للتنقل بين الحقول
        // =====================================================

        setupArrowNavigation(
                nameField,
                null,
                priceField
        );

        setupArrowNavigation(
                priceField,
                nameField,
                quantityField
        );

        setupArrowNavigation(
                quantityField,
                priceField,
                dateField
        );

        setupArrowNavigation(
                dateField,
                quantityField,
                null
        );

        // =====================================================
        // عند فتح النافذة يكون المؤشر على اسم العنصر
        // =====================================================

        dlg.addWindowListener(
                new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowOpened(
                            java.awt.event.WindowEvent e) {

                        nameField.requestFocusInWindow();
                        nameField.selectAll();
                    }
                }
        );

        // =====================================================
        // Escape
        // =====================================================

        dlg.getRootPane()
                .registerKeyboardAction(
                        e ->
                                dlg.dispose(),

                        KeyStroke.getKeyStroke(
                                KeyEvent.VK_ESCAPE,
                                0
                        ),

                        JComponent.WHEN_IN_FOCUSED_WINDOW
                );

        dlg.pack();

        dlg.setLocationRelativeTo(
                owner
        );

        dlg.setVisible(
                true
        );
    }

    // =========================================================
    // إعداد الأسهم ↑ ↓
    // =========================================================

    private void setupArrowNavigation(
            JTextField currentField,
            JTextField upField,
            JTextField downField) {

        InputMap inputMap =
                currentField.getInputMap(
                        JComponent.WHEN_FOCUSED
                );

        ActionMap actionMap =
                currentField.getActionMap();

        // ---------------------------------------------
        // السهم للأعلى
        // ---------------------------------------------

        if (upField != null) {

            inputMap.put(
                    KeyStroke.getKeyStroke(
                            KeyEvent.VK_UP,
                            0
                    ),
                    "moveUp"
            );

            actionMap.put(
                    "moveUp",
                    new AbstractAction() {

                        @Override
                        public void actionPerformed(
                                java.awt.event.ActionEvent e) {

                            upField.requestFocusInWindow();
                        }
                    }
            );
        }

        // ---------------------------------------------
        // السهم للأسفل
        // ---------------------------------------------

        if (downField != null) {

            inputMap.put(
                    KeyStroke.getKeyStroke(
                            KeyEvent.VK_DOWN,
                            0
                    ),
                    "moveDown"
            );

            actionMap.put(
                    "moveDown",
                    new AbstractAction() {

                        @Override
                        public void actionPerformed(
                                java.awt.event.ActionEvent e) {

                            downField.requestFocusInWindow();
                        }
                    }
            );
        }
    }
}
