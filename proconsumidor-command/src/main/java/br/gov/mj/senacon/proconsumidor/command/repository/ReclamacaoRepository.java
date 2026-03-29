package br.gov.mj.senacon.proconsumidor.command.repository;

import br.gov.mj.senacon.proconsumidor.command.domain.Reclamacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReclamacaoRepository extends JpaRepository<Reclamacao, Long> {
    Optional<Reclamacao> findByProtocolo(String protocolo);
}
