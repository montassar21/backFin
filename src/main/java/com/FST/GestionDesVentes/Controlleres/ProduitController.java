package com.FST.GestionDesVentes.Controlleres;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import com.fasterxml.jackson.core.type.TypeReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.FST.GestionDesVentes.Entities.Categorie;
import com.FST.GestionDesVentes.Entities.Panier;
import com.FST.GestionDesVentes.Entities.Produit;
import com.FST.GestionDesVentes.Entities.ProduitPanier;
import com.FST.GestionDesVentes.Repositories.PanierRepository;
import com.FST.GestionDesVentes.Repositories.ProduitPanierRepository;
import com.FST.GestionDesVentes.Repositories.ProduitRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.FST.GestionDesVentes.Repositories.CategorieRepository;

@RestController
@RequestMapping({ "/produits", "/home*" })
@CrossOrigin(origins = "http://localhost:4200")
public class ProduitController {

	private final PanierRepository panierRepository;
	private final ProduitRepository produitRepository;
	private final CategorieRepository categorieRepository;
	private final ProduitPanierRepository produitPanierRepository;

	@Autowired
	public ProduitController(PanierRepository panierRepository, ProduitRepository produitRepository,
			CategorieRepository categorieRepository, ProduitPanierRepository produitPanierRepository) {
		this.panierRepository = panierRepository;
		this.produitRepository = produitRepository;
		this.categorieRepository = categorieRepository;
		this.produitPanierRepository = produitPanierRepository;
	}

	@GetMapping("/list")
	public List<Produit> getAllProduits() {
		return (List<Produit>) produitRepository.findAll();
	}

	@PostMapping("/add")
	public ResponseEntity<Produit> createProduit(
			@RequestParam("nom") String nom,
			@RequestParam("description") String description,
			@RequestParam("prix") Double prix,
			@RequestParam("stock") int stock,
			@RequestParam("category") Long categoryId,
			@RequestParam("couleur") String couleur,
			@RequestParam("marque") String marque,
			@RequestParam("file") MultipartFile file) throws IOException {

		Produit produit = new Produit();
		produit.setNom(nom);
		produit.setDescription(description);
		produit.setPrix(prix);
		produit.setStock(stock);
		produit.setCouleur(couleur);
		produit.setMarque(marque);

		Categorie categorie = categorieRepository.findById(categoryId)
				.orElseThrow(() -> new RuntimeException("Catégorie non trouvée avec l'ID: " + categoryId));
		produit.setCategory(categorie);

		if (file != null && !file.isEmpty()) {
			produit.setImage(file.getBytes());
		}

		Produit savedProduit = produitRepository.save(produit);
		return ResponseEntity.ok(savedProduit);
	}

	@PostMapping("/addToCart")
	public ResponseEntity<String> addToCart(@RequestParam Long produitId, @RequestParam Long panierId) {
		Produit produit = produitRepository.findById(produitId)
				.orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID: " + produitId));

		Panier panier = panierRepository.findById(panierId)
				.orElseThrow(() -> new RuntimeException("Panier non trouvé avec l'ID: " + panierId));

		ProduitPanier produitPanier = new ProduitPanier();
		produitPanier.setProduit(produit);

		panier.addProduit(produitPanier);
		panierRepository.save(panier);

		return ResponseEntity.ok("Produit ajouté au panier avec succès.");
	}

