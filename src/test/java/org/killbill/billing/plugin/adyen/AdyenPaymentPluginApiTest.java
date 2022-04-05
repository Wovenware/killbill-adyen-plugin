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

package org.killbill.billing.plugin.adyen;

import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.apache.hc.client5.http.ClientProtocolException;
import org.junit.Assert;
import org.junit.Test;
import org.killbill.billing.osgi.api.Healthcheck;
import org.killbill.billing.payment.api.Payment;
import org.killbill.billing.payment.api.PaymentApiException;
import org.killbill.billing.payment.api.PaymentMethodPlugin;
import org.killbill.billing.payment.api.PaymentTransaction;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.payment.api.TransactionType;
import org.killbill.billing.payment.plugin.api.PaymentMethodInfoPlugin;
import org.killbill.billing.payment.plugin.api.PaymentPluginApiException;
import org.killbill.billing.payment.plugin.api.PaymentPluginStatus;
import org.killbill.billing.payment.plugin.api.PaymentTransactionInfoPlugin;
import org.killbill.billing.plugin.TestUtils;
import org.killbill.billing.plugin.adyen.api.AdyenPaymentMethodInfoPlugin;
import org.killbill.billing.plugin.adyen.api.AdyenPaymentMethodPlugin;
import org.killbill.billing.plugin.adyen.client.DefaultGatewayProcessor;
import org.killbill.billing.plugin.adyen.client.GatewayProcessor;
import org.killbill.billing.plugin.adyen.client.exceptions.ConnectionException;
import org.killbill.billing.plugin.adyen.core.AdyenHealthcheck;
import org.killbill.billing.plugin.adyen.dao.gen.tables.records.AdyenPaymentMethodsRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdyenPaymentPluginApiTest extends TestBase {

  private static final Logger logger = LoggerFactory.getLogger(AdyenPaymentPluginApiTest.class);
  private String defaultToken = "F8C833979563DDE0008FC110580B69F5";
  private String ccOneTime = "CC_ONE_TIME";
  private String testing = "Testing";
  private String ccRecurring = "CC_RECURRING";

  @Test
  public void testCreatePaymentMethod() throws PaymentPluginApiException {
    logger.info("testCreatePaymentMethod");
    final UUID kbAccountId = account.getId();
    Assert.assertEquals(0, syncPaymentMethods(kbAccountId).size());
    final UUID kbPaymentMethodId = UUID.randomUUID();
    AdyenPaymentMethodPlugin AdyenPaymentMethodPlugin =
        new AdyenPaymentMethodPlugin(
            kbPaymentMethodId, kbPaymentMethodId.toString(), true, ImmutableList.of());

    adyenPaymentPluginApi.addPaymentMethod(
        kbAccountId,
        kbPaymentMethodId,
        AdyenPaymentMethodPlugin,
        true,
        ImmutableList.of(),
        context);

    Assert.assertEquals(1, syncPaymentMethods(kbAccountId).size());
  }

  @Test
  public void testDeletePaymentMethod() throws PaymentPluginApiException {
    final UUID kbAccountId = account.getId();

    Assert.assertEquals(0, syncPaymentMethods(kbAccountId).size());
    final UUID kbPaymentMethodId = UUID.randomUUID();
    AdyenPaymentMethodPlugin AdyenPaymentMethodPlugin =
        new AdyenPaymentMethodPlugin(
            kbPaymentMethodId, kbPaymentMethodId.toString(), true, ImmutableList.of());

    adyenPaymentPluginApi.addPaymentMethod(
        kbAccountId,
        kbPaymentMethodId,
        AdyenPaymentMethodPlugin,
        true,
        ImmutableList.of(),
        context);

    Assert.assertEquals(1, syncPaymentMethods(kbAccountId).size());
    adyenPaymentPluginApi.deletePaymentMethod(kbAccountId, kbPaymentMethodId, null, context);
    Assert.assertEquals(0, syncPaymentMethods(kbAccountId).size());
  }

  @Test
  public void testSuccessfulPurchase() throws PaymentPluginApiException, PaymentApiException {

    final UUID kbAccountId = account.getId();

    Assert.assertEquals(0, syncPaymentMethods(kbAccountId).size());
    final UUID kbPaymentMethodId = UUID.randomUUID();
    AdyenPaymentMethodPlugin AdyenPaymentMethodPlugin =
        new AdyenPaymentMethodPlugin(
            kbPaymentMethodId, kbPaymentMethodId.toString(), true, ImmutableList.of());

    adyenPaymentPluginApi.addPaymentMethod(
        kbAccountId,
        kbPaymentMethodId,
        AdyenPaymentMethodPlugin,
        true,
        ImmutableList.of(),
        context);

    final Payment payment =
        TestUtils.buildPayment(kbAccountId, kbPaymentMethodId, account.getCurrency(), killbillApi);

    final PaymentTransaction purchaseTransaction =
        TestUtils.buildPaymentTransaction(
            payment, TransactionType.PURCHASE, BigDecimal.TEN, payment.getCurrency());

    final PaymentTransactionInfoPlugin purchaseInfoPlugin =
        adyenPaymentPluginApi.purchasePayment(
            kbAccountId,
            payment.getId(),
            purchaseTransaction.getId(),
            kbPaymentMethodId,
            purchaseTransaction.getAmount(),
            purchaseTransaction.getCurrency(),
            ImmutableList.of(),
            context);

    TestUtils.updatePaymentTransaction(purchaseTransaction, purchaseInfoPlugin);
    verifyPaymentTransactionInfoPlugin(
        payment, purchaseTransaction, purchaseInfoPlugin, PaymentPluginStatus.PROCESSED);
  }

  @Test
  public void testSuccessfulAuthVoid() throws PaymentPluginApiException, PaymentApiException {
    final UUID kbAccountId = account.getId();

    Assert.assertEquals(0, syncPaymentMethods(kbAccountId).size());
    final UUID kbPaymentMethodId = UUID.randomUUID();
    AdyenPaymentMethodPlugin AdyenPaymentMethodPlugin =
        new AdyenPaymentMethodPlugin(
            kbPaymentMethodId, kbPaymentMethodId.toString(), true, ImmutableList.of());

    adyenPaymentPluginApi.addPaymentMethod(
        kbAccountId,
        kbPaymentMethodId,
        AdyenPaymentMethodPlugin,
        true,
        ImmutableList.of(),
        context);

    final Payment payment =
        TestUtils.buildPayment(kbAccountId, kbPaymentMethodId, account.getCurrency(), killbillApi);
    final PaymentTransaction authorizationTransaction =
        TestUtils.buildPaymentTransaction(
            payment, TransactionType.AUTHORIZE, BigDecimal.TEN, payment.getCurrency());
    final PaymentTransaction voidTransaction =
        TestUtils.buildPaymentTransaction(
            payment, TransactionType.VOID, BigDecimal.TEN, payment.getCurrency());

    final PaymentTransactionInfoPlugin authorizationInfoPlugin =
        adyenPaymentPluginApi.authorizePayment(
            kbAccountId,
            payment.getId(),
            authorizationTransaction.getId(),
            kbPaymentMethodId,
            authorizationTransaction.getAmount(),
            authorizationTransaction.getCurrency(),
            ImmutableList.of(),
            context);
    TestUtils.updatePaymentTransaction(authorizationTransaction, authorizationInfoPlugin);
    Assert.assertEquals(PaymentPluginStatus.CANCELED, authorizationInfoPlugin.getStatus());

    final PaymentTransactionInfoPlugin voidInfoPlugin =
        adyenPaymentPluginApi.voidPayment(
            kbAccountId,
            payment.getId(),
            voidTransaction.getId(),
            kbPaymentMethodId,
            ImmutableList.of(),
            context);
    Assert.assertEquals(PaymentPluginStatus.CANCELED, voidInfoPlugin.getStatus());
  }

  @Test
  public void testSuccessfulCapture() throws PaymentPluginApiException, PaymentApiException {
    final UUID kbAccountId = account.getId();

    Assert.assertEquals(0, syncPaymentMethods(kbAccountId).size());
    final UUID kbPaymentMethodId = UUID.randomUUID();
    AdyenPaymentMethodPlugin AdyenPaymentMethodPlugin =
        new AdyenPaymentMethodPlugin(
            kbPaymentMethodId, kbPaymentMethodId.toString(), true, ImmutableList.of());

    adyenPaymentPluginApi.addPaymentMethod(
        kbAccountId,
        kbPaymentMethodId,
        AdyenPaymentMethodPlugin,
        true,
        ImmutableList.of(),
        context);

    final Payment payment =
        TestUtils.buildPayment(kbAccountId, kbPaymentMethodId, account.getCurrency(), killbillApi);
    final PaymentTransaction captureTransaction =
        TestUtils.buildPaymentTransaction(
            payment, TransactionType.AUTHORIZE, BigDecimal.TEN, payment.getCurrency());

    final PaymentTransactionInfoPlugin captureInfoPlugin =
        adyenPaymentPluginApi.capturePayment(
            kbAccountId,
            payment.getId(),
            captureTransaction.getId(),
            kbPaymentMethodId,
            captureTransaction.getAmount(),
            captureTransaction.getCurrency(),
            ImmutableList.of(),
            context);
    TestUtils.updatePaymentTransaction(captureTransaction, captureInfoPlugin);
    Assert.assertEquals(PaymentPluginStatus.CANCELED, captureInfoPlugin.getStatus());
  }

  @Test
  public void testSuccessfulCredit() throws PaymentPluginApiException, PaymentApiException {
    final UUID kbAccountId = account.getId();

    Assert.assertEquals(0, syncPaymentMethods(kbAccountId).size());
    final UUID kbPaymentMethodId = UUID.randomUUID();
    AdyenPaymentMethodPlugin AdyenPaymentMethodPlugin =
        new AdyenPaymentMethodPlugin(
            kbPaymentMethodId, kbPaymentMethodId.toString(), true, ImmutableList.of());

    adyenPaymentPluginApi.addPaymentMethod(
        kbAccountId,
        kbPaymentMethodId,
        AdyenPaymentMethodPlugin,
        true,
        ImmutableList.of(),
        context);

    final Payment payment =
        TestUtils.buildPayment(kbAccountId, kbPaymentMethodId, account.getCurrency(), killbillApi);
    final PaymentTransaction creditTransaction =
        TestUtils.buildPaymentTransaction(
            payment, TransactionType.AUTHORIZE, BigDecimal.TEN, payment.getCurrency());

    final PaymentTransactionInfoPlugin creditInfoPlugin =
        adyenPaymentPluginApi.creditPayment(
            kbAccountId,
            payment.getId(),
            creditTransaction.getId(),
            kbPaymentMethodId,
            creditTransaction.getAmount(),
            creditTransaction.getCurrency(),
            ImmutableList.of(),
            context);
    TestUtils.updatePaymentTransaction(creditTransaction, creditInfoPlugin);
    Assert.assertEquals(PaymentPluginStatus.CANCELED, creditInfoPlugin.getStatus());
  }

  @Test
  public void testSuccessfulPurchaseRefund() throws PaymentPluginApiException, PaymentApiException {

    final UUID kbAccountId = account.getId();

    Assert.assertEquals(0, syncPaymentMethods(kbAccountId).size());
    final UUID kbPaymentMethodId = UUID.randomUUID();
    AdyenPaymentMethodPlugin AdyenPaymentMethodPlugin =
        new AdyenPaymentMethodPlugin(
            kbPaymentMethodId, kbPaymentMethodId.toString(), true, ImmutableList.of());

    adyenPaymentPluginApi.addPaymentMethod(
        kbAccountId,
        kbPaymentMethodId,
        AdyenPaymentMethodPlugin,
        true,
        ImmutableList.of(),
        context);

    final Payment payment =
        TestUtils.buildPayment(kbAccountId, kbPaymentMethodId, account.getCurrency(), killbillApi);

    final PaymentTransaction purchaseTransaction =
        TestUtils.buildPaymentTransaction(
            payment, TransactionType.PURCHASE, BigDecimal.valueOf(300), payment.getCurrency());

    final PaymentTransactionInfoPlugin purchaseInfoPlugin =
        adyenPaymentPluginApi.purchasePayment(
            kbAccountId,
            payment.getId(),
            purchaseTransaction.getId(),
            kbPaymentMethodId,
            purchaseTransaction.getAmount(),
            purchaseTransaction.getCurrency(),
            ImmutableList.of(),
            context);
    TestUtils.updatePaymentTransaction(purchaseTransaction, purchaseInfoPlugin);
    verifyPaymentTransactionInfoPlugin(
        payment, purchaseTransaction, purchaseInfoPlugin, PaymentPluginStatus.PROCESSED);
    final PaymentTransaction refundTransaction =
        TestUtils.buildPaymentTransaction(
            payment, TransactionType.REFUND, BigDecimal.TEN, payment.getCurrency());

    final PaymentTransactionInfoPlugin refundInfoPlugin =
        adyenPaymentPluginApi.refundPayment(
            kbAccountId,
            payment.getId(),
            refundTransaction.getId(),
            kbPaymentMethodId,
            refundTransaction.getAmount(),
            refundTransaction.getCurrency(),
            ImmutableList.of(),
            context);
    System.out.println(refundInfoPlugin);
    TestUtils.updatePaymentTransaction(refundTransaction, refundInfoPlugin);
    verifyPaymentTransactionInfoPlugin(
        payment, refundTransaction, refundInfoPlugin, PaymentPluginStatus.PROCESSED);
  }

  @Test
  public void testPurchaseRefundWithEqualAmount()
      throws PaymentPluginApiException, PaymentApiException {

    final UUID kbAccountId = account.getId();

    Assert.assertEquals(0, syncPaymentMethods(kbAccountId).size());
    final UUID kbPaymentMethodId = UUID.randomUUID();
    AdyenPaymentMethodPlugin AdyenPaymentMethodPlugin =
        new AdyenPaymentMethodPlugin(
            kbPaymentMethodId, kbPaymentMethodId.toString(), true, ImmutableList.of());

    adyenPaymentPluginApi.addPaymentMethod(
        kbAccountId,
        kbPaymentMethodId,
        AdyenPaymentMethodPlugin,
        true,
        ImmutableList.of(),
        context);

    final Payment payment =
        TestUtils.buildPayment(kbAccountId, kbPaymentMethodId, account.getCurrency(), killbillApi);

    final PaymentTransaction purchaseTransaction =
        TestUtils.buildPaymentTransaction(
            payment, TransactionType.PURCHASE, BigDecimal.TEN, payment.getCurrency());

    final PaymentTransactionInfoPlugin purchaseInfoPlugin =
        adyenPaymentPluginApi.purchasePayment(
            kbAccountId,
            payment.getId(),
            purchaseTransaction.getId(),
            kbPaymentMethodId,
            purchaseTransaction.getAmount(),
            purchaseTransaction.getCurrency(),
            ImmutableList.of(),
            context);
    TestUtils.updatePaymentTransaction(purchaseTransaction, purchaseInfoPlugin);
    verifyPaymentTransactionInfoPlugin(
        payment, purchaseTransaction, purchaseInfoPlugin, PaymentPluginStatus.PROCESSED);
    final PaymentTransaction refundTransaction =
        TestUtils.buildPaymentTransaction(
            payment, TransactionType.REFUND, BigDecimal.TEN, payment.getCurrency());

    final PaymentTransactionInfoPlugin refundInfoPlugin =
        adyenPaymentPluginApi.refundPayment(
            kbAccountId,
            payment.getId(),
            refundTransaction.getId(),
            kbPaymentMethodId,
            refundTransaction.getAmount(),
            refundTransaction.getCurrency(),
            ImmutableList.of(),
            context);
    TestUtils.updatePaymentTransaction(refundTransaction, refundInfoPlugin);
    verifyPaymentTransactionInfoPlugin(
        payment, refundTransaction, refundInfoPlugin, PaymentPluginStatus.PROCESSED);
  }

  @Test
  public void testPurchaseRefundWithWrongRefundAmount()
      throws PaymentPluginApiException, PaymentApiException {

    final UUID kbAccountId = account.getId();

    Assert.assertEquals(0, syncPaymentMethods(kbAccountId).size());
    final UUID kbPaymentMethodId = UUID.randomUUID();
    AdyenPaymentMethodPlugin AdyenPaymentMethodPlugin =
        new AdyenPaymentMethodPlugin(
            kbPaymentMethodId, kbPaymentMethodId.toString(), true, ImmutableList.of());

    adyenPaymentPluginApi.addPaymentMethod(
        kbAccountId,
        kbPaymentMethodId,
        AdyenPaymentMethodPlugin,
        true,
        ImmutableList.of(),
        context);

    final Payment payment =
        TestUtils.buildPayment(kbAccountId, kbPaymentMethodId, account.getCurrency(), killbillApi);

    final PaymentTransaction purchaseTransaction =
        TestUtils.buildPaymentTransaction(
            payment, TransactionType.PURCHASE, BigDecimal.TEN, payment.getCurrency());

    final PaymentTransactionInfoPlugin purchaseInfoPlugin =
        adyenPaymentPluginApi.purchasePayment(
            kbAccountId,
            payment.getId(),
            purchaseTransaction.getId(),
            kbPaymentMethodId,
            purchaseTransaction.getAmount(),
            purchaseTransaction.getCurrency(),
            ImmutableList.of(),
            context);
    TestUtils.updatePaymentTransaction(purchaseTransaction, purchaseInfoPlugin);
    verifyPaymentTransactionInfoPlugin(
        payment, purchaseTransaction, purchaseInfoPlugin, PaymentPluginStatus.PROCESSED);
    final PaymentTransaction refundTransaction =
        TestUtils.buildPaymentTransaction(
            payment, TransactionType.REFUND, BigDecimal.valueOf(200), payment.getCurrency());

    final PaymentTransactionInfoPlugin refundInfoPlugin =
        adyenPaymentPluginApi.refundPayment(
            kbAccountId,
            payment.getId(),
            refundTransaction.getId(),
            kbPaymentMethodId,
            refundTransaction.getAmount(),
            refundTransaction.getCurrency(),
            ImmutableList.of(),
            context);
    TestUtils.updatePaymentTransaction(refundTransaction, refundInfoPlugin);
  }

  @Test
  public void testPurchaseRefundWithRefundAmountZero()
      throws PaymentPluginApiException, PaymentApiException {

    final UUID kbAccountId = account.getId();

    Assert.assertEquals(0, syncPaymentMethods(kbAccountId).size());
    final UUID kbPaymentMethodId = UUID.randomUUID();
    AdyenPaymentMethodPlugin AdyenPaymentMethodPlugin =
        new AdyenPaymentMethodPlugin(
            kbPaymentMethodId, kbPaymentMethodId.toString(), true, ImmutableList.of());

    adyenPaymentPluginApi.addPaymentMethod(
        kbAccountId,
        kbPaymentMethodId,
        AdyenPaymentMethodPlugin,
        true,
        ImmutableList.of(),
        context);

    final Payment payment =
        TestUtils.buildPayment(kbAccountId, kbPaymentMethodId, account.getCurrency(), killbillApi);

    final PaymentTransaction purchaseTransaction =
        TestUtils.buildPaymentTransaction(
            payment, TransactionType.PURCHASE, BigDecimal.TEN, payment.getCurrency());

    final PaymentTransactionInfoPlugin purchaseInfoPlugin =
        adyenPaymentPluginApi.purchasePayment(
            kbAccountId,
            payment.getId(),
            purchaseTransaction.getId(),
            kbPaymentMethodId,
            purchaseTransaction.getAmount(),
            purchaseTransaction.getCurrency(),
            ImmutableList.of(),
            context);
    TestUtils.updatePaymentTransaction(purchaseTransaction, purchaseInfoPlugin);
    verifyPaymentTransactionInfoPlugin(
        payment, purchaseTransaction, purchaseInfoPlugin, PaymentPluginStatus.PROCESSED);
    final PaymentTransaction refundTransaction =
        TestUtils.buildPaymentTransaction(
            payment, TransactionType.REFUND, BigDecimal.ZERO, payment.getCurrency());

    final PaymentTransactionInfoPlugin refundInfoPlugin =
        adyenPaymentPluginApi.refundPayment(
            kbAccountId,
            payment.getId(),
            refundTransaction.getId(),
            kbPaymentMethodId,
            refundTransaction.getAmount(),
            refundTransaction.getCurrency(),
            ImmutableList.of(),
            context);
    TestUtils.updatePaymentTransaction(refundTransaction, refundInfoPlugin);

    Assert.assertEquals(PaymentPluginStatus.CANCELED, refundInfoPlugin.getStatus());
  }

  @Test
  public void testBuildFormDescriptor() throws PaymentPluginApiException {
    final UUID kbAccountId = account.getId();
    adyenPaymentPluginApi.buildFormDescriptor(
        kbAccountId, null, ImmutableList.of(new PluginProperty("amount", 10, false)), context);
  }

  @Test
  public void testProcessNotification() throws PaymentPluginApiException {
    Assert.assertThrows(
        PaymentPluginApiException.class,
        () -> adyenPaymentPluginApi.processNotification(DEFAULT_COUNTRY, null, context));
  }

  @Test
  public void testGetPaymentMethodDetail() throws PaymentPluginApiException {
    final UUID kbAccountId = account.getId();

    Assert.assertEquals(0, syncPaymentMethods(kbAccountId).size());
    final UUID kbPaymentMethodId = UUID.randomUUID();
    AdyenPaymentMethodPlugin AdyenPaymentMethodPlugin =
        new AdyenPaymentMethodPlugin(
            kbPaymentMethodId, kbPaymentMethodId.toString(), true, ImmutableList.of());

    adyenPaymentPluginApi.addPaymentMethod(
        kbAccountId,
        kbPaymentMethodId,
        AdyenPaymentMethodPlugin,
        true,
        ImmutableList.of(),
        context);

    Assert.assertEquals(1, syncPaymentMethods(kbAccountId).size());

    PaymentMethodPlugin test =
        adyenPaymentPluginApi.getPaymentMethodDetail(kbAccountId, kbPaymentMethodId, null, context);
    Assert.assertEquals(
        test.getKbPaymentMethodId(), AdyenPaymentMethodPlugin.getKbPaymentMethodId());
  }

  @Test
  public void testHealthcheck() {
    final Healthcheck healthcheck = new AdyenHealthcheck();
    Assert.assertTrue(healthcheck.getHealthStatus(null, null).isHealthy());
  }

  @Test
  public void testDefaultGatewayProcessor()
      throws PaymentPluginApiException, GeneralSecurityException, ConnectionException,
          ClientProtocolException {
    GatewayProcessor processor = new DefaultGatewayProcessor();
    Assert.assertEquals(null, processor.getPaymentInfo(null));
    Assert.assertEquals(null, processor.getPaymentMethodDetail(null));
    Assert.assertEquals(null, processor.processOneTimePayment(null));
    Assert.assertEquals(null, processor.processPayment(null));
    Assert.assertEquals(null, processor.refundPayment(null));
    Assert.assertEquals(null, processor.validateData(null, null, null, null));
  }

  @Test
  public void testGetPaymentInfo() throws PaymentPluginApiException, PaymentApiException {
    final UUID kbAccountId = account.getId();

    Assert.assertEquals(0, syncPaymentMethods(kbAccountId).size());
    final UUID kbPaymentMethodId = UUID.randomUUID();
    AdyenPaymentMethodPlugin AdyenPaymentMethodPlugin =
        new AdyenPaymentMethodPlugin(
            kbPaymentMethodId, kbPaymentMethodId.toString(), true, ImmutableList.of());

    adyenPaymentPluginApi.addPaymentMethod(
        kbAccountId,
        kbPaymentMethodId,
        AdyenPaymentMethodPlugin,
        true,
        ImmutableList.of(),
        context);

    final Payment payment =
        TestUtils.buildPayment(kbAccountId, kbPaymentMethodId, account.getCurrency(), killbillApi);

    final PaymentTransaction purchaseTransaction =
        TestUtils.buildPaymentTransaction(
            payment, TransactionType.PURCHASE, BigDecimal.TEN, payment.getCurrency());

    final PaymentTransactionInfoPlugin purchaseInfoPlugin =
        adyenPaymentPluginApi.purchasePayment(
            kbAccountId,
            payment.getId(),
            purchaseTransaction.getId(),
            kbPaymentMethodId,
            purchaseTransaction.getAmount(),
            purchaseTransaction.getCurrency(),
            ImmutableList.of(),
            context);

    TestUtils.updatePaymentTransaction(purchaseTransaction, purchaseInfoPlugin);
    verifyPaymentTransactionInfoPlugin(
        payment, purchaseTransaction, purchaseInfoPlugin, PaymentPluginStatus.PROCESSED);
    adyenPaymentPluginApi.getPaymentInfo(kbAccountId, payment.getId(), null, context);
  }

  @Test
  public void testInfoPlugin() throws PaymentPluginApiException, PaymentApiException {
    AdyenPaymentMethodsRecord methodRecord = new AdyenPaymentMethodsRecord();
    methodRecord.setKbAccountId(account.getId().toString());
    methodRecord.setIsDefault((short) 1);

    methodRecord.setKbPaymentMethodId(UUID.randomUUID().toString());
    methodRecord.setKbTenantId(context.getTenantId().toString());

    methodRecord.setCreatedDate(LocalDateTime.now());
    Assert.assertEquals(
        account.getId(), AdyenPaymentMethodInfoPlugin.build(methodRecord).getAccountId());
  }

  private List<PaymentMethodInfoPlugin> syncPaymentMethods(UUID kbAccountId)
      throws PaymentPluginApiException {
    return adyenPaymentPluginApi.getPaymentMethods(kbAccountId, true, ImmutableList.of(), context);
  }

  private void verifyPaymentTransactionInfoPlugin(
      final Payment payment,
      final PaymentTransaction paymentTransaction,
      final PaymentTransactionInfoPlugin paymentTransactionInfoPlugin,
      final PaymentPluginStatus expectedPaymentPluginStatus) {
    Assert.assertEquals(paymentTransactionInfoPlugin.getKbPaymentId(), payment.getId());
    Assert.assertEquals(
        paymentTransactionInfoPlugin.getKbTransactionPaymentId(), paymentTransaction.getId());
    Assert.assertEquals(
        paymentTransactionInfoPlugin.getTransactionType(), paymentTransaction.getTransactionType());

    Assert.assertEquals(
        0, paymentTransactionInfoPlugin.getAmount().compareTo(paymentTransaction.getAmount()));
    Assert.assertEquals(
        paymentTransactionInfoPlugin.getCurrency(), paymentTransaction.getCurrency());

    Assert.assertNotNull(paymentTransactionInfoPlugin.getCreatedDate());

    Assert.assertEquals(paymentTransactionInfoPlugin.getStatus(), expectedPaymentPluginStatus);
  }
}
