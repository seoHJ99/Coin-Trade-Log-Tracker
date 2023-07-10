package CoinLogger.api.binance;

import lombok.RequiredArgsConstructor;

import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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

    @RequestMapping("/binance/wallet")
    @ResponseBody
    public String saveWalletInfo(HttpServletRequest request){
        // id나중에
        String id =  "";
        //
        Map<String,String[]> map = request.getParameterMap();
        return "1";
    }

    @GetMapping("/test")
    public String makeAvgPriceAndSave(Model model) throws IOException, ParseException {
        List<String> coinName=binanceService.getMyCoinName();
        model.addAttribute("coinName", coinName);
        return "binanceCoin";
    }
}
