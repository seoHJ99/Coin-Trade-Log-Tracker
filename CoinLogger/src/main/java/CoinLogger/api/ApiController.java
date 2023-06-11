package CoinLogger.api;


import CoinLogger.api.upbit.UpbitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SimpleTimeZone;

@RestController
@RequiredArgsConstructor
public class ApiController {
    private final UpbitService upbit;
    @GetMapping("/upbit/{id}/all-accounts")
    public Map getAllAccounts() throws IOException {
        upbit.getNowPrice("a");
        return upbit.getAccounts();
    }

}
