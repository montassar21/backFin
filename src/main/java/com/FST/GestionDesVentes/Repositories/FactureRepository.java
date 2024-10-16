package com.FST.GestionDesVentes.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.FST.GestionDesVentes.Entities.Facture;



@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {

	  Optional<Facture> findByCommandeId(Long commandeId);
}
