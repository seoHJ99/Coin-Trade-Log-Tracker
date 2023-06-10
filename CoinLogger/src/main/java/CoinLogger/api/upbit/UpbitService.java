package CoinLogger.api.upbit;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpbitService {

    private final HttpClient httpClient;



    public String getAccounts() {
        String accessKey = (""); // 받아오기
        String secretKey = (""); // 받아오기
        String serverUrl = ("https://api.upbit.com");

        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        String jwtToken = JWT.create()
                .withClaim("access_key", accessKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .sign(algorithm);

        String authenticationToken = "Bearer " + jwtToken;

        try {
            HttpGet request = new HttpGet(serverUrl + "/v1/accounts");
            request.setHeader("Content-Type", "application/json");
            request.addHeader("Authorization", authenticationToken);
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return "GetAccounts 오류";
        }
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
