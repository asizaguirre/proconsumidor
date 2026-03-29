package br.gov.mj.senacon.proconsumidor.query.repository;

import br.gov.mj.senacon.proconsumidor.query.domain.ReclamacaoDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReclamacaoSearchRepository extends ElasticsearchRepository<ReclamacaoDocument, String> {
    Page<ReclamacaoDocument> findByFornecedorId(String fornecedorId, Pageable pageable);
}
