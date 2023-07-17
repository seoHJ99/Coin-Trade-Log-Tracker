package CoinLogger.api.coinone;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "coinone")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Coinone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idx;
    @Column(name = "owner_id")
    private String ownerId;
    @Column(name = "access_key")
    private String accessKey;
    @Column(name = "secret_key")
    private String secretKey;
}
