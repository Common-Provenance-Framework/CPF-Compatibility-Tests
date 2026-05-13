package org.commonprovenance.compatibility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class W3cSchemaValidationTest {
  private final ObjectMapper objectMapper = new ObjectMapper();
  private Schema schema;

  @BeforeAll
  void loadSchema() throws IOException {
    Path schemaPath = TestDocumentSource.w3cSchemaPath();
    assertTrue(Files.exists(schemaPath), () -> "W3C schema not found at " + schemaPath);

    try (InputStream inputStream = Files.newInputStream(schemaPath)) {
      schema = SchemaRegistry
          .withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
          .getSchema(inputStream);
    }
  }

  static Stream<Arguments> issueDocuments() throws IOException {
    return TestDocumentSource.issueDocuments();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("issueDocuments")
  void validatesAgainstW3cSchema(String displayName, Path documentPath) throws IOException {
    JsonNode documentNode;
    try (InputStream inputStream = Files.newInputStream(documentPath)) {
      documentNode = objectMapper.readTree(inputStream);
    }

    List<com.networknt.schema.Error> errors = schema.validate(documentNode);
    boolean passed = errors.isEmpty();
    System.out.printf("[W3C ] %-70s %s%n", displayName, passed ? "PASS" : "FAIL");

    Boolean expected = SelectedExpectations.expectedW3c(displayName);

    if (expected != null) {
      if (!expected) {
        String errorMsg = String.format("[ASSERTION]: fail with (errors: %s)", errors);
        System.err.println(errorMsg);
      }

      assertEquals(expected, passed,
          () -> "Selected W3C expectation mismatch for " + displayName + ": " + errors);
    }
  }
}