package com.inuker.bluetooth.library.utils.parse;

import com.inuker.bluetooth.library.ClientManager;
import com.inuker.bluetooth.library.utils.ByteUtils;
import com.inuker.bluetooth.library.utils.CRC8Utils;

import java.nio.ByteBuffer;

public class BuildCall {

    public static ByteBuffer getCh() {
        ByteBuffer byteBuffer = allocate(6);
        byteBuffer.put(3, (byte) 0x24);
        byte[] fill = {};
        dataSeg(byteBuffer, fill);
        return byteBuffer;
    }

    public static ByteBuffer getCounts() {
        ByteBuffer byteBuffer = allocate(6);
        byteBuffer.put(3, (byte) 0x25);
        byte[] fill = {};
        dataSeg(byteBuffer, fill);
        return byteBuffer;
    }

    public static ByteBuffer switchCh(int chNo) {
        ByteBuffer byteBuffer = allocate(7);
        byteBuffer.put(3, (byte) 0x05);
        byte[] fill = {(byte) (chNo & 0xff)};
        byteBuffer.put(5, (byte) fill.length);
        dataSeg(byteBuffer, fill);
        return byteBuffer;
    }

    public static ByteBuffer setScript(int chNo, int textNo) {
        ByteBuffer byteBuffer = allocate(9);
        byteBuffer.put(3, (byte) 0x03);
        byte[] text = ByteUtils.intToByteArray(textNo);
        byte[] fill = {(byte) (chNo & 0xff), text[0], text[1]};
        byteBuffer.put(5, (byte) fill.length);
        dataSeg(byteBuffer, fill);
        return byteBuffer;
    }

    public static ByteBuffer clearChNum(int chNo) {
        ByteBuffer byteBuffer = allocate(7);
        byteBuffer.put(3, (byte) 0x06);
        byte[] fill = {(byte) (chNo & 0xff)};
        byteBuffer.put(5, (byte) fill.length);
        dataSeg(byteBuffer, fill);
        return byteBuffer;
    }

    public static ByteBuffer enableClear(int yes) {
        ByteBuffer byteBuffer = allocate(7);
        byteBuffer.put(3, (byte) 0x06);
        byte[] fill = {(byte) (yes & 0xff)};
        byteBuffer.put(5, (byte) fill.length);
        dataSeg(byteBuffer, fill);
        return byteBuffer;
    }

    public static ByteBuffer autoUp() {
        ByteBuffer byteBuffer = allocate(7);
        byteBuffer.put(3, (byte) 0x04);
        byte[] fill = {0x01};
        byteBuffer.put(5, (byte) fill.length);
        dataSeg(byteBuffer, fill);
        return byteBuffer;
    }

    public static ByteBuffer allocate(int len) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(len);
        byteBuffer.put((byte) 0xfe);
        byteBuffer.put((byte) 0x00);
        byteBuffer.put((byte) ClientManager.seqIndex++);
        byteBuffer.put((byte) 0x00);
        byteBuffer.put((byte) 0x00);
        byteBuffer.put((byte) 0x00);
        return byteBuffer;
    }

    public static void dataSeg(ByteBuffer byteBuffer, byte[] fill) {
        for (byte b : fill) {
            byteBuffer.put(b);
        }
        byteBuffer.put(1, (byte) CRC8Utils.CRC8(byteBuffer.array(), 2, byteBuffer.capacity() - 2));
    }
}
