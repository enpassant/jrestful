package jrestful;

import jrestful.link.RelLink;
import jrestful.type.MediaType;
import jrestful.type.Types;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record RestApi(Types types, Transition... transition) {
  @Override
  public String toString() {
    return prettyPrint(0);
  }

  public String prettyPrint(final int tab) {
    final String indent = " ".repeat(tab);

    final String transitions = Arrays.stream(transition)
      .map(Objects::toString)
      .collect(Collectors.joining("\n    " + indent));

    final String mediaTypes = types.prettyPrint(tab + 2);

    return
      "\n" + indent + "RestApi: " +
        "\n" + mediaTypes +
        "\n\n  " + indent + "Transitions:" +
        "\n    " + indent + transitions +
        '}';
  }

  public Optional<RelLink> getLink(final Context context, final String rel) {
    return Arrays.stream(transition())
      .filter(transition -> transition.context().equals(context))
      .map(Transition::relLink)
      .filter(relLink -> relLink.rel().equalsIgnoreCase(rel))
      .findAny();
  }

  public Optional<MediaType> getMediaType(final String contentType) {
    return Arrays.stream(transition())
      .flatMap(t -> Stream.concat(t.relLink().in().stream(), Stream.of(t.relLink().out())))
      .distinct()
      .filter(mediaType -> mediaType.name().equalsIgnoreCase(contentType))
      .findAny();
  }

  public Optional<Transition> getTransition(final String transitionName) {
    return Arrays.stream(transition())
      .filter(t -> t.name().equalsIgnoreCase(transitionName))
      .findAny();
  }
}
