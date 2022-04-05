/*
 * This file is generated by jOOQ.
 */
package org.killbill.billing.plugin.adyen.dao.gen;

import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;
import org.killbill.billing.plugin.adyen.dao.gen.tables.AdyenHppRequests;
import org.killbill.billing.plugin.adyen.dao.gen.tables.AdyenNotifications;
import org.killbill.billing.plugin.adyen.dao.gen.tables.AdyenResponses;

/** A class modelling indexes of tables in killbill. */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class Indexes {

  // -------------------------------------------------------------------------
  // INDEX definitions
  // -------------------------------------------------------------------------

  public static final Index ADYEN_HPP_REQUESTS_ADYEN_HPP_REQUESTS_KB_ACCOUNT_ID =
      Internal.createIndex(
          DSL.name("adyen_hpp_requests_kb_account_id"),
          AdyenHppRequests.ADYEN_HPP_REQUESTS,
          new OrderField[] {AdyenHppRequests.ADYEN_HPP_REQUESTS.KB_ACCOUNT_ID},
          false);
  public static final Index ADYEN_HPP_REQUESTS_ADYEN_HPP_REQUESTS_KB_PAYMENT_TRANSACTION_ID =
      Internal.createIndex(
          DSL.name("adyen_hpp_requests_kb_payment_transaction_id"),
          AdyenHppRequests.ADYEN_HPP_REQUESTS,
          new OrderField[] {AdyenHppRequests.ADYEN_HPP_REQUESTS.KB_PAYMENT_TRANSACTION_ID},
          false);
  public static final Index ADYEN_HPP_REQUESTS_ADYEN_HPP_REQUESTS_KB_TRANSACTION_EXTERNAL_KEY =
      Internal.createIndex(
          DSL.name("adyen_hpp_requests_kb_transaction_external_key"),
          AdyenHppRequests.ADYEN_HPP_REQUESTS,
          new OrderField[] {AdyenHppRequests.ADYEN_HPP_REQUESTS.TRANSACTION_EXTERNAL_KEY},
          false);
  public static final Index ADYEN_NOTIFICATIONS_ADYEN_NOTIFICATIONS_KB_PAYMENT_ID =
      Internal.createIndex(
          DSL.name("adyen_notifications_kb_payment_id"),
          AdyenNotifications.ADYEN_NOTIFICATIONS,
          new OrderField[] {AdyenNotifications.ADYEN_NOTIFICATIONS.KB_PAYMENT_ID},
          false);
  public static final Index ADYEN_NOTIFICATIONS_ADYEN_NOTIFICATIONS_KB_PAYMENT_TRANSACTION_ID =
      Internal.createIndex(
          DSL.name("adyen_notifications_kb_payment_transaction_id"),
          AdyenNotifications.ADYEN_NOTIFICATIONS,
          new OrderField[] {AdyenNotifications.ADYEN_NOTIFICATIONS.KB_PAYMENT_TRANSACTION_ID},
          false);
  public static final Index ADYEN_NOTIFICATIONS_ADYEN_NOTIFICATIONS_PSP_REFERENCE =
      Internal.createIndex(
          DSL.name("adyen_notifications_psp_reference"),
          AdyenNotifications.ADYEN_NOTIFICATIONS,
          new OrderField[] {AdyenNotifications.ADYEN_NOTIFICATIONS.PSP_REFERENCE},
          false);
  public static final Index ADYEN_RESPONSES_ADYEN_RESPONSES_KB_PAYMENT_ID =
      Internal.createIndex(
          DSL.name("adyen_responses_kb_payment_id"),
          AdyenResponses.ADYEN_RESPONSES,
          new OrderField[] {AdyenResponses.ADYEN_RESPONSES.KB_PAYMENT_ID},
          false);
  public static final Index ADYEN_RESPONSES_ADYEN_RESPONSES_KB_PAYMENT_TRANSACTION_ID =
      Internal.createIndex(
          DSL.name("adyen_responses_kb_payment_transaction_id"),
          AdyenResponses.ADYEN_RESPONSES,
          new OrderField[] {AdyenResponses.ADYEN_RESPONSES.KB_PAYMENT_TRANSACTION_ID},
          false);
  public static final Index ADYEN_RESPONSES_PSP_REFERENCE_IDX =
      Internal.createIndex(
          DSL.name("psp_reference_idx"),
          AdyenResponses.ADYEN_RESPONSES,
          new OrderField[] {AdyenResponses.ADYEN_RESPONSES.PSP_REFERENCE},
          false);
}
