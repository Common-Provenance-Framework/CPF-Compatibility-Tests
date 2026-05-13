package org.commonprovenance.compatibility;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openprovenance.prov.interop.InteropFramework;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.interop.Formats;

class ProvDeserializationTest {
  private final InteropFramework interopFramework = new InteropFramework();
  private String lastExceptionMessage = "";

  static Stream<Arguments> issueDocuments() throws IOException {
    return TestDocumentSource.issueDocuments();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("issueDocuments")
  void deserializesWithProvToolbox(String displayName, Path documentPath) {
    boolean passed = tryReadDocumentSilently(documentPath);
    System.out.printf("[PROV] %-70s %s%n", displayName, passed ? "PASS" : "FAIL");


    Boolean expected = SelectedExpectations.expectedProv(displayName);
    if (expected != null) {
      if (!expected) {
        String errorMsg = String.format("[ASSERTION]: fail to deserialize (%s)", lastExceptionMessage);
        System.err.println(errorMsg);
      }
      assertEquals(expected, passed,
          () -> "Selected PROV expectation mismatch for " + displayName);
    }

  }

  private boolean tryReadDocumentSilently(Path documentPath) {
    PrintStream originalErr = System.err;
    try {
      // ProvToolbox prints full stack traces to stderr for parsing failures.
      System.setErr(new PrintStream(OutputStream.nullOutputStream()));
      readDocument(documentPath);
      lastExceptionMessage = "";
      return true;
    } catch (Exception exception) {
      lastExceptionMessage = exception.getClass().getSimpleName() + ": " + exception.getMessage()
      + "\n" +  exception.getCause().getClass().getSimpleName() + ": " + exception.getCause().getMessage();
      return false;
    } finally {
      System.setErr(originalErr);
    }
  }

  private Document readDocument(Path documentPath) throws IOException {
    try (InputStream inputStream = Files.newInputStream(documentPath)) {
      return interopFramework.readDocument(inputStream, Formats.ProvFormat.JSON);
    }
  }
}