package ma.project.grpc.services;

import ma.project.grpc.entities.Compte;
import ma.project.grpc.repositories.CompteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompteService {
    private final CompteRepository compteRepository;
    
    public CompteService(CompteRepository compteRepository) {
        this.compteRepository = compteRepository;
    }
    
    public List<Compte> findAllComptes() {
        return compteRepository.findAll();
    }
    
    public Compte findCompteById(String id) {
        return compteRepository.findById(id).orElse(null);
    }
    
    public Compte saveCompte(Compte compte) {
        return compteRepository.save(compte);
    }
}