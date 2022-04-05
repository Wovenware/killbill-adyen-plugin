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

package org.killbill.billing.plugin.adyen.dao;

import static org.killbill.billing.plugin.adyen.dao.gen.tables.AdyenPaymentMethods.ADYEN_PAYMENT_METHODS;
import static org.killbill.billing.plugin.adyen.dao.gen.tables.AdyenResponses.ADYEN_RESPONSES;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import org.joda.time.DateTime;
import org.jooq.impl.DSL;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.payment.api.TransactionType;
import org.killbill.billing.payment.plugin.api.PaymentPluginStatus;
import org.killbill.billing.plugin.adyen.api.PaymentMethodStatus;
import org.killbill.billing.plugin.adyen.api.ProcessorOutputDTO;
import org.killbill.billing.plugin.adyen.client.exceptions.FormaterException;
import org.killbill.billing.plugin.adyen.dao.gen.tables.AdyenPaymentMethods;
import org.killbill.billing.plugin.adyen.dao.gen.tables.AdyenResponses;
import org.killbill.billing.plugin.adyen.dao.gen.tables.records.AdyenPaymentMethodsRecord;
import org.killbill.billing.plugin.adyen.dao.gen.tables.records.AdyenResponsesRecord;
import org.killbill.billing.plugin.dao.payment.PluginPaymentDao;

