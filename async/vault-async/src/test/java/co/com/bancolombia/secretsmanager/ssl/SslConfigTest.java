package co.com.bancolombia.secretsmanager.ssl;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.connector.ssl.SslConfig;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URI;

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
        URI keyUri = getClass().getClassLoader().getResource("client_key.pem").toURI();
        URI certUri = getClass().getClassLoader().getResource("client_cert.pem").toURI();
        File keyFile = new File(keyUri);
        File certFile = new File(certUri);
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

}
