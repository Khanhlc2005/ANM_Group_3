package des;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DemoFlowTest {
    @Test
    void demoTextFlowWorksWithGeneratedKeyAndBase64Output() {
        DesService service = new DesService();
        String key = service.generateRandomKeyHex();
        String plainText = "DES Studio demo text";

        String cipherText = service.encrypt(plainText, InputFormat.TEXT, key, EncodingFormat.BASE64);
        String decryptedText = service.decrypt(cipherText, InputFormat.BASE64, key, EncodingFormat.TEXT);

        assertFalse(cipherText.isBlank());
        assertEquals(plainText, decryptedText);
        assertDoesNotThrow(() -> service.describeKey(key));
    }

    @Test
    void demoHexFlowWorksWithManualKeyAndHexOutput() {
        DesService service = new DesService();
        String key = "13 34 57 79 9B BC DF F1";
        String plainHex = "48656C6C6F20444553";

        String cipherHex = service.encrypt(plainHex, InputFormat.HEX, key, EncodingFormat.HEX);
        String decryptedText = service.decrypt(cipherHex, InputFormat.HEX, key, EncodingFormat.TEXT);

        assertFalse(cipherHex.isBlank());
        assertEquals("Hello DES", decryptedText);
    }
}
