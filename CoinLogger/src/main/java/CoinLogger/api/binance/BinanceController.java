package CoinLogger.api.binance;

import CoinLogger.api.coinone.CoinoneService;
import com.zaxxer.hikari.pool.HikariProxyCallableStatement;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;


@Controller
public class BinanceController {


    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        String secretKey = "o3qjLnuwzNFkh2RQRr8wRCpR8pYqMC1Dt7fF1B9T6xggn73ksNAQSmBUTeeRzpUL";
        String accessKey = "lTJrdh5xuwUhaeUiYHNP3pMYP7MEPKoDM3h9L4Ka6jy7VZiBlKXPbHKNDh7UuFME";
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secKey = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
        hmacSha256.init(secKey);

        String actualSign = new String(Hex.encodeHex(hmacSha256.doFinal()));
        secretKey = "&signature=" + actualSign;
        String timestamp = Long.toString(System.currentTimeMillis());
        timestamp = "timestamp=" + timestamp;
        String serverUrl = "https://api.binance.com";

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(serverUrl + "/sapi/v1/capital/config/getall?"+ timestamp + secretKey);
        request.addHeader("Content-Type", "application/json");
        request.addHeader("X-MBX-APIKEY", accessKey);

        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();;

        String entityString = EntityUtils.toString(entity, "UTF-8");
        System.out.println(entityString);

    }
}
