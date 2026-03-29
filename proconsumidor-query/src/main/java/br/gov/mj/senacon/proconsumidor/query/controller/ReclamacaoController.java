package br.gov.mj.senacon.proconsumidor.query.controller;

import br.gov.mj.senacon.proconsumidor.query.domain.ReclamacaoDocument;
import br.gov.mj.senacon.proconsumidor.query.repository.ReclamacaoSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reclamacoes")
public class ReclamacaoController {

    private static final Logger logger = LoggerFactory.getLogger(ReclamacaoController.class);

    private final ReclamacaoSearchRepository searchRepository;

    public ReclamacaoController(ReclamacaoSearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    /**
     * Endpoint para consultar reclamações do fornecedor atual.
     * Na prática, APIM faria o bloqueio prévio, mas a API também filtra com base no JWT do Entra ID.
     */
    @GetMapping
    public Page<ReclamacaoDocument> listar(
            @RequestParam(required = false) String status,
            Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {
        
        // Simulando que o Entra ID adiciona um claim chamado "fornecedorId"
        String fornecedorId = jwt != null && jwt.hasClaim("fornecedorId") 
                ? jwt.getClaimAsString("fornecedorId") 
                : "FORNECEDOR_MOCK";

        logger.info("Consulta recebida - Fornecedor ID: {}", fornecedorId);
        
        return searchRepository.findByFornecedorId(fornecedorId, pageable);
    }
}
