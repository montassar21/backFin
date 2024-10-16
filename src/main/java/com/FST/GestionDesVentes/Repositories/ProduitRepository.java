package com.FST.GestionDesVentes.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.FST.GestionDesVentes.Entities.Produit;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {

	List<Produit> findByCategory_Id(Long categoryId);

	// Méthode pour filtrer les produits entre un prix minimum et maximum
	List<Produit> findByPrixBetween(Double min, Double max);

	// Méthode pour filtrer les produits au-dessus d'un prix minimum
	List<Produit> findByPrixGreaterThanEqual(Double min);

	// Méthode pour filtrer les produits en dessous d'un prix maximum
	List<Produit> findByPrixLessThanEqual(Double max);

	@Query("SELECT p FROM Produit p WHERE CONCAT(',', p.couleur, ',') LIKE %:couleur%")
	List<Produit> findByCouleur(@Param("couleur") String couleur);

	List<Produit> findByMarque(String marque);

	List<Produit> findByStockGreaterThan(int stock);

}
