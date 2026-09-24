package frontEnd.invoice;

import backEnd.invoice.Invoice;
import backEnd.invoice.InvoiceItem;
import backEnd.invoice.InvoiceService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;

public class InvoiceExternalTileDialog {

    private final Window owner;
    private final Invoice invoice;
    private final InvoiceService invoiceService;

    private final Runnable reloadItemTables;
    private final Runnable refreshTotals;

    public InvoiceExternalTileDialog(
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
                        "إضافة بلاط خارجي",
                        Dialog.ModalityType.APPLICATION_MODAL
                );

        dlg.setLayout(
                new BorderLayout(
                        10,
                        10
                )
        );

        dlg.setResizable(false);

        dlg.getRootPane()
                .registerKeyboardAction(
                        e -> dlg.dispose(),
                        KeyStroke.getKeyStroke(
                                KeyEvent.VK_ESCAPE,
                                0
                        ),
                        JComponent.WHEN_IN_FOCUSED_WINDOW
                );

        // =====================================================
        // الحقول
        // =====================================================

        JTextField codeField =
                new JTextField(
                        "00",
                        20
                );

        JTextField nameField =
                new JTextField(20);

        JTextField gradeField =
                new JTextField(20);

        JTextField boxAreaField =
                new JTextField(20);

        JTextField quantityField =
                new JTextField(20);

        JTextField priceField =
                new JTextField(20);

        // =====================================================
        // النموذج
        // =====================================================

