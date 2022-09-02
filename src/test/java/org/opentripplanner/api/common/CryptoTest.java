package org.opentripplanner.api.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

public class CryptoTest {

  OffsetDateTime expiry = OffsetDateTime.parse("2020-02-17T17:04:55+01:00");

  String plainText = "Roxanne! You don't need to put out the red light!";

  @Test
  public void shouldEncryptAndDecrypt() throws Exception {
    String cipherText = Crypto.encrypt(plainText);

    assertEquals(
      cipherText,
      "zal31JId-7PA4zSWSlhququ6b5jogbdqUHE1-QTaCdeuONKTo4Zj8-fmvNPTKcdW4Dv1KYDQuNsq6OI3abC8SA"
    );

    assertEquals(Crypto.decrypt(cipherText), plainText);
  }

  @Test
  public void shouldEncryptAndDecryptWithExpiry() throws Exception {
    String cipherText = Crypto.encryptWithExpiry(plainText, expiry);

    assertEquals(
      cipherText,
      "zal31JId-7PA4zSWSlhququ6b5jogbdqUHE1-QTaCdeuONKTo4Zj8-fmvNPTKcdW0vBJuQgEn_U-nn0E0HpnzewlNqjOsEQTBMKJDPRqW1w"
    );
    assertEquals(
      Crypto.decrypt(cipherText),
      "Roxanne! You don't need to put out the red light!___-___1581955495"
    );

    Crypto.DecryptionResult result = Crypto.decryptWithExpiry(cipherText);

    assertTrue(result.expiry.isEqual(expiry));
    assertEquals(result.plainText, plainText);
  }

  @Test
  public void shouldDecryptUrls() throws Exception {
    String cipherText =
      "iHZUunQLPB_iGqxydlOUb-uwA7EjBpxzHJOpuCNUjBpKfVg7MljdxM2QiRz_i0zt4eqHMCVLj6u8d1lYApjssw";

    Crypto.DecryptionResult result = Crypto.decryptWithExpiry(cipherText);

    assertEquals(result.expiry.toString(), "2020-02-17T17:48:08Z");
    assertEquals(result.plainText, "https://de.wikipedia.org/wiki/Mitfahrzentrale");
  }
}
