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

package org.killbill.billing.plugin.adyen.core;

import java.util.Map;
import java.util.Properties;

public class AdyenConfigProperties {

  private static final String PROPERTY_PREFIX = "org.killbill.billing.plugin.adyen.";

  public static final String ADYEN_API_KEY = "ADYEN_API_KEY";
  public static final String ADYEN_RETURN_URL = "ADYEN_RETURN_URL";
  public static final String ADYEN_HMAC_KEY = "ADYEN_HMAC_KEY";
  public static final String ADYEN_MERCHANT_ACCOUNT = "ADYEN_MERCHANT_ACCOUNT";

  public static final String ADYEN_CAPTURE_DELAY_HOURS = "ADYEN_CAPTURE_DELAY_HOURS";

  private final String region;

  private String apiKey;
  private String merchantAccount;
  private String returnUrl;
  private String hcmaKey;
  private String captureDelayHours;

  public AdyenConfigProperties(final Properties properties, final String region) {
    this.region = region;

    this.apiKey = properties.getProperty(PROPERTY_PREFIX + "apiKey");
    this.merchantAccount = properties.getProperty(PROPERTY_PREFIX + "merchantAccount");
    this.returnUrl = properties.getProperty(PROPERTY_PREFIX + "returnUrl");
    this.hcmaKey = properties.getProperty(PROPERTY_PREFIX + "hcmaKey");
    this.captureDelayHours = properties.getProperty(PROPERTY_PREFIX + "captureDelayHours");
  }

  public String getRegion() {

    return region;
  }

  public String getApiKey() {
    if (apiKey == null || apiKey.isEmpty()) {
      return getClient(ADYEN_API_KEY, null);
    }
    return apiKey;
  }

  public String getHMACKey() {
    if (hcmaKey == null || hcmaKey.isEmpty()) {
      return getClient(ADYEN_HMAC_KEY, null);
    }
    return hcmaKey;
  }

  public String getMerchantAccount() {
    if (merchantAccount == null || merchantAccount.isEmpty()) {
      return getClient(ADYEN_MERCHANT_ACCOUNT, null);
    }

    return merchantAccount;
  }

  public String getReturnUrl() {
    if (returnUrl == null || returnUrl.isEmpty()) {
      return getClient(ADYEN_RETURN_URL, null);
    }

    return returnUrl;
  }

  public String getCaptureDelayHours() {
    if (captureDelayHours == null || captureDelayHours.isEmpty()) {
      return getClient(ADYEN_CAPTURE_DELAY_HOURS, "0");
    }

    return captureDelayHours;
  }

  private String getClient(String envKey, String defaultValue) {
    Map<String, String> env = System.getenv();

    String value = env.get(envKey);

    if (value == null || value.isEmpty()) {
      return defaultValue;
    }

    return value;
  }
}
