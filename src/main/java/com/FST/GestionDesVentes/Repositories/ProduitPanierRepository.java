
package com.FST.GestionDesVentes.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.FST.GestionDesVentes.Entities.ProduitPanier;

@Repository
public interface ProduitPanierRepository extends JpaRepository<ProduitPanier, Long> {

	List<ProduitPanier> findByPanierId(long panierId);

	//Optional<ProduitPanier> findByPanierIdAndProduitId(Long panierId, Long produitId);
	Optional<ProduitPanier> findByPanier_UserEmailAndProduitId(String userEmail, Long produitId);


	@Modifying
	@Query("DELETE FROM ProduitPanier pp WHERE pp.produit.id = :produitId")
	void deleteByProduitId(@Param("produitId") Long produitId);

}