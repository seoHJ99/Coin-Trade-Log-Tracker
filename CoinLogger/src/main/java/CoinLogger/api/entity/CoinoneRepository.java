package CoinLogger.api.entity;

import CoinLogger.api.entity.Coinone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoinoneRepository extends JpaRepository<Coinone, Integer> {
    Coinone findByOwnerId(String id);
}
