package com.wizaord.boursycrypto.gdax.interceptor;

import com.wizaord.boursycrypto.gdax.domain.auth.SignatureHeader;
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

    traceRequest(request, body);
    ClientHttpResponse response = execution.execute(request, body);
    traceResponse(response);
    return response;
  }

  private String getRequestBody(byte[] body) throws UnsupportedEncodingException {
    if (body != null && body.length > 0) {
      return (new String(body, "UTF-8"));
    } else {
      return null;
    }
  }

  private void traceRequest(HttpRequest request, byte[] body) throws IOException {
    LOG.debug("===========================request begin================================================");
    LOG.debug("URI         : {}", request.getURI());
    LOG.debug("Method      : {}", request.getMethod());
    LOG.debug("Headers     : {}", request.getHeaders() );
    LOG.debug("Request body: {}", new String(body, "UTF-8"));
    LOG.debug("==========================request end================================================");
  }

  private void traceResponse(ClientHttpResponse response) throws IOException {
    StringBuilder inputStringBuilder = new StringBuilder();
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(), "UTF-8"));
    String line = bufferedReader.readLine();
    while (line != null) {
      inputStringBuilder.append(line);
      inputStringBuilder.append('\n');
      line = bufferedReader.readLine();
    }
    LOG.debug("============================response begin==========================================");
    LOG.debug("Status code  : {}", response.getStatusCode());
    LOG.debug("Status text  : {}", response.getStatusText());
    LOG.debug("Headers      : {}", response.getHeaders());
    LOG.debug("Response body: {}", inputStringBuilder.toString());
    LOG.debug("=======================response end=================================================");
  }

}
