package com.provectus.kafka.ui.config.auth;

import static com.provectus.kafka.ui.config.auth.OAuthProperties.OAuth2Provider;
import static org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties.Provider;
import static org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties.Registration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OAuthPropertiesConverter {

  private static final String TYPE = "type";
  private static final String GOOGLE = "google";

  public static OAuth2ClientProperties convertProperties(final OAuthProperties properties) {
    final var result = new OAuth2ClientProperties();
    properties.getClient().forEach((key, provider) -> {
      var registration = new Registration();
      registration.setClientId(provider.getClientId());
      registration.setClientSecret(provider.getClientSecret());
      registration.setClientName(provider.getClientName());
      registration.setScope(provider.getScope());
      registration.setRedirectUri(provider.getRedirectUri());
      registration.setAuthorizationGrantType(provider.getAuthorizationGrantType());

      result.getRegistration().put(key, registration);

      var clientProvider = new Provider();
      applyCustomTransformations(provider);

      clientProvider.setAuthorizationUri(provider.getAuthorizationUri());
      clientProvider.setIssuerUri(provider.getIssuerUri());
      clientProvider.setJwkSetUri(provider.getJwkSetUri());
      clientProvider.setTokenUri(provider.getTokenUri());
      clientProvider.setUserInfoUri(provider.getUserInfoUri());
      clientProvider.setUserNameAttribute(provider.getUserNameAttribute());

      result.getProvider().put(key, clientProvider);
    });
    return result;
  }

  private static void applyCustomTransformations(OAuth2Provider provider) {
    applyGoogleTransformations(provider);
  }

  private static void applyGoogleTransformations(OAuth2Provider provider) {
    if (!isGoogle(provider)) {
      return;
    }

    String allowedDomain = provider.getCustomParams().get("allowedDomain");
    if (StringUtils.isEmpty(allowedDomain)) {
      return;
    }

    final String newUri = provider.getAuthorizationUri() + "?hd=" + allowedDomain;
    provider.setAuthorizationUri(newUri);
  }

  private static boolean isGoogle(OAuth2Provider provider) {
    return provider.getCustomParams().get(TYPE).equalsIgnoreCase(GOOGLE);
  }
}

