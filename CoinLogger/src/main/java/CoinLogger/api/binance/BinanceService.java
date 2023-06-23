package CoinLogger.api.binance;

import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class BinanceService {
    private final HttpClient httpClient;
    private final JSONParser jsonParser;
    public void getAccountCoin() throws IOException {
        String secretKey = "";
        String accessKey = "";

        HmacSignatureGenerator signature = new HmacSignatureGenerator(secretKey);

        String timestamp = Long.toString(System.currentTimeMillis());
        String queryString = "timestamp=" + timestamp;
        String actualSign = signature.getSignature(queryString);

        String serverUrl = "https://api.binance.com";

        HttpPost request = new HttpPost(serverUrl + "/sapi/v3/asset/getUserAsset?" + queryString + "&signature="+actualSign);
        request.addHeader("Content-Type", "application/json");
        request.addHeader("X-MBX-APIKEY", accessKey);

        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();;

        String entityString = EntityUtils.toString(entity, "UTF-8");
        System.out.println(entityString);
    }
}
