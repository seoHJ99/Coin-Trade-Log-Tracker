package CoinLogger.api.upbit;


import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class UpbitController {
    private final UpbitService upbit;
    @GetMapping("/upbit/account")
    public String getAllAccounts(Model model) throws IOException, ParseException {
        if(upbit.getKeys()) {
            List<AccountDto> dtoList = upbit.accountDtoMaker();
            Map<String, String> secondData = upbit.makeSumData(dtoList);
            model.addAttribute("data", dtoList);
            model.addAttribute("secondData", secondData);
        }else {
            model.addAttribute("data", null);
            model.addAttribute("secondData", null);
        }
        return "AccountsListPage";
    }

    @GetMapping("upbit/all-trade-log")
    public String getTradeLog(Model model) throws UnsupportedEncodingException, NoSuchAlgorithmException, ParseException {
        if(upbit.getKeys()) {
            model.addAttribute("log", upbit.makeLogList());
        }else {
            model.addAttribute("log", null);
        }
         return "LogListPage";
    }
}
