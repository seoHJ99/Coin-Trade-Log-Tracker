package CoinLogger.api;

import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = ParseException.class)
    public Object parseExceptionHandler(Exception e){
        e.printStackTrace();
        return ResponseEntity.ok("<script>alert('파싱 에러! 개발자에게 연락하세요!')</script>");
    }

    @ExceptionHandler(value = ClassCastException.class)
    public Object classCastExceptionHandler(Exception e){
        e.printStackTrace();
        return ResponseEntity.ok("<script>alert('변환 에러!" +
                "1. 업비트 api ip주소가 옳바른지 확인해주세요" +
                "2. api key가 옳바른지 확인해주세요" +
                "3. 해결법이 통하지 않으면 개발자에게 연락하세요!')</script>");
    }

    @ExceptionHandler(value = IOException.class)
    public Object IOExceptionHandler(Exception e){
        e.printStackTrace();
        return ResponseEntity.ok("<script>alert('통신 에러! 개발자에게 연락하세요!')</script>");
    }
}
