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

## Roteiro Prático de Teste (Ponta a Ponta)

Siga este roteiro assim que a infraestrutura (`docker-compose up -d`) e as aplicações Spring Boot estiverem mapeadas para validar todo o fluxo proposto de **CQRS e Proteção Relacional**:

### Passo 1: Habilitar o CDC no Banco de Dados
O microserviço `proconsumidor-command` recriou a tabela `reclamacao` usando o Hibernate. Agora, você deve habilitar o monitoramento de CDC no SQL Server para esta tabela. Rode o comando:
```bash
docker exec -it mssql /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P 'ProConsu@2026QA' -C -Q "USE ProConsumidorDB; EXEC sys.sp_cdc_enable_db; EXEC sys.sp_cdc_enable_table @source_schema = N'dbo', @source_name = N'reclamacao', @role_name = NULL;"
```

### Passo 2: Inicializar Leitor Debezium e Escritor (Elastic)
Registraremos as tasks no cluster do **Kafka Connect**, que orquestrará as transferências passivas e em tempo real.
```bash
# Registrar Leitor no SQL Server
curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" http://localhost:8083/connectors/ -d @infrastructure/cdc-connectors/sqlserver-source-connector.json

# Registrar Escritor para o Elasticsearch
curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" http://localhost:8083/connectors/ -d @infrastructure/cdc-connectors/elasticsearch-sink-connector.json
```

### Passo 3: Injetar Operação de Escrita do "Bot" na Fila
Em vez de sobrecarregar a porta HTTP, o *Worker* está ouvindo a fila do RabbitMQ. Vamos postar um JSON simulando que o Azure API Management e o Service Bus processaram e entregaram uma "Atualização de Status de Reclamação" automatizada para nossa fila `reclamacao.atualizar.queue`:

```bash
curl -i -u admin:admin -X POST http://localhost:15672/api/exchanges/%2f/amq.default/publish \
-H "content-type:application/json" \
-d '{
  "properties": {},
  "routing_key": "reclamacao.atualizar.queue",
  "payload": "{\"protocolo\": \"2026-XPT001-A\", \"fornecedorId\": \"FORNECEDOR_MOCK\", \"status\": \"EM_ANALISE\", \"usuarioBot\": \"Bot_Submarino_01\", \"originIp\": \"192.168.1.55\"}",
  "payload_encoding": "string"
}'
```
*Observe em seu terminal do `proconsumidor-command` o console confirmar a gravação da linha.*

### Passo 4: Verificar a API de Indexação/Consultas (Query Module)
Em questão de milissegundos após a gravação no MSSQL, o Kafka Connect lê o CT Log do SQL, injeta no Elasticsearch, e agora os *Bots* ou *Data Lakes* poderão consultar o dado recém-criado batendo apenas contra a nossa API Otimizada de Leitura sem incomodar o sistema legado do Procon:

Para viabilizar este teste de prova de conceito local e rápido - e uma vez que não conectamos a API a um Active Directory em nuvem ainda - você precisará **remover ou comentar temporariamente a restrição JWT** alterando e salvando a classe `SecurityConfig.java` no pacote `proconsumidor-query` para `.anyRequest().permitAll()` antes de disparar o run inicial ou reenviar as requisições, para liberar o Actuator API sem um Token Entra ID.

Após a alteração do `SecurityConfig`, reinicie o serviço Query e rode:
```bash
curl -X GET "http://localhost:8081/api/v1/reclamacoes?page=0&size=10"
```

**Resultado esperado**: Sua resposta deverá conter o Payload `2026-XPT001-A` servido totalmente do cache de indexação (`_id` mapeado da base master), garantindo que nossa implementação CQRS funcionou perfeitamente e é escalável!
