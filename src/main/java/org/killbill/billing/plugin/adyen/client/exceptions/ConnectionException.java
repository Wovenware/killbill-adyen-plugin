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
package org.killbill.billing.plugin.adyen.client.exceptions;

import java.io.IOException;
import lombok.Getter;

@Getter
public class ConnectionException extends IOException {
  private final int status;

  public ConnectionException(String message, Throwable cause) {
    super(message, cause);
    this.status = 0;
  }

  public ConnectionException(String message, int status) {
    super(message);
    this.status = status;
  }

  public int getStatus() {
    return this.status;
  }
}
