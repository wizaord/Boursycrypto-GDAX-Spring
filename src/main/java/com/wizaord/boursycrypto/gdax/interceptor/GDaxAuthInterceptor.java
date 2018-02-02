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

import java.io.IOException;
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

    return execution.execute(request, body);
  }

  private String getRequestBody(byte[] body) throws UnsupportedEncodingException {
    if (body != null && body.length > 0) {
      return (new String(body, "UTF-8"));
    } else {
      return null;
    }
  }

}
