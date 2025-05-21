package br.com.fiap.on_data_consumer.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
public class TelegramBotService extends TelegramLongPollingBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramBotService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.chat.id}")
    private String chatId;

    public TelegramBotService(@Value("${telegram.bot.token}") String botToken) {
        super(botToken);
    }

    @PostConstruct
    public void initialize() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(this);
            LOGGER.info("Telegram bot initialized successfully for {}", botUsername);

            String startupMsg = String.format(
                    "🤖 *Bot Service Started*\n" +
                            "⏰ Startup Time (UTC): %s\n",
                    LocalDateTime.now(ZoneOffset.UTC).format(DATE_FORMATTER)
            );
            sendMessageToTelegram(startupMsg);

        } catch (TelegramApiException e) {
            LOGGER.error("Failed to initialize Telegram bot: {}", e.getMessage());
        }
    }

    public void sendMessageToTelegram(String message) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .parseMode(ParseMode.MARKDOWN)
                .build();

        try {
            execute(sendMessage);
            LOGGER.info("Message sent to Telegram successfully");
        } catch (TelegramApiException e) {
            LOGGER.error("Failed to send message to Telegram: {}", e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            LOGGER.info("Received message: {}", update.getMessage().getText());
        }
    }
}
