package ma.project.grpc.repositories;

import ma.project.grpc.entities.Compte;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CompteRepository extends JpaRepository<Compte, String> {
}
