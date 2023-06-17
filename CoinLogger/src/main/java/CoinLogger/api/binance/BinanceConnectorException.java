package CoinLogger.api.binance;

public class BinanceConnectorException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BinanceConnectorException(String fullErrMsg) {
        super(fullErrMsg);
    }

}
