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
        
        // [PRODUÇÃO] O fornecedorId virá obrigatoriamente do Claim injetado pelo Entra ID via APIM.
        // [TESTE LOCAL] Usamos "FORNECEDOR_MOCK" para validar o fluxo do Elasticsearch sem infra de Identity.
        String fornecedorId = jwt != null && jwt.hasClaim("fornecedorId") 
                ? jwt.getClaimAsString("fornecedorId") 
                : "FORNECEDOR_MOCK"; // <-- Em produção, este fallback deve ser removido ou gerar erro 403.

        logger.info("Consulta recebida - Fornecedor ID: {}", fornecedorId);
        
        return searchRepository.findByFornecedorId(fornecedorId, pageable);
    }
}
