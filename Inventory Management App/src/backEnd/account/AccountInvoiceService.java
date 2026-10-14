package backEnd.account;

import backEnd.sanitary.SanitaryItem;
import backEnd.sanitary.SanitaryService;
import backEnd.tile.TileItem;
import backEnd.tile.TileService;
import backEnd.warehouse.Warehouse;

import java.time.LocalDate;

public class AccountInvoiceService {

    private final AccountsManager accountsManager;
    private final TileService tileService;
    private final SanitaryService sanitaryService;

    public AccountInvoiceService(
            AccountsManager accountsManager,
            TileService tileService,
            SanitaryService sanitaryService
    ) {

        if (accountsManager == null) {
            throw new IllegalArgumentException(
                    "مدير الحسابات مطلوب"
            );
        }

        this.accountsManager =
                accountsManager;

        this.tileService =
                tileService;

        this.sanitaryService =
                sanitaryService;
    }

    public AccountInvoice createInvoice(
            Account account,
            AccountInvoiceType type,
            LocalDate date,
            String description
    ) {

        requireAccount(account);

        AccountInvoice invoice =
                new AccountInvoice(
                        type,
                        date,
                        description
                );

        account.addAccountInvoice(
                invoice
        );

        accountsManager.saveQuietly();

        return invoice;
    }

    public AccountInvoice createPayment(
            Account account,
            LocalDate date,
            double amount,
            String description
    ) {

        AccountInvoice invoice =
                createInvoice(
                        account,
                        AccountInvoiceType.PAYMENT,
                        date,
                        description
                );

        invoice.setAmount(amount);

        accountsManager.saveQuietly();

        return invoice;
    }

    public AccountInvoice createReceipt(
            Account account,
            LocalDate date,
            double amount,
            String description
    ) {

        AccountInvoice invoice =
                createInvoice(
                        account,
                        AccountInvoiceType.RECEIPT,
                        date,
                        description
                );

        invoice.setAmount(amount);

        accountsManager.saveQuietly();

        return invoice;
    }

    /**
     * إضافة سطر إلى فاتورة مبيع.
     */
    public AccountEntry addSaleEntry(
            AccountInvoice invoice,
            String material,
            double quantity1,
            String unit,
            double price,
            double quantity2
    ) {

        requireType(
                invoice,
                AccountInvoiceType.SALE
        );

        AccountEntry entry =
                new AccountEntry();

        entry.setMaterial(material);
        entry.setQuantity1(quantity1);
        entry.setUnit(unit);
        entry.setPrice(price);
        entry.setQuantity2(quantity2);

        entry.calculateTotal();

        invoice.addEntry(entry);

        accountsManager.saveQuietly();

        return entry;
    }

    /**
     * بيع بلاط من صنف موجود في المستودع.
     * لا يتم تغيير السعر الأساسي للصنف عند البيع.
     */
    public AccountEntry addSaleExistingTile(
            AccountInvoice invoice, TileItem item, double boxes, double price) {
        requireType(invoice, AccountInvoiceType.SALE);
        if (item == null) throw new IllegalArgumentException("صنف البلاط مطلوب");
        validatePositive(boxes, "عدد الصناديق");
        validatePositiveOrZero(price, "السعر");
        if (boxes > item.getBoxes()) {
            throw new IllegalArgumentException("الكمية المطلوبة أكبر من الكمية المتوفرة في المستودع");
        }
        alignSaleMovements(invoice);
        double area = item.getBoxArea();
        String material = buildTileMaterial(item);
        AccountEntry entry = AccountEntryFactory.createTileEntry(
                material, area, boxes, price);
        item.setBoxes(item.getBoxes() - boxes);
        tileService.save();
        invoice.addEntry(entry);
        invoice.addStockMovement(new AccountStockMovement(
                AccountStockMovementType.EXISTING_ITEM, AccountInventoryKind.TILE,
                item.getIdNumber(), material, area * boxes, boxes, price,
                item.getPrice(), area));
        accountsManager.saveQuietly();
        return entry;
    }

