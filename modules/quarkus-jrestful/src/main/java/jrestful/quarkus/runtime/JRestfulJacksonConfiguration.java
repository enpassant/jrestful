package jrestful.quarkus.runtime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;
import jrestful.fp.Option;

@Singleton
public class JRestfulJacksonConfiguration implements ObjectMapperCustomizer {
  @Override
  public void customize(final ObjectMapper objectMapper) {
    objectMapper.addMixIn(Option.class, OptionMixin.class);
  }

  @JsonIgnoreProperties({"empty", "present"})
  private abstract static class OptionMixin {
  }
}
