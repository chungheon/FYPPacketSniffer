package fyp.com.packetsniffer;

import java.io.CharArrayReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PacketDate {

    public static Calendar getMinRange(int range, Calendar date){
        Calendar temp = null;
        switch (range){
            //10 min range
            case 1:
                    break;
            //30 min range
            case 2:
                    break;
            //1 hour range
            case 3: temp = Calendar.getInstance();
                    temp.setTime(date.getTime());
                    temp.set(Calendar.MINUTE, 0);
                    temp.set(Calendar.SECOND, 0);
                    temp.set(Calendar.MILLISECOND, 0);
                    break;
        }
        return temp;
    }

    public static Calendar getMaxRange(int range, Calendar date){
        Calendar temp = date;
        switch (range){
            //10 min range
            case 1:
                break;
            //30 min range
            case 2:
                break;
            //1 hour range
            case 3: temp = Calendar.getInstance();
                temp.setTime(date.getTime());
                temp.set(Calendar.MINUTE, 0);
                temp.set(Calendar.SECOND, 0);
                temp.set(Calendar.MILLISECOND, 0);
                temp.add(Calendar.HOUR, 1);
                break;
        }
        return temp;
    }

    public static Calendar nextDay(Calendar date){
        Calendar temp = date;
        temp.set(Calendar.HOUR, 0);
        temp.set(Calendar.MINUTE, 0);
        temp.set(Calendar.SECOND, 0);
        temp.set(Calendar.MILLISECOND, 0);
        temp.set(Calendar.AM_PM, Calendar.AM);
        temp.add(Calendar.DATE, 1);
        return temp;
    }

    public static String getString(Calendar date){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh");
        String strDate = dateFormat.format(date.getTime());
        return strDate;
    }

}
