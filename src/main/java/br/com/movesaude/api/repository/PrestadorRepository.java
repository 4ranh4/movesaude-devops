package br.com.movesaude.api.repository;

import br.com.movesaude.api.domain.model.Prestador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrestadorRepository extends JpaRepository<Prestador, Long> {

    List<Prestador> findAllByOrderByIdAsc();

    boolean existsByCnpj(String cnpj);
}
