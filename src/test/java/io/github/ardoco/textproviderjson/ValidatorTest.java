package io.github.ardoco.textproviderjson;

import io.github.ardoco.textproviderjson.converter.Converter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class ValidatorTest {

    @Test
    void testValidating () throws IOException {
        Assertions.assertTrue(Converter.validateJson(Files.readString(Path.of("./src/test/resources/valid-example-text.json"))));
        Assertions.assertFalse(Converter.validateJson(Files.readString(Path.of("./src/test/resources/invalid-example-text.json"))));

    }

}
