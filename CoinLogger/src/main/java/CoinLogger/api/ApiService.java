package CoinLogger.api;

import org.apache.http.client.HttpClient;

import java.io.IOException;
import java.util.List;


public interface ApiService {


    List<List<String>> getAccounts () throws IOException;

    // 코인 이름은 ,으로 구분
    String getNowPrice(String coinName);
}
