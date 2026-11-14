package io.github.enpassant.jrestful.example.quarkus.client;

import io.github.enpassant.jrestful.example.quarkus.api.Account;
import io.github.enpassant.jrestful.example.quarkus.api.AccountNumber;
import io.github.enpassant.jrestful.example.quarkus.api.ChangeName;
import io.github.enpassant.jrestful.example.quarkus.api.Deposit;
import io.github.enpassant.jrestful.example.quarkus.api.Name;
import io.github.enpassant.jrestful.example.quarkus.api.Withdraw;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jrestful.RestApi;
import jrestful.Root;
import jrestful.Transition;
import jrestful.fp.None;
import jrestful.fp.Some;
import jrestful.link.RelLink;
import jrestful.type.MediaType;
import jrestful.type.TypeList;
import jrestful.type.TypeObject;
import jrestful.type.Types;

@RegisterForReflection(targets = {
  RestApi.class, Root.class, Transition.class, RelLink.class,
  None.class, Some.class, Types.class, MediaType.class, TypeList.class, TypeObject.class,
  Account.class, AccountNumber.class, Name.class, ChangeName.class, Deposit.class, Withdraw.class,
  LongExampleResult.class, SagaExampleResult.class
})
final class NativeReflectionConfiguration {
  private NativeReflectionConfiguration() {
  }
}
