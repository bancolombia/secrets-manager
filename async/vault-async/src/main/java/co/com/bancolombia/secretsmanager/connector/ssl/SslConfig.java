package co.com.bancolombia.secretsmanager.connector.ssl;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;

import javax.net.ssl.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class SslConfig implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final String TLSV_1_2 = "TLSv1.2";

    private transient SSLContext sslContext;
    private transient KeyStore trustStore;
    private transient KeyStore keyStore;
    private String keyStorePassword;
    private String pemUTF8;
    private String clientPemUTF8;
    private String clientKeyPemUTF8;

    public SslConfig trustStoreFile(File trustStoreFile) throws SecretException {
        try (InputStream inputStream = new FileInputStream(trustStoreFile)) {
            this.trustStore = this.inputStreamToKeyStore(inputStream, (String)null);
            return this;
        } catch (IOException e) {
            throw new SecretException(e.getMessage());
        }
    }

    public SslConfig pemFile(File pemFile) throws SecretException {
        try (InputStream input = new FileInputStream(pemFile)) {
            this.pemUTF8 = inputStreamToUTF8(input);
            return this;
        } catch (Exception ex) {
            throw new SecretException(ex.getMessage());
        }
    }

    public SslConfig keyStoreFile(File keyStoreFile, String password) throws SecretException {
        try (InputStream inputStream = new FileInputStream(keyStoreFile)) {
            this.keyStore = this.inputStreamToKeyStore(inputStream, password);
            this.keyStorePassword = password;
            return this;
        } catch (IOException e) {
            throw new SecretException(e.getMessage());
        }
    }

    public SslConfig clientPemFile(File clientPemFile) throws SecretException {
        try (InputStream input = new FileInputStream(clientPemFile)) {
            this.clientPemUTF8 = inputStreamToUTF8(input);
            return this;
        } catch (IOException e) {
            throw new SecretException(e.getMessage());
        }
    }

    public SslConfig clientKeyPemFile(File clientKeyPemFile) throws SecretException {
        try (InputStream input = new FileInputStream(clientKeyPemFile)) {
            this.clientKeyPemUTF8 = inputStreamToUTF8(input);
            return this;
        } catch (IOException e) {
            throw new SecretException(e.getMessage());
        }
    }

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

    public SSLContext getSslContext() {
        return this.sslContext;
    }

    private SSLContext buildSslContextFromJks() throws SecretException {
        TrustManager[] trustManagers = null;
        if (this.trustStore != null) {
            try {
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(this.trustStore);
                trustManagers = trustManagerFactory.getTrustManagers();
            } catch (KeyStoreException | NoSuchAlgorithmException var6) {
                throw new SecretException(var6.getMessage());
            }
        }

        KeyManager[] keyManagers = null;
        if (this.keyStore != null) {
            try {
                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(this.keyStore, this.keyStorePassword == null ? null : this.keyStorePassword.toCharArray());
                keyManagers = keyManagerFactory.getKeyManagers();
            } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException var5) {
                throw new SecretException(var5.getMessage());
            }
        }

        try {
            SSLContext lSslContext = SSLContext.getInstance(TLSV_1_2);
            lSslContext.init(keyManagers, trustManagers, (SecureRandom)null);
            return lSslContext;
        } catch (KeyManagementException | NoSuchAlgorithmException var4) {
            throw new SecretException(var4.getMessage());
        }
    }

    private SSLContext buildSslContextFromPem() throws SecretException {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            TrustManager[] trustManagers = null;
            if (this.pemUTF8 != null) {
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

                X509Certificate certificate = buildX509Certificate(certificateFactory, this.pemUTF8.getBytes(StandardCharsets.UTF_8));

                KeyStore iKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                iKeyStore.load((KeyStore.LoadStoreParameter)null);
                iKeyStore.setCertificateEntry("caCert", certificate);
                trustManagerFactory.init(iKeyStore);
                trustManagers = trustManagerFactory.getTrustManagers();
            }

            KeyManager[] keyManagers = null;
            if (this.clientPemUTF8 != null && this.clientKeyPemUTF8 != null) {
                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                X509Certificate clientCertificate = buildX509Certificate(certificateFactory, this.clientPemUTF8.getBytes(StandardCharsets.UTF_8));

                String strippedKey = this.clientKeyPemUTF8.replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "");
                byte[] keyBytes = Base64.getMimeDecoder().decode(strippedKey);
                PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
                KeyFactory var9 = KeyFactory.getInstance("RSA");
                PrivateKey privateKey = var9.generatePrivate(pkcs8EncodedKeySpec);
                KeyStore ikeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                String k = Math.random() + "";
                ikeyStore.load((InputStream)null, k.toCharArray());
                ikeyStore.setCertificateEntry("clientCert", clientCertificate);
                ikeyStore.setKeyEntry("key", privateKey, k.toCharArray(),
                        new Certificate[]{clientCertificate});
                keyManagerFactory.init(ikeyStore, k.toCharArray());
                keyManagers = keyManagerFactory.getKeyManagers();
            }

            SSLContext lSslContext = SSLContext.getInstance(TLSV_1_2);
            lSslContext.init(keyManagers, trustManagers, (SecureRandom)null);
            return lSslContext;
        } catch (IOException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException |
                 UnrecoverableKeyException | InvalidKeySpecException | CertificateException e) {
            throw new SecretException(e.getMessage());
        }
    }

    private X509Certificate buildX509Certificate(CertificateFactory certificateFactory, byte[] pemBytes) throws SecretException {
        try (ByteArrayInputStream pem = new ByteArrayInputStream(pemBytes)) {
            return (X509Certificate)certificateFactory.generateCertificate(pem);
        } catch (IOException | CertificateException e) {
            throw new SecretException(e.getMessage());
        }
    }

    private KeyStore inputStreamToKeyStore(InputStream inputStream, String password) throws SecretException {
        try {
            KeyStore lKeyStore = KeyStore.getInstance("JKS");
            lKeyStore.load(inputStream, password == null ? null : password.toCharArray());
            return lKeyStore;
        } catch (CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException var4) {
            throw new SecretException(var4.getMessage());
        }
    }

    private static String inputStreamToUTF8(InputStream input) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        StringBuilder utf8 = new StringBuilder();

        String str;
        while((str = in.readLine()) != null) {
            utf8.append(str).append(System.lineSeparator());
        }

        in.close();
        return utf8.toString();
    }
}