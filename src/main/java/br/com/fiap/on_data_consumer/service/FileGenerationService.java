package br.com.fiap.on_data_consumer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

            // Create the content of the PDF
            document.add(new Paragraph("Nova Ocorrência Registrada - Odontoprev"));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Olá " + occurrence.getPatientName() + ","));
            document.add(new Paragraph("\nInformamos que foi registrada uma nova ocorrência em nosso sistema:"));
            document.add(new Paragraph("\nNúmero da Ocorrência: " + occurrence.getOccurrenceId()));
            document.add(new Paragraph("Data de Registro: " + formattedDate));
            document.add(new Paragraph("Descrição: " + occurrence.getDescription()));
            document.add(new Paragraph("Status: " + occurrence.getStatus()));
            document.add(new Paragraph("\nPara mais informações ou esclarecimentos, entre em contato com a nossa Central de Atendimento."));
            document.add(new Paragraph("\n\nAtenciosamente,\nEquipe Odontoprev"));

            LOGGER.info("PDF generated successfully: {}", fileName);
        } catch (DocumentException | IOException e) {
            LOGGER.error("Failed to generate PDF: {}", e.getMessage());
        } finally {
            document.close();
        }
    }
}
