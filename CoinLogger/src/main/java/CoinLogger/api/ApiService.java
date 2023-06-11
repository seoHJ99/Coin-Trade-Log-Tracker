package CoinLogger.api;

import org.apache.http.client.HttpClient;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.Map;

public interface ApiService {
    HttpClient httpClient = null;

    Map getAccounts () throws IOException;

    // 코인 이름은 ,으로 구분
    String getNowPrice(String coinName);
}
