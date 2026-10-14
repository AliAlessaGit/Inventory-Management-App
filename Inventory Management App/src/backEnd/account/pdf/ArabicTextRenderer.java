package backEnd.account.pdf;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.Bidi;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

public final class ArabicTextRenderer {

    private ArabicTextRenderer() {
    }

    /**
     * تجهيز النص العربي:
     *
     * Arabic shaping
     * +
     * Bidirectional processing
     */
    public static String prepare(String text)
            throws Exception {

        if (text == null || text.isEmpty()) {
            return "";
        }

        ArabicShaping shaping =
                new ArabicShaping(
                        ArabicShaping.LETTERS_SHAPE
                );

        String shaped =
                shaping.shape(text);

        Bidi bidi =
                new Bidi(
                        shaped,
                        Bidi.DIRECTION_RIGHT_TO_LEFT
                );

        return bidi.writeReordered(0);
    }

    /**
     * حساب عرض النص بعد المعالجة.
     */
    public static float getTextWidth(
            PDType0Font font,
            String text,
            float fontSize
    ) throws Exception {

        String visualText =
                prepare(text);

        return font.getStringWidth(visualText)
                / 1000f
                * fontSize;
    }

    /**
     * رسم نص بمحاذاة اليمين.
     *
     * rightX = الحد الأيمن للنص.
     */
    public static void drawRight(
            PDPageContentStream content,
            PDType0Font font,
            String text,
            float rightX,
            float y,
            float fontSize
    ) throws Exception {

        String visualText =
                prepare(text);

        float width =
                font.getStringWidth(visualText)
                        / 1000f
                        * fontSize;

        float x =
                rightX - width;

        content.beginText();

        content.setFont(
                font,
                fontSize
        );

        content.newLineAtOffset(
                x,
                y
        );

        content.showText(
                visualText
        );

        content.endText();
    }

    /**
     * رسم نص في منتصف منطقة محددة.
     */
    public static void drawCentered(
            PDPageContentStream content,
            PDType0Font font,
            String text,
            float centerX,
            float y,
            float fontSize
    ) throws Exception {

        String visualText =
                prepare(text);

        float width =
                font.getStringWidth(visualText)
                        / 1000f
                        * fontSize;

        float x =
                centerX - (width / 2f);

        content.beginText();

        content.setFont(
                font,
                fontSize
        );

        content.newLineAtOffset(
                x,
                y
        );

        content.showText(
                visualText
        );

        content.endText();
    }

    /**
     * رسم نص داخل خلية جدول مع توسيط أفقي.
     */
    public static void drawCenteredInCell(
            PDPageContentStream content,
            PDType0Font font,
            String text,
            float cellX,
            float cellWidth,
            float y,
            float fontSize
    ) throws Exception {

        drawCentered(
                content,
                font,
                text,
                cellX + cellWidth / 2f,
                y,
                fontSize
        );
    }
}