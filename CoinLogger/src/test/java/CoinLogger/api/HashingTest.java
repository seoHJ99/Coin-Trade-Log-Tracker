package CoinLogger.api;

import org.junit.Assert;

import javax.crypto.Mac;

import static org.junit.Assert.*;

public class HashingTest {
    public void testEmptyInputs() throws Exception {
        String knownOutput = "8cbf764cbe2e4623d99a41354adfd390";
        Mac mac = Mac.getInstance("HmacMD5");
        mac.init(MD5_KEY);
        assertEquals(knownOutput, HashCode.fromBytes(mac.doFinal()).toString());
        assertEquals(knownOutput, Hashing.hmacMd5(MD5_KEY).newHasher().hash().toString());
    }
}