    /** بيع أداة صحية من صنف موجود في المستودع. */
    public AccountEntry addSaleExistingSanitary(
            AccountInvoice invoice, SanitaryItem item, double quantity, double price) {
        requireType(invoice, AccountInvoiceType.SALE);
        if (item == null) throw new IllegalArgumentException("الصنف الصحي مطلوب");
        validateWholeQuantity(quantity);
        validatePositive(quantity, "الكمية");
        validatePositiveOrZero(price, "السعر");
        if (quantity > item.getQuantity()) {
            throw new IllegalArgumentException("الكمية المطلوبة أكبر من الكمية المتوفرة في المستودع");
        }
        alignSaleMovements(invoice);
        String material = buildSanitaryMaterial(item);
        AccountEntry entry = AccountEntryFactory.createSanitaryEntry(
                material, quantity, "قطعة", price);
        item.setQuantity(item.getQuantity() - (int) quantity);
        sanitaryService.save();
        invoice.addEntry(entry);
        invoice.addStockMovement(new AccountStockMovement(
                AccountStockMovementType.EXISTING_ITEM, AccountInventoryKind.SANITARY,
                item.getIdNumber(), material, quantity, quantity, price,
                item.getPrice(), 0.0));
        accountsManager.saveQuietly();
        return entry;
    }

    /** بيع مادة من خارج المستودع؛ لا تُنشأ حركة مخزون. */
    public AccountEntry addSaleNotInStock(
            AccountInvoice invoice, AccountInventoryKind kind, String material,
            double quantity1, String unit, double price, double quantity2) {
        requireType(invoice, AccountInvoiceType.SALE);
        requireKind(kind);
        validatePositive(quantity1, "الكمية");
        validatePositiveOrZero(price, "السعر");
        alignSaleMovements(invoice);
        AccountEntry entry = new AccountEntry();
        entry.setMaterial(material);
        if (kind == AccountInventoryKind.TILE) {
            validatePositive(quantity2, "عدد الصناديق");
            entry.setUnit("متر");
            entry.setQuantity1(quantity1);
            entry.setQuantity2(quantity2);
            entry.setPrice(price);
            entry.calculateTotal();
        } else {
            entry.calculateSanitary(quantity1, unit, price);
        }
        invoice.addEntry(entry);
        invoice.addStockMovement(new AccountStockMovement(
                AccountStockMovementType.NOT_IN_STOCK, kind, 0L,
                entry.getMaterial(), entry.getQuantity1(), entry.getQuantity2(),
                entry.getPrice(), 0.0, 0.0));
        accountsManager.saveQuietly();
        return entry;
    }

    /*
     * تحفظ السندات القديمة دون حركات مخزون. قبل إضافة حركة جديدة نضيف
     * حركات محايدة للبنود القديمة حتى تبقى فهارس البنود والحركات متطابقة.
     */
    private void alignSaleMovements(AccountInvoice invoice) {
        while (invoice.getStockMovements().size() < invoice.getEntries().size()) {
            AccountEntry old = invoice.getEntries().get(invoice.getStockMovements().size());
            invoice.addStockMovement(new AccountStockMovement(
                    AccountStockMovementType.NOT_IN_STOCK,
                    old != null && old.isTile() ? AccountInventoryKind.TILE : AccountInventoryKind.SANITARY,
                    0L, old == null ? "" : old.getMaterial(),
                    old == null ? 0 : old.getQuantity1(),
                    old == null ? 0 : old.getQuantity2(),
                    old == null ? 0 : old.getPrice(), 0.0, 0.0));
        }
    }

    /**
     * شراء لا يضاف إلى المستودع.
     */
    public AccountEntry addPurchaseNotInStock(
            AccountInvoice invoice,
            AccountInventoryKind kind,
            String material,
            double quantity1,
            String unit,
            double price,
            double quantity2
    ) {

        requireType(
                invoice,
                AccountInvoiceType.PURCHASE
        );

        requireKind(kind);

        AccountEntry entry =
                new AccountEntry();

        entry.setMaterial(material);

        if (
                kind ==
                        AccountInventoryKind.TILE
        ) {

            entry.setQuantity1(
                    quantity1
            );

            entry.setQuantity2(
                    quantity2
            );

            entry.setUnit("متر");
            entry.setPrice(price);

            entry.calculateTotal();

        } else {

            entry.calculateSanitary(
                    quantity1,
                    unit,
                    price
            );
        }

        invoice.addEntry(entry);

        invoice.addStockMovement(
                new AccountStockMovement(
                        AccountStockMovementType
                                .NOT_IN_STOCK,
                        kind,
                        0L,
                        entry.getMaterial(),
                        entry.getQuantity1(),
                        entry.getQuantity2(),
                        entry.getPrice(),
                        0.0,
                        0.0
                )
        );

        accountsManager.saveQuietly();

        return entry;
    }

