package com.wizaord.boursycrypto.gdax.interceptor;

import com.wizaord.boursycrypto.gdax.domain.SignatureHeader;
import com.wizaord.boursycrypto.gdax.service.SignatureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static com.wizaord.boursycrypto.gdax.config.RestConfiguration.GDAX_URI;

@Component
public class GDaxAuthInterceptor implements ClientHttpRequestInterceptor {

  private static final Logger LOG = LoggerFactory.getLogger(GDaxAuthInterceptor.class);

  @Autowired
  private SignatureService signatureService;

  @Override
  public ClientHttpResponse intercept(
          final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException {
    final String uri = request.getURI().toString().replace(GDAX_URI, "");
    final String methodType = request.getMethod().name();
    final String contentAsString = getRequestBody(body);

    LOG.debug("request URI : " + uri);
    LOG.debug("request method : " + methodType);
    LOG.debug("request body : " + contentAsString);

    final SignatureHeader signature = signatureService.getSignature(uri, methodType, contentAsString);

    HttpHeaders headers = request.getHeaders();
    headers.add("CB-ACCESS-KEY", signature.getCbAccessKey());
    headers.add("CB-ACCESS-SIGN", signature.getCbAccessSign());
    headers.add("CB-ACCESS-TIMESTAMP", signature.getCbAccessTimestamp());
    headers.add("CB-ACCESS-PASSPHRASE", signature.getCbAccessPassphrase());

    ClientHttpResponse clientHttpResponse = execution.execute(request, body);
    traceResponse(clientHttpResponse);

    return clientHttpResponse;
  }

  private String getRequestBody(byte[] body) throws UnsupportedEncodingException {
    if (body != null && body.length > 0) {
      return (new String(body, "UTF-8"));
    } else {
      return null;
    }
  }

  private void traceResponse(ClientHttpResponse response) throws IOException {
    String body = getBodyString(response);
    LOG.debug("response status code: " + response.getStatusCode());
    LOG.debug("response status text: " + response.getStatusText());
    LOG.debug("response body : " + body);
  }

  private String getBodyString(ClientHttpResponse response) {
    try {
      if (response != null && response.getBody() != null) {// &&
        // isReadableResponse(response))
        // {
        StringBuilder inputStringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8));
        String line = bufferedReader.readLine();
        while (line != null) {
          inputStringBuilder.append(line);
          inputStringBuilder.append('\n');
          line = bufferedReader.readLine();
        }
        return inputStringBuilder.toString();
      } else {
        return null;
      }
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
      return null;
    }
  }
}
