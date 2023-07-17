package CoinLogger.api.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "binance")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Binance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idx;
    private String ownerId;
    private String accessKey;
    private String secretKey;
}