	@PutMapping("/{produitId}")
	public ResponseEntity<Produit> updateProduit(
			@PathVariable Long produitId,
			@RequestParam(value = "image", required = false) MultipartFile image,
			@RequestParam("nom") String nom,
			@RequestParam("description") String description,
			@RequestParam("prix") double prix,
			@RequestParam("stock") int stock,
			@RequestParam("marque") String marque,
			@RequestParam("category") String categoryJson,
			@RequestParam("couleur") String couleurJson) {

		Optional<Produit> produitOptional = produitRepository.findById(produitId);

		if (!produitOptional.isPresent()) {
			return ResponseEntity.notFound().build();
		}

		Produit produit = produitOptional.get();
		produit.setNom(nom);
		produit.setDescription(description);
		produit.setPrix(prix);
		produit.setStock(stock);
		produit.setMarque(marque);

		ObjectMapper objectMapper = new ObjectMapper();
		try {
			// Parsing de la catégorie à partir de JSON
			Categorie category = objectMapper.readValue(categoryJson, Categorie.class);

			// Vérifiez ici que 'category.getId()' est de type Long
			if (category.getId() == null) {
				categorieRepository.save(category); // Sauvegarde de la nouvelle catégorie
			}

			produit.setCategory(category);

			// Conversion de la couleur de JSON (array) à une chaîne délimitée par des
			// virgules
			List<String> couleurs = objectMapper.readValue(couleurJson, new TypeReference<List<String>>() {
			});
			produit.setCouleur(String.join(",", couleurs));
		} catch (JsonProcessingException e) {
			return ResponseEntity.badRequest().build();
		}

		if (image != null && !image.isEmpty()) {
			try {
				produit.setImage(image.getBytes());
			} catch (IOException e) {
				return ResponseEntity.badRequest().build();
			}
		}

		produitRepository.save(produit);
		return ResponseEntity.ok(produit);
	}

	@DeleteMapping("/{produitId}")
	public ResponseEntity<?> deleteProduit(@PathVariable Long produitId) {
		return produitRepository.findById(produitId).map(produit -> {
			produitRepository.delete(produit);
			return ResponseEntity.ok().build();
		}).orElseThrow(() -> new IllegalArgumentException("ProduitId " + produitId + " not found"));
	}

	/*
	 * @DeleteMapping("/{produitId}")
	 * public ResponseEntity<?> deleteProduit(@PathVariable Long produitId) {
	 * // Supprimer les références dans t_panier_produit
	 * produitPanierRepository.deleteByProduitId(produitId); // Appel à partir de
	 * l'instance injectée
	 * 
	 * // Ensuite, supprimer le produit
	 * return produitRepository.findById(produitId).map(produit -> {
	 * produitRepository.delete(produit);
	 * return ResponseEntity.ok().build();
	 * }).orElseThrow(() -> new IllegalArgumentException("ProduitId " + produitId +
	 * " not found"));
	 * }
	 */

	@GetMapping("/{produitId}")
	public Produit getProduit(@PathVariable Long produitId) {
		Optional<Produit> p = produitRepository.findById(produitId);
		return p.orElseThrow(() -> new IllegalArgumentException("ProduitId " + produitId + " not found"));
	}

	@GetMapping("/byCategory/{categoryId}")
	public List<Produit> getProduitsByCategoryId(@PathVariable long categoryId) {
		return produitRepository.findByCategory_Id(categoryId);
	}

	@GetMapping("/filter")
	public ResponseEntity<List<Produit>> filterProduitsByPrix(
			@RequestParam(required = false) Double min,
			@RequestParam(required = false) Double max) {

		List<Produit> produits;

		// Logique de filtrage
		if (min != null && max != null) {
			produits = produitRepository.findByPrixBetween(min, max);
		} else if (min != null) {
			produits = produitRepository.findByPrixGreaterThanEqual(min);
		} else if (max != null) {
			produits = produitRepository.findByPrixLessThanEqual(max);
		} else {
			produits = produitRepository.findAll();
		}

		return ResponseEntity.ok(produits);
	}

	@GetMapping("/filterByColor")
	public ResponseEntity<List<Produit>> filterProduitsByCouleur(@RequestParam String couleur) {
		List<Produit> produits = produitRepository.findByCouleur(couleur);
		return ResponseEntity.ok(produits);
	}

	@GetMapping("/filterByBrand")
	public ResponseEntity<List<Produit>> filterProduitsByBrand(@RequestParam String marque) {
		List<Produit> produits = produitRepository.findByMarque(marque);
		return ResponseEntity.ok(produits);
	}

	@GetMapping("/filterByHighStock")
	public List<Produit> filterProduitsByHighStock() {
		return produitRepository.findByStockGreaterThan(90);
	}

}