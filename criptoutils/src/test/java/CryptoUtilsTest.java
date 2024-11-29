import com.jonathan.services.CryptoUtils;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class CryptoUtilsTest {

    @Test
    void getHash() throws Exception {
        var text = "Some text...";
        var base64Text = "quonJ6BjRSC1DBOGuBWNdqixj8z20nuP-QH7cVvp7PI";

        assertEquals(base64Text, CryptoUtils.getHash(text));
    }

    @Test
    void createSecretKey() throws Exception {
        SecretKey key = CryptoUtils.createSecretKey();
        assertNotNull(key);
        assertEquals("AES", key.getAlgorithm());
    }

    @Test
    void decodeSecretKey() throws Exception {
        var key = CryptoUtils.createSecretKey();
        var base64Key = Base64.getUrlEncoder().withoutPadding().encodeToString(key.getEncoded());
        var decodedKey = CryptoUtils.decodeSecretKey(base64Key);

        assertNotNull(decodedKey);
        assertEquals(key.getAlgorithm(), decodedKey.getAlgorithm());
        assertArrayEquals(key.getEncoded(), decodedKey.getEncoded());
    }

    @Test
    void encryptAndDecrypt() throws Exception {
        var text = "Hello, World!";
        var key = CryptoUtils.createSecretKey();

        var encrypted = CryptoUtils.encrypt(text, key);
        assertNotNull(encrypted);

        var decrypted = CryptoUtils.decrypt(encrypted, key);
        assertEquals(text, decrypted);
    }

    @Test
    void asymmetricEncryptAndDecrypt() throws Exception {
        // Generar un parell de claus RSA
        var keyPairGenerator = java.security.KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        var keyPair = keyPairGenerator.generateKeyPair();

        var text = "Sensitive Data";
        var publicKey = keyPair.getPublic();
        var privateKey = keyPair.getPrivate();

        var encrypted = CryptoUtils.asymmetricEncrypt(text, publicKey);
        assertNotNull(encrypted);

        var decrypted = CryptoUtils.asymmetricDecrypt(encrypted, privateKey);
        assertEquals(text, decrypted);
    }
}
