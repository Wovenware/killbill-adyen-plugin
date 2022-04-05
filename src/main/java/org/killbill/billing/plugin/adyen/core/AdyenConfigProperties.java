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
  private static final String DEFAULT_CONNECTION_TIMEOUT = "5";
  private static final String DEFAULT_READ_TIMEOUT = "90";

  private final String region;

  private String connectionTimeout;
  private String readTimeout;
  private String apiKey;
  private String merchantAccount;
  private String returnUrl;

  public AdyenConfigProperties(final Properties properties, final String region) {
    this.region = region;

    this.apiKey = properties.getProperty(PROPERTY_PREFIX + "apiKey");
    this.merchantAccount = properties.getProperty(PROPERTY_PREFIX + "merchantAccount");
    this.returnUrl = properties.getProperty(PROPERTY_PREFIX + "returnUrl");
    setConnectionTimeout(
        properties.getProperty(PROPERTY_PREFIX + "connectionTimeout", DEFAULT_CONNECTION_TIMEOUT));
    setReadTimeout(properties.getProperty(PROPERTY_PREFIX + "readTimeout", DEFAULT_READ_TIMEOUT));
  }

  public String getRegion() {
    return region;
  }

  public String getConnectionTimeout() {
    return connectionTimeout;
  }

  public String getReadTimeout() {
    return readTimeout;
  }

  public String getApiKey() {
    return apiKey;
  }

  public String getMerchantAccount() {
    return merchantAccount;
  }

  public String getReturnUrl() {
    return returnUrl;
  }

  private void setReadTimeout(String readTimeout) {
    long readTimeoutValue = 10000;
    if (!readTimeout.equals("")) {
      try {
        readTimeoutValue = Long.parseLong(readTimeout);
      } catch (NumberFormatException e) {
        // Allow default to be returned
      }
    }
    this.readTimeout = ((Long) readTimeoutValue).toString();
  }

  private void setConnectionTimeout(String connectionTimeout) {
    long connectionTimeoutValue = 10000;
    if (!connectionTimeout.equals("")) {
      try {
        connectionTimeoutValue = Long.parseLong(connectionTimeout);
      } catch (NumberFormatException e) {
        // Allow default to be returned
      }
    }
    this.connectionTimeout = ((Long) connectionTimeoutValue).toString();
  }

  private final boolean parseBoolean(String s) {
    if (s != null && s.equalsIgnoreCase("yes")) {
      return true;
    }

    return Boolean.parseBoolean(s);
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
