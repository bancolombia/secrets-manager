package co.com.bancolombia.secretsmanager.vault.ssl;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SslConfigTest {

    @SneakyThrows
    @Test
    void testLoadTrustStore() {
        URI trustStoreUri = getClass().getClassLoader().getResource("truststore.jks").toURI();
        File f = new File(trustStoreUri);
        SslConfig config = new SslConfig().trustStoreFile(f).build();
        assertNotNull(config);
        assertNotNull(config.getSslContext());
    }

    @Test
    void testHandleFailureToLoadTrustStore() {
        File f = new File("nofile.jks");
        assertThrows(SecretException.class, () -> new SslConfig().trustStoreFile(f));
    }

    @SneakyThrows
    @Test
    void testLoadPemCertificate() {
        URI certUri = getClass().getClassLoader().getResource("certificate.arm").toURI();
        File f = new File(certUri);
        SslConfig config = new SslConfig().pemFile(f).build();
        assertNotNull(config);
        assertNotNull(config.getSslContext());
    }

    @Test
    void testHandleFailureToLoadPemCertificate() {
        File f = new File("nofile.pem");
        assertThrows(SecretException.class, () -> new SslConfig().pemFile(f));
    }

    @SneakyThrows
    @Test
    void testLoadKeyStore() {
        URI keyStoreUri = getClass().getClassLoader().getResource("keystore.jks").toURI();
        File f = new File(keyStoreUri);
        SslConfig config = new SslConfig().keyStoreFile(f, "changeit").build();
        assertNotNull(config);
        assertNotNull(config.getSslContext());
    }

    @Test
    void testHandleFailureToLoadKeyStore() {
        File f = new File("nofile.jks");
        assertThrows(SecretException.class, () -> new SslConfig().keyStoreFile(f, "changeit"));
    }

    @SneakyThrows
    @Test
    void testLoadKeyPem() {
        URI certUri = getClass().getClassLoader().getResource("client_cert.pem").toURI();
        File certFile = new File(certUri);

        File keyFile = generateTestKey(certFile.getParentFile().toString());

        SslConfig config = new SslConfig().clientKeyPemFile(keyFile)
                .clientPemFile(certFile).build();
        assertNotNull(config);
        assertNotNull(config.getSslContext());
    }

    @Test
    void testHandleFailureToLoadKeyPem() {
        File f = new File("nofile.pem");
        assertThrows(SecretException.class, () -> new SslConfig().clientKeyPemFile(f));
    }

    @SneakyThrows
    private File generateTestKey(String dir) {
        //Creating KeyPair generator object
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");

        //Initializing the KeyPairGenerator
        keyPairGen.initialize(2048);

        //Generating the pair of keys
        KeyPair pair = keyPairGen.generateKeyPair();

        //Getting the private key from the key pair
        PrivateKey privKey = pair.getPrivate();

        String keypem = "-----BEGIN PRIVATE KEY-----\n" +
                Base64.getEncoder().encodeToString(privKey.getEncoded()) +
                "\n-----END PRIVATE KEY-----\n";

        File dest = new File(dir + File.separator + "rsaPrivateKey.pem");
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(dest))) {
            dos.write(keypem.getBytes());
            dos.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return dest;
    }

}
