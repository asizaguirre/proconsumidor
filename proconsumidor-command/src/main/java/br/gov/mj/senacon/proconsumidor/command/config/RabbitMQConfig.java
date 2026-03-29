package br.gov.mj.senacon.proconsumidor.command.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String RECLAMACAO_QUEUE = "reclamacao.atualizar.queue";

    @Bean
    public Queue reclamacaoQueue() {
        return new Queue(RECLAMACAO_QUEUE, true);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
