package jrestful.quarkus.runtime;

import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

import java.util.List;

@Recorder
public class JRestfulRecorder {
  public RuntimeValue<JRestfulResourceRegistry> registry(final List<String> resourceClassNames) {
    return new RuntimeValue<>(new JRestfulResourceRegistry(resourceClassNames));
  }
}
