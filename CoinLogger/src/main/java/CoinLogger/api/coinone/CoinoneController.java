package CoinLogger.api.coinone;

import CoinLogger.PublicMethod;
import CoinLogger.api.upbit.AccountDto;
import CoinLogger.api.upbit.LogDto;
import lombok.*;
import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequiredArgsConstructor
public class CoinoneController {

    private final CoinoneService coinoneService;

    @GetMapping("/coinone/account")
    public String getAccount(Model model) throws ParseException {
        if(coinoneService.getKeys()) {
            List<AccountDto> dtoList = coinoneService.accountDtoMaker();
            Map<String, String> secondData = new HashMap<>();
            int totalBuyPrice = 0;
            int totalNowPrice = 0;
            int totalEarning = 0;

            for (AccountDto dto : dtoList) {
                totalBuyPrice += (int) (dto.getBuyPrice() * dto.getOwnAmount());
                totalEarning += dto.getEarning();
                totalNowPrice += dto.getSumNowPrice();
            }
            double avgRate = (int) ((double) totalEarning / totalBuyPrice * 10000d) / 100d;
            secondData.put("totalBuyPrice", totalBuyPrice + "");
            secondData.put("totalNowPrice", totalNowPrice + "");
            secondData.put("totalEarning", totalEarning + "");
            secondData.put("avgRate", avgRate + "");

            model.addAttribute("data", dtoList);
            model.addAttribute("secondData", secondData);
        }else {
            model.addAttribute("data", null);
            model.addAttribute("secondData", null);
        }
      return "AccountsListPage";
    }

    @GetMapping("/coinone/all-trade-log")
    public Object getLog(Model model) throws ParseException {
        if(coinoneService.getKeys()) {
            List<LogDto> result = coinoneService.getAllLog();
            if(result.size() == 1 && result.get(0).getState().contains("error")){
                return ResponseEntity.ok("<script>" +
                        "alert('"+result.get(0).getState() + "');" +
                        "history.back();" +
                        "</script>");
            }
            model.addAttribute("log", result);
        }else {
            model.addAttribute("log", null);
        }
        return "LogListPage";
    }
}
