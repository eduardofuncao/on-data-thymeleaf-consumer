package br.com.fiap.on_data_consumer.service;

import br.com.fiap.on_data_consumer.dto.OcorrenciaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class OcorrenciaConsumerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OcorrenciaConsumerService.class);

    private final FileGenerationService fileGenerationService;

    public OcorrenciaConsumerService(FileGenerationService fileGenerationService) {
        this.fileGenerationService = fileGenerationService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void consumeOcorrenciaMessage(OcorrenciaMessage message) {
        LOGGER.info("Received occurrence message from queue: {}", message);
        System.out.println(message.getOccurrenceId());

        fileGenerationService.generateOcorrenciaPDF(message);
    }
}
