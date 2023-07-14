package CoinLogger.api;

import CoinLogger.CoinSumBuyPriceComparator;
import CoinLogger.api.NameComparator;
import CoinLogger.api.binance.BinanceService;
import CoinLogger.api.binance.LogTimeComparator;
import CoinLogger.api.coinone.CoinoneService;
import CoinLogger.api.upbit.AccountDto;
import CoinLogger.api.upbit.LogDto;
import CoinLogger.api.upbit.UpbitService;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@RequiredArgsConstructor
@Controller
public class AllController {
    private final BinanceService binanceService;
    private final UpbitService upbitService;
    private final CoinoneService coinoneService;

    @GetMapping("/all/{id}/account")
    public String getAllAccount(Model model) throws IOException, ParseException {
        List<AccountDto> coinones = coinoneService.accountDtoMaker();
        List<AccountDto> binances = binanceService.accountDtoMaker();
        List<AccountDto> allAccount =  upbitService.accountDtoMaker();
        allAccount.addAll(coinones);
        allAccount.addAll(binances);
        Collections.sort(allAccount, new CoinSumBuyPriceComparator());

        Map<String, String> secondData = new HashMap<>();
        int totalBuyPrice = 0;
        int totalNowPrice = 0;
        int totalEarning = 0;

        for(AccountDto dto : allAccount){
            totalBuyPrice += (int) (dto.getBuyPrice() * dto.getOwnAmount());
            totalEarning += dto.getEarning();
            totalNowPrice += dto.getSumNowPrice();
        }
        double avgRate = (int)((double)totalEarning/totalBuyPrice * 10000d)/100d;
        secondData.put("totalBuyPrice", totalBuyPrice + "");
        secondData.put("totalNowPrice", totalNowPrice + "");
        secondData.put("totalEarning", totalEarning + "");
        secondData.put("avgRate", avgRate + "");

        model.addAttribute("data", allAccount);
        model.addAttribute("secondData", secondData);
        return "AccountsListPage";
    }

    @GetMapping("all/{id}/all-trade-log")
    public String getAllLog(Model model) throws IOException, NoSuchAlgorithmException, ParseException {
        List<LogDto> allLog = upbitService.makeLogList();
        allLog.addAll(coinoneService.getAllLog());
        allLog.addAll(binanceService.getAllCoinLog());
        Collections.sort(allLog, new LogTimeComparator());
        model.addAttribute("log", allLog);
        return "LogListPage";
    }
}
