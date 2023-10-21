package io.github.enpassant.jrestful.example.starter;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;

import java.util.List;
import java.util.Optional;

public class VertxAuthenticate implements AuthenticationProvider {

  private final JWTAuth jwtAuth;

  public VertxAuthenticate(final Vertx vertx) {
    final JsonObject jsonOptions = JsonObject.of("permissionsClaimKey", "permissions");
    final JWTAuthOptions config = new JWTAuthOptions(jsonOptions)
      .setKeyStore(new KeyStoreOptions()
        .setPath("keystore.pkcs12")
        .setPassword("jrestful_secret_pass")
      );

    this.jwtAuth = JWTAuth.create(vertx, config);
  }

  public Optional<String> login(final String userName, final String password) {
    if ("xxx".equals(password)) {
      final List<String> permissions = "admin".equals(userName) ?
        List.of("admin", "user") :
        List.of("user");
      final String token = jwtAuth.generateToken(
        new JsonObject()
          .put("sub", userName)
          .put("permissions", permissions),
        new JWTOptions().setAlgorithm("RS256")
      );
      return Optional.of(token);
    }
    return Optional.empty();
  }

  public JWTAuth getJwtAuth() {
    return jwtAuth;
  }

  @Override
  public void authenticate(final Credentials credentials, final Handler<AsyncResult<User>> resultHandler) {
    if (credentials instanceof final UsernamePasswordCredentials basicCredential) {
      final Optional<String> tokenOptional = login(basicCredential.getUsername(), basicCredential.getPassword());
      tokenOptional.ifPresentOrElse(
        token -> jwtAuth.authenticate(new TokenCredentials(token), resultHandler),
        () -> AuthenticationProvider.super.authenticate(credentials, resultHandler)
      );
    } else {
      AuthenticationProvider.super.authenticate(credentials, resultHandler);
    }
  }

  @Override
  public Future<User> authenticate(final Credentials credentials) {
    return AuthenticationProvider.super.authenticate(credentials);
  }

  @Override
  public void authenticate(final JsonObject credentials, final Handler<AsyncResult<User>> resultHandler) {

  }

  @Override
  public Future<User> authenticate(final JsonObject credentials) {
    return AuthenticationProvider.super.authenticate(credentials);
  }
}
