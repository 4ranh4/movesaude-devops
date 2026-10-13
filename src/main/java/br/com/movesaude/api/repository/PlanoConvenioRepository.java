package br.com.movesaude.api.repository;

import br.com.movesaude.api.domain.model.PlanoConvenio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanoConvenioRepository extends JpaRepository<PlanoConvenio, String> {

    List<PlanoConvenio> findAllByOrderByOperadoraAscNomeAsc();
}
