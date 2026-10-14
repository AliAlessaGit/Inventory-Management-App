package backEnd.account.pdf;

import backEnd.account.Account;
import backEnd.account.AccountEntry;
import backEnd.account.AccountInvoice;
import backEnd.account.AccountInvoiceType;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public class AccountInvoicePdfExporter {

    private static final float PAGE_WIDTH =
            PDRectangle.A4.getWidth();

    private static final float PAGE_HEIGHT =
            PDRectangle.A4.getHeight();

    private static final float MARGIN = 40f;

    private static final float RIGHT =
            PAGE_WIDTH - MARGIN;

    private static final String FONT_RESOURCE =
            "/fonts/NotoNaskhArabic-Regular.ttf";

    /**
     * تصدير سند حساب إلى PDF.
     */
    public static void export(
            Account account,
            AccountInvoice invoice,
            File outputFile
    ) throws Exception {

        if (account == null) {
            throw new IllegalArgumentException(
                    "الحساب مطلوب"
            );
        }

        if (invoice == null) {
            throw new IllegalArgumentException(
                    "السند مطلوب"
            );
        }

        if (outputFile == null) {
            throw new IllegalArgumentException(
                    "ملف الإخراج مطلوب"
            );
        }

        try (PDDocument document =
                     new PDDocument()) {

            PDType0Font font =
                    loadArabicFont(document);

            PDPage page =
                    new PDPage(
                            PDRectangle.A4
                    );

            document.addPage(page);

            drawInvoice(
                    document,
                    page,
                    font,
                    account,
                    invoice
            );

            document.save(outputFile);
        }
    }

    /**
     * تحميل الخط العربي من resources.
     */
    private static PDType0Font loadArabicFont(
            PDDocument document
    ) throws IOException {

        InputStream stream =
                AccountInvoicePdfExporter.class
                        .getResourceAsStream(
                                FONT_RESOURCE
                        );

        if (stream == null) {
            throw new IOException(
                    "لم يتم العثور على الخط العربي:\n"
                            + FONT_RESOURCE
                            + "\n\n"
                            + "تأكد من أن resources "
                            + "مُعرّف كـ Resources Root."
            );
        }

        return PDType0Font.load(
                document,
                stream
        );
    }

    /**
     * رسم السند.
     */
    private static void drawInvoice(
            PDDocument document,
            PDPage page,
            PDType0Font font,
            Account account,
            AccountInvoice invoice
    ) throws Exception {

        float y =
                PAGE_HEIGHT - 55;

        try (PDPageContentStream content =
                     new PDPageContentStream(
                             document,
                             page
                     )) {

            /*
             * ==========================
             * العنوان
             * ==========================
             */

            String title =
                    getTitle(invoice.getType());

            ArabicTextRenderer.drawCentered(
                    content,
                    font,
                    title,
                    PAGE_WIDTH / 2f,
                    y,
                    24
            );

            y -= 45;

            /*
             * ==========================
             * اسم الحساب
             * ==========================
             */

            ArabicTextRenderer.drawRight(
                    content,
                    font,
                    "الحساب: "
                            + account.getName(),
                    RIGHT,
                    y,
                    16
            );

            y -= 28;

            /*
             * ==========================
             * رقم السند
             * ==========================
             */

            ArabicTextRenderer.drawRight(
                    content,
                    font,
                    "رقم السند: "
                            + invoice.getNumber(),
                    RIGHT,
                    y,
                    15
            );

            y -= 25;

            /*
             * التاريخ
             */

            ArabicTextRenderer.drawRight(
                    content,
                    font,
                    "التاريخ: "
                            + invoice.getFormattedDate(),
                    RIGHT,
                    y,
                    15
            );

            y -= 35;

            /*
             * ==========================
             * البيان
             * ==========================
             */

            if (
                    invoice.getDetails() != null
                            && !invoice.getDetails()
                            .trim()
                            .isEmpty()
            ) {

                ArabicTextRenderer.drawRight(
                        content,
                        font,
                        "البيان: "
                                + invoice.getDetails(),
                        RIGHT,
                        y,
                        15
                );

                y -= 35;
            }

            /*
             * ==========================
             * البنود
             * ==========================
             */

            if (invoice.getType().hasEntries()) {

                y =
                        drawEntriesTable(
                                content,
                                font,
                                invoice.getEntries(),
                                y
                        );

            } else {

                /*
                 * PAYMENT / RECEIPT
                 */

                y -= 10;

                drawAmountBox(
                        content,
                        font,
                        invoice,
                        y
                );

                y -= 70;
            }

            /*
             * ==========================
             * الإجمالي
             * ==========================
             */

            y -= 20;

            drawTotalBox(
                    content,
                    font,
                    invoice,
                    y
            );

            /*
             * ==========================
             * تذييل
             * ==========================
             */

            ArabicTextRenderer.drawCentered(
                    content,
                    font,
                    "تم إنشاء هذا المستند بواسطة برنامج العاتقي المطور من قبل المهندس علي العيسى ",
                    PAGE_WIDTH / 2f,
                    35,
                    15
            );
        }
    }

    /**
     * رسم جدول البنود.
     */
    private static float drawEntriesTable(
            PDPageContentStream content,
            PDType0Font font,
            List<AccountEntry> entries,
            float y
    ) throws Exception {

        float tableWidth =
                PAGE_WIDTH
                        - (MARGIN * 2);

        /*
         * من اليمين إلى اليسار:
         *
         * المادة
         * الكمية 2
         * الكمية 1
         * الوحدة
         * السعر
         * الإجمالي
         */
        float materialWidth = 155;
        float unitWidth = 55;
        float quantity1Width = 65;
        float priceWidth = 80;
        float totalWidth = 100;
        float quantity2Width = 60;

        float[] widths = {
                materialWidth,
                unitWidth,
                quantity1Width,
                priceWidth,
                totalWidth,
                quantity2Width
        };

        String[] headers = {
                "الإجمالي",
                "الوحدة",
                "الكمية 1",
                "السعر",
                "الإجمالي",
                "الكمية 2"
        };


        /*
         * ==========================
         * رأس الجدول
         * ==========================
         */

        float x = RIGHT;

        for (int i = 0; i < widths.length; i++) {

            x -= widths[i];

            drawCell(
                    content,
                    font,
                    headers[i],
                    x,
                    y,
                    widths[i],
                    28,
                    true
            );
        }

        y -= 28;

        /*
         * ==========================
         * الصفوف
         * ==========================
         */

        for (AccountEntry entry : entries) {

            if (y < 95) {
                break;
            }

            x = RIGHT;

            // المادة — أقصى اليمين
            x -= materialWidth;

            drawCell(
                    content,
                    font,
                    safe(entry.getMaterial()),
                    x,
                    y,
                    materialWidth,
                    28,
                    false
            );

            // الوحدة
            x -= unitWidth;

            drawCell(
                    content,
                    font,
                    safe(entry.getUnit()),
                    x,
                    y,
                    unitWidth,
                    28,
                    false
            );

            // الكمية 1
            x -= quantity1Width;

            drawCell(
                    content,
                    font,
                    formatNumber(entry.getQuantity1()),
                    x,
                    y,
                    quantity1Width,
                    28,
                    false
            );

            // السعر
            x -= priceWidth;

            drawCell(
                    content,
                    font,
                    formatMoney(entry.getPrice()),
                    x,
                    y,
                    priceWidth,
                    28,
                    false
            );

            // الإجمالي
            x -= totalWidth;

            drawCell(
                    content,
                    font,
                    formatMoney(entry.getTotal()),
                    x,
                    y,
                    totalWidth,
                    28,
                    false
            );

            // الكمية 2 — أقصى اليسار
            x -= quantity2Width;

            drawCell(
                    content,
                    font,
                    formatNumber(entry.getQuantity2()),
                    x,
                    y,
                    quantity2Width,
                    28,
                    false
            );

            y -= 28;
        }
        return y;
    }

    /**
     * رسم خلية.
     */
    private static void drawCell(
            PDPageContentStream content,
            PDType0Font font,
            String text,
            float x,
            float y,
            float width,
            float height,
            boolean header
    ) throws Exception {

        content.addRect(
                x,
                y - height,
                width,
                height
        );

        content.stroke();

        float fontSize =
                header
                        ? 12
                        : 11;

        ArabicTextRenderer.drawCenteredInCell(
                content,
                font,
                text,
                x,
                width,
                y - 18,
                fontSize
        );
    }

    /**
     * مربع المبلغ للدفع والقبض.
     */
    private static void drawAmountBox(
            PDPageContentStream content,
            PDType0Font font,
            AccountInvoice invoice,
            float y
    ) throws Exception {

        float width = 260;
        float height = 50;

        float x =
                (PAGE_WIDTH - width)
                        / 2f;

        content.addRect(
                x,
                y - height,
                width,
                height
        );

        content.stroke();

        String label =
                invoice.isPayment()
                        ? "مبلغ الدفع"
                        : "مبلغ القبض";

        ArabicTextRenderer.drawCentered(
                content,
                font,
                label
                        + ": "
                        + formatMoney(
                        invoice.getAmount()
                ),
                PAGE_WIDTH / 2f,
                y - 30,
                16
        );
    }

    /**
     * مربع الإجمالي.
     */
    private static void drawTotalBox(
            PDPageContentStream content,
            PDType0Font font,
            AccountInvoice invoice,
            float y
    ) throws Exception {

        float width = 300;
        float height = 50;

        float x =
                PAGE_WIDTH
                        - MARGIN
                        - width;

        content.addRect(
                x,
                y - height,
                width,
                height
        );

        content.stroke();

        double total =
                invoice.getTotalDebit()
                        + invoice.getTotalCredit();

        ArabicTextRenderer.drawCentered(
                content,
                font,
                "الإجمالي: "
                        + formatMoney(total),
                x + width / 2f,
                y - 30,
                17
        );
    }

    /**
     * عنوان المستند.
     */
    private static String getTitle(
            AccountInvoiceType type
    ) {

        if (type == null) {
            return "سند حساب";
        }

        switch (type) {

            case PURCHASE:
                return "سند شراء";

            case SALE:
                return "سند مبيع";

            case PAYMENT:
                return "سند دفع";

            case RECEIPT:
                return "سند قبض";

            default:
                return "سند حساب";
        }
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

        return value == null
                ? ""
                : value;
    }
}