    /**
     * شراء بلاط من صنف موجود.
     */
    public AccountEntry addPurchaseExistingTile(
            AccountInvoice invoice,
            TileItem item,
            String editedCode,
            String editedName,
            double boxes,
            double price
    ) {

        requireType(
                invoice,
                AccountInvoiceType.PURCHASE
        );

        if (item == null) {
            throw new IllegalArgumentException(
                    "صنف البلاط مطلوب"
            );
        }

        validatePositiveOrZero(
                boxes,
                "عدد الصناديق"
        );

        validatePositiveOrZero(
                price,
                "السعر"
        );

        double previousPrice =
                item.getPrice();

        /*
         * إذا أدخل المستخدم رمزًا أو اسمًا جديدًا يتم تعديلهما
         * في نفس صنف المستودع. إذا ترك الحقل فارغًا يبقى القديم.
         */
        if (editedCode != null && !editedCode.trim().isEmpty()) {
            item.setCode(editedCode);
        }

        if (editedName != null && !editedName.trim().isEmpty()) {
            item.setName(editedName);
        }

        double quantity1 =
                item.getBoxArea()
                        * boxes;

        /*
         * زيادة المخزون.
         */
        item.setBoxes(
                item.getBoxes()
                        + boxes
        );

        /*
         * تحديث السعر.
         */
        item.setPrice(price);

        /*
         * بعد تعديل الرمز/الاسم يجب بناء المادة من القيم الجديدة.
         */
        String material =
                buildTileMaterial(item);

        tileService.save();

        AccountEntry entry =
                new AccountEntry();

        entry.setMaterial(material);

        entry.calculateTile(
                item.getBoxArea(),
                boxes,
                price
        );

        invoice.addEntry(entry);

        invoice.addStockMovement(
                new AccountStockMovement(
                        AccountStockMovementType
                                .EXISTING_ITEM,
                        AccountInventoryKind.TILE,
                        item.getIdNumber(),
                        material,
                        quantity1,
                        boxes,
                        price,
                        previousPrice,
                        item.getBoxArea()
                )
        );

        accountsManager.saveQuietly();

        return entry;
    }

    /**
     * شراء أداة صحية من صنف موجود.
     */
    public AccountEntry addPurchaseExistingSanitary(
            AccountInvoice invoice,
            SanitaryItem item,
            double quantity,
            String unit,
            double price
    ) {

        requireType(
                invoice,
                AccountInvoiceType.PURCHASE
        );

        if (item == null) {
            throw new IllegalArgumentException(
                    "الصنف الصحي مطلوب"
            );
        }

        validateWholeQuantity(
                quantity
        );

        validatePositiveOrZero(
                price,
                "السعر"
        );

        int oldQuantity =
                item.getQuantity();

        double previousPrice =
                item.getPrice();

        String material =
                buildSanitaryMaterial(item);

        item.setQuantity(
                oldQuantity
                        + (int) quantity
        );

        item.setPrice(price);

        sanitaryService.save();

        AccountEntry entry =
                new AccountEntry();

        entry.setMaterial(material);

        entry.calculateSanitary(
                quantity,
                unit,
                price
        );

        invoice.addEntry(entry);

        invoice.addStockMovement(
                new AccountStockMovement(
                        AccountStockMovementType
                                .EXISTING_ITEM,
                        AccountInventoryKind.SANITARY,
                        item.getIdNumber(),
                        material,
                        quantity,
                        quantity,
                        price,
                        previousPrice,
                        0.0
                )
        );

        accountsManager.saveQuietly();

        return entry;
    }

