package com.inuker.bluetooth.library.model;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.io.Serializable;
import java.util.UUID;

public class DetailItem implements Serializable, MultiItemEntity {

    public static final int TYPE_HEADER = 1;
    public static final int TYPE_BIND_DEV = 2;
    public static final int TYPE_DEV = 3;
    private int itemType;//viewType
    private String hn;//分组名

    private boolean isItgCounter = false;
    private boolean isConnected = false;
    private boolean enableNotify = false;
    private int curNo = 1;
    private int curCount;

    private String mac;

    private String devId;

    private int rssi;

    private UUID chara;

    private UUID service;

    public DetailItem(String mac, String devId, int rssi, UUID chara, UUID service) {
        this.mac = mac;
        this.devId = devId;
        this.rssi = rssi;
        this.chara = chara;
        this.service = service;
    }

    public boolean isItgCounter() {
        return isItgCounter;
    }

    public void setItgCounter(boolean itgCounter) {
        isItgCounter = itgCounter;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public boolean isEnableNotify() {
        return enableNotify;
    }

    public void setEnableNotify(boolean enableNotify) {
        this.enableNotify = enableNotify;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getCurNo() {
        return curNo;
    }

    public void setCurNo(int curNo) {
        this.curNo = curNo;
    }

    public int getCurCount() {
        return curCount;
    }

    public void setCurCount(int curCount) {
        this.curCount = curCount;
    }

    public UUID getChara() {
        return chara;
    }

    public void setChara(UUID chara) {
        this.chara = chara;
    }

    public UUID getService() {
        return service;
    }

    public void setService(UUID service) {
        this.service = service;
    }

    @Override
    public int getItemType() {
        return itemType;
    }

    public void setItemType(int viewType) {
        this.itemType = viewType;
    }

    public String getHn() {
        return hn;
    }

    public void setHn(String hn) {
        this.hn = hn;
    }
}