package co.com.bancolombia.secretsmanager.vault.ssl;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import lombok.Getter;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class SslConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    public static final String TLSV_1_2 = "TLSv1.2";

    @Getter
    private transient SSLContext sslContext;
    private transient KeyStore trustStore;
    private transient KeyStore keyStore;
    private String keyStorePassword;
    private String pemUTF8;
    private String clientPemUTF8;
    private String clientKeyPemUTF8;

    /**
     * Loads a JKS trust store from the given file.
     *
     * @return this SslConfig.
     * @throws SecretException if the file cannot be read or is not a valid key store
     */
    public SslConfig trustStoreFile(File trustStoreFile) throws SecretException {
        try (InputStream inputStream = new FileInputStream(trustStoreFile)) {
            this.trustStore = this.inputStreamToKeyStore(inputStream, (String) null);
            return this;
        } catch (IOException e) {
            throw new SecretException(e.getMessage(), e);
        }
    }

    /**
     * Loads a PEM trust certificate from the given file.
     *
     * @return this SslConfig.
     * @throws SecretException if the file cannot be read
     */
    public SslConfig pemFile(File pemFile) throws SecretException {
        try (InputStream input = new FileInputStream(pemFile)) {
            this.pemUTF8 = inputStreamToUTF8(input);
            return this;
        } catch (Exception ex) {
            throw new SecretException(ex.getMessage(), ex);
        }
    }

    /**
     * Loads a JKS key store from the given file.
     *
     * @return this SslConfig.
     * @throws SecretException if the file cannot be read, is not a valid key store or the password is wrong
     */
    public SslConfig keyStoreFile(File keyStoreFile, String password) throws SecretException {
        try (InputStream inputStream = new FileInputStream(keyStoreFile)) {
            this.keyStore = this.inputStreamToKeyStore(inputStream, password);
            this.keyStorePassword = password;
            return this;
        } catch (IOException e) {
            throw new SecretException(e.getMessage(), e);
        }
    }

    /**
     * Loads the client PEM certificate from the given file.
     *
     * @return this SslConfig.
     * @throws SecretException if the file cannot be read
     */
    public SslConfig clientPemFile(File clientPemFile) throws SecretException {
        try (InputStream input = new FileInputStream(clientPemFile)) {
            this.clientPemUTF8 = inputStreamToUTF8(input);
            return this;
        } catch (IOException e) {
            throw new SecretException(e.getMessage(), e);
        }
    }

    /**
     * Loads the client PEM private key from the given file.
     *
     * @return this SslConfig.
     * @throws SecretException if the file cannot be read
     */
    public SslConfig clientKeyPemFile(File clientKeyPemFile) throws SecretException {
        try (InputStream input = new FileInputStream(clientKeyPemFile)) {
            this.clientKeyPemUTF8 = inputStreamToUTF8(input);
            return this;
        } catch (IOException e) {
            throw new SecretException(e.getMessage(), e);
        }
    }

    /**
     * Builds the SSLContext from the loaded JKS or PEM resources.
     *
     * @return this SslConfig.
     * @throws SecretException if the SSLContext cannot be created from the loaded resources
     */
    public SslConfig build() throws SecretException {
        if (this.keyStore == null && this.trustStore == null) {
            if (this.pemUTF8 != null || this.clientPemUTF8 != null || this.clientKeyPemUTF8 != null) {
                this.sslContext = this.buildSslContextFromPem();
            }
        } else {
            this.sslContext = this.buildSslContextFromJks();
        }
        return this;
    }

    private SSLContext buildSslContextFromJks() throws SecretException {
        TrustManager[] trustManagers = null;
        if (this.trustStore != null) {
            try {
                var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(this.trustStore);
                trustManagers = trustManagerFactory.getTrustManagers();
            } catch (KeyStoreException | NoSuchAlgorithmException e) {
                throw new SecretException(e.getMessage(), e);
            }
        }

        KeyManager[] keyManagers = null;
        if (this.keyStore != null) {
            try {
                var keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(
                        this.keyStore, this.keyStorePassword == null ? null : this.keyStorePassword.toCharArray()
                );
                keyManagers = keyManagerFactory.getKeyManagers();
            } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
                throw new SecretException(e.getMessage(), e);
            }
        }

        try {
            SSLContext lSslContext = SSLContext.getInstance(TLSV_1_2);
            lSslContext.init(keyManagers, trustManagers, (SecureRandom) null);
            return lSslContext;
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new SecretException(e.getMessage(), e);
        }
    }

    private SSLContext buildSslContextFromPem() throws SecretException {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            TrustManager[] trustManagers = null;
            if (this.pemUTF8 != null) {
                var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

                X509Certificate certificate = buildX509Certificate(
                        certificateFactory, this.pemUTF8.getBytes(StandardCharsets.UTF_8)
                );

                KeyStore iKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                iKeyStore.load((KeyStore.LoadStoreParameter) null);
                iKeyStore.setCertificateEntry("caCert", certificate);
                trustManagerFactory.init(iKeyStore);
                trustManagers = trustManagerFactory.getTrustManagers();
            }

            KeyManager[] keyManagers = null;
            if (this.clientPemUTF8 != null && this.clientKeyPemUTF8 != null) {
                var keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                X509Certificate clientCertificate = buildX509Certificate(
                        certificateFactory, this.clientPemUTF8.getBytes(StandardCharsets.UTF_8)
                );

                String strippedKey = this.clientKeyPemUTF8.replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "");
                byte[] keyBytes = Base64.getMimeDecoder().decode(strippedKey);
                PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
                KeyStore ikeyStore = KeyStore.getInstance(KeyStore.getDefaultType());

                ikeyStore.load((InputStream) null, null);
                ikeyStore.setCertificateEntry("clientCert", clientCertificate);
                ikeyStore.setKeyEntry("key", privateKey, null,
                        new Certificate[]{clientCertificate});
                keyManagerFactory.init(ikeyStore, null);
                keyManagers = keyManagerFactory.getKeyManagers();
            }

            SSLContext lSslContext = SSLContext.getInstance(TLSV_1_2);
            lSslContext.init(keyManagers, trustManagers, (SecureRandom) null);
            return lSslContext;
        } catch (IOException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException |
                 UnrecoverableKeyException | InvalidKeySpecException | CertificateException e) {
            throw new SecretException(e.getMessage(), e);
        }
    }

    private X509Certificate buildX509Certificate(CertificateFactory certificateFactory, byte[] pemBytes)
            throws SecretException {
        try (ByteArrayInputStream pem = new ByteArrayInputStream(pemBytes)) {
            return (X509Certificate) certificateFactory.generateCertificate(pem);
        } catch (IOException | CertificateException e) {
            throw new SecretException(e.getMessage(), e);
        }
    }

    private KeyStore inputStreamToKeyStore(InputStream inputStream, String password) throws SecretException {
        try {
            KeyStore lKeyStore = KeyStore.getInstance("JKS");
            lKeyStore.load(inputStream, password == null ? null : password.toCharArray());
            return lKeyStore;
        } catch (CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException e) {
            throw new SecretException(e.getMessage(), e);
        }
    }

    private static String inputStreamToUTF8(InputStream input) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        StringBuilder utf8 = new StringBuilder();

        String str;
        while ((str = in.readLine()) != null) {
            utf8.append(str).append(System.lineSeparator());
        }

        in.close();
        return utf8.toString();
    }
}