        JPanel formPanel =
                new JPanel(
                        new GridLayout(
                                6,
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

        // =====================================================
        // الليبلات
        // =====================================================

        JLabel codeLabel =
                new JLabel(
                        "الرمز:",
                        SwingConstants.CENTER
                );

        JLabel nameLabel =
                new JLabel(
                        "الاسم:",
                        SwingConstants.CENTER
                );

        JLabel gradeLabel =
                new JLabel(
                        "النخب:",
                        SwingConstants.CENTER
                );

        JLabel boxAreaLabel =
                new JLabel(
                        "مساحة الصندوق:",
                        SwingConstants.CENTER
                );

        JLabel quantityLabel =
                new JLabel(
                        "عدد الصناديق:",
                        SwingConstants.CENTER
                );

        JLabel priceLabel =
                new JLabel(
                        "السعر:",
                        SwingConstants.CENTER
                );

        // =====================================================
        // جعل حقول الكتابة في المنتصف
        // =====================================================

        codeField.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        nameField.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        gradeField.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        boxAreaField.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        quantityField.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        priceField.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        // =====================================================
        // إضافة الحقول
        //
        // الرمز
        // الاسم
        // النخب
        // مساحة الصندوق
        // عدد الصناديق
        // السعر
        // =====================================================

        formPanel.add(codeLabel);
        formPanel.add(codeField);

        formPanel.add(nameLabel);
        formPanel.add(nameField);

        formPanel.add(gradeLabel);
        formPanel.add(gradeField);

        formPanel.add(boxAreaLabel);
        formPanel.add(boxAreaField);

        formPanel.add(quantityLabel);
        formPanel.add(quantityField);

        formPanel.add(priceLabel);
        formPanel.add(priceField);

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
        // زر إلغاء
        // =====================================================

        cancelBtn.addActionListener(
                e ->
                        dlg.dispose()
        );

        // =====================================================
        // ترتيب الحقول
        // =====================================================

        JTextField[] fields = {
                codeField,
                nameField,
                gradeField,
                boxAreaField,
                quantityField,
                priceField
        };

        // =====================================================
        // Enter + الأسهم ↑ ↓
        // =====================================================

        for (int i = 0; i < fields.length; i++) {

            final int index = i;

            JTextField currentField =
                    fields[i];

            // -------------------------------------------------
            // Enter
            // -------------------------------------------------

            if (index < fields.length - 1) {

                currentField.addActionListener(
                        e ->
                                fields[index + 1]
                                        .requestFocusInWindow()
                );

            } else {

                // Enter في السعر = الانتقال إلى زر الإضافة
                currentField.addActionListener(
                        e ->
                                addBtn.requestFocusInWindow()
                );
            }

            // -------------------------------------------------
            // السهم للأسفل
            // -------------------------------------------------

            currentField.getInputMap(
                    JComponent.WHEN_FOCUSED
            ).put(
                    KeyStroke.getKeyStroke(
                            KeyEvent.VK_DOWN,
                            0
                    ),
                    "moveDown"
            );

            currentField.getActionMap()
                    .put(
                            "moveDown",
                            new AbstractAction() {

                                @Override
                                public void actionPerformed(
                                        java.awt.event.ActionEvent e) {

                                    if (index < fields.length - 1) {

                                        fields[index + 1]
                                                .requestFocusInWindow();

                                    } else {

                                        addBtn.requestFocusInWindow();
                                    }
                                }
                            }
                    );

            // -------------------------------------------------
            // السهم للأعلى
            // -------------------------------------------------

            currentField.getInputMap(
                    JComponent.WHEN_FOCUSED
            ).put(
                    KeyStroke.getKeyStroke(
                            KeyEvent.VK_UP,
                            0
                    ),
                    "moveUp"
            );

            currentField.getActionMap()
                    .put(
                            "moveUp",
                            new AbstractAction() {

                                @Override
                                public void actionPerformed(
                                        java.awt.event.ActionEvent e) {

                                    if (index > 0) {

                                        fields[index - 1]
                                                .requestFocusInWindow();
                                    }
                                }
                            }
                    );
        }

        // =====================================================
        // الأسهم الخاصة بالأزرار
        //
        // إضافة ← إلغاء
        // إلغاء → إضافة
        //
        // ↑ من أي زر → السعر
        // =====================================================

        setupButtonNavigation(
                addBtn,
                cancelBtn,
                priceField
        );

        setupButtonNavigation(
                cancelBtn,
                addBtn,
                priceField
        );

        // =====================================================
        // إضافة البلاط الخارجي
        // =====================================================

        addBtn.addActionListener(
                e -> {

                    // -----------------------------------------
                    // قراءة البيانات
                    // -----------------------------------------

                    String code =
                            codeField
                                    .getText()
                                    .trim();

                    String name =
                            nameField
                                    .getText()
                                    .trim();

                    String grade =
                            gradeField
                                    .getText()
                                    .trim();

                    String boxAreaText =
                            boxAreaField
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

                    String priceText =
                            priceField
                                    .getText()
                                    .trim()
                                    .replace(
                                            ',',
                                            '.'
                                    );

                    // -----------------------------------------
                    // الرمز
                    // -----------------------------------------

                    if (code.isEmpty()) {

                        code = "00";
                    }

                    // -----------------------------------------
                    // الاسم
                    // -----------------------------------------

                    if (name.isEmpty()) {

                        JOptionPane.showMessageDialog(
                                dlg,
                                "اسم البلاط لا يمكن أن يكون فارغًا.",
                                "خطأ",
                                JOptionPane.ERROR_MESSAGE
                        );

                        nameField.requestFocusInWindow();

                        return;
                    }

                    // -----------------------------------------
                    // النخب
                    // -----------------------------------------

                    if (grade.isEmpty()) {

                        JOptionPane.showMessageDialog(
                                dlg,
                                "النخب لا يمكن أن يكون فارغًا.",
                                "خطأ",
                                JOptionPane.ERROR_MESSAGE
                        );

                        gradeField.requestFocusInWindow();

                        return;
                    }

                    // -----------------------------------------
                    // مساحة الصندوق
                    // -----------------------------------------

                    if (boxAreaText.isEmpty()) {

                        JOptionPane.showMessageDialog(
                                dlg,
                                "مساحة الصندوق لا يمكن أن تكون فارغة.",
                                "خطأ",
                                JOptionPane.ERROR_MESSAGE
                        );

                        boxAreaField.requestFocusInWindow();

                        return;
                    }

                    // -----------------------------------------
                    // عدد الصناديق
                    // -----------------------------------------

                    if (quantityText.isEmpty()) {

                        JOptionPane.showMessageDialog(
                                dlg,
                                "عدد الصناديق لا يمكن أن يكون فارغًا.",
                                "خطأ",
                                JOptionPane.ERROR_MESSAGE
                        );

                        quantityField.requestFocusInWindow();

                        return;
                    }

                    // -----------------------------------------
                    // السعر
                    // -----------------------------------------

                    if (priceText.isEmpty()) {

                        JOptionPane.showMessageDialog(
                                dlg,
                                "السعر لا يمكن أن يكون فارغًا.",
                                "خطأ",
                                JOptionPane.ERROR_MESSAGE
                        );

                        priceField.requestFocusInWindow();

                        return;
                    }

                    try {

                        double boxArea =
                                Double.parseDouble(
                                        boxAreaText
                                );

                        double quantity =
                                Double.parseDouble(
                                        quantityText
                                );

                        double price =
                                Double.parseDouble(
                                        priceText
                                );

                        // -------------------------------------
                        // التحقق من مساحة الصندوق
                        // -------------------------------------

                        if (boxArea <= 0) {

                            JOptionPane.showMessageDialog(
                                    dlg,
                                    "مساحة الصندوق يجب أن تكون أكبر من صفر.",
                                    "خطأ",
                                    JOptionPane.ERROR_MESSAGE
                            );

                            boxAreaField.requestFocusInWindow();

                            return;
                        }

                        // -------------------------------------
                        // التحقق من عدد الصناديق
                        // -------------------------------------

                        if (quantity <= 0) {

                            JOptionPane.showMessageDialog(
                                    dlg,
                                    "عدد الصناديق يجب أن يكون أكبر من صفر.",
                                    "خطأ",
                                    JOptionPane.ERROR_MESSAGE
                            );

                            quantityField.requestFocusInWindow();

                            return;
                        }

                        // -------------------------------------
                        // التحقق من السعر
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
                        // إنشاء البلاط الخارجي
                        // -------------------------------------

                        InvoiceItem externalTile =
                                new InvoiceItem(
                                        code,
                                        name,
                                        grade,
                                        boxArea,
                                        quantity,
                                        price,
                                        LocalDateTime.now()
                                );

                        // -------------------------------------
                        // إضافة إلى الفاتورة
                        // -------------------------------------

                        invoice.addItem(
                                externalTile
                        );

                        // -------------------------------------
                        // حفظ الفاتورة
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

                        // -------------------------------------
                        // إغلاق النافذة
                        // -------------------------------------

                        dlg.dispose();

                    } catch (NumberFormatException ex) {

                        JOptionPane.showMessageDialog(
                                dlg,
                                "مساحة الصندوق أو عدد الصناديق أو السعر غير صحيح.",
                                "خطأ",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
        );

        // =====================================================
        // عند فتح النافذة
        // =====================================================

        dlg.addWindowListener(
                new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowOpened(
                            java.awt.event.WindowEvent e) {

                        codeField.requestFocusInWindow();
                        codeField.selectAll();
                    }
                }
        );

        // =====================================================
        // الحجم والموقع
        // =====================================================

        dlg.setMinimumSize(
                new Dimension(
                        500,
                        380
                )
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
    // التنقل بين الأزرار
    //
    // leftButton  = الزر الذي نتحرك منه بالسهم الأيسر
    // rightButton = الزر الذي نتحرك إليه بالسهم الأيسر
    // priceField  = حقل السعر عند الضغط على ↑
    // =========================================================

    private void setupButtonNavigation(
            JButton currentButton,
            JButton otherButton,
            JTextField priceField) {

        InputMap inputMap =
                currentButton.getInputMap(
                        JComponent.WHEN_FOCUSED
                );

        ActionMap actionMap =
                currentButton.getActionMap();

        // =====================================================
        // السهم الأيسر
        // =====================================================

        inputMap.put(
                KeyStroke.getKeyStroke(
                        KeyEvent.VK_LEFT,
                        0
                ),
                "moveLeft"
        );

        actionMap.put(
                "moveLeft",
                new AbstractAction() {

                    @Override
                    public void actionPerformed(
                            java.awt.event.ActionEvent e) {

                        otherButton.requestFocusInWindow();
                    }
                }
        );

        // =====================================================
        // السهم الأيمن
        // =====================================================

        inputMap.put(
                KeyStroke.getKeyStroke(
                        KeyEvent.VK_RIGHT,
                        0
                ),
                "moveRight"
        );

        actionMap.put(
                "moveRight",
                new AbstractAction() {

                    @Override
                    public void actionPerformed(
                            java.awt.event.ActionEvent e) {

                        otherButton.requestFocusInWindow();
                    }
                }
        );

        // =====================================================
        // السهم للأعلى → السعر
        // =====================================================

        inputMap.put(
                KeyStroke.getKeyStroke(
                        KeyEvent.VK_UP,
                        0
                ),
                "moveToPrice"
        );

        actionMap.put(
                "moveToPrice",
                new AbstractAction() {

                    @Override
                    public void actionPerformed(
                            java.awt.event.ActionEvent e) {

                        priceField.requestFocusInWindow();
                    }
                }
        );
    }
}