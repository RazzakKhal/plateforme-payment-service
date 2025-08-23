package com.bookNDrive.payment_service.infrastructure.encoder;

public interface Encoder {

    byte[] hexStringToByteArray(String s);

    String toHex(byte[] bytes);

}
