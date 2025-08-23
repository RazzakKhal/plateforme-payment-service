package com.bookNDrive.payment_service.infrastructure.encoder;

import org.springframework.stereotype.Component;

@Component
public class HexEncoder implements Encoder{

    public byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));  // %02X = deux chiffres hex, majuscules
        }
        return sb.toString();
    }

}
