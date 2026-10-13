package br.com.movesaude.api.repository;

import br.com.movesaude.api.domain.model.Passageiro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PassageiroRepository extends JpaRepository<Passageiro, Long> {

    List<Passageiro> findAllByOrderByIdAsc();
}
