package CoinLogger.api.binance;

public interface SignatureGenerator {
    String getSignature(String payload);
}
