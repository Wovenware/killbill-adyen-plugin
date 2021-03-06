/*
 * This file is generated by jOOQ.
 */
package org.killbill.billing.plugin.adyen.dao.gen.tables;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row8;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.jooq.types.ULong;
import org.killbill.billing.plugin.adyen.dao.gen.Indexes;
import org.killbill.billing.plugin.adyen.dao.gen.Keys;
import org.killbill.billing.plugin.adyen.dao.gen.Killbill;
import org.killbill.billing.plugin.adyen.dao.gen.tables.records.AdyenHppRequestsRecord;

/** This class is generated by jOOQ. */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class AdyenHppRequests extends TableImpl<AdyenHppRequestsRecord> {

  private static final long serialVersionUID = 1L;

  /** The reference instance of <code>killbill.adyen_hpp_requests</code> */
  public static final AdyenHppRequests ADYEN_HPP_REQUESTS = new AdyenHppRequests();

  /** The class holding records for this type */
  @Override
  public Class<AdyenHppRequestsRecord> getRecordType() {
    return AdyenHppRequestsRecord.class;
  }

  /** The column <code>killbill.adyen_hpp_requests.record_id</code>. */
  public final TableField<AdyenHppRequestsRecord, ULong> RECORD_ID =
      createField(
          DSL.name("record_id"),
          SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true),
          this,
          "");

  /** The column <code>killbill.adyen_hpp_requests.kb_account_id</code>. */
  public final TableField<AdyenHppRequestsRecord, String> KB_ACCOUNT_ID =
      createField(DSL.name("kb_account_id"), SQLDataType.CHAR(36).nullable(false), this, "");

  /** The column <code>killbill.adyen_hpp_requests.kb_payment_id</code>. */
  public final TableField<AdyenHppRequestsRecord, String> KB_PAYMENT_ID =
      createField(
          DSL.name("kb_payment_id"),
          SQLDataType.CHAR(36).defaultValue(DSL.inline("NULL", SQLDataType.CHAR)),
          this,
          "");

  /** The column <code>killbill.adyen_hpp_requests.kb_payment_transaction_id</code>. */
  public final TableField<AdyenHppRequestsRecord, String> KB_PAYMENT_TRANSACTION_ID =
      createField(
          DSL.name("kb_payment_transaction_id"),
          SQLDataType.CHAR(36).defaultValue(DSL.inline("NULL", SQLDataType.CHAR)),
          this,
          "");

  /** The column <code>killbill.adyen_hpp_requests.transaction_external_key</code>. */
  public final TableField<AdyenHppRequestsRecord, String> TRANSACTION_EXTERNAL_KEY =
      createField(
          DSL.name("transaction_external_key"), SQLDataType.VARCHAR(255).nullable(false), this, "");

  /** The column <code>killbill.adyen_hpp_requests.additional_data</code>. */
  public final TableField<AdyenHppRequestsRecord, String> ADDITIONAL_DATA =
      createField(
          DSL.name("additional_data"),
          SQLDataType.CLOB.defaultValue(DSL.inline("NULL", SQLDataType.CLOB)),
          this,
          "");

  /** The column <code>killbill.adyen_hpp_requests.created_date</code>. */
  public final TableField<AdyenHppRequestsRecord, LocalDateTime> CREATED_DATE =
      createField(DSL.name("created_date"), SQLDataType.LOCALDATETIME(0).nullable(false), this, "");

  /** The column <code>killbill.adyen_hpp_requests.kb_tenant_id</code>. */
  public final TableField<AdyenHppRequestsRecord, String> KB_TENANT_ID =
      createField(DSL.name("kb_tenant_id"), SQLDataType.CHAR(36).nullable(false), this, "");

  private AdyenHppRequests(Name alias, Table<AdyenHppRequestsRecord> aliased) {
    this(alias, aliased, null);
  }

  private AdyenHppRequests(
      Name alias, Table<AdyenHppRequestsRecord> aliased, Field<?>[] parameters) {
    super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
  }

  /** Create an aliased <code>killbill.adyen_hpp_requests</code> table reference */
  public AdyenHppRequests(String alias) {
    this(DSL.name(alias), ADYEN_HPP_REQUESTS);
  }

  /** Create an aliased <code>killbill.adyen_hpp_requests</code> table reference */
  public AdyenHppRequests(Name alias) {
    this(alias, ADYEN_HPP_REQUESTS);
  }

  /** Create a <code>killbill.adyen_hpp_requests</code> table reference */
  public AdyenHppRequests() {
    this(DSL.name("adyen_hpp_requests"), null);
  }

  public <O extends Record> AdyenHppRequests(
      Table<O> child, ForeignKey<O, AdyenHppRequestsRecord> key) {
    super(child, key, ADYEN_HPP_REQUESTS);
  }

  @Override
  public Schema getSchema() {
    return Killbill.KILLBILL;
  }

  @Override
  public List<Index> getIndexes() {
    return Arrays.<Index>asList(
        Indexes.ADYEN_HPP_REQUESTS_ADYEN_HPP_REQUESTS_KB_ACCOUNT_ID,
        Indexes.ADYEN_HPP_REQUESTS_ADYEN_HPP_REQUESTS_KB_PAYMENT_TRANSACTION_ID,
        Indexes.ADYEN_HPP_REQUESTS_ADYEN_HPP_REQUESTS_KB_TRANSACTION_EXTERNAL_KEY);
  }

  @Override
  public Identity<AdyenHppRequestsRecord, ULong> getIdentity() {
    return (Identity<AdyenHppRequestsRecord, ULong>) super.getIdentity();
  }

  @Override
  public UniqueKey<AdyenHppRequestsRecord> getPrimaryKey() {
    return Keys.KEY_ADYEN_HPP_REQUESTS_PRIMARY;
  }

  @Override
  public List<UniqueKey<AdyenHppRequestsRecord>> getKeys() {
    return Arrays.<UniqueKey<AdyenHppRequestsRecord>>asList(
        Keys.KEY_ADYEN_HPP_REQUESTS_PRIMARY, Keys.KEY_ADYEN_HPP_REQUESTS_RECORD_ID);
  }

  @Override
  public AdyenHppRequests as(String alias) {
    return new AdyenHppRequests(DSL.name(alias), this);
  }

  @Override
  public AdyenHppRequests as(Name alias) {
    return new AdyenHppRequests(alias, this);
  }

  /** Rename this table */
  @Override
  public AdyenHppRequests rename(String name) {
    return new AdyenHppRequests(DSL.name(name), null);
  }

  /** Rename this table */
  @Override
  public AdyenHppRequests rename(Name name) {
    return new AdyenHppRequests(name, null);
  }

  // -------------------------------------------------------------------------
  // Row8 type methods
  // -------------------------------------------------------------------------

  @Override
  public Row8<ULong, String, String, String, String, String, LocalDateTime, String> fieldsRow() {
    return (Row8) super.fieldsRow();
  }
}
