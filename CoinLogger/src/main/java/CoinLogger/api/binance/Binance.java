package CoinLogger.api.binance;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "binance")
@Builder
@Getter
@Setter
public class Binance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idx;
    private String ownerId;
    private String accessKey;
    private String secretKey;
}
