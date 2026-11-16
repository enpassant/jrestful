package io.github.enpassant.jrestful.example.quarkus.server.jaxrs;

import io.github.enpassant.jrestful.example.quarkus.api.*;
import io.quarkus.runtime.annotations.RegisterForReflection;
@RegisterForReflection(targets = {
  Account.class, AccountNumber.class, Name.class, ChangeName.class, Deposit.class, Withdraw.class
})
final class NativeReflectionConfiguration {
  private NativeReflectionConfiguration() { }
}
