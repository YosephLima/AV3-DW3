package com.autobots.automanager.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autobots.automanager.entidades.Credencial;

public interface RepositorioCredencial extends JpaRepository<Credencial, Long> {
}
