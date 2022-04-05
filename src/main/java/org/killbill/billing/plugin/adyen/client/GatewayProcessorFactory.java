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

import org.killbill.billing.plugin.adyen.api.AdyenPaymentPluginApi.Callback;
import org.killbill.billing.plugin.adyen.api.PaymentMethod;
import org.killbill.billing.plugin.adyen.core.AdyenConfigProperties;
import org.killbill.billing.plugin.adyen.dao.AdyenDao;

public class GatewayProcessorFactory {
  private GatewayProcessorFactory() {}

  public static GatewayProcessor get(
      String paymentMethod,
      AdyenConfigProperties adyenConfigProperties,
      Callback callback,
      AdyenDao dao) {
    HttpClientImpl httpClient = new HttpClientImpl(adyenConfigProperties);
    if (PaymentMethod.CC_ONE_TIME.toString().equals(paymentMethod)
        || PaymentMethod.CC_RECURRING.toString().equals(paymentMethod)) {
      return new CreditCardProcessorImpl(httpClient, adyenConfigProperties, callback, dao);
    }

    return new DefaultGatewayProcessor();
  }
}
