package com.FST.GestionDesVentes.Controlleres;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.FST.GestionDesVentes.Entities.Commande;
import com.FST.GestionDesVentes.Entities.Panier;
import com.FST.GestionDesVentes.Entities.Produit;
import com.FST.GestionDesVentes.Entities.ProduitPanier;
import com.FST.GestionDesVentes.Repositories.CommandeRepository;
import com.FST.GestionDesVentes.Repositories.PanierRepository;
import com.FST.GestionDesVentes.Repositories.ProduitPanierRepository;
import com.FST.GestionDesVentes.Repositories.ProduitRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/panier")
@CrossOrigin(origins = "http://localhost:4200")
public class PanierController {

	private final PanierRepository panierRepository;
	private final ProduitPanierRepository produitPanierRepo;
	private final ProduitRepository produitRepository;
	private final CommandeRepository commandeRepository;

	@Autowired
	public PanierController(PanierRepository panierRepository, ProduitRepository produitRepository,
			ProduitPanierRepository produitPanierRepository,
			CommandeRepository commandeRepository) {
		this.panierRepository = panierRepository;
		this.produitPanierRepo = produitPanierRepository;
		this.produitRepository = produitRepository;
		this.commandeRepository = commandeRepository;
	}

	@GetMapping("/list")
	public List<Panier> getAllPaniers() {
		return (List<Panier>) panierRepository.findAll();
	}

	@PostMapping("/add")
	public ResponseEntity<String> createPanier(@RequestBody Panier panier) {

		// Enregistrez le panier
		try {
			panierRepository.save(panier);
			return ResponseEntity.ok("Panier créé avec succès");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body("Erreur lors de la création du panier : " + e.getMessage());
		}
	}

	@PutMapping("/{panierId}")
	public ResponseEntity<Panier> updatePanier(@PathVariable Long panierId, @Valid @RequestBody Panier panierRequest) {
		return panierRepository.findById(panierId).map(panier -> {
			panier.setQuantite(panierRequest.getQuantite());
			panier.setStatut(panierRequest.getStatut().trim());
			Panier updatedPanier = panierRepository.save(panier);
			return ResponseEntity.ok(updatedPanier);
		}).orElseThrow(() -> new IllegalArgumentException("PanierId " + panierId + " not found"));
	}

	@Transactional
	@DeleteMapping("/{panierId}")
	public ResponseEntity<?> deletePanier(@PathVariable Long panierId) {
		return panierRepository.findById(panierId).map(panier -> {
			// Trouver les commandes associées au panier
			List<Commande> commandes = commandeRepository.findByPanierId(panierId);

			// Supprimer les commandes associées
			commandeRepository.deleteAll(commandes);

			// Supprimer le panier
			panierRepository.delete(panier);

			return ResponseEntity.ok().build();
		}).orElseThrow(() -> new IllegalArgumentException("PanierId " + panierId + " not found"));
	}

	
	
		 @GetMapping("/{userEmail}")
	    public ResponseEntity<?> getPanierByEmail(@PathVariable String userEmail) {
	        Panier panier = panierRepository.findByuserEmail(userEmail);
	        if (panier == null) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	        }
	        return ResponseEntity.ok(panier);
        }


	 
	 
	
	  @PutMapping("/increaseQuantity/{userEmail}/{produitId}")
	  public ResponseEntity<Panier> increaseQuantity(@PathVariable String userEmail, @PathVariable Long produitId) {
	      Optional<ProduitPanier> produitPanierOptional = produitPanierRepo.findByPanier_UserEmailAndProduitId(userEmail, produitId);

		if (produitPanierOptional.isPresent()) {
			ProduitPanier produitPanier = produitPanierOptional.get();
			produitPanier.setQuantite(produitPanier.getQuantite() + 1);
			produitPanierRepo.save(produitPanier);

			 Panier panier = panierRepository.findByuserEmail(userEmail);
			if (panier != null) {
				// Mettre à jour le total du panier
				panier.setTotal(panier.getTotal() + produitPanier.getProduit().getPrix());
				panierRepository.save(panier);

				// Retourner le panier mis à jour
				return ResponseEntity.ok(panier);
			}
		}

		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}

	
	
	

