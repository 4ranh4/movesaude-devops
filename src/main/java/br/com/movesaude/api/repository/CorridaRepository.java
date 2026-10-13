package br.com.movesaude.api.repository;

import br.com.movesaude.api.domain.model.Corrida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CorridaRepository extends JpaRepository<Corrida, Long> {

    List<Corrida> findAllByOrderByIdDesc();

    /** Créditos (trechos cobertos) já usados pelo passageiro no período, sem contar canceladas. */
    @Query("""
            select coalesce(sum(c.trechosCobertos), 0) from Corrida c
            where c.passageiro.id = :passageiroId
              and c.status <> br.com.movesaude.api.domain.StatusCorrida.CANCELADA
              and c.data between :inicio and :fim
            """)
    int creditosUsados(@Param("passageiroId") Long passageiroId,
                       @Param("inicio") LocalDate inicio,
                       @Param("fim") LocalDate fim);
}
