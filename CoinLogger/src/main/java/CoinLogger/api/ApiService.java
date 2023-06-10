package CoinLogger.api;

import org.apache.http.client.HttpClient;

public interface ApiService {
    HttpClient httpClient = null;

    String getAccounts ();

    // 코인 이름은 ,으로 구분
    String getNowPrice(String coinName);
}
