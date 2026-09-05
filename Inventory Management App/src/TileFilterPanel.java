import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TileFilterPanel extends JPanel {

    private final List<TileItem> allItems;
    private final List<Warehouse> warehouses;
    private final TileFilter filter;

    private Runnable onFilterApplied;
    private Runnable onClearApplied;

    private final JCheckBox[] materialChecks;
    private final JCheckBox[] subtypeChecks;
    private final JCheckBox[] warehouseChecks;
    private final JCheckBox[] locationChecks;
    private final JCheckBox[] gradeChecks;
    private final JCheckBox[] boxAreaChecks;

    private final JButton applyButton;
    private final JButton clearButton;

    private static final Font TITLE_FONT =
            new Font("Tahoma", Font.BOLD, 17);

    private static final Font NORMAL_FONT =
            new Font("Tahoma", Font.PLAIN, 16);

    private static final Font BUTTON_FONT =
            new Font("Tahoma", Font.BOLD, 16);

    private static final Color PRIMARY_COLOR =
            new Color(63, 81, 181);


    public TileFilterPanel(
            List<TileItem> allItems,
            List<Warehouse> warehouses,
            TileFilter filter) {

        this.allItems =
                allItems != null
                        ? allItems
                        : List.of();

        this.warehouses =
                warehouses != null
                        ? warehouses
                        : List.of();

        this.filter =
                Objects.requireNonNull(
                        filter,
                        "filter cannot be null"
                );


        setLayout(
                new BorderLayout(
                        10,
                        10
                )
        );

        setBorder(
                BorderFactory.createEmptyBorder(
                        12,
                        12,
                        12,
                        12
                )
        );

        setBackground(
                Color.WHITE
        );


        /*
         * =========================================
         * اللوحة الرئيسية العمودية
         * =========================================
         */
        /*
         * ============================================================
         * الحاوية الرئيسية - عمودان
         * ============================================================
         */

        JPanel fieldsPanel =
                new JPanel(
                        new GridLayout(
                                1,
                                2,
                                15,
                                0
                        )
                );

        fieldsPanel.setBackground(
                Color.WHITE
        );


        /*
         * ============================================================
         * العمود الأيسر
         *
         * المادة
         * النخب
         * سعة الصندوق
         * المستودع
         * ============================================================
         */

        JPanel leftColumn =
                new JPanel();

        leftColumn.setLayout(
                new BoxLayout(
                        leftColumn,
                        BoxLayout.Y_AXIS
                )
        );

        leftColumn.setBackground(
                Color.WHITE
        );


        /*
         * ============================================================
         * العمود الأيمن
         *
         * النوع
         * الموقع
         * ============================================================
         */

        JPanel rightColumn =
                new JPanel();

        rightColumn.setLayout(
                new BoxLayout(
                        rightColumn,
                        BoxLayout.Y_AXIS
                )
        );

        rightColumn.setBackground(
                Color.WHITE
        );


        /*
         * ============================================================
         * المادة
         * ============================================================
         */

        JPanel materialPanel =
                createSection("المادة");

        TileItem.MaterialType[] materials =
                TileItem.MaterialType.values();

        materialChecks =
                new JCheckBox[materials.length];

        for (int i = 0;
             i < materials.length;
             i++) {

            TileItem.MaterialType material =
                    materials[i];

            JCheckBox check =
                    createCheckBox(
                            material.name()
                    );

            check.setSelected(
                    filter.getMaterials()
                            .contains(material)
            );

            materialChecks[i] =
                    check;

            materialPanel.add(
                    check
            );
        }

        leftColumn.add(
                materialPanel
        );

        leftColumn.add(
                Box.createVerticalStrut(10)
        );


        /*
         * ============================================================
         * النخب
         * ============================================================
         */

        JPanel gradePanel =
                createSection("النخب");

        List<String> grades =
                allItems.stream()
                        .map(TileItem::getGrade)
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList());

        gradeChecks =
                new JCheckBox[grades.size()];

        for (int i = 0;
             i < grades.size();
             i++) {

            String grade =
                    grades.get(i);

            JCheckBox check =
                    createCheckBox(
                            grade
                    );

            check.setSelected(
                    filter.getGrades()
                            .contains(grade)
            );

            gradeChecks[i] =
                    check;

            gradePanel.add(
                    check
            );
        }

        leftColumn.add(
                gradePanel
        );

        leftColumn.add(
                Box.createVerticalStrut(10)
        );


        /*
         * ============================================================
         * سعة الصندوق
         * ============================================================
         */

        JPanel boxAreaPanel =
                createSection("سعة الصندوق");

        List<Double> boxAreas =
                allItems.stream()
                        .map(TileItem::getBoxArea)
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList());

        boxAreaChecks =
                new JCheckBox[boxAreas.size()];

        for (int i = 0;
             i < boxAreas.size();
             i++) {

            Double area =
                    boxAreas.get(i);

            JCheckBox check =
                    createCheckBox(
                            formatNumber(area)
                    );

            check.setSelected(
                    filter.getBoxAreas()
                            .contains(area)
            );

            boxAreaChecks[i] =
                    check;

            boxAreaPanel.add(
                    check
            );
        }

        leftColumn.add(
                boxAreaPanel
        );

        leftColumn.add(
                Box.createVerticalStrut(10)
        );


        /*
         * ============================================================
         * المستودع
         * ============================================================
         */

        JPanel warehousePanel =
                createSection("المستودع");

        warehouseChecks =
                new JCheckBox[
                        warehouses.size()
                        ];

        for (int i = 0;
             i < warehouses.size();
             i++) {

            Warehouse warehouse =
                    warehouses.get(i);

            JCheckBox check =
                    createCheckBox(
                            warehouse.getDisplayName()
                    );

            check.setSelected(
                    containsWarehouse(
                            warehouse
                    )
            );

            warehouseChecks[i] =
                    check;

            warehousePanel.add(
                    check
            );
        }

        leftColumn.add(
                warehousePanel
        );


        /*
         * ============================================================
         * النوع
         * ============================================================
         */

        JPanel subtypePanel =
                createSection("النوع");

        TileItem.SubType[] subtypes =
                TileItem.SubType.values();

        subtypeChecks =
                new JCheckBox[subtypes.length];

        for (int i = 0;
             i < subtypes.length;
             i++) {

            TileItem.SubType subtype =
                    subtypes[i];

            JCheckBox check =
                    createCheckBox(
                            subtype.name()
                    );

            check.setSelected(
                    filter.getSubTypes()
                            .contains(subtype)
            );

            subtypeChecks[i] =
                    check;

            subtypePanel.add(
                    check
            );
        }

        rightColumn.add(
                subtypePanel
        );

        rightColumn.add(
                Box.createVerticalStrut(10)
        );


        /*
         * ============================================================
         * الموقع
         * ============================================================
         */

        JPanel locationPanel =
                createSection("الموقع");

        TileItem.Location[] locations =
                TileItem.Location.values();

        locationChecks =
                new JCheckBox[locations.length];

        for (int i = 0;
             i < locations.length;
             i++) {

            TileItem.Location location =
                    locations[i];

            JCheckBox check =
                    createCheckBox(
                            location.getArabicName()
                    );

            check.setSelected(
                    filter.getLocations()
                            .contains(location)
            );

            locationChecks[i] =
                    check;

            locationPanel.add(
                    check
            );
        }

        rightColumn.add(
                locationPanel
        );


        /*
         * ============================================================
         * إضافة العمودين
         *
         * في RTL:
         * اليمين = النوع + الموقع
         * اليسار = المادة + النخب + سعة الصندوق + المستودع
         * ============================================================
         */

        fieldsPanel.add(
                rightColumn
        );

        fieldsPanel.add(
                leftColumn
        );
        /*
         * =========================================
         * Scroll
         * =========================================
         */

        JScrollPane scrollPane =
                new JScrollPane(
                        fieldsPanel
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

// سرعة السكرول داخل الفلتر فقط
        scrollPane.getVerticalScrollBar().setUnitIncrement(30);
        scrollPane.getVerticalScrollBar().setBlockIncrement(120);

        scrollPane.setPreferredSize(
                new Dimension(
                        520,
                        500
                )
        );

        add(
                scrollPane,
                BorderLayout.CENTER
        );

        /*
         * =========================================
         * أزرار الأسفل
         * =========================================
         */

        JPanel buttonsPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER,
                                10,
                                5
                        )
                );

        buttonsPanel.setBackground(
                Color.WHITE
        );


        clearButton =
                new JButton(
                        "مسح"
                );

        applyButton =
                new JButton(
                        "فلترة"
                );


        clearButton.setFont(
                BUTTON_FONT
        );

        applyButton.setFont(
                BUTTON_FONT
        );


        clearButton.setPreferredSize(
                new Dimension(
                        120,
                        42
                )
        );

        applyButton.setPreferredSize(
                new Dimension(
                        120,
                        42
                )
        );


        applyButton.setBackground(
                PRIMARY_COLOR
        );

        applyButton.setForeground(
                Color.WHITE
        );


        clearButton.setCursor(
                new Cursor(
                        Cursor.HAND_CURSOR
                )
        );

        applyButton.setCursor(
                new Cursor(
                        Cursor.HAND_CURSOR
                )
        );


        buttonsPanel.add(
                clearButton
        );

        buttonsPanel.add(
                applyButton
        );


        add(
                buttonsPanel,
                BorderLayout.SOUTH
        );


        /*
         * =========================================
         * أحداث الأزرار
         * =========================================
         */

        applyButton.addActionListener(
                e -> applyFilter()
        );


        clearButton.addActionListener(
                e -> clearFilter()
        );


        /*
         * =========================================
         * Enter = فلترة
         * =========================================
         */

        addEnterAction(
                this
        );
    }


    /*
     * ============================================================
     * Section
     * ============================================================
     */

    private JPanel createSection(
            String title) {

        JPanel panel =
                new JPanel();

        panel.setLayout(
                new BoxLayout(
                        panel,
                        BoxLayout.Y_AXIS
                )
        );

        panel.setBackground(
                Color.WHITE
        );

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                title
                        ),
                        BorderFactory.createEmptyBorder(
                                5,
                                8,
                                5,
                                8
                        )
                )
        );


        return panel;
    }


    /*
     * ============================================================
     * Checkbox
     * ============================================================
     */

    private JCheckBox createCheckBox(
            String text) {

        JCheckBox check =
                new JCheckBox(
                        text
                );

        check.setFont(
                NORMAL_FONT
        );

        check.setBackground(
                Color.WHITE
        );

        check.setAlignmentX(
                Component.RIGHT_ALIGNMENT
        );


        return check;
    }


    /*
     * ============================================================
     * تطبيق الفلتر
     * ============================================================
     */

    private void applyFilter() {

        /*
         * المادة
         */

        filter.getMaterials()
                .clear();


        for (int i = 0;
             i < materialChecks.length;
             i++) {

            if (materialChecks[i]
                    .isSelected()) {

                filter.getMaterials()
                        .add(
                                TileItem.MaterialType
                                        .values()[i]
                        );
            }
        }


        /*
         * النوع
         */

        filter.getSubTypes()
                .clear();


        TileItem.SubType[] subtypes =
                TileItem.SubType.values();


        for (int i = 0;
             i < subtypeChecks.length;
             i++) {

            if (subtypeChecks[i]
                    .isSelected()) {

                filter.getSubTypes()
                        .add(
                                subtypes[i]
                        );
            }
        }


        /*
         * المستودع
         */

        filter.getWarehouses()
                .clear();


        for (int i = 0;
             i < warehouseChecks.length;
             i++) {

            if (warehouseChecks[i]
                    .isSelected()) {

                filter.getWarehouses()
                        .add(
                                warehouses.get(i)
                        );
            }
        }


        /*
         * الموقع
         */

        filter.getLocations()
                .clear();


        TileItem.Location[] locations =
                TileItem.Location.values();


        for (int i = 0;
             i < locationChecks.length;
             i++) {

            if (locationChecks[i]
                    .isSelected()) {

                filter.getLocations()
                        .add(
                                locations[i]
                        );
            }
        }


        /*
         * النخب
         */

        filter.getGrades()
                .clear();


        List<String> grades =
                allItems.stream()
                        .map(
                                TileItem::getGrade
                        )
                        .filter(
                                Objects::nonNull
                        )
                        .map(
                                String::trim
                        )
                        .filter(
                                s -> !s.isEmpty()
                        )
                        .distinct()
                        .sorted()
                        .collect(
                                Collectors.toList()
                        );


        for (int i = 0;
             i < gradeChecks.length;
             i++) {

            if (gradeChecks[i]
                    .isSelected()) {

                filter.getGrades()
                        .add(
                                grades.get(i)
                        );
            }
        }


        /*
         * سعة الصندوق
         */

        filter.getBoxAreas()
                .clear();


        List<Double> boxAreas =
                allItems.stream()
                        .map(
                                TileItem::getBoxArea
                        )
                        .distinct()
                        .sorted()
                        .collect(
                                Collectors.toList()
                        );


        for (int i = 0;
             i < boxAreaChecks.length;
             i++) {

            if (boxAreaChecks[i]
                    .isSelected()) {

                filter.getBoxAreas()
                        .add(
                                boxAreas.get(i)
                        );
            }
        }


        /*
         * إشعار TilePanel.
         */

        if (onFilterApplied != null) {

            onFilterApplied.run();
        }
    }


    /*
     * ============================================================
     * مسح الفلتر
     * ============================================================
     */

    private void clearFilter() {

        /*
         * مسح الحالة الداخلية.
         */

        filter.getMaterials()
                .clear();

        filter.getSubTypes()
                .clear();

        filter.getWarehouses()
                .clear();

        filter.getLocations()
                .clear();

        filter.getGrades()
                .clear();

        filter.getBoxAreas()
                .clear();


        /*
         * إلغاء جميع CheckBox.
         */

        uncheckAll(
                materialChecks
        );

        uncheckAll(
                subtypeChecks
        );

        uncheckAll(
                warehouseChecks
        );

        uncheckAll(
                locationChecks
        );

        uncheckAll(
                gradeChecks
        );

        uncheckAll(
                boxAreaChecks
        );


        /*
         * إشعار TilePanel.
         */

        if (onClearApplied != null) {

            onClearApplied.run();
        }
    }


    /*
     * ============================================================
     * فحص المستودع
     * ============================================================
     */

    private boolean containsWarehouse(
            Warehouse warehouse) {

        return filter.getWarehouses()
                .stream()
                .anyMatch(
                        w ->
                                w.getId()
                                        == warehouse.getId()
                );
    }


    /*
     * ============================================================
     * إلغاء تحديد مجموعة
     * ============================================================
     */

    private void uncheckAll(
            JCheckBox[] checks) {

        for (JCheckBox check :
                checks) {

            check.setSelected(
                    false
            );
        }
    }


    /*
     * ============================================================
     * تنسيق الرقم
     * ============================================================
     */

    private String formatNumber(
            double value) {

        if (value ==
                Math.rint(value)) {

            return String.format(
                    "%.0f",
                    value
            );
        }


        return String.format(
                "%.2f",
                value
        );
    }


    /*
     * ============================================================
     * Enter
     * ============================================================
     */

    private void addEnterAction(
            Component component) {

        component.addKeyListener(
                new KeyAdapter() {

                    @Override
                    public void keyPressed(
                            KeyEvent e) {

                        if (e.getKeyCode()
                                == KeyEvent.VK_ENTER) {

                            applyFilter();

                            e.consume();
                        }
                    }
                }
        );


        if (component instanceof
                Container container) {

            for (Component child :
                    container.getComponents()) {

                addEnterAction(
                        child
                );
            }
        }
    }


    /*
     * ============================================================
     * Setter
     * ============================================================
     */

    public void setOnFilterApplied(
            Runnable callback) {

        this.onFilterApplied =
                callback;
    }


    /*
     * ============================================================
     * Setter
     * ============================================================
     */

    public void setOnClearApplied(
            Runnable callback) {

        this.onClearApplied =
                callback;
    }


    /*
     * ============================================================
     * Getter اختياري
     * ============================================================
     */

    public TileFilter getFilter() {

        return filter;
    }
}