package rapidgrails;

import fi.joensuu.joyds1.calendar.JalaliCalendar;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Author: Farshid Zaker
 * Shayen Information Techology
 * 2/6/11 - 11:41 PM
 */

public class JalaliDateFormat extends DateFormat {
    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.getInstance();
        cal.setTime(date);

        JalaliCalendar jc = new JalaliCalendar(cal);
        StringBuilder sb;
        toAppendTo.append(String.format("%04d/%02d/%02d", jc.getYear(), jc.getMonth(), jc.getDay()));
        return toAppendTo;
    }

    public Date parse(String source, ParsePosition parsePosition) {
        return null;
    }

    @Override
    public Date parse(String source) {
        try {
            String[] dates = source.split("/");
            JalaliCalendar jc = new JalaliCalendar();
            jc.set(Integer.parseInt(dates[0]), Integer.parseInt(dates[1]), Integer.parseInt(dates[2]));
            return jc.toJavaUtilGregorianCalendar().getTime();
        } catch (Exception x) {
            return null;
        }
    }
}
