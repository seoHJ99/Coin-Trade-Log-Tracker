package CoinLogger.api;

import CoinLogger.CoinSumBuyPriceComparator;
import CoinLogger.PublicMethod;
import CoinLogger.api.binance.BinanceService;
import CoinLogger.api.coinone.CoinoneService;
import CoinLogger.api.upbit.AccountDto;
import CoinLogger.api.upbit.LogDto;
import CoinLogger.api.upbit.UpbitService;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@RequiredArgsConstructor
@Controller
public class AllController {
    private final BinanceService binanceService;
    private final UpbitService upbitService;
    private final CoinoneService coinoneService;
    private final PublicMethod publicMethod;
    private final AllService allService;

    @GetMapping("/all/account")
    public String getAllAccount(Model model) throws IOException, ParseException {
        List<AccountDto> allAccount = new ArrayList<>();
        if(coinoneService.getKeys()){
            allAccount.addAll( coinoneService.accountDtoMaker());
        }
        if(coinoneService.getKeys()){
            allAccount.addAll( binanceService.accountDtoMaker());
        }
        if(coinoneService.getKeys()){
            allAccount.addAll( upbitService.accountDtoMaker());
        }
        Collections.sort(allAccount, new CoinSumBuyPriceComparator());
        Map<String, String> secondData = publicMethod.makeSumData(allAccount);
        model.addAttribute("data", allAccount);
        model.addAttribute("secondData", secondData);
        return "AccountsListPage";
    }

    @GetMapping("/all/all-trade-log")
    public String getAllLog(Model model) throws IOException, NoSuchAlgorithmException, ParseException {
        List<LogDto> allLog = new ArrayList<>();
        if(coinoneService.getKeys()){
            allLog.addAll( coinoneService.getAllLog());
        }
        if(binanceService.getKeys()){
            allLog.addAll( binanceService.getAllCoinLog());
        }
        if(upbitService.getKeys()){
            allLog.addAll( upbitService.makeLogList());
        }
        model.addAttribute("log", allLog);
        return "LogListPage";
    }

    @GetMapping("/myinfo/keys")
    public String myApiKeys(){
        return "MyInfo";
    }

    @PostMapping("/api/key")
    @ResponseBody
    public String saveKeys(@RequestBody Map<String, String[]> data){
        allService.saveKeys(data);
        return "";
    }
}
