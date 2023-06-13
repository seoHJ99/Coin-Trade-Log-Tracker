package CoinLogger.api.coinone;

import lombok.RequiredArgsConstructor;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CoinoneController {

    private final CoinoneService coinoneService;

    @GetMapping("/coinone/{id}/accounts")
    @ResponseBody
    public String getAccount() throws ParseException {
        List<AccountDto_Coinone> dtoList = new ArrayList<>();
        dtoList = coinoneService.accountDtoMaker();
      return dtoList.toString();
    }
}
