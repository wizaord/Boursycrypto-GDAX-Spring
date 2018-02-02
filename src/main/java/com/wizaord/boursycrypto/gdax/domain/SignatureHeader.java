package com.wizaord.boursycrypto.gdax.domain;

import lombok.Value;

@Value
public class SignatureHeader {

  private String cbAccessKey;
  private String cbAccessSign;
  private String cbAccessTimestamp;
  private String cbAccessPassphrase;

}
