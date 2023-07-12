package CoinLogger.api.binance;

import CoinLogger.api.coinone.AccountDto_Coinone;
import lombok.RequiredArgsConstructor;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequiredArgsConstructor
public class BinanceController {
    private final BinanceService binanceService;

    @GetMapping("/binance/{id}/account")
    public String getAccount(Model model) throws IOException, ParseException {
        List<AccountDto_Binance> dtoList = binanceService.makeAccountDto();
        Map<String, String> secondData = new HashMap<>();
        int totalBuyPrice = 0;
        int totalNowPrice = 0;
        int totalEarning = 0;

        for(AccountDto_Binance dto : dtoList){
            totalBuyPrice += (int) (dto.getBuyPrice() * dto.getOwnAmount());
            totalEarning += dto.getEarning();
            totalNowPrice += dto.getSumNowPrice();
        }
        double avgRate = (int)((double)totalEarning/totalBuyPrice * 10000d)/100d;
        secondData.put("totalBuyPrice", totalBuyPrice + "");
        secondData.put("totalNowPrice", totalNowPrice + "");
        secondData.put("totalEarning", totalEarning + "");
        secondData.put("avgRate", avgRate + "");
        model.addAttribute("data", dtoList);
        model.addAttribute("secondData", secondData);
        return "AccountsListPage";
    }

    @GetMapping("/binance/wallet")
    public String makeAvgPriceAndSave(Model model) throws IOException, ParseException {
        List<String> coinName=binanceService.getMyCoinName();
        model.addAttribute("coinName", coinName);
        return "binanceCoin";
    }

    @GetMapping("/binance/{id}/all-trade-log")
    public String getLog(Model model) throws ParseException, IOException {
        model.addAttribute("log", binanceService.getAllCoinLog());
        return "LogListPage";
    }

    @PostMapping("/test")
    @ResponseBody
    public String saveWalletInfo(@RequestBody Map data){
        // id나중에
        String id =  "";
        //
        binanceService.saveWalletInfo(data);

        return "1";
    }
}
