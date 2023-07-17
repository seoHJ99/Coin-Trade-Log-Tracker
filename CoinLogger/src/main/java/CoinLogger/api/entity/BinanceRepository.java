package CoinLogger.api.entity;



import CoinLogger.api.entity.Binance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BinanceRepository extends JpaRepository<Binance, Integer> {
    Binance findByOwnerId(String id);
}
