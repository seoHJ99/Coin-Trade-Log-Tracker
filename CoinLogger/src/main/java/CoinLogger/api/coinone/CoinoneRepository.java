package CoinLogger.api.coinone;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoinoneRepository extends JpaRepository<Coinone, Integer> {
    Coinone findByOwnerId(String id);
}
