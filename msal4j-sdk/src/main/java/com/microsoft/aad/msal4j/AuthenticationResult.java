// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.aad.msal4j;

import com.nimbusds.jwt.JWTParser;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

@Accessors(fluent = true)
@Getter
@EqualsAndHashCode
@Builder
final class AuthenticationResult implements IAuthenticationResult {
    private static final long serialVersionUID = 1L;

    private final String accessToken;

    @Getter(value = AccessLevel.PACKAGE)
    private final long expiresOn;

    @Getter(value = AccessLevel.PACKAGE)
    private final long extExpiresOn;

    private final String refreshToken;

    private final Long refreshOn;

    @Getter(value = AccessLevel.PACKAGE)
    private final String familyId;

    private final String idToken;

    @Getter(value = AccessLevel.PACKAGE, lazy = true)
    private final IdToken idTokenObject = getIdTokenObj();

    private IdToken getIdTokenObj() {
        if (StringHelper.isBlank(idToken)) {
            return null;
        }
        try {
            String idTokenJson = JWTParser.parse(idToken).getParsedParts()[1].decodeToString();

            return JsonHelper.convertJsonToObject(idTokenJson, IdToken.class);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Getter(value = AccessLevel.PACKAGE)
    private final AccountCacheEntity accountCacheEntity;

    @Getter(lazy = true)
    private final IAccount account = getAccount();

    private IAccount getAccount() {
        if (accountCacheEntity == null) {
            return null;
        }
        return accountCacheEntity.toAccount();
    }

    @Getter(lazy = true)
    private final ITenantProfile tenantProfile = getTenantProfile();

    private ITenantProfile getTenantProfile() {
        if (StringHelper.isBlank(idToken)) {
            return null;
        }

        try {
            return new TenantProfile(JWTParser.parse(idToken).getJWTClaimsSet().getClaims(),
                    getAccount().environment());
        } catch (ParseException e) {
            throw new MsalClientException("Cached JWT could not be parsed: " + e.getMessage(), AuthenticationErrorCode.INVALID_JWT);
        }
    }

    private String environment;

    @Getter(lazy = true)
    private final Date expiresOnDate = new Date(expiresOn * 1000);

    private final String scopes;

    @Builder.Default
    private final AuthenticationResultMetadata metadata = AuthenticationResultMetadata.builder().build();

    @Getter(value = AccessLevel.PACKAGE)
    private final Boolean isPopAuthorization;
}