    /**
     * إنشاء صنف بلاط جديد من خلال فاتورة الشراء.
     */
    public AccountEntry addPurchaseNewTile(
            AccountInvoice invoice,
            String code,
            String name,
            String grade,
            double boxArea,
            double boxes,
            TileItem.MaterialType materialType,
            TileItem.SubType subType,
            Warehouse warehouse,
            TileItem.Location location,
            double price
    ) {

        requireType(
                invoice,
                AccountInvoiceType.PURCHASE
        );

        requireWarehouse(
                warehouse
        );

        validatePositive(
                boxArea,
                "مساحة الصندوق"
        );

        validatePositiveOrZero(
                boxes,
                "عدد الصناديق"
        );

        validatePositiveOrZero(
                price,
                "السعر"
        );

        TileItem item =
                new TileItem(
                        name,
                        code,
                        price,
                        boxes,
                        boxArea,
                        materialType,
                        subType,
                        warehouse,
                        grade,
                        location
                );

        tileService.add(item);
        tileService.save();

        String material =
                buildTileMaterial(item);

        AccountEntry entry =
                new AccountEntry();

        entry.setMaterial(material);

        entry.calculateTile(
                boxArea,
                boxes,
                price
        );

        invoice.addEntry(entry);

        invoice.addStockMovement(
                new AccountStockMovement(
                        AccountStockMovementType
                                .NEW_ITEM,
                        AccountInventoryKind.TILE,
                        item.getIdNumber(),
                        material,
                        entry.getQuantity1(),
                        entry.getQuantity2(),
                        price,
                        0.0,
                        boxArea
                )
        );

        accountsManager.saveQuietly();

        return entry;
    }

    /**
     * إنشاء أداة صحية جديدة من خلال فاتورة الشراء.
     */
    public AccountEntry addPurchaseNewSanitary(
            AccountInvoice invoice,
            String name,
            double price,
            int quantity,
            SanitaryItem.SanitaryType type,
            Warehouse warehouse,
            String grade
    ) {

        requireType(
                invoice,
                AccountInvoiceType.PURCHASE
        );

        requireWarehouse(
                warehouse
        );

        validatePositiveOrZero(
                price,
                "السعر"
        );

        if (quantity < 0) {
            throw new IllegalArgumentException(
                    "الكمية لا يمكن أن تكون سالبة"
            );
        }

        SanitaryItem item =
                new SanitaryItem(
                        name,
                        price,
                        quantity,
                        type,
                        warehouse,
                        grade
                );

        sanitaryService.add(item);

        String material =
                buildSanitaryMaterial(item);

        AccountEntry entry =
                new AccountEntry();

        entry.setMaterial(material);

        entry.calculateSanitary(
                quantity,
                price
        );

        invoice.addEntry(entry);

        invoice.addStockMovement(
                new AccountStockMovement(
                        AccountStockMovementType
                                .NEW_ITEM,
                        AccountInventoryKind.SANITARY,
                        item.getIdNumber(),
                        material,
                        quantity,
                        quantity,
                        price,
                        0.0,
                        0.0
                )
        );

        accountsManager.saveQuietly();

        return entry;
    }

