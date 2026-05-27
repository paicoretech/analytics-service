package com.paic.nbm.analyticsservice.PcapGenerator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SerializableController {

    static String keyPass = "0GNFL4AOm7i2Vy9P";
    static byte[] keyBytes = keyPass.getBytes();
    static SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

    static String transformation = "AES/ECB/PKCS5Padding";

    public static String encode(String pcapDownloaderInfo) {
        String result = "";
        try {
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] cipherText = cipher.doFinal(pcapDownloaderInfo.getBytes(StandardCharsets.UTF_8));
            result = Base64.encodeBase64URLSafeString(cipherText);
        } catch (Exception ex) {
            result = "";
            log.error("Error on generate the encode pcap data " + ex.getMessage());
        }

        return result;
    }

    public static List<PcapDownloaderInfo> decode(String  encodePcapDownloaderInfo) {
        Gson gson = new Gson();
        List<PcapDownloaderInfo> resultList = new ArrayList<>();
        try {
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.DECRYPT_MODE, key);
            String decodeStr = URLDecoder.decode(encodePcapDownloaderInfo, StandardCharsets.UTF_8);
            byte[] base64decodedTokenArr = Base64.decodeBase64(decodeStr.getBytes(StandardCharsets.UTF_8));
            byte[] decryptedPassword = cipher.doFinal(base64decodedTokenArr);
            String json = new String(decryptedPassword);
            resultList = gson.fromJson(json, new TypeToken<List<PcapDownloaderInfo>>(){}.getType());

        } catch (Exception ex) {
            log.error("Error on generate the result list " + ex.getMessage());
        }
        return resultList;

    }
}
