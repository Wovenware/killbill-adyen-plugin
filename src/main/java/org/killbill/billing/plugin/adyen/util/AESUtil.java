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
package org.killbill.billing.plugin.adyen.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {
  private static AESUtil instance = null;

  private AESUtil() {}

  public static AESUtil getInstance() {
    if (instance == null) {
      instance = new AESUtil();
    }
    return instance;
  }

  private String temporalEncryptionKey = "ftggfgbfgasdjkdfjkhsdf";

  public String getTemporalEncryptionKey() {
    return temporalEncryptionKey;
  }

  public void setTemporalEncryptionKey(String temporalEncryptionKey) {
    this.temporalEncryptionKey = temporalEncryptionKey;
  }

  private static String algorithm = "AES/GCM/NoPadding";
  private static final int GCM_NONCE_LENGTH = 12; // in bytes
  private static final int SALT_LENGTH_BYTE = 16;
  private static final int TAG_LENGTH_BIT = 128;

  private static byte[] getRandomNonce(int numBytes) {
    final byte[] nonce = new byte[numBytes];
    new SecureRandom().nextBytes(nonce);
    return nonce;
  }

  /**
   * Encrypt string
   *
   * @param text the text to encrypt
   * @return
   * @throws InvalidAlgorithmParameterException
   * @throws InvalidKeyException
   * @throws Exception
   */
  public static String encrypt(String text) throws GeneralSecurityException {

    byte[] salt = getRandomNonce(SALT_LENGTH_BYTE);
    byte[] src = getRandomNonce(GCM_NONCE_LENGTH);

    // secret key from password
    SecretKey aesKeyFromPassword = getKey(salt);

    Cipher cipher = Cipher.getInstance(algorithm);

    // ASE-GCM needs GCMParameterSpec
    cipher.init(Cipher.ENCRYPT_MODE, aesKeyFromPassword, new GCMParameterSpec(TAG_LENGTH_BIT, src));

    byte[] cipherText = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));

    // prefix IV and Salt to cipher text
    byte[] cipherTextWithIvSalt =
        ByteBuffer.allocate(src.length + salt.length + cipherText.length)
            .put(src)
            .put(salt)
            .put(cipherText)
            .array();

    // string representation, base64, send this string to other for decryption.
    return Base64.getEncoder().encodeToString(cipherTextWithIvSalt);
  }

  /**
   * Decrypt encrypted string
   *
   * @param cText
   * @return
   * @throws Exception
   */
  public static String decrypt(String cText) throws GeneralSecurityException {

    byte[] decode = Base64.getDecoder().decode(cText.getBytes(StandardCharsets.UTF_8));

    // get back the iv and salt from the cipher text
    ByteBuffer bb = ByteBuffer.wrap(decode);

    byte[] src = new byte[GCM_NONCE_LENGTH];
    bb.get(src);

    byte[] salt = new byte[SALT_LENGTH_BYTE];
    bb.get(salt);

    byte[] cipherText = new byte[bb.remaining()];
    bb.get(cipherText);

    // get back the aes key from the same password and salt
    SecretKey aesKeyFromPassword = getKey(salt);

    Cipher cipher = Cipher.getInstance(algorithm);

    cipher.init(Cipher.DECRYPT_MODE, aesKeyFromPassword, new GCMParameterSpec(TAG_LENGTH_BIT, src));
    byte[] plainText = cipher.doFinal(cipherText);

    return new String(plainText, StandardCharsets.UTF_8);
  }

  private static SecretKey getKey(byte[] salt)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    String secretKey = AESUtil.getInstance().getTemporalEncryptionKey();
    int iterationCount = 65536;
    int baseKeyLength = 256;

    if (secretKey == null || secretKey.isEmpty()) {
      throw new InvalidParameterException(("Missing encryption password!"));
    }

    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt, iterationCount, baseKeyLength);

    return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
  }
}
