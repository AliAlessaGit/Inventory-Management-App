import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InvoicePaymentsDialog {

    private final Window owner;
    private final Invoice invoice;

    private final InvoiceService invoiceService;
    private final CurrencySettingsService currencySettingsService;

    private final DateTimeFormatter dtf;

    private final Runnable refreshMainInvoice;

    public InvoicePaymentsDialog(
            Window owner,
            Invoice invoice,
            InvoiceService invoiceService,
            CurrencySettingsService currencySettingsService,
            DateTimeFormatter dtf,
            Runnable refreshMainInvoice) {

        this.owner = owner;
        this.invoice = invoice;
        this.invoiceService = invoiceService;
        this.currencySettingsService =
                currencySettingsService;
        this.dtf = dtf;
        this.refreshMainInvoice =
                refreshMainInvoice;
    }

    public void show() {

        JDialog paymentsDlg =
                new JDialog(
                        owner,
                        "دفعات الفاتورة رقم: "
                                + invoice.getInvoiceNumber(),
                        Dialog.ModalityType.APPLICATION_MODAL
                );

        paymentsDlg.setLayout(
                new BorderLayout(
                        8,
                        8
                )
        );

        paymentsDlg.setResizable(true);

        // =========================================================
        // جدول الدفعات
        // =========================================================

        String[] pCols = {
                "قيمة الدفعة",
                "العملة",
                "القيمة بالدولار",
                "سعر الصرف",
                "تاريخ الدفعة"
        };

        DefaultTableModel pModel =
                new DefaultTableModel(
                        pCols,
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column) {

                        return false;
                    }
                };

        JTable paymentsTable =
                new JTable(
                        pModel
                );

        paymentsTable.setRowHeight(
                28
        );

        paymentsTable.setFillsViewportHeight(
                true
        );

        paymentsTable.setAutoResizeMode(
                JTable.AUTO_RESIZE_ALL_COLUMNS
        );

        paymentsTable.getTableHeader()
                .setReorderingAllowed(
                        false
                );

        paymentsDlg.add(
                new JScrollPane(
                        paymentsTable
                ),
                BorderLayout.CENTER
        );

        // =========================================================
        // تحديث جدول الدفعات
        // =========================================================

        Runnable reloadPaymentsTable =
                () -> {

                    pModel.setRowCount(
                            0
                    );

                    for (
                            Payment p :
                            invoice.getPayments()
                    ) {

                        pModel.addRow(
                                new Object[]{
                                        String.format(
                                                "%,.2f",
                                                p.getAmount()
                                        ),

                                        p.getCurrency(),

                                        String.format(
                                                "%,.2f",
                                                p.getDollarAmount()
                                        ),

                                        String.format(
                                                "%,.2f",
                                                p.getExchangeRate()
                                        ),

                                        p.getDate() != null
                                                ? dtf.format(
                                                p.getDate()
                                        )
                                                : ""
                                }
                        );
                    }
                };

        reloadPaymentsTable.run();

        // =========================================================
        // شريط التحكم
        // =========================================================

        JPanel paymentControls =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER,
                                10,
                                8
                        )
                );

        paymentControls.setComponentOrientation(
                ComponentOrientation.RIGHT_TO_LEFT
        );

        JTextField payAmountField =
                new JTextField();

        payAmountField.setPreferredSize(
                new Dimension(
                        180,
                        40
                )
        );

        JToggleButton currencyToggle =
                new JToggleButton(
                        "USD"
                );

        currencyToggle.setPreferredSize(
                new Dimension(
                        100,
                        40
                )
        );

        currencyToggle.setCursor(
                new Cursor(
                        Cursor.HAND_CURSOR
                )
        );

        JButton addPaymentBtn =
                new JButton(
                        "إضافة دفعة"
                );

        addPaymentBtn.setPreferredSize(
                new Dimension(
                        150,
                        40
                )
        );




        currencyToggle.addActionListener(
                e ->
                        currencyToggle.setText(
                                currencyToggle.isSelected()
                                        ? "SYP"
                                        : "USD"
                        )
        );

        paymentControls.add(
                payAmountField
        );

        paymentControls.add(
                currencyToggle
        );

        paymentControls.add(
                addPaymentBtn
        );


        paymentsDlg.add(
                paymentControls,
                BorderLayout.SOUTH
        );

        // =========================================================
        // إضافة دفعة
        // =========================================================

        Action addPaymentAction =
                new AbstractAction() {

                    @Override
                    public void actionPerformed(
                            ActionEvent e) {

                        String amtStr =
                                payAmountField
                                        .getText()
                                        .trim()
                                        .replace(
                                                ',',
                                                '.'
                                        );

                        if (amtStr.isEmpty()) {

                            JOptionPane.showMessageDialog(
                                    paymentsDlg,
                                    "أدخل قيمة الدفعة أولاً.",
                                    "تنبيه",
                                    JOptionPane.WARNING_MESSAGE
                            );

                            payAmountField.requestFocus();

                            return;
                        }

                        try {

                            double amount =
                                    Double.parseDouble(
                                            amtStr
                                    );

                            if (amount <= 0) {

                                JOptionPane.showMessageDialog(
                                        paymentsDlg,
                                        "قيمة الدفعة يجب أن تكون أكبر من صفر.",
                                        "خطأ",
                                        JOptionPane.ERROR_MESSAGE
                                );

                                payAmountField.requestFocus();

                                return;
                            }

                            LocalDateTime date =
                                    LocalDateTime.now();

                            String currency;

                            double exchangeRate;

                            if (
                                    currencyToggle.isSelected()
                            ) {

                                currency =
                                        "SYP";

                                exchangeRate =
                                        currencySettingsService
                                                .getDollarToSyrianRate();

                            } else {

                                currency =
                                        "USD";

                                exchangeRate =
                                        1.0;
                            }

                            invoice.addPayment(
                                    amount,
                                    date,
                                    currency,
                                    exchangeRate
                            );

                            invoiceService.saveToFile();

                            payAmountField.setText(
                                    ""
                            );

                            reloadPaymentsTable.run();

                            if (
                                    refreshMainInvoice
                                            != null
                            ) {

                                refreshMainInvoice.run();
                            }

                            payAmountField.requestFocus();

                        } catch (
                                NumberFormatException ex
                        ) {

                            JOptionPane.showMessageDialog(
                                    paymentsDlg,
                                    "قيمة الدفعة غير صحيحة.",
                                    "خطأ",
                                    JOptionPane.ERROR_MESSAGE
                            );

                            payAmountField.requestFocus();
                        }
                    }
                };

        addPaymentBtn.addActionListener(
                addPaymentAction
        );

        payAmountField.addActionListener(
                addPaymentAction
        );

        // =========================================================
        // حذف الدفعة
        // =========================================================

        addDeleteShortcut(
                paymentsTable,
                () -> {

                    int selectedRow =
                            paymentsTable
                                    .getSelectedRow();

                    if (selectedRow < 0) {
                        return;
                    }

                    int confirm =
                            JOptionPane.showConfirmDialog(
                                    paymentsDlg,
                                    "هل أنت متأكد من حذف هذه الدفعة؟",
                                    "تأكيد الحذف",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.WARNING_MESSAGE
                            );

                    if (
                            confirm
                                    == JOptionPane.YES_OPTION
                    ) {

                        invoice.getPayments()
                                .remove(
                                        selectedRow
                                );

                        invoiceService.saveToFile();

                        reloadPaymentsTable.run();

                        if (
                                refreshMainInvoice
                                        != null
                        ) {

                            refreshMainInvoice.run();
                        }

                        payAmountField.requestFocus();
                    }
                }
        );

        // =========================================================
        // Escape
        // =========================================================

        paymentsDlg.getRootPane()
                .registerKeyboardAction(
                        e ->
                                paymentsDlg.dispose(),
                        KeyStroke.getKeyStroke(
                                KeyEvent.VK_ESCAPE,
                                0
                        ),
                        JComponent.WHEN_IN_FOCUSED_WINDOW
                );

        // =========================================================
        // الحجم
        // =========================================================

        paymentsDlg.setMinimumSize(
                new Dimension(
                        500,
                        300
                )
        );

        paymentsDlg.setSize(
                700,
                450
        );

        paymentsDlg.setLocationRelativeTo(
                owner
        );

        paymentsDlg.setVisible(
                true
        );
    }

    private void addDeleteShortcut(
            JTable table,
            Runnable action) {

        KeyStroke ks =
                KeyStroke.getKeyStroke(
                        KeyEvent.VK_D,
                        KeyEvent.CTRL_DOWN_MASK
                                |
                                KeyEvent.SHIFT_DOWN_MASK
                );

        table.getInputMap(
                JComponent.WHEN_FOCUSED
        ).put(
                ks,
                "deletePayment"
        );

        table.getActionMap()
                .put(
                        "deletePayment",
                        new AbstractAction() {

                            @Override
                            public void actionPerformed(
                                    ActionEvent e) {

                                action.run();
                            }
                        }
                );
    }
}