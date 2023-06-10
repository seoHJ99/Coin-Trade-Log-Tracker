package CoinLogger.api.upbit;


import CoinLogger.api.ApiService;
import CoinLogger.api.HttpSender;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpbitService implements ApiService {

    private final HttpClient httpClient;
    private final HttpSender httpSender;

    @Override
    public String getAccounts() {
        String accessKey = (""); // 받아오기
        String secretKey = (""); // 받아오기
        String serverUrl = ("https://api.upbit.com");

        httpSender.setApiRequest("https://api.upbit.com");
        httpSender.setApiRequest("/v1/accounts");

        HttpResponse response = httpSender.sendApi(accessKey, secretKey);
        // response를 Map 형태로 변환하는 함수 제작 필요

        return "aaaaa" ;
    }

    public String getCoinList() {
        try {
            HttpGet request = new HttpGet("https://api.upbit.com/v1/market/all?isDetails=false");
            request.setHeader("accept", "application/json");
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            String list = EntityUtils.toString(entity, "UTF-8");
            return list;
        }catch (IOException e){
            System.out.println(e);
            return "getCoinList 오류";
        }
    }

    // 코인 이름은 ,으로 구분
    @Override
    public String getNowPrice(String coinName)  {
        try {
            HttpGet request = new HttpGet("https://api.upbit.com/v1/ticker?markets=%20" + coinName);
            request.setHeader("accept", "application/json");
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            String list = EntityUtils.toString(entity, "UTF-8");
            return list;
        }catch (IOException e){
            return "geNowPrice 오류";
        }
    }
}
