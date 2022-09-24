package com.xebialabs.restito.server;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static com.xebialabs.restito.server.StubServer.*;

public class StubServerTls {

    /**
     * Get the key store for server's built-in public key certificate.
     * Check `guide.UsingHttpsTest` to see how to use this in your tests.
     *
     * Please note that this is the *default* certificate shipped with the server
     * and the server can be configured to use a different one at runtime.
     *
     * @return key store object
     */
    public static KeyStore defaultTrustStore() {
        try (InputStream trustStore = StubServer.class.getResourceAsStream("/" + SERVER_CERT_KEYSTORE)) {
            KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
            store.load(trustStore, SERVER_CERT_KEYSTORE_PASS.toCharArray());
            return store;
        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }
}
