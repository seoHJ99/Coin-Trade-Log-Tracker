package CoinLogger.api.upbit;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "upbit")
@Getter
@Builder
@Setter
public class Upbit {
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

