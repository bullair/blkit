package com.inuker.bluetooth.library.ui;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.inuker.bluetooth.library.BluetoothContext;
import com.inuker.bluetooth.library.ClientManager;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.R;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.utils.ByteUtils;
import com.inuker.bluetooth.library.utils.RxToast;
import com.inuker.bluetooth.library.utils.UUIDUtils;
import com.inuker.bluetooth.library.utils.parse.BuildCall;
import com.permissionx.saltedfish.PermissionX;

import java.util.Arrays;
import java.util.UUID;

@Route(path = ARouterPath.Bluetooth.ADD_DEVICE)
public class AddBlDeviceActivity extends AppCompatActivity {

    private boolean hasBluePerm = false;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setCustomView(R.layout.actionbar_title);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        setContentView(R.layout.activity_add_device);
        TextView title = actionBar.getCustomView().findViewById(R.id.title);
//        TextView mTvCount = findViewById(R.id.counter);
        title.setText(getString(R.string.str_bl_my_device));//连接之后，显示搜索页面的列表形式
        actionBar.getCustomView().findViewById(R.id.back_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        BluetoothContext.set(this);
        String[] perms = null;
        if (Build.VERSION.SDK_INT >= 31) {
            perms = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN};
        } else {
            perms = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION/*, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN*/};
        }

        try {
            PermissionX.INSTANCE.requestPermissions(this, perms, (stringBooleanMap, all) -> {
                if (all) {
                    hasBluePerm = true;
                }
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
            perms = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION/*, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN*/};
            PermissionX.INSTANCE.requestPermissions(this, perms, (stringBooleanMap, all) -> {
                if (all) {
                    hasBluePerm = true;
                }
                return null;
            });
        }

        findViewById(R.id.add_dev).setOnClickListener(v -> {
            if (!isOpenBluetooth()) {
                RxToast.info(getString(R.string.str_bl_your_phone_s_bluetooth_isnot_turned_on_you_cant_connect_to_the_device_please_turn_it_on_manually));
            } else {
                if (hasBluePerm) {
                    startActivity(new Intent(this, ScanBluetoothActivity.class));
                } else {
                    RxToast.info(getString(R.string.str_bl_please_allow_the_relevant_permission_locate_and_bluetooth_to_operate));
                }
            }
        });

        ClientManager.getDetail().observe(this, device -> ClientManager.getClient(getApplicationContext()).notify(device.getMac(), device.getService(), device.getChara(), new BleNotifyResponse() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onNotify(UUID service, UUID character, byte[] value) {
                String resNtf = ByteUtils.byteToString(value);
//                LogUtils.d("recV", "onNotify data = " + String.format("%s", resNtf));
                if (value.length == 11) {
                    int anInt = ByteUtils.bytesToInt(Arrays.copyOfRange(value, value.length - 4, value.length));
                    ClientManager.setCount(anInt);
//                    mTvCount.setText(String.format("当前计数：%d", anInt));
                }
                StringBuilder str = new StringBuilder();
                for (byte v :
                        value) {
                    str.append("_").append(v & 0xff);
                }
//                LogUtils.e("recV", str.toString());
                if (resNtf.length() == 18 && resNtf.startsWith("FE") && resNtf.contains("61000324")) {
                    if (TextUtils.isDigitsOnly(resNtf.substring(16))) {
                        int no = Integer.parseInt(resNtf.substring(16));
                        if (no == 6 || no == 10) no = 0;
                        ClientManager.setCurChNo(no);
                    }
                }
                String myDataSeg = parseNotify(value);
                if (!TextUtils.isEmpty(myDataSeg) && myDataSeg.startsWith("2500")) {
                    String allCounts = myDataSeg.substring(4);
                    int cno = ClientManager.getCurChNo();
                    if ((cno + 1) * 10 <= allCounts.length()) {
                        String cNum = allCounts.substring(cno * 10 + 2, cno * 10 + 10);
//                        mTvCount.setText(String.format("当前计数：%d", Integer.parseInt(cNum, 16)));
                    }
                }
            }

            private StringBuilder resSet = null;
            private int MultiSize = 0;

            private String parseNotify(byte[] value) {
                String ret = ByteUtils.byteToString(value);
                if (value.length > 6) {
                    int bi0 = value[0] & 0xff;
                    int bi3 = value[3] & 0xff;
                    if (bi0 == 254 && bi3 == 97) {
                        MultiSize = ByteUtils.mergeByte2Hex(value[4], value[5]);
                        if (MultiSize > 14) {
                            resSet = new StringBuilder();
                            resSet.append(ret.substring(12));
                        } else {
                            return ret;
                        }
                    } else if (resSet != null) {
                        if (ret.length() + resSet.length() == MultiSize * 2) {
                            String val = resSet.toString();
                            resSet = null;
                            return val + ret;
                        }
                        resSet.append(ret);
                    }
                }
                return ret;
            }

            @Override
            public void onResponse(int code) {
                device.setEnableNotify(true);
                ClientManager.getClient(AddBlDeviceActivity.this).write(device.getMac(), device.getService(), UUIDUtils.makeUUID(0xFFFB), BuildCall.getCh().array(), code1 -> {
                    if (code1 == REQUEST_SUCCESS) {
                        Log.d("recV", "write getCh = " + code1);
                        ClientManager.getClient(AddBlDeviceActivity.this).write(device.getMac(), device.getService(), UUIDUtils.makeUUID(0xFFFB), BuildCall.getCounts().array(), code2 -> {
                            if (code2 == REQUEST_SUCCESS) {
                                Log.d("recV", "write getCounts = " + code2);
                            }
                        });
                    }
                });
            }
        }));
        int status = ClientManager.getClient(this).getConnectStatus(ClientManager.getCurMac());
        if (status == Constants.STATUS_DEVICE_CONNECTED) {
            // 需要请求所有通道(即使单个)
//            mTvCount.setText(String.format("当前计数：%d", ClientManager.getCount()));
        }
    }

    private boolean isOpenBluetooth() {
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager.getAdapter();
        return adapter != null && adapter.isEnabled();
    }
}