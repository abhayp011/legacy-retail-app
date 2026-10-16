package com.acme.retail.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    // shared formatter (SimpleDateFormat is not thread-safe)
    private static final SimpleDateFormat DAY = new SimpleDateFormat("yyyy-MM-dd");

    public static String today() {
        return DAY.format(new Date());
    }

    public static String compact() {
        return new SimpleDateFormat("yyyyMMdd").format(new Date());
    }

    public static java.sql.Date plusDays(int days) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, days);
        return new java.sql.Date(c.getTimeInMillis());
    }

    public static java.sql.Date parse(String yyyyMMdd) throws ParseException {
        return new java.sql.Date(DAY.parse(yyyyMMdd).getTime());
    }
}
