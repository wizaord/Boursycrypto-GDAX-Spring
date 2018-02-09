package com.wizaord.boursycrypto.gdax.config.properties;

import lombok.Data;

import java.util.List;

@Data
public class Slack {
  private String tokenId;
  private String personalAccountChannel;
  private List<String> listChannel;
}
