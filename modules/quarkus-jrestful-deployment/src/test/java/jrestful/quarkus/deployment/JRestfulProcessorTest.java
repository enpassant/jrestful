package jrestful.quarkus.deployment;

import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JRestfulProcessorTest {
  private final JRestfulProcessor processor = new JRestfulProcessor();

  @Test
  void acceptsAnApplicationWithoutQuarkusRest() {
    assertDoesNotThrow(() -> processor.feature(new Capabilities(Set.of())));
  }

  @Test
  void rejectsQuarkusRestDuringBuild() {
    final IllegalStateException exception = assertThrows(IllegalStateException.class,
      () -> processor.feature(new Capabilities(Set.of(Capability.REST))));

    assertTrue(exception.getMessage().contains("mutually exclusive"));
  }
}
