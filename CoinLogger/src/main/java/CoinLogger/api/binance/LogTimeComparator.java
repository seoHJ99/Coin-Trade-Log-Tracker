package CoinLogger.api.binance;

import java.util.Comparator;

public class LogTimeComparator implements Comparator<LogDto_Binance> {

    @Override
    public int compare(LogDto_Binance o1, LogDto_Binance o2) {
        if (o1.getOrderTime().isAfter( o2.getOrderTime())) {
            return -1;
        } else if(o1.getOrderTime().isBefore( o2.getOrderTime())) {
            return 1;
        }else {
            return 0;
        }
    }
}
