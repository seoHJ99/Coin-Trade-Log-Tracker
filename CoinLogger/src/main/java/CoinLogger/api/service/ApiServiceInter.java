package CoinLogger.api.service;

import CoinLogger.api.dto.AccountDto;
import CoinLogger.api.dto.LogDto;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.List;

public interface ApiServiceInter {

    List<LogDto> getAllLogDto() throws IOException, ParseException;

    List<AccountDto> getAccountList() throws IOException, ParseException;
}
