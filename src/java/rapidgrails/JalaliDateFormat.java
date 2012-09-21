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

public class JalaliDateFormat extends DateFormat{
    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.getInstance();
        cal.setTime(date);

        JalaliCalendar jc = new JalaliCalendar(cal);
        StringBuilder sb;
        toAppendTo.append(String.format("%04d/%02d/%02d", jc.getYear(), jc.getMonth(), jc.getDay()));
        return toAppendTo;
    }

    @Override
    public Date parse(String source, ParsePosition pos) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
