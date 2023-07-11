package CoinLogger.api.binance;

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
import java.util.List;
import java.util.Map;


@Controller
@RequiredArgsConstructor
public class BinanceController {
    private final BinanceService binanceService;

    @GetMapping("/binance/{id}/account")
    public String getAccount(Model model) throws IOException, ParseException {
        List<AccountDto_Binance> dtos = binanceService.makeAccountDto();
        model.addAttribute("data", dtos);
        return "AccountsListPage";
    }

    @GetMapping("/binance/wallet")
    public String makeAvgPriceAndSave(Model model) throws IOException, ParseException {
        List<String> coinName=binanceService.getMyCoinName();
        model.addAttribute("coinName", coinName);
        return "binanceCoin";
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
