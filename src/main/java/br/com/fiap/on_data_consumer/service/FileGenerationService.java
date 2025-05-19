package br.com.fiap.on_data_consumer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.FileOutputStream;
import java.io.IOException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.time.format.DateTimeFormatter;

import br.com.fiap.on_data_consumer.dto.OcorrenciaMessage;

@Service
public class FileGenerationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileGenerationService.class);

    private final OllamaChatModel chatClient;

    @Autowired
    public FileGenerationService(OllamaChatModel chatClient) {
        this.chatClient = chatClient;
    }

    public void generateOcorrenciaPDF(OcorrenciaMessage occurrence) {
        LOGGER.info("Generating PDF for occurrence: {}", occurrence.getOccurrenceId());

        Document document = new Document();

        try {
            String fileName = "pdf/Ocorrencia_" + occurrence.getOccurrenceId() + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // Format the date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String formattedDate = occurrence.getCreatedAt().format(formatter);

            // AI personalization prompt
            String aiPrompt = String.format(
                    "Gere uma mensagem personalizada e empática para um paciente chamado %s que acabou de registrar uma ocorrência com a seguinte descrição: \"%s\". O status da ocorrência é \"%s\". Seja cordial, reforce o suporte da empresa, mencione o número da ocorrência (%s) e a data de registro (%s). Não podem haver placeholders na mensagem",
                    occurrence.getPatientName(),
                    occurrence.getDescription(),
                    occurrence.getStatus(),
                    occurrence.getOccurrenceId(),
                    formattedDate
            );

            String personalizedMessage = chatClient.call(aiPrompt);

            // Create the content of the PDF
            document.add(new Paragraph("Nova Ocorrência Registrada - Odontoprev"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph(personalizedMessage));
            document.add(new Paragraph("\n\nAtenciosamente,\nEquipe Odontoprev"));

            LOGGER.info("PDF generated successfully: {}", fileName);
        } catch (DocumentException | IOException e) {
            LOGGER.error("Failed to generate PDF: {}", e.getMessage());
        } finally {
            document.close();
        }
    }
}
