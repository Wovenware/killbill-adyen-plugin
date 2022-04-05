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
package org.killbill.billing.plugin.adyen.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.payment.api.TransactionType;
import org.killbill.billing.payment.plugin.api.PaymentTransactionInfoPlugin;
import org.killbill.billing.plugin.adyen.dao.AdyenDao;
import org.killbill.billing.plugin.adyen.dao.gen.tables.records.AdyenPaymentMethodsRecord;
import org.killbill.billing.plugin.adyen.dao.gen.tables.records.AdyenResponsesRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdyenPaymentPluginApiHelper {
  private static final String PAYMENT_METHOD_ID_KEY = "paymentMethodIDKey";
  private static final String MISSING_PAYMENT_METHOD = "Missing Payment Method";
  private static final String INVALID_PAYMENT_METHOD = "Payment Method is not valid";
  protected static final ObjectMapper objectMapper = new ObjectMapper();

  private final AdyenDao adyenDao;
  private static final Logger logger = LoggerFactory.getLogger(AdyenPaymentPluginApiHelper.class);
  private OSGIKillbillAPI killbillAPI;

  public AdyenPaymentPluginApiHelper(final OSGIKillbillAPI killbillAPI, final AdyenDao dao) {
    this.adyenDao = dao;
    this.killbillAPI = killbillAPI;
  }

  public String getValueFromAdditionalData(String additionalData, String key) {
    if (additionalData == null) {
      return null;
    }

    Map<String, String> map = this.getAdditionalDataMap(additionalData);
    if (map == null) {
      return null;
    }
    return map.get(key);
  }

  public Map<String, String> getAdditionalDataMap(String additionalData) {
    if (additionalData == null) {
      return Collections.emptyMap();
    }
    try {
      return objectMapper.readValue(additionalData, Map.class);

    } catch (Exception e) {
      logger.error("", e);
    }
    return Collections.emptyMap();
  }

  public String getPaymentMethodsByMethodId(UUID kbPaymentMethodId) {
    try {
      AdyenPaymentMethodsRecord methodRecord =
          this.adyenDao.getPaymentMethodsByMethodId(kbPaymentMethodId);
      if (PaymentMethodStatus.NOT_VALID.name().equals(methodRecord.getState())) {
        return PaymentMethodStatus.NOT_VALID.name();
      }
      return methodRecord.getAdditionalData();
    } catch (Exception e1) {
      logger.error("", e1);
      return null;
    }
  }

  //  public void storeRequest(
  //      final Map<String, String> requestParameters,
  //      final UUID kbAccountId,
  //      final UUID kbPaymentId,
  //      final UUID kbTransactionId,
  //      final UUID kbPaymentMethodId,
  //      final List<String> keyOfValuesToEncrypt,
  //      final UUID context) {
  //    try {
  //      Map<String, String> requestParametersWithEncryption = new LinkedHashMap<>();
  //
  //      for (Map.Entry<String, String> entry : requestParameters.entrySet()) {
  //        String key = entry.getKey();
  //        String value = entry.getValue();
  //        if (keyOfValuesToEncrypt.contains(key)) {
  //          AESUtil.getInstance();
  //          value = AESUtil.encrypt(value);
  //        }
  //        requestParametersWithEncryption.put(key, value);
  //      }
  //      this.spsDao.addRequest(
  //          kbAccountId,
  //          kbPaymentId,
  //          kbTransactionId,
  //          kbPaymentMethodId,
  //          requestParametersWithEncryption,
  //          context);
  //    } catch (SQLException | GeneralSecurityException e) {
  //      logger.error("[sps-plugin][storeRequest] Error inserting request ", e);
  //    }
  //  }

  public List<PluginProperty> mapToPluginPropertyList(Map<String, String> map) {
    List<PluginProperty> pluginList = new ArrayList<>();
    StringBuilder mapAsString = new StringBuilder();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();

      mapAsString.append(key + "=" + value + "&");
    }
    mapAsString.delete(mapAsString.length() - 1, mapAsString.length());
    pluginList.add(new PluginProperty("Response", mapAsString, true));
    return pluginList;
  }

  public PaymentTransactionInfoPlugin refundValidations(
      AdyenResponsesRecord adyenRecord, BigDecimal amount) {
    if (adyenRecord == null) {
      logger.error("[refundPayment] Purchase do not exists");
      return AdyenPaymentTransactionInfoPlugin.cancelPaymentTransactionInfoPlugin(
          TransactionType.REFUND, "Purchase do not exists");
    }

    if (adyenRecord.getAmount().compareTo(amount) < 0) {
      logger.error("[refundPayment] The refund amount is more than the transaction amount");
      return AdyenPaymentTransactionInfoPlugin.cancelPaymentTransactionInfoPlugin(
          TransactionType.REFUND, "The refund amount is more than the transaction amount");
    }
    if (BigDecimal.ZERO.compareTo(amount) == 0) {
      logger.error("[refundPayment] The refund amount can not be zero");
      return AdyenPaymentTransactionInfoPlugin.cancelPaymentTransactionInfoPlugin(
          TransactionType.REFUND, "The refund amount can not be zero");
    } else {
      return null;
    }
  }

  public PaymentTransactionInfoPlugin refundValidations(String paymentMethodAdditionalData) {
    if (paymentMethodAdditionalData == null) {
      logger.error("[refundPayment] Missing Payment Method");
      return AdyenPaymentTransactionInfoPlugin.cancelPaymentTransactionInfoPlugin(
          TransactionType.REFUND, MISSING_PAYMENT_METHOD);
    }
    if (PaymentMethodStatus.NOT_VALID.name().equals(paymentMethodAdditionalData)) {
      logger.error("[refundPayment] Payment Method is not valid");
      return AdyenPaymentTransactionInfoPlugin.cancelPaymentTransactionInfoPlugin(
          TransactionType.REFUND, INVALID_PAYMENT_METHOD);
    }
    return null;
  }

  public PaymentTransactionInfoPlugin purchaseValidations(String paymentMethodAdditionalData) {
    if (paymentMethodAdditionalData == null) {
      logger.error("[purchasePayment] Missing Payment Method");
      return AdyenPaymentTransactionInfoPlugin.cancelPaymentTransactionInfoPlugin(
          TransactionType.PURCHASE, MISSING_PAYMENT_METHOD);
    }
    if (PaymentMethodStatus.NOT_VALID.name().equals(paymentMethodAdditionalData)) {
      logger.error(" [purchasePayment] Payment Method is not valid");
      return AdyenPaymentTransactionInfoPlugin.cancelPaymentTransactionInfoPlugin(
          TransactionType.PURCHASE, INVALID_PAYMENT_METHOD);
    }
    return null;
  }
}
