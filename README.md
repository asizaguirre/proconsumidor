# Pro Consumidor - Evolução Arquitetural CQRS

Este projeto apresenta a Prova de Conceito (PoC) da nova arquitetura proposta para a mitigação de uso indevido por integrações automatizadas (bots), utilizando os conceitos de **CQRS** (Command Query Responsibility Segregation) e mensageria assíncrona.

## Arquitetura
A arquitetura baseada no modelo Azure com tolerância a alto tráfego se divide em:
1. **proconsumidor-query**: Módulo de leitura REST isolado, usando Elasticsearch.
2. **proconsumidor-command**: Módulo Worker assíncrono conectado via AMQP (simulando Service Bus).
3. **Change Data Capture**: Debezium conectado ao SQL Server alimentando o índice do Elasticsearch de forma automática.

## Requisitos
* Docker e Docker Compose instalados na máquina.
* Java 21 (JDK) ou superior instalado.
* Apache Maven (versão 3.8+).

## Instruções para Executar

### 1. Subir a Infraestrutura (Bancos e Filas)
O projeto conta com scripts pre-configurados em Docker para montar o ambiente local: SQL Server, RabbitMQ, Elasticsearch, Kafka, Zookeeper e Kafka Connect.
Abra seu terminal na raiz do projeto e acesse a pasta da infraestrutura:

```bash
cd infrastructure
docker-compose up -d
```

> **Atenção:** Aguarde que os containeres constem como "healthy". O Kafka Connect (`debezium/connect`) pode demorar mais para instanciar (verifique via `docker ps`).

### 2. Cadastrar Conectores de Sincronismo (CDC)
Com o `kafka-connect` operando na porta 8083, registre os conectores de Source (SQL) e Sink (Elasticsearch). Execute estes comandos um de cada vez em um terminal HTTP novo, apontando no diretório `infrastructure/cdc-connectors`:

```bash
# Registrar Leitor no SQL Server
curl -i -X POST -H "Accept:application/json" -H  "Content-Type:application/json" http://localhost:8083/connectors/ -d @infrastructure/cdc-connectors/sqlserver-source-connector.json

# Registrar Escritor para o Elasticsearch
curl -i -X POST -H "Accept:application/json" -H  "Content-Type:application/json" http://localhost:8083/connectors/ -d @infrastructure/cdc-connectors/elasticsearch-sink-connector.json
```

### 3. Compilar a Aplicação
Volte à pasta raiz do repositório (`/home/alam/Área de trabalho/Proconsumidor`) e execute a construção do monorepo:

```bash
mvn clean install -DskipTests
```

### 4. Executar os Módulos Spring Boot
Abra dois terminais diferentes para inicializar cada módulo simultaneamente:

**Terminal 1 (Worker de API de Escrita):**
```bash
cd proconsumidor-command
mvn spring-boot:run
```

**Terminal 2 (API de Leitura/Gateway):**
```bash
cd proconsumidor-query
mvn spring-boot:run
```

## Como Testar?
1. **Gerar Reclamação:** Faça o publish de uma mensagem no RabbitMQ (`localhost:15672`, admin/admin) na fila de atualização. O `proconsumidor-command` gravará a linha no MSSQL.
2. **Sincronismo:** O Debezium monitorará a operação e enviará o JSON em tempo real para o Elasticsearch (porta 9200).
3. **Leitura Exclusiva (Bots):** Consuma a API do módulo Query em `localhost:8081/api/v1/reclamacoes` informando um Token JWT válido ou removendo provisoriamente a autenticação na classe `SecurityConfig`. Essa API lerá super rápido na base paralela, protegendo o MSSQL do tráfego do legado.
