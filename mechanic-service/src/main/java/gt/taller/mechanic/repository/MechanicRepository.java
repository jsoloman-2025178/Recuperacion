package gt.taller.mechanic.repository;

import gt.taller.mechanic.entity.Mechanic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MechanicRepository extends JpaRepository<Mechanic, Long> {

    List<Mechanic> findByActive(Boolean active);
}
