package des;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DesServiceTest {
    private static final String KEY_HEX = "133457799BBCDFF1";

    @Test
    void encryptsTextInputToBase64AndDecryptsToHexPlainBytes() {
        DesService service = new DesService();
        String plainText = "DES Studio";

        String cipherText = service.encrypt(plainText, InputFormat.TEXT, KEY_HEX, EncodingFormat.BASE64);
        String decryptedHex = service.decrypt(cipherText, InputFormat.BASE64, KEY_HEX, EncodingFormat.HEX);

        assertFalse(cipherText.isBlank());
        assertEquals(EncodingUtils.encodeHex(EncodingUtils.utf8Bytes(plainText)), decryptedHex);
    }

    @Test
    void encryptsHexInputToHexAndDecryptsToBase64PlainBytes() {
        DesService service = new DesService();
        String plainHex = "0123456789ABCDEF";

        String cipherHex = service.encrypt(plainHex, InputFormat.HEX, KEY_HEX, EncodingFormat.HEX);
        String decryptedBase64 = service.decrypt(cipherHex, InputFormat.HEX, KEY_HEX, EncodingFormat.BASE64);

        assertFalse(cipherHex.isBlank());
        assertEquals(EncodingUtils.encodeBase64(EncodingUtils.decodeHex(plainHex)), decryptedBase64);
    }

    @Test
    void generatesValidRandomKeyAndDescribesRoundKeys() {
        DesService service = new DesService();
        String key = service.generateRandomKeyHex();

        assertEquals(16, key.length());
        assertDoesNotThrow(() -> EncodingUtils.decodeDesKeyHex(key));
        assertDoesNotThrow(() -> service.describeKey(key));
    }

    @Test
    void rejectsInvalidUiInputsWithClearExceptions() {
        DesService service = new DesService();

        assertThrows(IllegalArgumentException.class,
                () -> service.encrypt("", InputFormat.TEXT, KEY_HEX, EncodingFormat.HEX));
        assertThrows(IllegalArgumentException.class,
                () -> service.encrypt("text", InputFormat.TEXT, "bad-key", EncodingFormat.HEX));
        assertThrows(IllegalArgumentException.class,
                () -> service.decrypt("not-base64", InputFormat.BASE64, KEY_HEX, EncodingFormat.HEX));
    }
}
