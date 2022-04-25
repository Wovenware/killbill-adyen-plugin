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

import com.adyen.model.notification.NotificationRequest;
import com.adyen.model.notification.NotificationRequestItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.killbill.billing.plugin.adyen.core.AdyenActivator;
import org.killbill.billing.plugin.adyen.core.AdyenConfigurationHandler;
import org.killbill.billing.plugin.adyen.dao.AdyenDao;
import org.killbill.billing.plugin.adyen.dao.gen.tables.AdyenPaymentMethods;
import org.killbill.billing.plugin.adyen.dao.gen.tables.AdyenResponses;
import org.killbill.billing.plugin.adyen.dao.gen.tables.records.AdyenPaymentMethodsRecord;
import org.killbill.billing.plugin.adyen.dao.gen.tables.records.AdyenResponsesRecord;
import org.killbill.billing.plugin.api.PluginCallContext;
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
  private static final String SESSION_DATA = "sessionData";
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
      for (AdyenResponsesRecord record : records) {
        Map<String, String> additionalDataMap =
            helper.getAdditionalDataMap(record.getAdditionalData());
        List<PluginProperty> pluginProperty = helper.mapToPluginPropertyList(additionalDataMap);
        PaymentTransactionInfoPlugin infoPlugin =
            new AdyenPaymentTransactionInfoPlugin(
                record,
                kbPaymentId,
                UUID.fromString(record.getKbPaymentTransactionId()),
                TransactionType.valueOf(record.getTransactionType()),
                record.getAmount(),
                Currency.valueOf(record.getCurrency()),
                PaymentPluginStatus.valueOf(record.getTransactionStatus()),
                null,
                null,
                record.getSessionId(),
                null,
                DateTime.parse(record.getCreatedDate().toString()),
                DateTime.parse(record.getCreatedDate().toString()),
                pluginProperty);
        result.add(infoPlugin);
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
    input.setAmount(amount);
    input.setKbTransactionId(kbTransactionId.toString());
    input.setCurrency(currency);
    ProcessorOutputDTO outputDTO = gatewayProcessor.processPayment(input);
    List<PluginProperty> formFields = new ArrayList<>();
    formFields.add(
        new PluginProperty(SESSION_DATA, outputDTO.getAdditionalData().get(SESSION_DATA), false));
    AdyenResponsesRecord adyenRecord = null;
    try {
      adyenRecord =
          this.adyenDao.addResponse(
              kbAccountId,
              kbPaymentId,
              kbTransactionId,
              TransactionType.PURCHASE,
              amount,
              currency,
              PaymentPluginStatus.PENDING,
              outputDTO.getFirstPaymentReferenceId(),
              outputDTO,
              context.getTenantId());
    } catch (SQLException e) {
      logger.error("We encountered a database error ", e);
    }
    return new AdyenPaymentTransactionInfoPlugin(
        adyenRecord,
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
        formFields);
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

    try {
      adyenRecord = this.adyenDao.getSuccessfulPurchaseResponse(kbPaymentId, context.getTenantId());
      if (helper.refundValidations(adyenRecord, amount) != null) {
        return helper.refundValidations(adyenRecord, amount);
      }

    } catch (SQLException e) {
      logger.error("[refundPayment]  but we encountered a database error", e);
      return AdyenPaymentTransactionInfoPlugin.cancelPaymentTransactionInfoPlugin(
          TransactionType.REFUND, "[refundPayment] but we encountered a database error");
    }

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
    input.setPspReference(adyenRecord.getPspReference());
    input.setAmount(amount);
    input.setKbTransactionId(kbTransactionId.toString());
    input.setCurrency(currency);
    ProcessorOutputDTO outputDTO = gatewayProcessor.refundPayment(input);

    try {
      adyenRecord =
          this.adyenDao.addResponse(
              kbAccountId,
              kbPaymentId,
              kbTransactionId,
              TransactionType.REFUND,
              amount,
              currency,
              PaymentPluginStatus.PENDING,
              outputDTO.getFirstPaymentReferenceId(),
              outputDTO,
              context.getTenantId());
    } catch (SQLException e) {
      logger.error("We encountered a database error ", e);
    }
    return new AdyenPaymentTransactionInfoPlugin(
        adyenRecord,
        kbPaymentId,
        kbTransactionId,
        TransactionType.REFUND,
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
    Account kbAccount = null;
    Payment payment = null;
    try {
      kbAccount = killbillAPI.getAccountUserApi().getAccountById(kbAccountId, context);
      //      paymentMethodId =
      //          killbillAPI
      //              .getPaymentApi()
      //              .addPaymentMethod(kbAccount, null, PLUGIN_NAME, true, null, properties,
      // context);
    } catch (AccountApiException e) {
      logger.error("Account Api {}", e.getMessage(), e);
      throw new PaymentPluginApiException(INTERNAL, e.getMessage());
    }
    try {
      final Map<String, String> mergedProperties =
          PluginProperties.toStringMap(properties, customFields);
      payment =
          killbillAPI
              .getPaymentApi()
              .createPurchase(
                  kbAccount,
                  UUID.fromString(mergedProperties.get("paymentMethodId")),
                  null,
                  BigDecimal.valueOf(Long.valueOf(mergedProperties.get("amount"))),
                  kbAccount.getCurrency(),
                  DateTime.now(),
                  null,
                  null,
                  properties,
                  context);
    } catch (PaymentApiException e) {
      logger.error("Payment Api {}", e.getMessage(), e);
      throw new PaymentPluginApiException(INTERNAL, e.getMessage());
    }
    logger.info("payment {}", payment);
    logger.info("payment transaction {}", payment.getTransactions());
    PaymentTransactionInfoPlugin paymentInfo =
        payment.getTransactions().get(payment.getTransactions().size() - 1).getPaymentInfoPlugin();

    List<PluginProperty> formFields = new ArrayList<>();
    formFields.add(
        new PluginProperty("sessionId", paymentInfo.getFirstPaymentReferenceId(), false));
    formFields.add(
        new PluginProperty(SESSION_DATA, paymentInfo.getProperties().get(0).getValue(), false));
    return new PluginHostedPaymentPageFormDescriptor(
        kbAccount.getId(),
        this.adyenConfigurationHandler.getConfigurable(context.getTenantId()).getReturnUrl(),
        formFields);
  }

  @Override
  public GatewayNotification processNotification(
      final String notification,
      final Iterable<PluginProperty> properties,
      final CallContext context)
      throws PaymentPluginApiException {
    logger.info("Notification recieved");
    ObjectMapper objectMapper = new ObjectMapper();
    try {
 
      NotificationRequest notificationRequest =
          objectMapper.readValue(notification, NotificationRequest.class);
      NotificationRequestItem notificationItem = notificationRequest.getNotificationItems().get(0);
      logger.info("request is  {}", notificationRequest);
      AdyenResponsesRecord record =
          adyenDao.getResponseFromMerchantReference(notificationItem.getMerchantReference());
      final CallContext tempContext =
          new PluginCallContext(
              AdyenActivator.PLUGIN_NAME,
              clock.getUTCNow(),
              UUID.fromString(record.getKbAccountId()),
              UUID.fromString(record.getKbTenantId()));
      Account account =
          this.killbillAPI
              .getAccountUserApi()
              .getAccountById(UUID.fromString(record.getKbAccountId()), tempContext);
      this.killbillAPI
          .getPaymentApi()
          .notifyPendingTransactionOfStateChanged(
              account,
              UUID.fromString(record.getKbPaymentTransactionId()),
              notificationItem.isSuccess(),
              tempContext);
      ProcessorOutputDTO outputDTO = new ProcessorOutputDTO();
      outputDTO.setPspReferenceCode(notificationItem.getPspReference());
      if (notificationItem.isSuccess()) {
        outputDTO.setStatus(PaymentPluginStatus.PROCESSED);
      } else {
        outputDTO.setStatus(PaymentPluginStatus.ERROR);
      }
      this.adyenDao.updateResponse(
          UUID.fromString(record.getKbPaymentId()),
          outputDTO,
          UUID.fromString(record.getKbTenantId()));
      this.adyenDao.addNotification(
          UUID.fromString(record.getKbAccountId()),
          UUID.fromString(record.getKbPaymentId()),
          UUID.fromString(record.getKbPaymentTransactionId()),
          notificationItem,
          UUID.fromString(record.getKbTenantId()));
    } catch (JsonProcessingException | PaymentApiException | SQLException | AccountApiException e) {
      e.printStackTrace();
    }
    return new PluginGatewayNotification("[accepted]");
  }

  public AdyenPaymentPluginApiHelper getHelper() {
    return helper;
  }
}
