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

    private final JTabbedPane companyTabs = new JTabbedPane();
    private final Map<Account, AccountTabPanel> openTabs = new HashMap<>();

    public AccountsPanel(AccountsManager manager,
                         TileService tileService,
                         SanitaryService sanitaryService,
                         WarehouseManager warehouseManager) {
        this.manager = manager;
        this.tileService = tileService;
        this.sanitaryService = sanitaryService;
        this.warehouseManager = warehouseManager;

        setLayout(new BorderLayout(0, 8));
        setBackground(new Color(247, 248, 250));
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        add(buildToolbar(), BorderLayout.NORTH);

        companyTabs.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        companyTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        companyTabs.setFont(new Font("Tahoma", Font.BOLD, 15));
        add(companyTabs, BorderLayout.CENTER);

        for (Account account : manager.getAccounts()) openAccount(account);
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel title = new JLabel("حسابات الشركات");
        title.setFont(new Font("Tahoma", Font.BOLD, 23));

        JLabel hint = new JLabel("كل شركة تفتح في تبويب مستقل، والفواتير تفتح داخل تبويب الشركة نفسه");
        hint.setForeground(new Color(95, 95, 95));

        JButton addCompany = new JButton("+ إضافة شركة");
        addCompany.setFont(new Font("Tahoma", Font.BOLD, 15));
        addCompany.addActionListener(e -> addCompany());

        JPanel right = new JPanel(new GridLayout(2, 1));
        right.setBackground(Color.WHITE);
        right.add(title);
        right.add(hint);

        toolbar.add(right, BorderLayout.WEST);
        toolbar.add(addCompany, BorderLayout.EAST);
        return toolbar;
    }

    private void addCompany() {
        JTextField nameField = new JTextField();
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(10, 10, 5, 10));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 7, 7, 7);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;
        g.gridy = 0;
        g.weightx = .25;
        panel.add(new JLabel("اسم الشركة:"), g);
        g.gridx = 1;
        g.weightx = .75;
        panel.add(nameField, g);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "إضافة شركة", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String name = nameField.getText().trim();
        if (name.isEmpty()) return;
        try {
            Account account = manager.addAccount(name);
            manager.saveQuietly();
            openAccount(account);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "تنبيه", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void openAccount(Account account) {
        if (openTabs.containsKey(account)) {
            companyTabs.setSelectedComponent(openTabs.get(account));
            return;
        }

        AccountTabPanel panel = new AccountTabPanel(
                account, manager, tileService, sanitaryService, warehouseManager);
        openTabs.put(account, panel);
        companyTabs.addTab(account.getName(), panel);
        companyTabs.setSelectedComponent(panel);
    }
}
