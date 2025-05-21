package br.com.fiap.on_data_consumer.service;

import br.com.fiap.on_data_consumer.dto.OcorrenciaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class OcorrenciaConsumerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OcorrenciaConsumerService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter USER_FRIENDLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final TelegramBotService telegramBotService;
    private final OllamaChatModel chatClient;

    @Autowired
    public OcorrenciaConsumerService(TelegramBotService telegramBotService, OllamaChatModel chatClient) {
        this.telegramBotService = telegramBotService;
        this.chatClient = chatClient;
    }

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void consumeOcorrenciaMessage(OcorrenciaMessage message) {
        LOGGER.info("Received occurrence message from queue: {}", message);

        String personalizedMessage = generatePersonalizedMessage(message);

        String telegramMessage = formatTelegramMessage(message, personalizedMessage);

        telegramBotService.sendMessageToTelegram(telegramMessage);
    }

    private String generatePersonalizedMessage(OcorrenciaMessage occurrence) {
        try {
            String formattedDate = occurrence.getCreatedAt() != null ?
                    occurrence.getCreatedAt().format(USER_FRIENDLY_FORMATTER) : "data não disponível";

            String aiPrompt = String.format(
                    "Gere uma mensagem personalizada e empática para um paciente chamado %s que acabou de registrar uma ocorrência com a seguinte descrição: \"%s\". O status da ocorrência é \"%s\". Seja cordial, reforce o suporte da empresa, mencione o número da ocorrência (%s) e a data de registro (%s). A mensagem deve ser curta e direta, adequada para um alerta de Telegram. Não podem haver placeholders na mensagem.",
                    occurrence.getPatientName(),
                    occurrence.getDescription(),
                    occurrence.getStatus(),
                    occurrence.getOccurrenceId(),
                    formattedDate
            );

            String personalizedMessage = chatClient.call(aiPrompt);
            LOGGER.info("Generated personalized message with AI for occurrence: {}", occurrence.getOccurrenceId());

            return personalizedMessage;
        } catch (Exception e) {
            LOGGER.error("Failed to generate personalized message: {}", e.getMessage());
            return "Não foi possível gerar uma mensagem personalizada neste momento.";
        }
    }

    private String formatTelegramMessage(OcorrenciaMessage message, String personalizedMessage) {
        String patientName = escapeMarkdown(message.getPatientName());
        String patientEmail = escapeMarkdown(message.getPatientEmail());
        String description = escapeMarkdown(message.getDescription());
        String status = escapeMarkdown(message.getStatus());
        String aiMessage = escapeMarkdown(personalizedMessage);

        String formattedDateTime = message.getCreatedAt() != null ?
                message.getCreatedAt().format(DATE_FORMATTER) : "N/A";

        return String.format(
                "🔔 *NOVA OCORRÊNCIA REGISTRADA*\n\n" +
                        "📝 *ID:* `%s`\n" +
                        "👤 *Paciente:* %s\n" +
                        "📧 *Email:* %s\n" +
                        "📄 *Descrição:* %s\n" +
                        "⏰ *Registrado em:* %s\n" +
                        "🚦 *Status:* %s\n\n" +
                        "💬 *Descrição:*\n_%s_\n\n" +
                        "Processado em: %s",
                message.getOccurrenceId(),
                patientName,
                patientEmail,
                description,
                formattedDateTime,
                status,
                aiMessage,
                DATE_FORMATTER.format(java.time.LocalDateTime.now())
        );
    }

    private String escapeMarkdown(String text) {
        if (text == null) {
            return "N/A";
        }
        return text.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("`", "\\`")
                .replace("[", "\\[");
    }
}
