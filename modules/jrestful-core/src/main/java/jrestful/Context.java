package jrestful;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jrestful.type.MediaType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = Root.class, name = "Root"),
  @JsonSubTypes.Type(value = MediaType.class, name = "MediaType")
})
public interface Context {
  String name();
}