    /**
     * حذف فاتورة.
     *
     * إذا كانت شراء يتم أولاً عكس
     * حركة المستودع.
     */
    public void deleteInvoice(
            Account account,
            AccountInvoice invoice
    ) {

        requireAccount(account);

        if (invoice == null) {
            return;
        }

        if (invoice.isPurchase()) {
            rollbackPurchaseStock(invoice);
        } else if (invoice.isSale()) {
            rollbackSaleStock(invoice);
        }

        account.removeAccountInvoice(
                invoice
        );

        accountsManager.saveQuietly();
    }
    public void deleteEntry(
            AccountInvoice invoice,
            int entryIndex
    ) {

        if (invoice == null) {
            throw new IllegalArgumentException(
                    "الفاتورة مطلوبة"
            );
        }

        if (
                entryIndex < 0
                        || entryIndex >= invoice
                        .getEntries()
                        .size()
        ) {

            throw new IllegalArgumentException(
                    "البند المحدد غير موجود"
            );
        }

        /*
         * في فاتورة الشراء:
         * كل بند مرتبط بحركة مخزون بنفس الفهرس.
         */
        if (invoice.isPurchase()) {
            if (entryIndex < invoice.getStockMovements().size()) {
                rollbackPurchaseStockMovement(invoice.getStockMovements().get(entryIndex));
                invoice.getStockMovements().remove(entryIndex);
            }
        } else if (invoice.isSale()
                && invoice.getStockMovements().size() == invoice.getEntries().size()) {
            rollbackSaleStockMovement(invoice.getStockMovements().get(entryIndex));
            invoice.getStockMovements().remove(entryIndex);
        }

        invoice
                .getEntries()
                .remove(entryIndex);

        accountsManager.saveQuietly();
    }private void rollbackPurchaseStockMovement(
            AccountStockMovement movement
    ) {

        if (
                movement == null
                        || movement.getMovementType()
                        == AccountStockMovementType.NOT_IN_STOCK
        ) {
            return;
        }

        if (
                movement.getInventoryKind()
                        == AccountInventoryKind.TILE
        ) {

            TileItem item =
                    tileService.findById(
                            movement.getInventoryItemId()
                    );

            if (item == null) {
                return;
            }

            if (
                    movement.getMovementType()
                            == AccountStockMovementType.NEW_ITEM
            ) {

                tileService.remove(item);
                tileService.save();

                return;
            }

            item.setBoxes(
                    Math.max(
                            0,
                            item.getBoxes()
                                    - movement.getQuantity2()
                    )
            );

            item.setPrice(
                    movement.getPreviousPrice()
            );

            tileService.save();

            return;
        }

        SanitaryItem item =
                sanitaryService.findById(
                        movement.getInventoryItemId()
                );

        if (item == null) {
            return;
        }

        if (
                movement.getMovementType()
                        == AccountStockMovementType.NEW_ITEM
        ) {

            sanitaryService.remove(item);
            sanitaryService.save();

            return;
        }

        int quantity =
                (int) Math.round(
                        movement.getQuantity2()
                );

        item.setQuantity(
                Math.max(
                        0,
                        item.getQuantity()
                                - quantity
                )
        );

        item.setPrice(
                movement.getPreviousPrice()
        );

        sanitaryService.save();
    }

    /** عكس حركات المخزون الناتجة عن البيع. */
    private void rollbackSaleStockMovement(AccountStockMovement movement) {
        if (movement == null
                || movement.getMovementType() == AccountStockMovementType.NOT_IN_STOCK) return;

        if (movement.getInventoryKind() == AccountInventoryKind.TILE) {
            TileItem item = tileService.findById(movement.getInventoryItemId());
            if (item != null) {
                item.setBoxes(item.getBoxes() + movement.getQuantity2());
                tileService.save();
            }
        } else {
            SanitaryItem item = sanitaryService.findById(movement.getInventoryItemId());
            if (item != null) {
                item.setQuantity(item.getQuantity() + (int) Math.round(movement.getQuantity2()));
                sanitaryService.save();
            }
        }
    }

    private void rollbackSaleStock(AccountInvoice invoice) {
        if (invoice == null || !invoice.isSale()
                || invoice.getStockMovements().size() != invoice.getEntries().size()) return;
        for (AccountStockMovement movement : invoice.getStockMovements()) {
            rollbackSaleStockMovement(movement);
        }
    }

    /**
     * التراجع عن حركات فاتورة الشراء.
     */
    public void rollbackPurchaseStock(
            AccountInvoice invoice
    ) {

        if (
                invoice == null
                        || !invoice.isPurchase()
        ) {
            return;
        }

        boolean tileChanged = false;
        boolean sanitaryChanged = false;

        for (
                AccountStockMovement movement :
                invoice.getStockMovements()
        ) {

            if (
                    movement.getMovementType()
                            ==
                            AccountStockMovementType
                                    .NOT_IN_STOCK
            ) {
                continue;
            }

            if (
                    movement.getInventoryKind()
                            ==
                            AccountInventoryKind.TILE
            ) {

                TileItem item =
                        tileService.findById(
                                movement.getInventoryItemId()
                        );

                if (item == null) {
                    continue;
                }

                if (
                        movement.getMovementType()
                                ==
                                AccountStockMovementType
                                        .NEW_ITEM
                ) {

                    tileService.remove(
                            item
                    );

                } else {

                    item.setBoxes(
                            Math.max(
                                    0,
                                    item.getBoxes()
                                            - movement
                                            .getQuantity2()
                            )
                    );

                    item.setPrice(
                            movement.getPreviousPrice()
                    );

                    tileChanged = true;
                }

            } else {

                SanitaryItem item =
                        sanitaryService.findById(
                                movement.getInventoryItemId()
                        );

                if (item == null) {
                    continue;
                }

                if (
                        movement.getMovementType()
                                ==
                                AccountStockMovementType
                                        .NEW_ITEM
                ) {

                    sanitaryService.remove(
                            item
                    );

                } else {

                    int quantity =
                            (int) Math.round(
                                    movement
                                            .getQuantity2()
                            );

                    item.setQuantity(
                            Math.max(
                                    0,
                                    item.getQuantity()
                                            - quantity
                            )
                    );

                    item.setPrice(
                            movement.getPreviousPrice()
                    );

                    sanitaryChanged = true;
                }
            }
        }

        if (tileChanged) {
            tileService.save();
        }

        if (sanitaryChanged) {
            sanitaryService.save();
        }
    }

