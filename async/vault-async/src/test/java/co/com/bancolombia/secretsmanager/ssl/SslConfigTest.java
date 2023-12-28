package co.com.bancolombia.secretsmanager.ssl;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.connector.ssl.SslConfig;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.util.Base64;

import static org.junit.Assert.assertNotNull;

public class SslConfigTest {

    @SneakyThrows
    @Test
    public void testLoadTrustStore() {
        URI trustStoreUri = getClass().getClassLoader().getResource("truststore.jks").toURI();
        File f = new File(trustStoreUri);
        SslConfig config = new SslConfig().trustStoreFile(f).build();
        assertNotNull(config);
        assertNotNull(config.getSslContext());
    }

    @Test
    public void testHandleFailureToLoadTrustStore() {
        File f = new File("nofile.jks");
        Assert.assertThrows(SecretException.class, () -> new SslConfig().trustStoreFile(f));
    }

    @SneakyThrows
    @Test
    public void testLoadPemCertificate() {
        URI certUri = getClass().getClassLoader().getResource("certificate.arm").toURI();
        File f = new File(certUri);
        SslConfig config = new SslConfig().pemFile(f).build();
        assertNotNull(config);
        assertNotNull(config.getSslContext());
    }

    @Test
    public void testHandleFailureToLoadPemCertificate() {
        File f = new File("nofile.pem");
        Assert.assertThrows(SecretException.class, () -> new SslConfig().pemFile(f));
    }

    @SneakyThrows
    @Test
    public void testLoadKeyStore() {
        URI keyStoreUri = getClass().getClassLoader().getResource("keystore.jks").toURI();
        File f = new File(keyStoreUri);
        SslConfig config = new SslConfig().keyStoreFile(f, "changeit").build();
        assertNotNull(config);
        assertNotNull(config.getSslContext());
    }

    @Test
    public void testHandleFailureToLoadKeyStore() {
        File f = new File("nofile.jks");
        Assert.assertThrows(SecretException.class, () -> new SslConfig().keyStoreFile(f, "changeit"));
    }

    @SneakyThrows
    @Test
    public void testLoadKeyPem() {
        URI certUri = getClass().getClassLoader().getResource("client_cert.pem").toURI();
        File certFile = new File(certUri);

        File keyFile = generateTestKey(certFile.getParentFile().toString());

        SslConfig config = new SslConfig().clientKeyPemFile(keyFile)
                .clientPemFile(certFile).build();
        assertNotNull(config);
        assertNotNull(config.getSslContext());
    }

    @Test
    public void testHandleFailureToLoadKeyPem() {
        File f = new File("nofile.pem");
        Assert.assertThrows(SecretException.class, () -> new SslConfig().clientKeyPemFile(f));
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

        String keypem  = "-----BEGIN PRIVATE KEY-----\n" +
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