@PutMapping("/decreaseQuantity/{userEmail}/{produitId}")
public ResponseEntity<Panier> decreaseQuantity(@PathVariable String userEmail, @PathVariable Long produitId) {
    Optional<ProduitPanier> produitPanierOptional = produitPanierRepo.findByPanier_UserEmailAndProduitId(userEmail, produitId);

		if (produitPanierOptional.isPresent()) {
			ProduitPanier produitPanier = produitPanierOptional.get();
			if (produitPanier.getQuantite() > 1) {
				produitPanier.setQuantite(produitPanier.getQuantite() - 1);
				produitPanierRepo.save(produitPanier);

				 Panier panier = panierRepository.findByuserEmail(userEmail);
				panier.setTotal(panier.getTotal() - produitPanier.getProduit().getPrix());

				return ResponseEntity.ok(panierRepository.save(panier));
			} else {
				return ResponseEntity.badRequest().body(null); // Ne peut pas être inférieur à 1
			}
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
	}

	
	
	

	    @PostMapping("/addProduitToPanier/{userEmail}/{idProduit}")
	    public ResponseEntity<Map<String, String>> addProduitToPanier(
	            @PathVariable String userEmail,           // Paramètre userEmail
	            @PathVariable long idProduit,             // Paramètre idProduit
	            @RequestBody Map<String, Integer> requestBody) { // JSON contenant la quantité

		  System.out.println("Recherche du panier pour l'utilisateur : " + userEmail);
	    // 1. Vérifier si l'utilisateur a déjà un panier. Si non, créer un nouveau panier.
	    Panier panier = panierRepository.findByuserEmail(userEmail); // Recherche par email
	    if (panier == null) {
	        panier = new Panier();
	        panier.setUserEmail(userEmail); // Utiliser l'email de l'utilisateur
	        panier.setTotal(0.0);  // Initialisation du total à 0
	        panier.setQuantite(0); // Initialisation de la quantité à 0 (ou à 1 si vous préférez)
	        panier.setStatut("valide");
	        panier = panierRepository.save(panier);  // Sauvegarder le nouveau panier dans la base de données
	    }

	    System.out.println("Recherche du produit avec l'ID : " + idProduit);
	    // 2. Récupérer les informations du produit à partir de l'ID du produit
	    Produit produit = produitRepository.findById(idProduit).orElse(null);
	    int quantite = requestBody.get("quantite");  // Récupérer la quantité envoyée dans le corps de la requête

	    // 3. Vérifier si le produit existe
	    if (produit == null) {
	        Map<String, String> response = new HashMap<>();
	        response.put("message", "Produit non trouvé");
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	    }

	    // 4. Vérifier si le stock est suffisant pour la quantité demandée
	    if (produit.getStock() < quantite) {
	    	 System.out.println("Stock insuffisant pour le produit : " + produit.getNom());
	        Map<String, String> response = new HashMap<>();
	        response.put("message", "Stock insuffisant pour le produit : " + produit.getNom());
	        return ResponseEntity.badRequest().body(response);
	    }

	    // 5. Mise à jour du stock du produit après ajout au panier
	    produit.setStock(produit.getStock() - quantite);
	    produitRepository.save(produit);

	    try {
	        // 6. Vérifier si le produit est déjà dans le panier
	        boolean produitExist = false;
	        for (ProduitPanier pp : panier.getProduitsPanier()) {
	            if (pp.getProduit().getId() == idProduit) {
	                // Si le produit existe déjà, mettre à jour la quantité
	                pp.setQuantite(pp.getQuantite() + quantite);
	                produitExist = true;
	                produitPanierRepo.save(pp); // Enregistrer les changements
	                break;
	            }
	        }

	        if (!produitExist) {
	            // 7. Créer une nouvelle instance de ProduitPanier (relation entre le produit et le panier)
	            ProduitPanier produitPanier = new ProduitPanier();
	            produitPanier.setProduit(produit);
	            produitPanier.setPanier(panier);
	            produitPanier.setQuantite(quantite);
	            produitPanier.setUsEmail(userEmail);
	            produitPanierRepo.save(produitPanier); // Enregistrer le nouveau produit
	            panier.getProduitsPanier().add(produitPanier); // Ajouter au panier
	            System.out.println(produitPanier);
	        }

	        // 8. Recalculer le total du panier
	        double total = panier.calculateTotal(); // Appel à la méthode de calcul total
	        panier.setTotal(total);  // Mettre à jour le total du panier
	        panierRepository.save(panier);  // Sauvegarder le panier mis à jour

	        // 9. Retourner une réponse de succès
	        Map<String, String> response = new HashMap<>();
	        response.put("message", "Produit ajouté au panier");
	        return ResponseEntity.ok(response);
	    } catch (Exception e) {
	        // 10. Gérer les exceptions et retourner une réponse d'erreur
	        Map<String, String> response = new HashMap<>();
	        response.put("message", "Erreur lors de l'ajout du produit au panier");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}

	

	
	
	
	@DeleteMapping("removeProduitFromPanier/{userEmail}/{idProduit}")
	public ResponseEntity<Panier> removeProduitFromPanier(@PathVariable String userEmail, @PathVariable long idProduit) {
		
		  Panier panier = panierRepository.findByuserEmail(userEmail);
		Produit produit = produitRepository.findById(idProduit).orElse(null);

		if (panier == null || produit == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		panier.getProduitsPanier().removeIf(produitPanier -> produitPanier.getProduit().getId() == idProduit);

		double total = 0.0;
		List<ProduitPanier> produitsPanier = produitPanierRepo.findByPanierId(panier.getId());

		for (ProduitPanier produitPan : produitsPanier) {
			Produit produitpn = produitPan.getProduit();
			if (produitpn != null && produitpn.getPrix() != null) {
				total += produitPan.getQuantite() * produitpn.getPrix();
			} else {
				System.err.println("Produit or Produit Prix is null for ProduitPanier with ID: " + produitPan.getId());
			}
		}

		panier.setTotal(total);

		Panier updatedPanier = panierRepository.save(panier);

		return ResponseEntity.ok(updatedPanier);
	}

}