    /**
     * تركيب مادة البلاط:
     *
     * code + name + subtype + materialType + grade
     */
    public String buildTileMaterial(
            TileItem item
    ) {

        if (item == null) {
            return "";
        }

        String grade = normalizeGrade(item.getGrade());

        return safe(item.getCode())
                + " "
                + safe(item.getName())
                + " "
                + (
                item.getSubType() == null
                        ? ""
                        : item.getSubType().name()
        )
                + " "
                + (
                item.getMaterialType() == null
                        ? ""
                        : item.getMaterialType().name()
        )
                + " "
                + grade;
    }

    /**
     * تركيب مادة الأدوات الصحية:
     *
     * name + grade
     */
    public String buildSanitaryMaterial(
            SanitaryItem item
    ) {

        if (item == null) {
            return "";
        }

        return safe(item.getName())
                + " "
                + normalizeGrade(item.getGrade());
    }

    private String normalizeGrade(String value) {
        String normalized = safe(value);
        return switch (normalized) {
            case "1" -> "اول";
            case "2" -> "ثاني";
            case "3" -> "ثالث";
            case "4" -> "رابع";
            case "5" -> "خامس";
            default -> normalized;
        };
    }

    private String safe(String value) {

        return value == null
                ? ""
                : value.trim();
    }

    private void requireAccount(
            Account account
    ) {

        if (account == null) {
            throw new IllegalArgumentException(
                    "الحساب مطلوب"
            );
        }
    }

    private void requireType(
            AccountInvoice invoice,
            AccountInvoiceType expected
    ) {

        if (invoice == null) {
            throw new IllegalArgumentException(
                    "الفاتورة مطلوبة"
            );
        }

        if (
                invoice.getType()
                        != expected
        ) {
            throw new IllegalArgumentException(
                    "نوع الفاتورة غير صحيح لهذه العملية"
            );
        }
    }

    private void requireKind(
            AccountInventoryKind kind
    ) {

        if (kind == null) {
            throw new IllegalArgumentException(
                    "نوع المادة مطلوب"
            );
        }
    }

    private void requireWarehouse(
            Warehouse warehouse
    ) {

        if (warehouse == null) {
            throw new IllegalArgumentException(
                    "المستودع مطلوب"
            );
        }
    }

    private void validatePositive(
            double value,
            String field
    ) {

        if (
                Double.isNaN(value)
                        || Double.isInfinite(value)
                        || value <= 0
        ) {
            throw new IllegalArgumentException(
                    field
                            + " يجب أن يكون أكبر من الصفر"
            );
        }
    }

    private void validatePositiveOrZero(
            double value,
            String field
    ) {

        if (
                Double.isNaN(value)
                        || Double.isInfinite(value)
                        || value < 0
        ) {
            throw new IllegalArgumentException(
                    field
                            + " غير صالح"
            );
        }
    }

    private void validateWholeQuantity(
            double value
    ) {

        validatePositiveOrZero(
                value,
                "الكمية"
        );

        if (Math.rint(value) != value) {

            throw new IllegalArgumentException(
                    "كمية الأدوات الصحية يجب أن تكون عدداً صحيحاً"
            );
        }
    }
}