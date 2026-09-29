package frontEnd.account;

import backEnd.account.Account;
import backEnd.account.AccountsManager;
import backEnd.sanitary.SanitaryService;
import backEnd.tile.TileService;
import backEnd.warehouse.WarehouseManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class AccountsPanel extends JPanel {

    private final AccountsManager manager;
    private final TileService tileService;
    private final SanitaryService sanitaryService;
    private final WarehouseManager warehouseManager;

    private final AccountsTableModel accountsModel;
    private final JTable accountsTable;

    private final JPanel mainPanel =
            new JPanel(new BorderLayout());

    private final Map<Account, AccountTabPanel> openPanels =
            new HashMap<>();

    public AccountsPanel(
            AccountsManager manager,
            TileService tileService,
            SanitaryService sanitaryService,
            WarehouseManager warehouseManager
    ) {

        this.manager = manager;
        this.tileService = tileService;
        this.sanitaryService = sanitaryService;
        this.warehouseManager = warehouseManager;

        setLayout(new BorderLayout());
        setBackground(new Color(247, 248, 250));

        setComponentOrientation(
                ComponentOrientation.RIGHT_TO_LEFT
        );

        accountsModel =
                new AccountsTableModel(
                        manager.getAccounts()
                );

        accountsTable =
                new JTable(accountsModel);

        styleAccountsTable();

        accountsTable
                .getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        showSelectedAccount();
                    }
                });

        JSplitPane splitPane =
                new JSplitPane(
                        JSplitPane.HORIZONTAL_SPLIT,
                        buildMainArea(),
                        buildAccountsSidebar()
                );

        /*
         * القائمة في اليمين.
         * الواجهة الرئيسية في اليسار.
         */
        splitPane.setComponentOrientation(
                ComponentOrientation.LEFT_TO_RIGHT
        );

        /*
         * عرض قائمة الحسابات في البداية.
         * ويمكن للمستخدم سحب الفاصل وتغييره.
         */
        splitPane.setDividerLocation(1000);

        splitPane.setResizeWeight(1.0);

        splitPane.setOneTouchExpandable(true);

        splitPane.setContinuousLayout(true);

        splitPane.setBorder(null);

        add(
                splitPane,
                BorderLayout.CENTER
        );

        /*
         * اختيار أول حساب تلقائيًا.
         */
        if (!manager.getAccounts().isEmpty()) {

            accountsTable.setRowSelectionInterval(
                    0,
                    0
            );

        } else {

            showEmptyState();
        }
    }

    private JPanel buildAccountsSidebar() {

        JPanel sidebar =
                new JPanel(
                        new BorderLayout(
                                8,
                                8
                        )
                );

        sidebar.setBackground(Color.WHITE);

        sidebar.setBorder(
                new EmptyBorder(
                        12,
                        10,
                        12,
                        10
                )
        );

        sidebar.setComponentOrientation(
                ComponentOrientation.RIGHT_TO_LEFT
        );

        JLabel title =
                new JLabel("الحسابات");

        title.setFont(
                new Font(
                        "Tahoma",
                        Font.BOLD,
                        20
                )
        );

        title.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        title.setBorder(
                new EmptyBorder(
                        3,
                        3,
                        8,
                        3
                )
        );

        JScrollPane scrollPane =
                new JScrollPane(
                        accountsTable
                );

        scrollPane.setBorder(
                BorderFactory.createLineBorder(
                        new Color(
                                210,
                                210,
                                210
                        )
                )
        );

        JButton addButton =
                new JButton(
                        "إضافة حساب"
                );

        JButton removeButton =
                new JButton(
                        "إزالة حساب"
                );

        styleSideButton(addButton);
        styleSideButton(removeButton);

        addButton.addActionListener(
                e -> addAccount()
        );

        removeButton.addActionListener(
                e -> removeSelectedAccount()
        );

        /*
         * الزران تحت بعضهما.
         */
        JPanel buttons =
                new JPanel(
                        new GridLayout(
                                2,
                                1,
                                0,
                                8
                        )
                );

        buttons.setBackground(Color.WHITE);

        buttons.add(addButton);
        buttons.add(removeButton);

        sidebar.add(
                title,
                BorderLayout.NORTH
        );

        sidebar.add(
                scrollPane,
                BorderLayout.CENTER
        );

        sidebar.add(
                buttons,
                BorderLayout.SOUTH
        );

        return sidebar;
    }

    private JPanel buildMainArea() {

        mainPanel.setBackground(
                new Color(
                        247,
                        248,
                        250
                )
        );

        mainPanel.setComponentOrientation(
                ComponentOrientation.RIGHT_TO_LEFT
        );

        return mainPanel;
    }

    private void showSelectedAccount() {

        int row =
                accountsTable.getSelectedRow();

        if (
                row < 0
                        || row >= manager
                        .getAccounts()
                        .size()
        ) {

            showEmptyState();
            return;
        }

        Account account =
                manager.getAccounts()
                        .get(row);

        AccountTabPanel panel =
                openPanels.computeIfAbsent(
                        account,
                        a -> new AccountTabPanel(
                                a,
                                manager,
                                tileService,
                                sanitaryService,
                                warehouseManager
                        )
                );

        mainPanel.removeAll();

        mainPanel.add(
                panel,
                BorderLayout.CENTER
        );

        mainPanel.revalidate();
        mainPanel.repaint();

        panel.showOverview();
    }

    private void showEmptyState() {

        mainPanel.removeAll();

        JLabel label =
                new JLabel(
                        "اختر حسابًا من القائمة"
                );

        label.setFont(
                new Font(
                        "Tahoma",
                        Font.BOLD,
                        22
                )
        );

        label.setForeground(
                new Color(
                        110,
                        110,
                        110
                )
        );

        label.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        mainPanel.add(
                label,
                BorderLayout.CENTER
        );

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void addAccount() {

        JTextField nameField =
                new JTextField();

        JPanel panel =
                new JPanel(
                        new GridBagLayout()
                );

        panel.setBorder(
                new EmptyBorder(
                        10,
                        10,
                        5,
                        10
                )
        );

        GridBagConstraints g =
                new GridBagConstraints();

        g.insets =
                new Insets(
                        7,
                        7,
                        7,
                        7
                );

        g.fill =
                GridBagConstraints.HORIZONTAL;

        g.gridx = 0;
        g.gridy = 0;
        g.weightx = .25;

        panel.add(
                new JLabel(
                        "اسم الحساب:"
                ),
                g
        );

        g.gridx = 1;
        g.weightx = .75;

        panel.add(
                nameField,
                g
        );

        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        panel,
                        "إضافة حساب",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

        if (
                result
                        != JOptionPane.OK_OPTION
        ) {
            return;
        }

        String name =
                nameField
                        .getText()
                        .trim();

        if (name.isEmpty()) {
            return;
        }

        try {

            Account account =
                    manager.addAccount(name);

            manager.saveQuietly();

            accountsModel.fireTableDataChanged();

            int row =
                    manager.getAccounts()
                            .indexOf(account);

            if (row >= 0) {

                accountsTable.setRowSelectionInterval(
                        row,
                        row
                );

                accountsTable.scrollRectToVisible(
                        accountsTable.getCellRect(
                                row,
                                0,
                                true
                        )
                );
            }

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "تنبيه",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void removeSelectedAccount() {

        int row =
                accountsTable.getSelectedRow();

        if (
                row < 0
                        || row >= manager
                        .getAccounts()
                        .size()
        ) {
            return;
        }

        Account account =
                manager.getAccounts()
                        .get(row);

        int answer =
                JOptionPane.showConfirmDialog(
                        this,
                        "هل تريد إزالة الحساب \""
                                + account.getName()
                                + "\"؟\n"
                                + "سيتم حذف الحساب من القائمة مع جميع سنداته.",
                        "تأكيد إزالة الحساب",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (
                answer
                        != JOptionPane.YES_OPTION
        ) {
            return;
        }

        manager.removeAccount(account);

        manager.saveQuietly();

        openPanels.remove(account);

        accountsModel.fireTableDataChanged();

        if (manager.getAccounts().isEmpty()) {

            showEmptyState();

            return;
        }

        int newRow =
                Math.min(
                        row,
                        manager.getAccounts()
                                .size() - 1
                );

        accountsTable.setRowSelectionInterval(
                newRow,
                newRow
        );
    }

    private void styleAccountsTable() {

        accountsTable.setFont(
                new Font(
                        "Tahoma",
                        Font.PLAIN,
                        18
                )
        );

        accountsTable.setRowHeight(38);

        accountsTable.setAutoCreateRowSorter(true);

        accountsTable.setComponentOrientation(
                ComponentOrientation.RIGHT_TO_LEFT
        );

        accountsTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        accountsTable.setShowVerticalLines(false);

        accountsTable.setGridColor(
                new Color(
                        225,
                        225,
                        225
                )
        );

        accountsTable.setFillsViewportHeight(true);

        accountsTable.getTableHeader()
                .setFont(
                        new Font(
                                "Tahoma",
                                Font.BOLD,
                                17
                        )
                );

        accountsTable.getTableHeader()
                .setPreferredSize(
                        new Dimension(
                                0,
                                38
                        )
                );
    }

    private void styleSideButton(
            JButton button
    ) {

        button.setFont(
                new Font(
                        "Tahoma",
                        Font.BOLD,
                        17
                )
        );

        button.setPreferredSize(
                new Dimension(
                        0,
                        44
                )
        );

        button.setFocusPainted(false);

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );
    }
}