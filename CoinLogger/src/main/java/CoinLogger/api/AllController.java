package CoinLogger.api;

import CoinLogger.CoinSumBuyPriceComparator;
import CoinLogger.api.NameComparator;
import CoinLogger.api.binance.BinanceService;
import CoinLogger.api.coinone.CoinoneService;
import CoinLogger.api.upbit.AccountDto;
import CoinLogger.api.upbit.UpbitService;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
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
        Collections.sort(allAccount, new NameComparator());

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
}
