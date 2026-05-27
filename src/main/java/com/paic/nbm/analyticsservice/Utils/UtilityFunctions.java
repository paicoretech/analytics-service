package com.paic.nbm.analyticsservice.Utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class UtilityFunctions {

  public static String encodeHex(String string) {
    char[] chars = Hex.encodeHex(string.getBytes(StandardCharsets.UTF_8));
    return String.valueOf(chars);
  }

  public static String decodeHex(String hex) {
    String result = "";
    try {
      byte[] bytes = Hex.decodeHex(hex);
      result = new String(bytes, StandardCharsets.UTF_8);
    } catch (DecoderException e) {
      throw new IllegalArgumentException("Invalid Hex format!");
    }
    return result;
  }

  public static String generateMD5Hash(String clearText) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("MD5");
      messageDigest.update(clearText.getBytes());
      byte[] digest = messageDigest.digest();
      return Hex.encodeHexString(digest);
    } catch (NoSuchAlgorithmException ex) {
      log.warn("generateMD5Hash exception", ex.fillInStackTrace());
    }
    return "";
  }

  public static String decodeHexByAvpType(String hex, String type) {
    String decodedText = "";
    switch (type) {
      case "Float64":
      case "Integer64":
      case "Enumerated":
      case "Integer32":
      case "Unsigned32":
      case "Unsigned32Enumerated":
      case "Unsigned64":
        decodedText = "" + Integer.parseInt(hex, 16);
        break;
      case "#N/A":
      case "Address":
      case "DiameterIdentity":
      case "DiameterURI":
      case "IPAddress":
      case "IPFilterRule":
      case "OctetString":
      case "QOSFilterRule":
      case "Time":
      case "UTF8String":
      case "VendorId":
        decodedText = decodeHex(hex);
        break;
      case "grouped":
        decodedText = "Grouped AVPs";
        break;
    }
    return decodedText;
  }

  public static String getDateFormat(BigInteger timeEpoch, Integer uSecondsEpoch, String timezone){
    DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
    format.setTimeZone(TimeZone.getTimeZone(timezone));
    int milliseconds = (int) Math.round((uSecondsEpoch * 1.0) / 1000);
    Date date = new Date((timeEpoch.longValue() * 1000) + milliseconds);
    return format.format(date);
  }

  public static String validateParam(String param) {
    if (param == null) {
      return "NA";
    }
    return param;
  }

  public static BigInteger parseStringDateToEpoch(String date, String timezone) {
    long epochTime = 0L;
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    format.setTimeZone(TimeZone.getTimeZone(timezone));
    Date currentDate = new Date();
    try {
      currentDate = format.parse(date);
      epochTime = currentDate.getTime() / 1000;
    } catch (Exception ex) {
      log.error("Error on convert date " + date);
    }
    return new BigInteger(epochTime + "");
  }


  public static <T> List<T> cleanList(List<T> listToClean) {
    listToClean.removeAll(Collections.singleton(null));
    Set<T> setToList = new HashSet<>(listToClean);
    listToClean.clear();
    listToClean.addAll(setToList);
    return listToClean;
  }

  public static String getSeparateString(Stream<String> listOfString) {
    return listOfString.map(Objects::toString).collect(Collectors.joining(" "));
  }
}