public class AdyenDao
    extends PluginPaymentDao<
        AdyenResponsesRecord, AdyenResponses, AdyenPaymentMethodsRecord, AdyenPaymentMethods> {
  private static final String TRANSACTION_ID = "TransactionId";
  private static final String PURCHASE = "PURCHASE";

  public AdyenDao(final DataSource dataSource) throws SQLException {
    super(ADYEN_RESPONSES, ADYEN_PAYMENT_METHODS, dataSource);
    // Save space in the database
    objectMapper.setSerializationInclusion(Include.NON_EMPTY);
  }

  // Payment methods
  public void addPaymentMethod(
      final UUID kbAccountId,
      final UUID kbPaymentMethodId,
      final Map<String, String> additionalDataMap,
      final String token,
      final UUID kbTenantId,
      final String gatewayResponseCode,
      final PaymentMethodStatus paymentMethodStatus,
      final boolean setDefault)
      throws SQLException {
    final Map<String, String> clonedProperties = new HashMap<>(additionalDataMap);

    execute(
        dataSource.getConnection(),
        new WithConnectionCallback<AdyenResponsesRecord>() {

          @Override
          public AdyenResponsesRecord withConnection(final Connection conn) throws SQLException {
            DSL.using(conn, dialect, settings)
                .insertInto(
                    ADYEN_PAYMENT_METHODS,
                    ADYEN_PAYMENT_METHODS.KB_ACCOUNT_ID,
                    ADYEN_PAYMENT_METHODS.KB_PAYMENT_METHOD_ID,
                    ADYEN_PAYMENT_METHODS.IS_DELETED,
                    ADYEN_PAYMENT_METHODS.ADDITIONAL_DATA,
                    ADYEN_PAYMENT_METHODS.CREATED_DATE,
                    ADYEN_PAYMENT_METHODS.UPDATED_DATE,
                    ADYEN_PAYMENT_METHODS.IS_DEFAULT,
                    ADYEN_PAYMENT_METHODS.KB_TENANT_ID)
                .values(
                    kbAccountId.toString(),
                    kbPaymentMethodId.toString(),
                    (short) FALSE,
                    asString(clonedProperties),
                    toLocalDateTime(new DateTime()),
                    toLocalDateTime(new DateTime()),
                    (short) fromBoolean(setDefault),
                    kbTenantId.toString())
                .execute();

            return null;
          }
        });
  }

  public void updateIsDeletePaymentMethod(final UUID kbPaymentMethodId, final UUID kbTenantId)
      throws SQLException {
    execute(
        dataSource.getConnection(),
        new WithConnectionCallback<AdyenResponsesRecord>() {
          @Override
          public AdyenResponsesRecord withConnection(final Connection conn) throws SQLException {

            DSL.using(conn, dialect, settings)
                .update(ADYEN_PAYMENT_METHODS)
                .set(ADYEN_PAYMENT_METHODS.IS_DELETED, (short) TRUE)
                .where(
                    ADYEN_PAYMENT_METHODS.KB_PAYMENT_METHOD_ID.equal(kbPaymentMethodId.toString()))
                .and(ADYEN_PAYMENT_METHODS.KB_TENANT_ID.equal(kbTenantId.toString()))
                .and(ADYEN_PAYMENT_METHODS.IS_DELETED.equal((short) FALSE))
                .execute();
            return null;
          }
        });
  }

  //  public void updateResponse(UUID kbPaymentId, ProcessorOutputDTO outputDTO, UUID tenantId)
  //      throws SQLException {
  //    execute(
  //        dataSource.getConnection(),
  //        new WithConnectionCallback<AdyenResponsesRecord>() {
  //          @Override
  //          public AdyenResponsesRecord withConnection(final Connection conn) throws SQLException
  // {
  //
  //            DSL.using(conn, dialect, settings)
  //                .update(ADYEN_RESPONSES)
  //                .set(
  //                    ADYEN_RESPONSES.SPS_TRANSACTION_ID,
  //                    outputDTO.getAdditionalData().get(TRANSACTION_ID))
  //                .set(ADYEN_RESPONSES.TRANSACTION_STATUS, outputDTO.getStatus().name())
  //                .set(ADYEN_RESPONSES.ADDITIONAL_DATA, asString(outputDTO.getAdditionalData()))
  //                .where(ADYEN_RESPONSES.KB_PAYMENT_ID.equal(kbPaymentId.toString()))
  //                .and(ADYEN_RESPONSES.KB_TENANT_ID.equal(tenantId.toString()))
  //                .execute();
  //            return null;
  //          }
  //        });
  //  }

  //  public void addRequest(
  //      final UUID kbAccountId,
  //      final UUID kbPaymentId,
  //      final UUID kbPaymentTransactionId,
  //      final UUID kbPaymentMethodId,
  //      final Map<String, String> additionalDataMap,
  //      final UUID kbTenantId)
  //      throws SQLException {
  //    final Map<String, String> clonedProperties = new HashMap<>(additionalDataMap);
  //
  //    execute(
  //        dataSource.getConnection(),
  //        new WithConnectionCallback<AdyenResponsesRecord>() {
  //
  //          @Override
  //          public AdyenResponsesRecord withConnection(final Connection conn) throws SQLException
  // {
  //            DSL.using(conn, dialect, settings)
  //                .insertInto(
  //                    SPS_REQUESTS,
  //                    SPS_REQUESTS.KB_ACCOUNT_ID,
  //                    SPS_REQUESTS.KB_PAYMENT_ID,
  //                    SPS_REQUESTS.KB_PAYMENT_TRANSACTION_ID,
  //                    SPS_REQUESTS.KB_PAYMENT_METHOD_ID,
  //                    SPS_REQUESTS.ADDITIONAL_DATA,
  //                    SPS_REQUESTS.CREATED_DATE,
  //                    SPS_REQUESTS.KB_TENANT_ID)
  //                .values(
  //                    kbAccountId.toString(),
  //                    kbPaymentId == null ? null : kbPaymentId.toString(),
  //                    kbPaymentTransactionId == null ? null : kbPaymentTransactionId.toString(),
  //                    kbPaymentMethodId == null ? null : kbPaymentMethodId.toString(),
  //                    asString(clonedProperties),
  //                    toLocalDateTime(new DateTime()),
  //                    kbTenantId.toString())
  //                .execute();
  //
  //            return null;
  //          }
  //        });
  //  }

  public AdyenPaymentMethodsRecord getPaymentMethodsByMethodId(final UUID paymentMethodId)
      throws SQLException {
    return execute(
        dataSource.getConnection(),
        new WithConnectionCallback<AdyenPaymentMethodsRecord>() {
          @Override
          public AdyenPaymentMethodsRecord withConnection(final Connection conn)
              throws SQLException {
            final List<AdyenPaymentMethodsRecord> response =
                DSL.using(conn, dialect, settings)
                    .selectFrom(ADYEN_PAYMENT_METHODS)
                    .where(
                        ADYEN_PAYMENT_METHODS.KB_PAYMENT_METHOD_ID.equal(
                            paymentMethodId.toString()))
                    .and(ADYEN_PAYMENT_METHODS.IS_DELETED.equal((short) FALSE))
                    .orderBy(ADYEN_PAYMENT_METHODS.RECORD_ID.desc())
                    .fetch();

            if (response.isEmpty()) {
              throw new SQLException();
            }
            return response.get(0);
          }
        });
  }

  @SuppressWarnings({"squid:S00107", "squid:S1188"})
  // Responses
  public AdyenResponsesRecord addResponse(
      final UUID kbAccountId,
      final UUID kbPaymentId,
      final UUID kbPaymentTransactionId,
      final TransactionType transactionType,
      final PaymentPluginStatus status,
      final String transactionId,
      final BigDecimal amount,
      final Currency currency,
      final Map<String, String> additionalData,
      final UUID kbTenantId)
      throws SQLException {
    String tempCurrency = (currency != null) ? currency.name() : null;

    final BigDecimal dbAmount = (amount != null) ? new BigDecimal(amount.toString()) : null;
    final String dbCurrency = tempCurrency;

    return execute(
        dataSource.getConnection(),
        new WithConnectionCallback<AdyenResponsesRecord>() {
          @Override
          public AdyenResponsesRecord withConnection(final Connection conn) throws SQLException {
            return DSL.using(conn, dialect, settings)
                .insertInto(
                    ADYEN_RESPONSES,
                    ADYEN_RESPONSES.KB_ACCOUNT_ID,
                    ADYEN_RESPONSES.KB_PAYMENT_ID,
                    ADYEN_RESPONSES.KB_PAYMENT_TRANSACTION_ID,
                    ADYEN_RESPONSES.TRANSACTION_TYPE,
                    ADYEN_RESPONSES.AMOUNT,
                    ADYEN_RESPONSES.CURRENCY,
                    ADYEN_RESPONSES.ADDITIONAL_DATA,
                    ADYEN_RESPONSES.CREATED_DATE,
                    ADYEN_RESPONSES.KB_TENANT_ID)
                .values(
                    kbAccountId.toString(),
                    kbPaymentId.toString(),
                    kbPaymentTransactionId.toString(),
                    transactionType.toString(),
                    dbAmount,
                    dbCurrency,
                    asString(additionalData),
                    toLocalDateTime(DateTime.now()),
                    kbTenantId.toString())
                .returning()
                .fetchOne();
          }
        });
  }

  @SuppressWarnings({"squid:S00107", "squid:S1188"})
  public AdyenResponsesRecord addResponse(
      UUID kbAccountId,
      UUID kbPaymentId,
      UUID kbTransactionId,
      TransactionType transactionType,
      BigDecimal amount,
      Currency currency,
      ProcessorOutputDTO outputDTO,
      UUID tenantId)
      throws SQLException {
    String tempCurrency = (currency != null) ? currency.name() : null;

    final BigDecimal dbAmount = (amount != null) ? new BigDecimal(amount.toString()) : null;
    final String dbCurrency = tempCurrency;

    return execute(
        dataSource.getConnection(),
        new WithConnectionCallback<AdyenResponsesRecord>() {
          @Override
          public AdyenResponsesRecord withConnection(final Connection conn) throws SQLException {
            return DSL.using(conn, dialect, settings)
                .insertInto(
                    ADYEN_RESPONSES,
                    ADYEN_RESPONSES.KB_ACCOUNT_ID,
                    ADYEN_RESPONSES.KB_PAYMENT_ID,
                    ADYEN_RESPONSES.KB_PAYMENT_TRANSACTION_ID,
                    ADYEN_RESPONSES.TRANSACTION_TYPE,
                    ADYEN_RESPONSES.AMOUNT,
                    ADYEN_RESPONSES.CURRENCY,
                    ADYEN_RESPONSES.ADDITIONAL_DATA,
                    ADYEN_RESPONSES.CREATED_DATE,
                    ADYEN_RESPONSES.KB_TENANT_ID)
                .values(
                    kbAccountId.toString(),
                    kbPaymentId.toString(),
                    kbTransactionId.toString(),
                    transactionType.toString(),
                    dbAmount,
                    dbCurrency,
                    asString(outputDTO.getAdditionalData()),
                    toLocalDateTime(DateTime.now()),
                    tenantId.toString())
                .returning()
                .fetchOne();
          }
        });
  }

  public AdyenResponsesRecord getSuccessfulPurchaseResponse(
      final UUID kbPaymentId, final UUID kbTenantId) throws SQLException {
    return execute(
        dataSource.getConnection(),
        new WithConnectionCallback<AdyenResponsesRecord>() {
          @Override
          public AdyenResponsesRecord withConnection(final Connection conn) throws SQLException {
            return DSL.using(conn, dialect, settings)
                .selectFrom(ADYEN_RESPONSES)
                .where(DSL.field(ADYEN_RESPONSES.KB_PAYMENT_ID).equal(kbPaymentId.toString()))
                .and(DSL.field(ADYEN_RESPONSES.KB_TENANT_ID).equal(kbTenantId.toString()))
                .and(DSL.field(ADYEN_RESPONSES.TRANSACTION_TYPE).equal(PURCHASE))
                .orderBy(ADYEN_RESPONSES.RECORD_ID)
                .fetchOne();
          }
        });
  }

  public List<AdyenResponsesRecord> getSuccessfulPurchaseResponseList(
      final UUID kbPaymentId, final UUID kbTenantId) throws SQLException {
    return execute(
        dataSource.getConnection(),
        new WithConnectionCallback<List<AdyenResponsesRecord>>() {
          @Override
          public List<AdyenResponsesRecord> withConnection(final Connection conn)
              throws SQLException {
            return DSL.using(conn, dialect, settings)
                .selectFrom(ADYEN_RESPONSES)
                .where(DSL.field(ADYEN_RESPONSES.KB_PAYMENT_ID).equal(kbPaymentId.toString()))
                .and(DSL.field(ADYEN_RESPONSES.KB_TENANT_ID).equal(kbTenantId.toString()))
                .orderBy(ADYEN_RESPONSES.RECORD_ID.desc())
                .fetch();
          }
        });
  }

  //  public List<SpsRequestsRecord> getPurchaseRequestList(
  //      final UUID kbPaymentId, final UUID kbTenantId) throws SQLException {
  //    return execute(
  //        dataSource.getConnection(),
  //        new WithConnectionCallback<List<SpsRequestsRecord>>() {
  //          @Override
  //          public List<SpsRequestsRecord> withConnection(final Connection conn) throws
  // SQLException {
  //            return DSL.using(conn, dialect, settings)
  //                .selectFrom(SPS_REQUESTS)
  //                .where(DSL.field(SPS_REQUESTS.KB_PAYMENT_ID).equal(kbPaymentId.toString()))
  //                .and(DSL.field(SPS_REQUESTS.KB_TENANT_ID).equal(kbTenantId.toString()))
  //                .and(DSL.field(SPS_REQUESTS.ADDITIONAL_DATA).like("%1Gathering%"))
  //                .orderBy(SPS_REQUESTS.RECORD_ID.desc())
  //                .fetch();
  //          }
  //        });
  //  }

  //  public List<SpsRequestsRecord> getRefundRequestList(final UUID kbPaymentId, final UUID
  // kbTenantId)
  //      throws SQLException {
  //    return execute(
  //        dataSource.getConnection(),
  //        new WithConnectionCallback<List<SpsRequestsRecord>>() {
  //          @Override
  //          public List<SpsRequestsRecord> withConnection(final Connection conn) throws
  // SQLException {
  //            return DSL.using(conn, dialect, settings)
  //                .selectFrom(SPS_REQUESTS)
  //                .where(DSL.field(SPS_REQUESTS.KB_PAYMENT_ID).equal(kbPaymentId.toString()))
  //                .and(DSL.field(SPS_REQUESTS.KB_TENANT_ID).equal(kbTenantId.toString()))
  //                .and(DSL.field(SPS_REQUESTS.ADDITIONAL_DATA).like("%1Delete%"))
  //                .or(DSL.field(SPS_REQUESTS.ADDITIONAL_DATA).like("%1Change%"))
  //                .orderBy(SPS_REQUESTS.RECORD_ID.desc())
  //                .fetch();
  //          }
  //        });
  //  }

  @SuppressWarnings("rawtypes")
  public static Map mapFromAdditionalDataString(@Nullable final String additionalData) {
    if (additionalData == null) {
      return ImmutableMap.of();
    }

    try {
      return objectMapper.readValue(additionalData, Map.class);
    } catch (final IOException e) {
      throw new FormaterException(e);
    }
  }
}
