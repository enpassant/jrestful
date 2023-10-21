package jrestful.type;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = TypeList.class, name = "TypeList"),
  @JsonSubTypes.Type(value = TypeObject.class, name = "TypeObject"),
  @JsonSubTypes.Type(value = MediaType.class, name = "MediaType")
})
public interface Type {
}
