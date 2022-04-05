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

import static org.killbill.billing.plugin.adyen.core.AdyenActivator.PLUGIN_NAME;

import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.joda.time.DateTime;
import org.killbill.billing.account.api.Account;
import org.killbill.billing.account.api.AccountApiException;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.osgi.libs.killbill.OSGIConfigPropertiesService;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.payment.api.Payment;
import org.killbill.billing.payment.api.PaymentApiException;
import org.killbill.billing.payment.api.PaymentMethodPlugin;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.payment.api.TransactionType;
import org.killbill.billing.payment.plugin.api.GatewayNotification;
import org.killbill.billing.payment.plugin.api.HostedPaymentPageFormDescriptor;
import org.killbill.billing.payment.plugin.api.PaymentMethodInfoPlugin;
import org.killbill.billing.payment.plugin.api.PaymentPluginApiException;
import org.killbill.billing.payment.plugin.api.PaymentPluginStatus;
import org.killbill.billing.payment.plugin.api.PaymentTransactionInfoPlugin;
import org.killbill.billing.plugin.adyen.api.exceptions.PaymentMethodException;
import org.killbill.billing.plugin.adyen.client.GatewayProcessor;
import org.killbill.billing.plugin.adyen.client.GatewayProcessorFactory;
import org.killbill.billing.plugin.adyen.core.AdyenConfigurationHandler;
import org.killbill.billing.plugin.adyen.dao.AdyenDao;
import org.killbill.billing.plugin.adyen.dao.gen.tables.AdyenPaymentMethods;
import org.killbill.billing.plugin.adyen.dao.gen.tables.AdyenResponses;
import org.killbill.billing.plugin.adyen.dao.gen.tables.records.AdyenPaymentMethodsRecord;
import org.killbill.billing.plugin.adyen.dao.gen.tables.records.AdyenResponsesRecord;
import org.killbill.billing.plugin.api.PluginProperties;
import org.killbill.billing.plugin.api.payment.PluginGatewayNotification;
import org.killbill.billing.plugin.api.payment.PluginHostedPaymentPageFormDescriptor;
import org.killbill.billing.plugin.api.payment.PluginPaymentPluginApi;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.billing.util.callcontext.TenantContext;
import org.killbill.clock.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdyenPaymentPluginApi
    extends PluginPaymentPluginApi<
        AdyenResponsesRecord, AdyenResponses, AdyenPaymentMethodsRecord, AdyenPaymentMethods> {

  private static final Logger logger = LoggerFactory.getLogger(AdyenPaymentPluginApi.class);
  private static final String INTERNAL = "INTERNAL";
  private final AdyenConfigurationHandler adyenConfigurationHandler;
  private final AdyenDao adyenDao;

  private AdyenPaymentPluginApiHelper helper;

  public AdyenPaymentPluginApi(
      final AdyenConfigurationHandler adyenConfigPropertiesConfigurationHandler,
      final OSGIKillbillAPI killbillAPI,
      final OSGIConfigPropertiesService configProperties,
      final Clock clock,
      final AdyenDao dao) {
    super(killbillAPI, configProperties, clock, dao);
    this.adyenConfigurationHandler = adyenConfigPropertiesConfigurationHandler;
    this.adyenDao = dao;
    this.helper = new AdyenPaymentPluginApiHelper(killbillAPI, dao);
  }

  @Override
  public List<PaymentTransactionInfoPlugin> getPaymentInfo(
      final UUID kbAccountId,
      final UUID kbPaymentId,
      final Iterable<PluginProperty> properties,
      final TenantContext context)
      throws PaymentPluginApiException {
    logger.info("[getPaymentInfo] getPaymentInfo for account {}", kbAccountId);
    final List<AdyenResponsesRecord> records;

    List<PaymentTransactionInfoPlugin> result = new ArrayList<>();
    try {

      records = this.adyenDao.getSuccessfulPurchaseResponseList(kbPaymentId, context.getTenantId());
      if (records == null || records.isEmpty()) {

        return new ArrayList<>();
      }

    } catch (SQLException e) {
      logger.error("Error trying to access de DB ", e);
    }

    return result;
  }

  @Override
  protected PaymentTransactionInfoPlugin buildPaymentTransactionInfoPlugin(
      final AdyenResponsesRecord adyenRecord) {
    return AdyenPaymentTransactionInfoPlugin.build(adyenRecord);
  }

  @Override
  public PaymentMethodPlugin getPaymentMethodDetail(
      final UUID kbAccountId,
      final UUID kbPaymentMethodId,
      final Iterable<PluginProperty> properties,
      final TenantContext context)
      throws PaymentPluginApiException {
    logger.info("[getPaymentMethodDetail] Getting Payment Method Detail");
    AdyenPaymentMethodsRecord record;
    try {
      record = dao.getPaymentMethod(kbPaymentMethodId, context.getTenantId());

    } catch (final SQLException e) {
      logger.error(
          "[getPaymentMethodDetail] Unable to retrieve payment method for kbPaymentMethodId "
              + kbPaymentMethodId
              + " "
              + e.getMessage());
      throw new PaymentPluginApiException(
          "Unable to retrieve payment method for kbPaymentMethodId " + kbPaymentMethodId, e);
    }

    if (record == null) {
      // return error if null
      return new AdyenPaymentMethodPlugin(
          kbPaymentMethodId, null, false, ImmutableList.<PluginProperty>of());
    } else {
      return buildPaymentMethodPlugin(record);
    }
  }

  @Override
  protected PaymentMethodPlugin buildPaymentMethodPlugin(
      final AdyenPaymentMethodsRecord adyenRecord) {
    return AdyenPaymentMethodPlugin.build(adyenRecord);
  }

  @Override
  protected PaymentMethodInfoPlugin buildPaymentMethodInfoPlugin(
      final AdyenPaymentMethodsRecord adyenRecord) {
    return AdyenPaymentMethodInfoPlugin.build(adyenRecord);
  }

  @Override
  public void addPaymentMethod(
      UUID kbAccountId,
      UUID kbPaymentMethodId,
      PaymentMethodPlugin paymentMethodProps,
      boolean setDefault,
      Iterable<PluginProperty> properties,
      CallContext context)
      throws PaymentPluginApiException {
    logger.info("[addPaymentMethod] Adding Payment Method");
    final Map<String, String> mergedProperties =
        PluginProperties.toStringMap(paymentMethodProps.getProperties(), properties);

    try {
      this.adyenDao.addPaymentMethod(
          kbAccountId,
          kbPaymentMethodId,
          mergedProperties,
          null,
          context.getTenantId(),
          null,
          null,
          setDefault);
    } catch (SQLException e) {

      throw new PaymentMethodException("[addPaymentMethod] Error inserting payment method", e);
    }
  }

  @Override
  protected String getPaymentMethodId(final AdyenPaymentMethodsRecord adyenRecord) {
    return adyenRecord.getKbPaymentMethodId();
  }

  @Override
  public void deletePaymentMethod(
      final UUID kbAccountId,
      final UUID kbPaymentMethodId,
      final Iterable<PluginProperty> properties,
      final CallContext context)
      throws PaymentPluginApiException {
    logger.info("[deletePaymentMethod] Deleting Payment Method");
    super.deletePaymentMethod(kbAccountId, kbPaymentMethodId, properties, context);
  }

  @Override
  public List<PaymentMethodInfoPlugin> getPaymentMethods(
      final UUID kbAccountId,
      final boolean refreshFromGateway,
      final Iterable<PluginProperty> properties,
      final CallContext context)
      throws PaymentPluginApiException {
    logger.info("[getPaymentMethods] Getting Payment Method");
    /* Disabled and returning only the methods in db. Normally, we would synch gateway payment methods with db payment methods */
    return super.getPaymentMethods(kbAccountId, false, properties, context);
  }

  @Override
  public PaymentTransactionInfoPlugin authorizePayment(
      final UUID kbAccountId,
      final UUID kbPaymentId,
      final UUID kbTransactionId,
      final UUID kbPaymentMethodId,
      final BigDecimal amount,
      final Currency currency,
      final Iterable<PluginProperty> properties,
      final CallContext context)
      throws PaymentPluginApiException {

    return AdyenPaymentTransactionInfoPlugin.unImplementedAPI(TransactionType.AUTHORIZE);
  }

  @Override
  public PaymentTransactionInfoPlugin capturePayment(
      final UUID kbAccountId,
      final UUID kbPaymentId,
      final UUID kbTransactionId,
      final UUID kbPaymentMethodId,
      final BigDecimal amount,
      final Currency currency,
      final Iterable<PluginProperty> properties,
      final CallContext context)
      throws PaymentPluginApiException {
    return AdyenPaymentTransactionInfoPlugin.unImplementedAPI(TransactionType.CAPTURE);
  }

  @Override
  public PaymentTransactionInfoPlugin purchasePayment(
      final UUID kbAccountId,
      final UUID kbPaymentId,
      final UUID kbTransactionId,
      final UUID kbPaymentMethodId,
      final BigDecimal amount,
      final Currency currency,
      final Iterable<PluginProperty> properties,
      final CallContext context)
      throws PaymentPluginApiException {
    logger.info("Purchase Payment for account {}", kbAccountId);
    final Map<String, String> mergedProperties = PluginProperties.toStringMap(properties);
    GatewayProcessor gatewayProcessor =
        GatewayProcessorFactory.get(
            "CC_RECURRING",
            adyenConfigurationHandler.getConfigurable(context.getTenantId()),
            null,
            adyenDao);
    ProcessorInputDTO input =
        gatewayProcessor.validateData(
            adyenConfigurationHandler, mergedProperties, kbPaymentMethodId, kbAccountId);
    ProcessorOutputDTO outputDTO = gatewayProcessor.processPayment(input);

    return new AdyenPaymentTransactionInfoPlugin(
        null,
        kbPaymentId,
        kbTransactionId,
        TransactionType.PURCHASE,
        amount,
        currency,
        PaymentPluginStatus.PENDING,
        null,
        null,
        outputDTO.getFirstPaymentReferenceId(),
        outputDTO.getSecondPaymentReferenceId(),
        DateTime.now(),
        DateTime.now(),
        null);
  }

  @Override
  public PaymentTransactionInfoPlugin voidPayment(
      final UUID kbAccountId,
      final UUID kbPaymentId,
      final UUID kbTransactionId,
      final UUID kbPaymentMethodId,
      final Iterable<PluginProperty> properties,
      final CallContext context)
      throws PaymentPluginApiException {
    return AdyenPaymentTransactionInfoPlugin.unImplementedAPI(TransactionType.VOID);
  }

  @Override
  public PaymentTransactionInfoPlugin creditPayment(
      final UUID kbAccountId,
      final UUID kbPaymentId,
      final UUID kbTransactionId,
      final UUID kbPaymentMethodId,
      final BigDecimal amount,
      final Currency currency,
      final Iterable<PluginProperty> properties,
      final CallContext context)
      throws PaymentPluginApiException {
    return AdyenPaymentTransactionInfoPlugin.unImplementedAPI(TransactionType.CREDIT);
  }

  @Override
  public PaymentTransactionInfoPlugin refundPayment(
      final UUID kbAccountId,
      final UUID kbPaymentId,
      final UUID kbTransactionId,
      final UUID kbPaymentMethodId,
      final BigDecimal amount,
      final Currency currency,
      final Iterable<PluginProperty> properties,
      final CallContext context)
      throws PaymentPluginApiException {
    logger.info("Refund Payment for account {}", kbAccountId);
    AdyenResponsesRecord adyenRecord = null;
    String paymentMethodAdditionalData = helper.getPaymentMethodsByMethodId(kbPaymentMethodId);
    //    if (helper.refundValidations(paymentMethodAdditionalData) != null) {
    //      return helper.refundValidations(paymentMethodAdditionalData);
    //    }

    //    try {
    //      adyenRecord = this.adyenDao.getSuccessfulPurchaseResponse(kbPaymentId,
    // context.getTenantId());
    //      if (helper.refundValidations(adyenRecord, amount) != null) {
    //        return helper.refundValidations(adyenRecord, amount);
    //      }
    //      if (adyenRecord.getAmount().compareTo(amount) == 0) {
    //    	  // is Void
    //      }
    //
    //    } catch (SQLException e) {
    //      logger.error("[refundPayment]  but we encountered a database error", e);
    //      return AdyenPaymentTransactionInfoPlugin.cancelPaymentTransactionInfoPlugin(
    //          TransactionType.REFUND, "[refundPayment] but we encountered a database error");
    //    }

    final Map<String, String> mergedProperties = PluginProperties.toStringMap(properties);

    return new AdyenPaymentTransactionInfoPlugin(
        null,
        kbPaymentId,
        kbTransactionId,
        TransactionType.REFUND,
        amount,
        currency,
        PaymentPluginStatus.PROCESSED,
        null,
        null,
        kbPaymentMethodId.toString(),
        null,
        DateTime.now(),
        DateTime.now(),
        null);
  }

  @FunctionalInterface
  public interface Callback {
    void saveRequest(Map<String, String> map, List<String> keyOfValuesToEncrypt);
  }

  @Override
  public HostedPaymentPageFormDescriptor buildFormDescriptor(
      final UUID kbAccountId,
      final Iterable<PluginProperty> customFields,
      final Iterable<PluginProperty> properties,
      final CallContext context)
      throws PaymentPluginApiException {
    UUID paymentMethodId = null;
    try {
      Account kbAccount = killbillAPI.getAccountUserApi().getAccountById(kbAccountId, context);
      paymentMethodId =
          killbillAPI
              .getPaymentApi()
              .addPaymentMethod(kbAccount, null, PLUGIN_NAME, true, null, properties, context);
      final Map<String, String> mergedProperties =
          PluginProperties.toStringMap(properties, customFields);
      Payment payment =
          killbillAPI
              .getPaymentApi()
              .createPurchase(
                  kbAccount,
                  paymentMethodId,
                  UUID.randomUUID(),
                  BigDecimal.valueOf(Long.valueOf(mergedProperties.get("amount"))),
                  kbAccount.getCurrency(),
                  DateTime.now(),
                  null,
                  null,
                  properties,
                  context);
      return new PluginHostedPaymentPageFormDescriptor(
          kbAccount.getId(),
          this.adyenConfigurationHandler.getConfigurable(context.getTenantId()).getReturnUrl());
    } catch (PaymentApiException | AccountApiException e) {

      throw new PaymentPluginApiException(INTERNAL, e.getMessage());
    }
  }

  @Override
  public GatewayNotification processNotification(
      final String notification,
      final Iterable<PluginProperty> properties,
      final CallContext context)
      throws PaymentPluginApiException {
    logger.info("Notification recieved");
    return new PluginGatewayNotification(notification);
  }

  public AdyenPaymentPluginApiHelper getHelper() {
    return helper;
  }
}
