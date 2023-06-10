package CoinLogger.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HttpSender {
    private final HttpClient httpClient;
    private String serverUrl;
    private String apiRequest;

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void setApiRequest(String apiRequest) {
        this.apiRequest = apiRequest;
    }

    public HttpResponse sendApi() {
        HttpResponse httpResponse = null;
        try {
            HttpGet request = new HttpGet(serverUrl + apiRequest);
            request.setHeader("Content-Type", "application/json");
            httpResponse = httpClient.execute(request);
        }catch (IOException e){
            System.out.println("sendApi 에러 발생");
        }
        return httpResponse;
    }

    public HttpResponse sendApi(String accessKey, String secretKey) {

        //String accessKey = ("jaGJ8xxzTrxrqJcMPxwGxeAstH38fjXRAYNemMal"); // 받아오기
        //String secretKey = ("N8qFgecKx5B9s7HJJHexrADEDlT2znPSgTYENQCD"); // 받아오기
        HttpResponse httpResponse = null;
        try {
            HttpGet request = new HttpGet(serverUrl + apiRequest);
            request.setHeader("Content-Type", "application/json");

            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            String jwtToken = JWT.create()
                    .withClaim("access_key", accessKey)
                    .withClaim("nonce", UUID.randomUUID().toString())
                    .sign(algorithm);

            String authenticationToken = "Bearer " + jwtToken;
            request.addHeader("Authorization", authenticationToken);
             httpResponse = httpClient.execute(request);

        }catch (IOException e){
            System.out.println("sendApi 키 포함 메서드 에러 발생");
        }

        return httpResponse;
    }
}
