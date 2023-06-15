package CoinLogger.api.coinone;

import CoinLogger.api.upbit.AccountDto_Upbit;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class CoinoneController {

    private final CoinoneService coinoneService;

    @GetMapping("/coinone/{id}/accounts")
    public String getAccount(Model model) throws ParseException {
        List<AccountDto_Coinone> dtoList =  coinoneService.accountDtoMaker();
        Map<String, String> secondData = new HashMap<>();
        int totalBuyPrice = 0;
        int totalNowPrice = 0;
        int totalEarning = 0;
        double avgRate = 0;
        for(AccountDto_Coinone dto : dtoList){
            totalBuyPrice += (int) (dto.getBuyPrice() * dto.getOwnAmount());
            totalEarning += dto.getEarning();
            totalNowPrice += dto.getSumNowPrice();
        }
        avgRate = (int)((double)totalEarning/totalBuyPrice * 10000d)/100d;
        secondData.put("totalBuyPrice", totalBuyPrice + "");
        secondData.put("totalNowPrice", totalNowPrice + "");
        secondData.put("totalEarning", totalEarning + "");
        secondData.put("avgRate", avgRate + "");
        model.addAttribute("data", dtoList);
        model.addAttribute("secondData", secondData);
      return "AccountsListPage";
    }

}
