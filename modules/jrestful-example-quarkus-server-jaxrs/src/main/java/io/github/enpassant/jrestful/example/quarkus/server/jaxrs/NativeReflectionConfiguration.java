package io.github.enpassant.jrestful.example.quarkus.server.jaxrs;

import io.github.enpassant.jrestful.example.quarkus.api.*;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jrestful.RestApi;
import jrestful.Root;
import jrestful.Transition;
import jrestful.fp.None;
import jrestful.fp.Some;
import jrestful.link.RelLink;
import jrestful.type.*;

@RegisterForReflection(targets = {
  RestApi.class, Root.class, Transition.class, RelLink.class,
  None.class, Some.class, Types.class, MediaType.class, TypeList.class, TypeObject.class,
  Account.class, AccountNumber.class, Name.class, ChangeName.class, Deposit.class, Withdraw.class,
  AccountResource.class
})
final class NativeReflectionConfiguration {
  private NativeReflectionConfiguration() { }
}
