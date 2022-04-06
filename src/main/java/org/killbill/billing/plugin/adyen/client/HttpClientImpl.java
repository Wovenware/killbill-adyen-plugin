/*
 * Copyright 2021 Wovenware, Inc
 *
 * Wovenware licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.killbill.billing.plugin.adyen.client;

import com.adyen.Client;
import com.adyen.enums.Environment;
import com.adyen.model.Amount;
import com.adyen.model.checkout.CreateCheckoutSessionRequest;
import com.adyen.model.checkout.CreateCheckoutSessionResponse;
import com.adyen.service.Checkout;
import com.adyen.service.exception.ApiException;
import java.io.IOException;
import java.math.BigDecimal;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.plugin.adyen.core.AdyenConfigProperties;

public class HttpClientImpl implements HttpClient {

  private final AdyenConfigProperties adyenConfigProperties;
  private final Checkout checkout;

  public HttpClientImpl(AdyenConfigProperties adyenConfigProperties) {
    this.adyenConfigProperties = adyenConfigProperties;
    Client client = new Client(adyenConfigProperties.getApiKey(), Environment.TEST);
    checkout = new Checkout(client);
  }

  @Override
  public CreateCheckoutSessionResponse checkoutsessions(
      Currency currency, BigDecimal kbAmount, String transactionId)
      throws IOException, ApiException {

    Amount amount = new Amount().currency(currency.name()).value(kbAmount.longValue());
    CreateCheckoutSessionRequest checkoutSession = new CreateCheckoutSessionRequest();
    checkoutSession.merchantAccount(adyenConfigProperties.getMerchantAccount());
    checkoutSession.setChannel(CreateCheckoutSessionRequest.ChannelEnum.WEB);
    checkoutSession.setReference(transactionId);
    checkoutSession.setReturnUrl(adyenConfigProperties.getReturnUrl());
    checkoutSession.setAmount(amount);

    return checkout.sessions(checkoutSession);
  }
}
