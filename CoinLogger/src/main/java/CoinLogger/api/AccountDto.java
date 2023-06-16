package CoinLogger.api;


public interface AccountDto {
    String coinName = null;
    double ownAmount= 0;
    String bigAmount= null;
    double nowPrice= 0;
    String bigNow= null;
    double buyPrice= 0;
    String bigBuy= null;
    int sumNowPrice= 0;
    double sumBuyPrice= 0;
    int earning= 0;
    double rateOfReturn= 0;
    String trader = null;

    String getCoinName();
    double getOwnAmount();
    String getBigAmount();
    double getNowPrice();
    String getBigNow();
    double getBuyPrice();
    String getBigBuy();
    int getSumNowPrice();
    double getSumBuyPrice();
    int getEarning();
    double getRateOfReturn();


}
