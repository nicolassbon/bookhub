package com.bookhub.identity.application.auth;

import com.bookhub.identity.application.auth.TokenIssuer.IssuedTokenPair;
import com.bookhub.identity.domain.auth.ServicePrincipal;

public interface ServiceTokenIssuer {

  IssuedTokenPair issueFor(ServicePrincipal principal);
}
