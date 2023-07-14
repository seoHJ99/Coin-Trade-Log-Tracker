package CoinLogger.api.upbit;


import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ViewController {
    private final UpbitService upbit;
    @GetMapping("/upbit/{id}/accounts")
    public String getAllAccounts(Model model) throws IOException, ParseException {
        List<AccountDto> dtoList = upbit.accountDtoMaker();
        Map<String, String> secondData = upbit.makeSumData(dtoList);
        model.addAttribute( "data", dtoList);
        model.addAttribute("secondData", secondData);
        return "AccountsListPage";
    }

    @GetMapping("upbit/{id}/all-trade-log")
    public String getTradeLog(Model model) throws UnsupportedEncodingException, NoSuchAlgorithmException, ParseException {
         model.addAttribute("log", upbit.makeLogList());
         return "LogListPage";
    }
}
