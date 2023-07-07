package com.inuker.bluetooth.library.connect.response;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import com.inuker.bluetooth.library.connect.listener.IBluetoothGattResponse;

/**
 * Created by dingjikerbo on 2016/8/25.
 */
public class BluetoothGattResponse extends BluetoothGattCallback {

    private IBluetoothGattResponse response;

    public BluetoothGattResponse(IBluetoothGattResponse response) {
        this.response = response;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        response.onConnectionStateChange(status, newState);
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        response.onServicesDiscovered(status);
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.i("recV", "onCharacteristicRead: 接收数据成功,value =" + HexUtil
                .bytesToHexString(characteristic.getValue()));
        response.onCharacteristicRead(characteristic, status, characteristic.getValue());
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.i("recV", "onCharacteristicWrite");//原数据
        response.onCharacteristicWrite(characteristic, status, characteristic.getValue());
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        //Log.i("recV", "onCharacteristicChanged: 接收数据成功");//回复
        response.onCharacteristicChanged(characteristic, characteristic.getValue());
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        Log.i("recV", "onDescriptorWrite");
        response.onDescriptorWrite(descriptor, status);
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        Log.i("recV", "onDescriptorRead");
        response.onDescriptorRead(descriptor, status, descriptor.getValue());
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        Log.i("recV", "onReadRemoteRssi: 接收数据成功,value =" + rssi);
        response.onReadRemoteRssi(rssi, status);
    }

    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        Log.i("recV", "onMtuChanged: 接收数据成功,value =" + mtu);
        response.onMtuChanged(mtu, status);
    }
}
