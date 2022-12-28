package com.bootcamp.java.tarjetacredito.common;

import lombok.extern.slf4j.Slf4j;

import java.util.Calendar;
import java.util.Date;

@Slf4j
public class Funciones {
    public static Date GetFirstDayOfCurrentMonth(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        // set day to minimum
        calendar.set(Calendar.DAY_OF_MONTH,
                calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        log.info("Calendar date: " + calendar.getTime());
        return calendar.getTime();
    }
    //Integer billingDay
    public static Date GetBillingDate(Integer billingDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1 );
        var year = calendar.get(Calendar.YEAR);
        var month = calendar.get(Calendar.MONTH);

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DATE, billingDay);

        return calendar.getTime();
    }
    public static Date GetCurrentDate(){
        Date currentDate = new Date();
        return currentDate;
    }
}
