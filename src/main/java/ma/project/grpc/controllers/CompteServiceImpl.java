package ma.project.grpc.controllers;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import ma.project.grpc.entities.Compte;
import ma.project.grpc.repositories.CompteRepository;
import ma.project.grpc.stubs.*;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@GrpcService
public class CompteServiceImpl extends CompteServiceGrpc.CompteServiceImplBase {

    private final CompteRepository compteRepository;

    public CompteServiceImpl(CompteRepository compteRepository) {
        this.compteRepository = compteRepository;
    }

    // ===================== 1) AllComptes =====================
    @Override
    public void allComptes(GetAllComptesRequest request,
                           StreamObserver<GetAllComptesResponse> responseObserver) {

        List<Compte> comptesDB = compteRepository.findAll();
        GetAllComptesResponse.Builder response = GetAllComptesResponse.newBuilder();

        for (Compte c : comptesDB) {
            response.addComptes(toGrpc(c));
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    // ===================== 2) CompteById =====================
    @Override
    public void compteById(GetCompteByIdRequest request,
                           StreamObserver<GetCompteByIdResponse> responseObserver) {

        String id = request.getId();

        Compte compte = compteRepository.findById(id).orElse(null);
        if (compte == null) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Compte avec id=" + id + " introuvable")
                            .asRuntimeException()
            );
            return;
        }

        GetCompteByIdResponse response = GetCompteByIdResponse.newBuilder()
                .setCompte(toGrpc(compte))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // ===================== 3) TotalSolde (SoldeStats) =====================
    @Override
    public void totalSolde(GetTotalSoldeRequest request,
                           StreamObserver<GetTotalSoldeResponse> responseObserver) {

        List<Compte> comptes = compteRepository.findAll();

        int count = comptes.size();
        float sum = 0f;
        for (Compte c : comptes) sum += c.getSolde();

        float avg = (count == 0) ? 0f : (sum / count);

        SoldeStats stats = SoldeStats.newBuilder()
                .setCount(count)
                .setSum(sum)
                .setAverage(avg)
                .build();

        GetTotalSoldeResponse response = GetTotalSoldeResponse.newBuilder()
                .setStats(stats)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // ===================== 4) SaveCompte =====================
    @Override
    public void saveCompte(SaveCompteRequest request,
                           StreamObserver<SaveCompteResponse> responseObserver) {

        CompteRequest req = request.getCompte();

        Compte entity = new Compte();

        // ID : généré (car ton CompteRequest n'a pas de id)
        entity.setId(UUID.randomUUID().toString());

        entity.setSolde(req.getSolde());

        String date = req.getDateCreation();
        if (date == null || date.isBlank()) {
            date = LocalDate.now().toString();
        }
        entity.setDateCreation(date);

        // enum -> String en base
        entity.setType(req.getType().name());

        Compte saved = compteRepository.save(entity);

        SaveCompteResponse response = SaveCompteResponse.newBuilder()
                .setCompte(toGrpc(saved))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // ===================== Mapper Entity -> gRPC =====================
    private ma.project.grpc.stubs.Compte toGrpc(Compte c) {
        TypeCompte typeGrpc = "EPARGNE".equalsIgnoreCase(c.getType())
                ? TypeCompte.EPARGNE
                : TypeCompte.COURANT;

        return ma.project.grpc.stubs.Compte.newBuilder()
                .setId(c.getId() == null ? "" : c.getId())
                .setSolde(c.getSolde())
                .setDateCreation(c.getDateCreation() == null ? "" : c.getDateCreation())
                .setType(typeGrpc)
                .build();
    }
}
