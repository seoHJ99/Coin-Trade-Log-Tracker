package CoinLogger.api.binance;

import CoinLogger.PublicMethod;
import CoinLogger.api.coinone.CoinoneService;
import com.zaxxer.hikari.pool.HikariProxyCallableStatement;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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
@RequiredArgsConstructor
public class BinanceController {



    public static void main(String[] args) throws IOException, ParseException {
        String secretKey = "";
        String accessKey = "";

        HmacSignatureGenerator signature = new HmacSignatureGenerator(secretKey);

        String timestamp = Long.toString(System.currentTimeMillis());
        String queryString = "timestamp=" + timestamp;
        String actualSign = signature.getSignature(queryString);

        String serverUrl = "https://api.binance.com";

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(serverUrl + "/sapi/v3/asset/getUserAsset?" + queryString + "&signature="+actualSign);
        System.out.println(signature.getSignature(secretKey));
        request.addHeader("Content-Type", "application/json");
        request.addHeader("X-MBX-APIKEY", accessKey);


        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();;

        String entityString = EntityUtils.toString(entity, "UTF-8");
        System.out.println(entityString);
        JSONParser jsonParser = new JSONParser();
//        JSONArray jsonArray = (JSONArray) jsonParser.parse(entityString);

//        PublicMethod publicMethod = new PublicMethod();
//        String a = "";
//        for(int i =0; i<jsonArray.size(); i++){
//            JSONObject jsonObject =(JSONObject) jsonParser.parse(jsonArray.get(i).toString());
////            a = jsonObject.get("networkList").toString();
//            a += jsonObject.toJSONString();
//        }
//        System.out.println(a);
//        publicMethod.jsonToList(a);
//
//
    }
}
