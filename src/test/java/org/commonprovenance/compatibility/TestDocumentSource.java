package org.commonprovenance.compatibility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

final class TestDocumentSource {
    private static final Path REPOSITORY_ROOT = Paths.get("").toAbsolutePath().normalize();

    private TestDocumentSource() {
    }

    static Stream<Arguments> issueDocuments() throws IOException {
        List<Path> documents;
        try (Stream<Path> directories = Files.list(REPOSITORY_ROOT)) {
            documents = directories
                    .filter(Files::isDirectory)
                    .filter(path -> path.getFileName().toString().startsWith("issue"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .flatMap(TestDocumentSource::listJsonFiles)
                    .collect(Collectors.toList());
        }

        return documents.stream().map(path -> Arguments.of(displayName(path), path));
    }

    static Path w3cSchemaPath() {
        String override = System.getProperty("prov.w3c.schema.path");
        if (override != null && !override.isBlank()) {
            return Paths.get(override).toAbsolutePath().normalize();
        }

        return REPOSITORY_ROOT
                .resolveSibling("CPF-Storage")
                .resolve("src/main/resources/prov-json-schama-original.json")
                .toAbsolutePath()
                .normalize();
    }

    private static Stream<Path> listJsonFiles(Path directory) {
        try {
            return Files.list(directory)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to list JSON files in " + directory, exception);
        }
    }

    private static String displayName(Path path) {
        Path relativePath = REPOSITORY_ROOT.relativize(path);
        return relativePath.getParent().getFileName() + "/" + relativePath.getFileName();
    }
}