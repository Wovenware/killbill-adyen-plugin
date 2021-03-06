/*
 * This file is generated by jOOQ.
 */
package org.killbill.billing.plugin.adyen.dao.gen;

import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;
import org.killbill.billing.plugin.adyen.dao.gen.tables.AdyenHppRequests;
import org.killbill.billing.plugin.adyen.dao.gen.tables.AdyenNotifications;
import org.killbill.billing.plugin.adyen.dao.gen.tables.AdyenPaymentMethods;
import org.killbill.billing.plugin.adyen.dao.gen.tables.AdyenResponses;
import org.killbill.billing.plugin.adyen.dao.gen.tables.records.AdyenHppRequestsRecord;
import org.killbill.billing.plugin.adyen.dao.gen.tables.records.AdyenNotificationsRecord;
import org.killbill.billing.plugin.adyen.dao.gen.tables.records.AdyenPaymentMethodsRecord;
import org.killbill.billing.plugin.adyen.dao.gen.tables.records.AdyenResponsesRecord;

/** A class modelling foreign key relationships and constraints of tables in killbill. */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class Keys {

  // -------------------------------------------------------------------------
  // UNIQUE and PRIMARY KEY definitions
  // -------------------------------------------------------------------------

  public static final UniqueKey<AdyenHppRequestsRecord> KEY_ADYEN_HPP_REQUESTS_PRIMARY =
      Internal.createUniqueKey(
          AdyenHppRequests.ADYEN_HPP_REQUESTS,
          DSL.name("KEY_adyen_hpp_requests_PRIMARY"),
          new TableField[] {AdyenHppRequests.ADYEN_HPP_REQUESTS.RECORD_ID},
          true);
  public static final UniqueKey<AdyenHppRequestsRecord> KEY_ADYEN_HPP_REQUESTS_RECORD_ID =
      Internal.createUniqueKey(
          AdyenHppRequests.ADYEN_HPP_REQUESTS,
          DSL.name("KEY_adyen_hpp_requests_record_id"),
          new TableField[] {AdyenHppRequests.ADYEN_HPP_REQUESTS.RECORD_ID},
          true);
  public static final UniqueKey<AdyenNotificationsRecord> KEY_ADYEN_NOTIFICATIONS_PRIMARY =
      Internal.createUniqueKey(
          AdyenNotifications.ADYEN_NOTIFICATIONS,
          DSL.name("KEY_adyen_notifications_PRIMARY"),
          new TableField[] {AdyenNotifications.ADYEN_NOTIFICATIONS.RECORD_ID},
          true);
  public static final UniqueKey<AdyenNotificationsRecord> KEY_ADYEN_NOTIFICATIONS_RECORD_ID =
      Internal.createUniqueKey(
          AdyenNotifications.ADYEN_NOTIFICATIONS,
          DSL.name("KEY_adyen_notifications_record_id"),
          new TableField[] {AdyenNotifications.ADYEN_NOTIFICATIONS.RECORD_ID},
          true);
  public static final UniqueKey<AdyenPaymentMethodsRecord>
      KEY_ADYEN_PAYMENT_METHODS_ADYEN_PAYMENT_METHODS_KB_PAYMENT_ID =
          Internal.createUniqueKey(
              AdyenPaymentMethods.ADYEN_PAYMENT_METHODS,
              DSL.name("KEY_adyen_payment_methods_adyen_payment_methods_kb_payment_id"),
              new TableField[] {AdyenPaymentMethods.ADYEN_PAYMENT_METHODS.KB_PAYMENT_METHOD_ID},
              true);
  public static final UniqueKey<AdyenPaymentMethodsRecord> KEY_ADYEN_PAYMENT_METHODS_PRIMARY =
      Internal.createUniqueKey(
          AdyenPaymentMethods.ADYEN_PAYMENT_METHODS,
          DSL.name("KEY_adyen_payment_methods_PRIMARY"),
          new TableField[] {AdyenPaymentMethods.ADYEN_PAYMENT_METHODS.RECORD_ID},
          true);
  public static final UniqueKey<AdyenPaymentMethodsRecord> KEY_ADYEN_PAYMENT_METHODS_RECORD_ID =
      Internal.createUniqueKey(
          AdyenPaymentMethods.ADYEN_PAYMENT_METHODS,
          DSL.name("KEY_adyen_payment_methods_record_id"),
          new TableField[] {AdyenPaymentMethods.ADYEN_PAYMENT_METHODS.RECORD_ID},
          true);
  public static final UniqueKey<AdyenResponsesRecord> KEY_ADYEN_RESPONSES_PRIMARY =
      Internal.createUniqueKey(
          AdyenResponses.ADYEN_RESPONSES,
          DSL.name("KEY_adyen_responses_PRIMARY"),
          new TableField[] {AdyenResponses.ADYEN_RESPONSES.RECORD_ID},
          true);
  public static final UniqueKey<AdyenResponsesRecord> KEY_ADYEN_RESPONSES_RECORD_ID =
      Internal.createUniqueKey(
          AdyenResponses.ADYEN_RESPONSES,
          DSL.name("KEY_adyen_responses_record_id"),
          new TableField[] {AdyenResponses.ADYEN_RESPONSES.RECORD_ID},
          true);
}
