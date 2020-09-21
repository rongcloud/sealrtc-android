package cn.rongcloud.rtc.util.http;

import android.text.TextUtils;

import cn.rongcloud.rtc.util.http.callbacks.HttpErrorCode;
import io.rong.common.utils.SSLUtils;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import cn.rongcloud.rtc.utils.FinLog;
import io.rong.common.RLog;

public class HttpClient {
    public int CONNECT_TIME_OUT = 8 * 1000;
    private static final String TAG = "AppHttpClient";

    private HttpClient() {}

    private static class SingletonHolder {

        static HttpClient sDefaultHttpClient = new HttpClient();
    }

    private static class SingletonHolderExecutorService {

        static ExecutorService executorService = Executors.newCachedThreadPool();
    }

    public static HttpClient getDefault() {
        return SingletonHolder.sDefaultHttpClient;
    }

    public void request(final Request request, final ResultCallback callback) {
        executorService()
                .execute(
                        new Runnable() {
                            @Override
                            public void run() {
                                doRequest(
                                        request,
                                        new ResultCallback() {
                                            @Override
                                            public void onResponse(String result) {
                                                if (callback != null) {
                                                    callback.onResponse(result);
                                                }
                                            }

                                            @Override
                                            public void onFailure(int errorCode) {
                                                if (callback != null) {
                                                    callback.onFailure(errorCode);
                                                }
                                            }
                                        });
                            }
                        });
    }

    private void doRequest(Request request, ResultCallback callback) {
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        StringBuilder result = new StringBuilder();
        try {
            String header = request.getRequestHeaders();
            urlConnection = createConnection(request);
            urlConnection.connect();
            int responseCode = urlConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                InputStream is = urlConnection.getErrorStream();
                String errorMsg = "";
                if (is != null) {
                    byte[] bytes = new byte[is.available()];
                    is.read(bytes);
                    errorMsg = new String(bytes, "utf-8");
                    closeStream(is);
                    FinLog.v(TAG, "doRequest Failed :" + errorMsg);
                }
                urlConnection.disconnect();
                callback.onFailure(responseCode);
                return;
            }

            inputStream = urlConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            callback.onResponse(result.toString());
        } catch (SocketTimeoutException timeout) {
            FinLog.v(TAG, "SocketTimeoutException :" + timeout);
            callback.onFailure(HttpErrorCode.HttpTimeoutError.getValue());
        } catch (EOFException e) {
            FinLog.v(TAG, "EOFException :" + e);
            callback.onFailure(HttpErrorCode.HttpResponseError.getValue());
        } catch (Exception e) {
            FinLog.v(TAG, "exception :" + e);
            callback.onFailure(HttpErrorCode.HttpResponseError.getValue());
        } finally {
            closeStream(inputStream);
            closeStream(inputStreamReader);
            closeStream(bufferedReader);
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private static ExecutorService executorService() {
        return SingletonHolderExecutorService.executorService;
    }

    public interface ResultCallback {
        void onResponse(String result);

        void onFailure(int errorCode);
    }

    private void closeStream(Closeable stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e) {
            RLog.e(TAG, "closeStream exception ", e);
        }
    }

    private HttpURLConnection createConnection(Request request) throws IOException {
        URL url;
        HttpURLConnection conn;
        RLog.i(TAG, "request url : " + request.url());
        //url 是IP地址情况下，myHost解析出来的是ip
        final String myHost = getNavHost(request.url());
        //根据ip获取对应的host
        final String host = TextUtils.isEmpty(SnifferManager.getInstance().getOriginHost(myHost)) ? myHost : SnifferManager.getInstance().getOriginHost(myHost);
        if (request.url().toLowerCase().startsWith("https")) {
            url = new URL(request.url());
            if (SSLUtils.getSSLContext() != null) {
                SSLContext sslContext = SSLUtils.getSSLContext();
                HttpsURLConnection c = (HttpsURLConnection) url.openConnection();
                c.setSSLSocketFactory(sslContext.getSocketFactory());
                if (SSLUtils.getHostVerifier() != null) {
                    c.setHostnameVerifier(SSLUtils.getHostVerifier());
                }
                conn = c;
            } else {
                HttpsURLConnection httpsConnection = (HttpsURLConnection) url.openConnection();
                httpsConnection.setSSLSocketFactory(new TlsSniSocketFactory(httpsConnection));
                httpsConnection.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return HttpsURLConnection.getDefaultHostnameVerifier().verify(host, session);
                    }
                });
                conn = httpsConnection;
            }
        } else {
            url = new URL(request.url());
            conn = (HttpURLConnection) url.openConnection();
        }
        conn.setRequestMethod(request.method());
        conn.setConnectTimeout(CONNECT_TIME_OUT);
        conn.setReadTimeout(CONNECT_TIME_OUT);
        conn.setUseCaches(false);

        if (!TextUtils.isEmpty(host)) {
            conn.setRequestProperty("Host", host);
        }

        conn.setRequestProperty("Accept", "application/json;charset=UTF-8");
        conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        conn.setRequestProperty("Connection", "Keep-Alive");
        if (request.getHeaders() != null && request.getHeaders().getHeaders() != null) {
            Set<String> setKey = request.getHeaders().getHeaders().keySet();
            Iterator<String> iterator = setKey.iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                String value = request.getHeaders().getHeaders().get(key);
                conn.setRequestProperty(key, value);
            }
        }

        conn.setDoInput(true);
        if (TextUtils.equals(request.method(), RequestMethod.POST)) {
            conn.setDoOutput(true);
            String body = request.body();
            if (body == null) {
                throw new NullPointerException("Request.body == null");
            }
            OutputStream outputStream = conn.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);
            printWriter.write(request.body());
            printWriter.flush();
        }
        return conn;
    }

    private static String getNavHost(String navi) {
        try {
            URL url = new URL(navi);
            String host = url.getHost();
            int port = url.getPort();
            if (port != -1 && (url.getDefaultPort() != url.getPort())) {
                host = host + ":" + port;
            }
            return host;
        } catch (MalformedURLException e) {
            RLog.e(TAG, "MalformedURLException ", e);
        }
        return null;
    }
}
