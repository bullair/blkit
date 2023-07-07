package com.inuker.bluetooth.library.connect.response;

public class HexUtil {
    public static String bytesToHexString(byte[] bArray) {
        if (bArray == null || bArray.length < 1) return "";
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (byte b : bArray) {
            sTemp = Integer.toHexString(0xFF & b);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }
}
