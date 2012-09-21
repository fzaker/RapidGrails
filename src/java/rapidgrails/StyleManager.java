package rapidgrails;

import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.StyleBuilder;
import ar.com.fdvs.dj.domain.constants.*;
//import org.apache.poi.hwpf.usermodel.BorderCode;
//import org.apache.poi.ss.usermodel.BorderStyle;
import java.awt.Color;


/**
 * Author: Farshid Zaker
 * Shayen Information Techology
 * 1/16/11 - 10:14 AM
 */
public class StyleManager {
    public static Style getHeaderStyle() {
        Font headerFont = new Font(12, "Tahoma","/fonts/TAHOMA.TTF", Font.PDF_ENCODING_Identity_H_Unicode_with_horizontal_writing, true);

        Style headerStyle = new Style();
        headerStyle.setBorderTop(Border.PEN_1_POINT);
        headerStyle.setBorderLeft(Border.PEN_1_POINT);
        headerStyle.setBorderRight(Border.PEN_1_POINT);
        Color headerBackColor = new Color(75, 172, 198);
        headerStyle.setBackgroundColor(headerBackColor);
        headerStyle.setBorderColor(headerBackColor);
        headerStyle.setTextColor(Color.white);
        headerStyle.setTransparency(Transparency.OPAQUE);
        headerStyle.setHorizontalAlign(HorizontalAlign.CENTER);
        headerStyle.setVerticalAlign(VerticalAlign.MIDDLE);
        headerStyle.setFont(headerFont);

        return headerStyle;
    }

    public static Style getTitleStyle() {
        Font title_font = new Font(16, "Tahoma","/fonts/TAHOMA.TTF", Font.PDF_ENCODING_Identity_H_Unicode_with_horizontal_writing, true);
        Style titleStyle = new StyleBuilder(false)
                .setFont(title_font)
                .setHorizontalAlign(HorizontalAlign.CENTER)
                .setVerticalAlign(VerticalAlign.BOTTOM)
                .setPaddingBottom(5)
                .setPaddingTop(20)
                .build();
        return titleStyle;
    }

    public static Style getSubtitleStyle() {
        Font title_font = new Font(11, "Tahoma","/fonts/TAHOMA.TTF", Font.PDF_ENCODING_Identity_H_Unicode_with_horizontal_writing, true);
        Style titleStyle = new StyleBuilder(true)
                .setFont(title_font)
                .setHorizontalAlign(HorizontalAlign.CENTER)
                .setVerticalAlign(VerticalAlign.MIDDLE)
                .setPaddingBottom(10)
                .setPaddingTop(10)
                .build();
        return titleStyle;
    }

    public static Style getColumnStyle() {
        Font column_font = new Font(10, "Tahoma","/fonts/TAHOMA.TTF", Font.PDF_ENCODING_Identity_H_Unicode_with_horizontal_writing, true);
        Style columnStyle = new StyleBuilder(false)
                .setFont(column_font)
                .setBorderBottom(Border.PEN_1_POINT).setBorderTop(Border.PEN_1_POINT).setBorderLeft(Border.PEN_1_POINT).setBorderRight(Border.PEN_1_POINT)
                .setBorderColor(new Color(75, 172, 198))
                .setHorizontalAlign(HorizontalAlign.RIGHT)
                .setPaddingRight(10)
                .build();
        columnStyle.setBlankWhenNull(true);
        return columnStyle;
    }

    public static Style getOddRowStyle() {
        Style oddRowStyle = new Style();
        oddRowStyle.setBorder(Border.NO_BORDER);
        Color veryLightGrey = new Color(0xF7, 0xF7, 0xF7);
        oddRowStyle.setBackgroundColor(veryLightGrey);
        oddRowStyle.setTransparency(Transparency.OPAQUE);
        return oddRowStyle;
    }
}
