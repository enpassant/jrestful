package io.github.enpassant.jrestful.example.quarkus.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;
import jrestful.fp.Option;

@Singleton
public class JacksonConfiguration implements ObjectMapperCustomizer {
  @Override
  public void customize(final ObjectMapper objectMapper) {
    objectMapper.addMixIn(Option.class, OptionMixin.class);
  }

  @JsonIgnoreProperties({"empty", "present"})
  private abstract static class OptionMixin {
  }
}
