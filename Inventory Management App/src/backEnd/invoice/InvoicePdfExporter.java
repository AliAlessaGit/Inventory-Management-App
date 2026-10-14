package backEnd.invoice;

import backEnd.account.pdf.ArabicTextRenderer;
import backEnd.sanitary.SanitaryItem;
import backEnd.sanitary.SanitaryService;
import backEnd.tile.TileItem;
import backEnd.tile.TileService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class InvoicePdfExporter {

    private static final float PAGE_WIDTH =
            PDRectangle.A4.getWidth();

    private static final float PAGE_HEIGHT =
            PDRectangle.A4.getHeight();

    private static final float MARGIN = 30f;
    private static final float RIGHT = PAGE_WIDTH - MARGIN;
    private static final float ROW_HEIGHT = 26f;

    private static final String FONT_RESOURCE =
            "/fonts/NotoNaskhArabic-Regular.ttf";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    private InvoicePdfExporter() {
    }

    public static void export(
            Invoice invoice,
            TileService tileService,
            SanitaryService sanitaryService,
            File outputFile,
            boolean groupByType
    ) throws Exception {

        if (invoice == null) {
            throw new IllegalArgumentException(
                    "الفاتورة مطلوبة"
            );
        }

        if (outputFile == null) {
            throw new IllegalArgumentException(
                    "مسار حفظ الملف مطلوب"
            );
        }

        List<InvoiceItem> items =
                new ArrayList<>(invoice.getItems());

        if (groupByType) {

            // البلاط أولاً ثم الأدوات.
            // يحافظ الفرز المستقر على ترتيب العناصر
            // داخل كل مجموعة.

            items.sort(
                    Comparator.comparingInt(
                            item -> item.isTile() ? 0 : 1
                    )
            );

        } else {

            // الأقدم أولاً، بغض النظر عن نوع العنصر.
            // صيغة التاريخ ثابتة: yyyy/MM/dd HH:mm.

            items.sort(
                    Comparator.comparing(
                            InvoiceItem::getFormattedDate
                    )
            );
        }

        try (PDDocument document = new PDDocument()) {

            PDType0Font font = loadArabicFont(document);

            // صفحات العناصر.
            drawItemPages(
                    document,
                    font,
                    invoice,
                    items,
                    tileService,
                    sanitaryService
            );

            // جدول الدفعات يبدأ في صفحة مستقلة.
            drawPaymentPages(
                    document,
                    font,
                    invoice
            );

            document.save(outputFile);
        }
    }

    private static PDType0Font loadArabicFont(
            PDDocument document
    ) throws IOException {

        InputStream stream =
                InvoicePdfExporter.class
                        .getResourceAsStream(
                                FONT_RESOURCE
                        );

        if (stream == null) {
            throw new IOException(
                    "لم يتم العثور على الخط العربي:\n"
                            + FONT_RESOURCE
                            + "\n"
                            + "تأكد من وجوده ضمن Resources."
            );
        }

        try (InputStream input = stream) {
            return PDType0Font.load(document, input);
        }
    }

    // =====================================================
    // صفحات العناصر
    // =====================================================

    private static void drawItemPages(
            PDDocument document,
            PDType0Font font,
            Invoice invoice,
            List<InvoiceItem> items,
            TileService tileService,
            SanitaryService sanitaryService
    ) throws Exception {

        int index = 0;
        int pageNumber = 0;

        do {

            pageNumber++;

            PDPage page =
                    new PDPage(PDRectangle.A4);

            document.addPage(page);

            try (PDPageContentStream content =
                         new PDPageContentStream(
                                 document,
                                 page
                         )) {

                float y =
                        drawInvoiceHeader(
                                content,
                                font,
                                invoice,
                                pageNumber > 1
                        );

                y = drawItemTableHeader(
                        content,
                        font,
                        y
                );

                // نترك مساحة أسفل الصفحة للإجمالي.
                while (
                        index < items.size()
                                && y > 245
                ) {

                    InvoiceItem item =
                            items.get(index);

                    String material =
                            buildMaterial(
                                    item,
                                    tileService,
                                    sanitaryService
                            );

                    String type =
                            getItemType(
                                    item,
                                    sanitaryService
                            );

                    String quantity =
                            formatNumber(
                                    item.getQuantity()
                            );

                    String area =
                            item.isTile()
                                    ? formatNumber(
                                    item.getQuantity()
                                            * item.getBoxArea()
                            )
                                    : "-";

                    String[] row = {
                            material,
                            type,
                            quantity,
                            area,
                            formatMoneyUsd(item.getUnitPrice()),
                            formatMoneyUsd(item.getTotalPrice()),
                            safe(item.getFormattedDate())
                    };

                    y = drawRow(
                            content,
                            font,
                            row,
                            new float[]{
                                    205, 48, 50, 55, 55, 60, 62
                            },
                            y,
                            false
                    );

                    index++;
                }

                // الإجماليات تظهر في نهاية صفحات العناصر.
                if (index >= items.size()) {

                    drawInvoiceTotals(
                            content,
                            font,
                            invoice,
                            y - 30
                    );
                }

                drawFooter(
                        content,
                        font,
                        pageNumber
                );
            }

        } while (index < items.size());
    }

    private static float drawInvoiceHeader(
            PDPageContentStream content,
            PDType0Font font,
            Invoice invoice,
            boolean continuation
    ) throws Exception {

        float y = PAGE_HEIGHT - 55;

        ArabicTextRenderer.drawCentered(
                content,
                font,
                continuation
                        ? "تفاصيل الفاتورة - تابع"
                        : "فاتورة مبيعات",
                PAGE_WIDTH / 2f,
                y,
                22
        );

        y -= 30;

        if (continuation) {
            return y;
        }

        ArabicTextRenderer.drawRight(
                content,
                font,
                "العميل:               "
                        + safe(invoice.getCustomerName()),
                RIGHT,
                y,
                13
        );

        y -= 22;

        ArabicTextRenderer.drawRight(
                content,
                font,
                "رقم الفاتورة:      "
                        + safe(invoice.getInvoiceNumber()),
                RIGHT,
                y,
                13
        );

        y -= 22;

        String date =
                invoice.getDate() == null
                        ? ""
                        : DATE_FORMAT.format(invoice.getDate());

        ArabicTextRenderer.drawRight(
                content,
                font,
                "التاريخ:             " + date,
                RIGHT,
                y,
                13
        );

        y -= 25;

        if (!safe(invoice.getNote()).isBlank()) {

            ArabicTextRenderer.drawRight(
                    content,
                    font,
                    "ملاحظة: " + invoice.getNote(),
                    RIGHT,
                    y,
                    11
            );

            y -= 20;
        }

        return y;
    }

    private static float drawItemTableHeader(
            PDPageContentStream content,
            PDType0Font font,
            float y
    ) throws Exception {

        String[] headers = {
                "اسم العنصر",
                "النوع",
                "الكمية",
                "المساحة م",
                "سعر الوحدة",
                "الإجمالي",
                "التاريخ"
        };

        return drawRow(
                content,
                font,
                headers,
                new float[]{
                        205, 48, 50, 55, 55, 60, 62
                },
                y,
                true
        );
    }

    // =====================================================
    // تسمية العناصر
    // =====================================================

    private static String buildMaterial(
            InvoiceItem item,
            TileService tileService,
            SanitaryService sanitaryService
    ) {

        if (item.isTile()) {

            TileItem tile =
                    item.getIdNumber() > 0
                            && tileService != null
                            ? tileService.findById(
                            item.getIdNumber()
                    )
                            : null;

            if (tile != null) {

                // نفس منطق تركيب اسم البلاط في Account:
                // الرمز + الاسم + النوع الفرعي
                // + نوع المادة + النخب.

                return join(
                        tile.getCode(),
                        tile.getName(),
                        tile.getSubType() == null
                                ? ""
                                : tile.getSubType().name(),
                        tile.getMaterialType() == null
                                ? ""
                                : tile.getMaterialType().name(),
                        normalizeGrade(tile.getGrade())
                );
            }

            // العناصر الخارجية أو غير الموجودة حالياً
            // في المخزون: البيانات المحفوظة في الفاتورة.

            return join(
                    item.getCode(),
                    item.getName(),
                    normalizeGrade(item.getGrade())
            );
        }

        // الأدوات الصحية: الاسم والنخب، كما في Account.
        return join(
                item.getName(),
                normalizeGrade(item.getGrade())
        );
    }

    private static String getItemType(
            InvoiceItem item,
            SanitaryService sanitaryService
    ) {

        if (item.isTile()) {
            return "بلاط";
        }

        SanitaryItem sanitary =
                item.getIdNumber() > 0
                        && sanitaryService != null
                        ? sanitaryService.findById(
                        item.getIdNumber()
                )
                        : null;

        if (sanitary != null) {
            return sanitary.getTypeName();
        }

        return item.isExternalItem()
                ? "أداة خارجية"
                : "أداة صحية";
    }

    private static String normalizeGrade(
            String grade
    ) {

        String value = safe(grade).trim();

        return switch (value) {
            case "1" -> "اول";
            case "2" -> "ثاني";
            case "3" -> "ثالث";
            case "4" -> "رابع";
            case "5" -> "خامس";
            default -> value;
        };
    }

    private static String join(
            String... values
    ) {

        StringBuilder result =
                new StringBuilder();

        for (String value : values) {

            if (value == null || value.isBlank()) {
                continue;
            }

            if (!result.isEmpty()) {
                result.append(" ");
            }

            result.append(value.trim());
        }

        return result.toString();
    }

    // =====================================================
    // الإجماليات
    // =====================================================

    private static void drawInvoiceTotals(
            PDPageContentStream content,
            PDType0Font font,
            Invoice invoice,
            float y
    ) throws Exception {

        double total = invoice.getTotal();
        double discount = invoice.getDiscount();

        double beforeDiscount = total + discount;

        String[] lines = {
                "الإجمالي: "
                        + formatMoneyUsd(beforeDiscount),

                "الحسم: "
                        + formatMoneyUsd(discount),

                "مجموع الدفعات: "
                        + formatMoneyUsd(
                        invoice.getPaymentsTotal()
                ),

                "الباقي: "
                        + formatMoneyUsd(
                        invoice.getRemaining()
                )
        };

        for (String line : lines) {

            ArabicTextRenderer.drawRight(
                    content,
                    font,
                    line,
                    RIGHT,
                    y,
                    11
            );

            y -= 20;
        }
    }

    // =====================================================
    // صفحة الدفعات المستقلة
    // =====================================================

    private static void drawPaymentPages(
            PDDocument document,
            PDType0Font font,
            Invoice invoice
    ) throws Exception {

        List<Payment> payments =
                invoice.getPayments();

        int index = 0;
        int pageNumber = 0;

        do {

            pageNumber++;

            PDPage page =
                    new PDPage(PDRectangle.A4);

            document.addPage(page);

            try (PDPageContentStream content =
                         new PDPageContentStream(
                                 document,
                                 page
                         )) {

                float y = PAGE_HEIGHT - 55;

                ArabicTextRenderer.drawCentered(
                        content,
                        font,
                        "دفعات الفاتورة",
                        PAGE_WIDTH / 2f,
                        y,
                        22
                );

                y -= 30;

                ArabicTextRenderer.drawRight(
                        content,
                        font,
                        "العميل: "
                                + safe(invoice.getCustomerName()),
                        RIGHT,
                        y,
                        13
                );

                y -= 22;

                ArabicTextRenderer.drawRight(
                        content,
                        font,
                        "رقم الفاتورة: "
                                + safe(invoice.getInvoiceNumber()),
                        RIGHT,
                        y,
                        13
                );

                y -= 30;

                // ترتيب الأعمدة من اليمين إلى اليسار:
                // التاريخ، سعر الصرف، قيمة الدفعة،
                // العملة، القيمة بالدولار.
                //
                // العمود الثالث: قيمة الدفعة.
                // العمود الخامس الأيسر: القيمة بالدولار.

                float[] widths = {
                        120, 85, 100, 75, 155
                };

                String[] headers = {
                        "تاريخ الدفعة",
                        "سعر الصرف",
                        "قيمة الدفعة",
                        "العملة",
                        "القيمة بالدولار"
                };

                y = drawRow(
                        content,
                        font,
                        headers,
                        widths,
                        y,
                        true
                );

                if (payments.isEmpty()) {

                    drawRow(
                            content,
                            font,
                            new String[]{
                                    "-", "-", "لا توجد دفعات",
                                    "-", "-"
                            },
                            widths,
                            y,
                            false
                    );

                } else {

                    while (
                            index < payments.size()
                                    && y > 100
                    ) {

                        Payment payment =
                                payments.get(index);

                        String date =
                                payment.getDate() == null
                                        ? ""
                                        : DATE_FORMAT.format(
                                        payment.getDate()
                                );

                        String[] row = {
                                date,

                                formatMoney(
                                        payment.getExchangeRate()
                                ),

                                formatMoney(
                                        payment.getAmount()
                                ),

                                payment.getCurrency(),

                                formatMoneyUsd(
                                        payment.getDollarAmount()
                                )
                        };

                        y = drawRow(
                                content,
                                font,
                                row,
                                widths,
                                y,
                                false
                        );

                        index++;
                    }
                }

                drawFooter(
                        content,
                        font,
                        pageNumber
                );
            }

        } while (index < payments.size());
    }

    // =====================================================
    // رسم الصفوف والخلايا
    // =====================================================

    private static float drawRow(
            PDPageContentStream content,
            PDType0Font font,
            String[] values,
            float[] widths,
            float y,
            boolean header
    ) throws Exception {

        float x = RIGHT;
        float height = ROW_HEIGHT;

        for (int i = 0; i < widths.length; i++) {

            float width = widths[i];
            x -= width;

            content.addRect(
                    x,
                    y - height,
                    width,
                    height
            );

            content.stroke();

            ArabicTextRenderer.drawCenteredInCell(
                    content,
                    font,
                    safe(values[i]),
                    x,
                    width,
                    y - height / 2f - 4,
                    header ? 9 : 8
            );
        }

        return y - height;
    }

    private static void drawFooter(
            PDPageContentStream content,
            PDType0Font font,
            int pageNumber
    ) throws Exception {

        ArabicTextRenderer.drawCentered(
                content,
                font,
                "صفحة " + pageNumber,
                PAGE_WIDTH / 2f,
                25,
                9
        );
    }
    private static String formatMoneyUsd(double value) {

        return formatMoney(value) + " $";
    }
    private static String formatMoney(
            double value
    ) {

        return String.format(
                Locale.US,
                "%,.2f",
                value
        );
    }

    private static String formatNumber(
            double value
    ) {

        if (value == Math.rint(value)) {

            return String.format(
                    Locale.US,
                    "%.0f",
                    value
            );
        }

        return String.format(
                Locale.US,
                "%.2f",
                value
        );
    }

    private static String safe(
            String value
    ) {

        return value == null ? "" : value;
    }
}