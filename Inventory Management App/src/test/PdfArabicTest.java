package test;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.Bidi;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.File;

public class PdfArabicTest {

    private static final String FONT_PATH =
            "C:\\Ali\\My Projects\\Inventory-Management-App\\Inventory Management App\\resources\\fonts\\NotoNaskhArabic-Regular.ttf";

    private static final String OUTPUT_PATH =
            "C:\\Ali\\My Projects\\Inventory-Management-App\\Inventory Management App\\arabic-test.pdf";

    public static void main(String[] args) {

        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage();
            document.addPage(page);

            File fontFile = new File(FONT_PATH);

            if (!fontFile.exists()) {
                throw new RuntimeException(
                        "لم يتم العثور على الخط:\n" +
                                fontFile.getAbsolutePath()
                );
            }

            PDType0Font font =
                    PDType0Font.load(document, fontFile);

            try (PDPageContentStream content =
                         new PDPageContentStream(document, page)) {

                // =========================
                // العنوان
                // =========================

                drawCenteredArabicText(
                        content,
                        font,
                        "فاتورة شراء",
                        page,
                        22,
                        720
                );

                // =========================
                // البيانات
                // =========================

                drawRightArabicText(
                        content,
                        font,
                        "رقم المستند: PUR-2026-0015",
                        page,
                        16,
                        670
                );

                drawRightArabicText(
                        content,
                        font,
                        "التاريخ: 29/09/2026",
                        page,
                        16,
                        635
                );

                drawRightArabicText(
                        content,
                        font,
                        "اسم المورد: شركة المثال للتجارة",
                        page,
                        16,
                        600
                );

                drawRightArabicText(
                        content,
                        font,
                        "المبلغ الإجمالي: 1,250.00 EUR",
                        page,
                        16,
                        565
                );

                // =========================
                // English
                // =========================

                drawCenteredArabicText(
                        content,
                        font,
                        "Purchase Invoice - Test",
                        page,
                        16,
                        510
                );

                // =========================
                // نص عربي طويل
                // =========================

                drawCenteredArabicText(
                        content,
                        font,
                        "هذا اختبار لإنشاء ملف PDF باللغة العربية باستخدام PDFBox.",
                        page,
                        16,
                        460
                );
            }

            document.save(OUTPUT_PATH);

            System.out.println(
                    "تم إنشاء الملف بنجاح:\n" +
                            OUTPUT_PATH
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    // معالجة النص العربي
    // ============================================================

    private static String prepareArabicText(String text)
            throws Exception {

        // Arabic shaping
        ArabicShaping shaping =
                new ArabicShaping(
                        ArabicShaping.LETTERS_SHAPE
                );

        String shapedText =
                shaping.shape(text);

        // BiDi
        Bidi bidi =
                new Bidi(
                        shapedText,
                        Bidi.DIRECTION_RIGHT_TO_LEFT
                );

        return bidi.writeReordered(0);
    }

    // ============================================================
    // نص بمحاذاة اليمين
    // ============================================================

    private static void drawRightArabicText(
            PDPageContentStream content,
            PDType0Font font,
            String text,
            PDPage page,
            float fontSize,
            float y
    ) throws Exception {

        String visualText =
                prepareArabicText(text);

        float pageWidth =
                page.getMediaBox().getWidth();

        float margin = 50;

        float textWidth =
                font.getStringWidth(visualText)
                        / 1000f
                        * fontSize;

        float x =
                pageWidth
                        - margin
                        - textWidth;

        content.beginText();

        content.setFont(font, fontSize);

        content.newLineAtOffset(x, y);

        content.showText(visualText);

        content.endText();
    }

    // ============================================================
    // نص في منتصف الصفحة
    // ============================================================

    private static void drawCenteredArabicText(
            PDPageContentStream content,
            PDType0Font font,
            String text,
            PDPage page,
            float fontSize,
            float y
    ) throws Exception {

        String visualText =
                prepareArabicText(text);

        float pageWidth =
                page.getMediaBox().getWidth();

        float textWidth =
                font.getStringWidth(visualText)
                        / 1000f
                        * fontSize;

        float x =
                (pageWidth - textWidth) / 2f;

        content.beginText();

        content.setFont(font, fontSize);

        content.newLineAtOffset(x, y);

        content.showText(visualText);

        content.endText();
    }
}