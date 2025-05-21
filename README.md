# on-data-thymeleaf-consumer

Este Java Spring foi criado como Consumer da fila RabbitMQ para a qual o consumidor no projeto em [on-data-thymeleaf](https://github.com/eduardofuncao/on-data-thymeleaf) envia as mensagens de ocorrências médicas criadas. Além disso, para cada ocorrência obtida da fila, uma mensagem é enviada ao usuário final através de uma integração com a API do Telegram, utilizando também um LLM para personalizar as mensagens enviadas através do Spring AI.

## Tecnologias utilizadas
- SpringBoot
- Java
- RabbitMQ
- Telegram Bots / Bot Father
- Spring AI / Ollama / Llama 3.2:3b

## Como começar
### Pré-requisitos
- Java 17+
- Gradle
- Docker

### Build
Para realizar a etapa de build do jar para posterior conteinerização, seguir os seguintes passos. Primeiro, o jar dever ser obtido através de:
```bash
./gradlew build
```

Em seguida, a imagem docker pode ser criada com:
```bash
docker build -t on-data-thymeleaf-consumer .
```
O projeto pode ser executado individualmente para propósitos de desenvolvimento. Entretando, para que seja funcional, deve ser utilizado em paralelo com os outros serviços. Como facilidade, o arquivo docker-compose.yml em 
[link docker-compose.yml](https://github.com/eduardofuncao/on-data-thymeleaf/blob/main/docker-compose.yml) pode ser utilizado para rodar todos os containers necessários pela aplicação simultaneamente. Para executar os containers, basta utilizar o seguinte comando:
```bash
docker-compose up -d
```

## Utilização
Para realizar os testes, após rodar o arquivo docker-compose, a aplicação produtora é acessível na porta 8080. Nela, após a criação de uma ocorrência, uma mensagem será enviada à fila rabbitMQ, que será lida pela aplicação consumidora, modificada através de seus serviços e da LLM llama3.2, e, por fim, enviada para a API do telegram. Para que a mensagem chegue ao chat desejado no telegram, a variável em `application.properties` telegram.chat.id deve ser alterada conforme seu id de chat. o bot pode ser acessado em `t.me/on_data_bot`
