package br.gov.mj.senacon.proconsumidor.command.messaging;

import br.gov.mj.senacon.proconsumidor.command.config.RabbitMQConfig;
import br.gov.mj.senacon.proconsumidor.command.domain.Reclamacao;
import br.gov.mj.senacon.proconsumidor.command.dto.ReclamacaoEvent;
import br.gov.mj.senacon.proconsumidor.command.repository.ReclamacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReclamacaoConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ReclamacaoConsumer.class);

    private final ReclamacaoRepository repository;

    @Autowired
    public ReclamacaoConsumer(ReclamacaoRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = RabbitMQConfig.RECLAMACAO_QUEUE)
    @Transactional
    public void receiveMessage(ReclamacaoEvent event) {
        logger.info("Processando alteração para Protocolo {}. Solicitado por: IP {}, Fornecedor {}", 
            event.getProtocolo(), event.getOriginIp(), event.getUsuarioBot());

        try {
            Reclamacao reclamacao = repository.findByProtocolo(event.getProtocolo())
                .orElse(new Reclamacao());

            reclamacao.setProtocolo(event.getProtocolo());
            reclamacao.setStatus(event.getStatus());
            reclamacao.setFornecedorId(event.getFornecedorId());
            reclamacao.setDataAtualizacao(event.getDataEvento());

            // A chamada save() fará INSERT se for nova e UPDATE se possuir ID preenchido
            repository.save(reclamacao);
            logger.info("Reclamação salva no MSSQL com sucesso!");
        } catch (Exception e) {
            logger.error("Falha ao salvar a reclamação para protocolo: " + event.getProtocolo(), e);
            // Delega p/ spring-amqp reenviar / colocar em DLQ se configurada
            throw e; 
        }
    }
}
