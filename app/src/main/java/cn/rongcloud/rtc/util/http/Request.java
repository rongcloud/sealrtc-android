package cn.rongcloud.rtc.util.http;

import android.text.TextUtils;

public class Request {

    private String url;
    private String method;
    private Headers headers;
    private String requestBody;
    private Response response;
    private boolean valid = true;

    public String url() {
        return url;
    }

    public String method() {
        return method;
    }

    public Response response() {
        return response;
    }

    public String body() {
        return requestBody;
    }

    public boolean isValid() {
        return valid;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public Headers getHeaders() {
        return headers;
    }

    public String getRequestHeaders() {
        String sessionId = "Client-Session-Id";
        String requestId = "Request-Id";
        String session = headers.get(sessionId);
        String id = headers.get(requestId);
        if (!TextUtils.isEmpty(session)) {
            StringBuilder builder = new StringBuilder(sessionId).append("=").append(session)
                .append(",")
                .append(requestId).append("=").append(id);
            return builder.toString();
        }
        return "";
    }

    public static class Builder {
        private String url;
        private @RequestMethod String method;
        private Headers headers;
        private String requestBody;
        private Response response;

        public Builder() {
            this.method = RequestMethod.GET;
            headers = new Headers();
        }

        public Builder url(String url) {
            if (url == null) {
                throw new NullPointerException("url == null");
            }
            this.url = url;
            return this;
        }

        public Builder method(@RequestMethod String method) {
            this.method = method;
            return this;
        }

        public Builder body(String body) {
            this.requestBody = body;
            return this;
        }

        public Builder header(String key, String value) {
            headers.put(key, value);
            return this;
        }

        public Builder addHeader(String key, String value) {
            headers.add(key, value);
            return this;
        }

        public Request build() {
            Request request = new Request();
            request.url = url;
            request.method = method;
            request.headers = headers;
            request.requestBody = requestBody;
            request.response = response;
            return request;
        }
    }
}
