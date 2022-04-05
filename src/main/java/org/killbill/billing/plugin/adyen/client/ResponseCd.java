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

public enum ResponseCd {
  K("Format error. Please check the setting value and reprocess it."),
  K01(
      "The systemid setting value is not covered. (Missing submitted items or item errors)"
          + "Please check the setting value and reprocess it"),
  K02("MerchantId setting value is not valid."),
  K03("MerchantPass setting value is not valid."),
  K04("TenantId setting value is not valid."),
  K05("MerchantPass setting value is not valid. "),
  K06("The OperatorId setting value is not valid."),
  K07("MerchantFree1 setting value is not valid."),
  K08("MerchantFree2 setting value is not valid."),
  K09("MerchantFree3 setting value is not valid."),
  K10("ProcessId setting value is not valid."),
  K11("ProcessPass setting value is not valid."),
  K12("ProcessId or ProcessPass is invalid. Please check the setting value and reprocess it."),
  K14("OperateId status transition inconsistencies."),
  K15("Error in the number of members to be returned when referring to token members"),
  K20("CardNo setting value is not valid."),
  K21("CardExp setting value is not valid."),
  K22("PayType setting value is not valid."),
  K23("Amount setting value is not valid."),
  K24("SecCd setting value is not valid."),
  K25("KanaSei setting value is not valid."),
  K26("KanaMei setting value is not valid."),
  K27("BirthDay setting value is not valid."),
  K28("TelNo setting value is not valid."),
  K39("SalesDate setting value is not valid."),
  K41("Birth month and day unmatch verification."),
  K43("KanaSei unmatch verification ."),
  K44("KanaMei unmatch verification ."),
  K45("KaiinId setting value is not valid."),
  K46("KaiinPass setting value is not valid."),
  K68(
      "The membership registration function is not available. Please check the setting value and reprocess it."),
  K69("Duplicate member ID error. Please check the setting value and reprocess it."),
  K71("Member Id setting value is not valid. "),
  K73("The member is already active. Please check the setting value and reprocess it."),
  K74("Consecutive membership failures"),
  K79("Login is currently disabled or a member is disabled."),
  K80(
      "Member ID setting mismatch (setting required).Please check the setting value and reprocess it."),
  K81(
      "Member ID setting mismatch (setting no required).Please check the setting value and reprocess it."),
  K82("Card number setting value is not valid."),
  K83("Expiration date setting value is not valid."),
  K84("Member Id setting value is not valid."),
  K85("Member Password setting value is not valid."),
  K86("ProcNo setting value is not valid."),
  K88("Original transaction duplicate error"),
  K89("This processing number is already in use."),
  K96("System communication failure (timeout). Please check the setting value and reprocess it."),
  K98("Gateway Server Error, Please Contact Us."),
  KG8("Merchant ID, merchant password authentication failed continuously and was locked out."),
  KBZ("No original transaction error"),
  KHX("Token setting value is not valid."),
  KHZ("No Token"),
  KI2("Used Token."),
  KI3("Token Expirated."),
  KI5("Locked by continuous input of the same card number."),
  KI8("There are multiple transactions."),
  KIN("This is a transaction that does not require cancellation."),
  KIR("AuthKaiiAddFlag setting value is not valid."),
  KIW("KaiinIdRiyoFlag setting value is not valid."),
  KIU("It is locked by continuous input of the member ID."),
  C("Format error.Please check the setting value and reprocess it."),
  C01(
      "Errors related to our settings. Please contact us after confirming that your submission is in accordance with specifications"),
  C02("Comunication Error. Wait for retry"),
  C03("Card Company Center Unreachable. Wait for retry. "),
  C10("Please set the number of payments (divisions) with the contract and reprocess it."),
  C13("The card expiration date is entered incorrectly. Or an expired card."),
  C14(
      "Cancellation process has already been performed. Please check the processing status on the extranet."),
  C16("The card membership number does not exist."),
  C17("Card number outside the contract range. Or a card numbering system that does not exist."),
  C18(
      "The card number to be exempted from authorization. To generate this error, you must set up individually."
          + "it will be in need. Please contact us."),
  C70("Our configuration information error. Please contact us."),
  C71("Our configuration information error. Please contact us"),
  C80("Card Company Center Unreachable. Wait for retry"),
  C82("Duplicate transaction identification number error."),
  C98(
      "Please contact us after confirming that your submission is in accordance with specifications please. "),
  C99(
      "Please contact us after confirming that your submission is in accordance with specifications please."),
  G("Format error.Please check the setting value and reprocess it. "),
  G12("Card unavailable"),
  G22("Permanent payment ban"),
  G30("Means pending judgment of a transaction."),
  G42("The PIN is incorrect." + "*This may occur if you have a debit card."),
  G44("The security code entered is incorrect."),
  G45(" security code has been entered."),
  G54("Over the number of times or amount of daily use"),
  G55("The daily credit limit is over."),
  G56("The credit card is not valid."),
  G60("The credit card is not valid. "),
  G61("The credit card is not valid"),
  G65("The credit card number is not valid."),
  G68("The amount is entered incorrectly."),
  G72("The entered bonus amount is incorrectly."),
  G74("This means that the number of splits is entered incorrectly."),
  G75("Means that you are below the lower limit of the installment."),
  G78("Means that the payment method was entered incorrectly."),
  G83("The expiration date is entered incorrectly."),
  G84("The authorization number was entered incorrectly."),
  G85("An error occurred during cafis delegates."),
  G92("Card company optional error"),
  G94("Cycle number is not more than specified or numbered."),
  G95("Credit card company has completed its operation."),
  G96("This means that a non-handling credit card has been entered."),
  G97("The request has been rejected and cannot be handled."),
  G98("Is not covered by the connected credit card company."),
  G99("Connection request in-house reception refusal. Try in another moment"),
  OK("Everything Fine");

  private String description;

  ResponseCd(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public static String resolveDescription(String cd) {
    String descriptionFound = null;
    for (ResponseCd responseCd : ResponseCd.values()) {
      if (responseCd.name().equals(cd)) {
        descriptionFound = responseCd.getDescription();
      }
    }

    if (descriptionFound == null) {
      if (cd == null) {
        return "System failure";
      }
      return ResponseCd.valueOf(cd.substring(0, 1)).getDescription();
    }
    return descriptionFound;
  }
}
