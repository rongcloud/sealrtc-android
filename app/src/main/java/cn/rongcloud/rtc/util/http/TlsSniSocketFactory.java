package cn.rongcloud.rtc.util.http;

import android.net.SSLCertificateSocketFactory;
import android.util.Log;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

class TlsSniSocketFactory extends SSLSocketFactory {
    private static final String TAG = "AppTlsSniSocketFactory";
    HostnameVerifier hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
    private HttpsURLConnection conn;
    public TlsSniSocketFactory(HttpsURLConnection conn) {
        this.conn = conn;
    }
    @Override
    public Socket createSocket() throws IOException {
        return null;
    }
    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return null;
    }
    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return null;
    }
    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return null;
    }
    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return null;
    }
    // TLS layer
    @Override
    public String[] getDefaultCipherSuites() {
        return new String[0];
    }
    @Override
    public String[] getSupportedCipherSuites() {
        return new String[0];
    }
    @Override
    public Socket createSocket(Socket plainSocket, String host, int port, boolean autoClose) throws IOException {
        String peerHost = this.conn.getRequestProperty("Host");
        if (peerHost == null)
            peerHost = host;
        InetAddress address = plainSocket.getInetAddress();
        if (autoClose) {
            // we don't need the plainSocket
            plainSocket.close();
        }
        // create and connect SSL socket, but don't do hostname/certificate verification yet
        SSLCertificateSocketFactory sslSocketFactory =
            (SSLCertificateSocketFactory) SSLCertificateSocketFactory.getDefault(0);
        SSLSocket ssl = (SSLSocket) sslSocketFactory.createSocket(address, port);
        // enable TLSv1.1/1.2 if available
        ssl.setEnabledProtocols(ssl.getSupportedProtocols());
        // set up SNI before the handshake
        sslSocketFactory.setHostname(ssl, peerHost);

        // verify hostname and certificate
        SSLSession session = ssl.getSession();

        if (!hostnameVerifier.verify(peerHost, session)) {
            try {
                X509Certificate certificate = (X509Certificate) session.getPeerCertificates()[0];
                List<String> subjectAltNames = getSubjectAltNames(certificate, 2);
                for (String hostName : subjectAltNames) {
                    Log.e(TAG, "accepted host: " + hostName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            throw new SSLPeerUnverifiedException("Cannot verify hostname: " + peerHost);
        }
        return ssl;
    }

    private static List<String> getSubjectAltNames(X509Certificate certificate, int type) {
        List<String> result = new ArrayList<>();
        try {
            Collection<?> subjectAltNames = certificate.getSubjectAlternativeNames();
            if (subjectAltNames == null) {
                return Collections.emptyList();
            }
            for (Object subjectAltName : subjectAltNames) {
                List<?> entry = (List<?>) subjectAltName;
                if (entry == null || entry.size() < 2) {
                    continue;
                }
                Integer altNameType = (Integer) entry.get(0);
                if (altNameType == null) {
                    continue;
                }
                if (altNameType == type) {
                    String altName = (String) entry.get(1);
                    if (altName != null) {
                        result.add(altName);
                    }
                }
            }
            return result;
        } catch (CertificateParsingException e) {
            return Collections.emptyList();
        }
    }
}