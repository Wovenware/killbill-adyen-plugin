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

public enum AuthenticationCodes {
  MATCH("0", "Match"),
  UNMATCH("1", "Unmatch"),
  DURING_THE_DELEGATION("5", "During the delegation"),
  NOT_SET("8", "Not Set"),
  INQUIRY_NOT_POSSIBLE("9", "Inquiry not possible");

  private String description;
  private String status;

  AuthenticationCodes(String status, String description) {
    this.description = description;
    this.status = status;
  }

  public String getDescription() {
    return description;
  }

  public String getStatus() {
    return status;
  }

  public Boolean statusEquals(String value) {
    return this.status.equals(value);
  }

  public static String resolveDescription(String cd) {
    String descriptionFound = null;
    for (AuthenticationCodes code : AuthenticationCodes.values()) {
      if (code.status.equals(cd)) {
        descriptionFound = code.getDescription();
      }
    }

    return descriptionFound;
  }
}
