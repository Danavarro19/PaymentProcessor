package com.org.paymentprocessor.integration;

import com.org.paymentprocessor.exception.PaymentProcessingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Component
public class XmlFileWriter {

    private final Path outboxDirectory;

    public XmlFileWriter(
            @Value("${app.outbox.directory:outbox}") String directory
    ) {
        this.outboxDirectory = Path.of(directory)
                .toAbsolutePath()
                .normalize();
    }

    public void write(String paymentId, String xml) {
        if (paymentId == null ||
                !paymentId.matches("^[A-Za-z0-9_-]+$")) {
            throw new PaymentProcessingException(
                    "Invalid payment ID for output filename"
            );
        }

        Path outputFile = outboxDirectory
                .resolve("payment-" + paymentId + ".xml")
                .normalize();

        if (!outputFile.startsWith(outboxDirectory)) {
            throw new PaymentProcessingException(
                    "Invalid payment ID for output filename"
            );
        }

        try {
            Files.createDirectories(outboxDirectory);

            Files.writeString(
                    outputFile,
                    xml,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException exception) {
            throw new PaymentProcessingException(
                    "Could not write XML for payment " + paymentId,
                    exception
            );
        }
    }
}