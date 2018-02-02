package com.wizaord.boursycrypto.gdax.utils;

public class MathUtils {
  public static double calculateRemovePourcent(final double initPrice, final double pourcent) {
    return initPrice - ((initPrice * pourcent) / 100);
  }

  public static double calculateAddPourcent(final double initPrice, final double pourcent) {
    return initPrice + ((initPrice * pourcent) / 100);
  }

  public static double calculatePourcentDifference(final double referenceValue, final double newValue) {
    return ((referenceValue * 100) / newValue) - 100;
  }
}
