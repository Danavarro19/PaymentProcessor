package com.org.paymentprocessor.integration;

import com.org.paymentprocessor.exception.PaymentProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XmlFileWriterTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void shouldWritePaymentXmlToFile() throws IOException {
        Path outbox = temporaryDirectory.resolve("outbox");
        XmlFileWriter writer = new XmlFileWriter(outbox.toString());

        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <payment>
                    <id>PAY-1001</id>
                </payment>
                """;

        writer.write("PAY-1001", xml);

        Path expectedFile = outbox.resolve("payment-PAY-1001.xml");

        assertThat(expectedFile).exists();
        assertThat(Files.readString(expectedFile)).isEqualTo(xml);
    }

    @Test
    void shouldCreateOutboxDirectoryWhenItDoesNotExist() {
        Path outbox = temporaryDirectory.resolve("new-outbox");
        XmlFileWriter writer = new XmlFileWriter(outbox.toString());

        assertThat(outbox).doesNotExist();

        writer.write("PAY-1001", "<payment/>");

        assertThat(outbox).isDirectory();
        assertThat(outbox.resolve("payment-PAY-1001.xml")).exists();
    }

    @Test
    void shouldRejectPaymentIdThatEscapesOutboxDirectory() {
        Path outbox = temporaryDirectory.resolve("outbox");
        XmlFileWriter writer = new XmlFileWriter(outbox.toString());

        assertThatThrownBy(
                () -> writer.write("../../outside", "<payment/>")
        )
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessage("Invalid payment ID for output filename");
    }
}