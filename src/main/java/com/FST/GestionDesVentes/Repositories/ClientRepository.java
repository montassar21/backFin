package com.FST.GestionDesVentes.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.FST.GestionDesVentes.Entities.Client;

@Repository
public interface ClientRepository extends  JpaRepository <Client , Long> {
    Optional<Client> findByEmail(String userEmail);
}
