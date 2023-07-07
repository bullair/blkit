package com.inuker.bluetooth.library.ui;


import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.inuker.bluetooth.library.ClientManager;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.R;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;
import com.inuker.bluetooth.library.model.DetailItem;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.inuker.bluetooth.library.utils.BluetoothLog;
import com.inuker.bluetooth.library.utils.ByteUtils;
import com.inuker.bluetooth.library.utils.SPUtil;
import com.inuker.bluetooth.library.utils.UUIDUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnConfirmListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ScanBluetoothActivity extends AppCompatActivity {

    private static final String TAG = ScanBluetoothActivity.class.getSimpleName();
    private ArrayList<DetailItem> mList = new ArrayList<>();
    private BlueAdapter mAdapter;
    private ImageView scanningIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setContentView(R.layout.activity_scan_bluetooth);

        initView();
    }

    public void initView() {
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setCustomView(R.layout.actionbar_title);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        findViewById(R.id.refresh_iv).setVisibility(View.INVISIBLE);
        findViewById(R.id.back_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.search_bl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchDevice();
            }
        });
        findViewById(R.id.rescan_dev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchDevice();
            }
        });
        scanningIv = findViewById(R.id.scanning_iv);
        Glide.with(this).asGif().load(R.drawable.scanning).into(scanningIv);
        RecyclerView listRv = findViewById(R.id.bluetooth_list);
        listRv.setLayoutManager(new LinearLayoutManager(this));
        listRv.setHasFixedSize(true);
        mAdapter = new BlueAdapter(mList);
        mAdapter.addChildClickViewIds(R.id.unbind, R.id.connect_dev);
        mAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                List<DetailItem> adapterData = adapter.getData();
                if (adapterData.isEmpty()) return;
                DetailItem detailItem = (DetailItem) adapterData.get(position);
                int id = view.getId();
                if (id == R.id.unbind) {
                    adapterData.remove(detailItem);
                    adapterData.remove(0);
//                if (getData().size() < 1 || (getData().size() > 1 && getData().get(1).getItemType() == DetailItem.TYPE_DEV)) {
//                    DetailItem e = new DetailItem("", "", 0, null, null);
//                    e.setHn("搜索到的设备");
//                    e.setViewType(DetailItem.TYPE_HEADER);
//                    getData().add(e);
//                }
                    if (adapterData.size() == 1) adapterData.clear();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(ScanBluetoothActivity.this, "您已成功解绑设备", Toast.LENGTH_SHORT).show();
                    ClientManager.getClient(view.getContext()).disconnect(detailItem.getMac());
                    SharedPreferences preferences = view.getContext().getSharedPreferences("ble_profile", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("hasLastDev", false);
                    editor.apply();
                } else if (id == R.id.connect_dev) {
                    TextView cnnTv = (TextView) view;
                    AProgressDialog progressDialog = createProgressDialog();
                    Log.e("xxx190", "convert: user click cnn");
                    if (detailItem.getItemType() == DetailItem.TYPE_BIND_DEV) {
                        if (adapterData.size() > 1) {
                            if ("我的设备".equals(adapterData.get(0).getHn())) {
                                if (detailItem.isConnected()) {
                                    cnnTv.setText("连接");
                                    detailItem.setConnected(false);
                                    ClientManager.getClient(view.getContext()).disconnect(detailItem.getMac());
                                    progressDialog.dismiss();
                                    SPUtil.setObject(cnnTv.getContext(), "cnn_bluetooth", detailItem);
                                    return;
                                }
                            }
                        }
                    }
                    BleConnectOptions options = new BleConnectOptions.Builder()
                            .setConnectRetry(3)
                            .setConnectTimeout(20000)
                            .setServiceDiscoverRetry(3)
                            .setServiceDiscoverTimeout(10000)
                            .build();
                    ClientManager.getClient(view.getContext()).connect(detailItem.getMac(), options, new BleConnectResponse() {
                        @Override
                        public void onResponse(int code, BleGattProfile profile) {
                            BluetoothLog.v(String.format("profile:\n%s", profile));
                            if (code == REQUEST_SUCCESS) {
                                ClientManager.setDetail(detailItem);
                                if (detailItem.getItemType() == DetailItem.TYPE_BIND_DEV) {
                                    cnnTv.setText("已连接");
                                }
                                progressDialog.dismiss();
                                detailItem.setConnected(true);
                                detailItem.setItemType(DetailItem.TYPE_BIND_DEV);
                                cnnTv.setSelected(true);

                                new XPopup.Builder(ScanBluetoothActivity.this)
                                        .dismissOnTouchOutside(false)
                                        .asConfirm("", "您已成功连接设备，是否跳转到首页进行计数", new OnConfirmListener() {
                                            @Override
                                            public void onConfirm() {
                                                openActivity(ARouterPath.URL_MAIN_AC, postcard -> {
                                                    postcard.withString("key set fragment", "CourseFragment");
                                                    return null;
                                                });
                                            }
                                        }).show();

                                SharedPreferences preferences = getSharedPreferences("ble_profile", MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putBoolean("hasLastDev", true);
                                editor.apply();

                                if (adapterData.size() > 4 && detailItem.getItemType() == DetailItem.TYPE_DEV) {
                                    adapterData.get(1).setConnected(false);
                                    adapterData.get(1).setItemType(DetailItem.TYPE_DEV);
                                    Collections.swap(adapterData, 1, position);
                                    adapter.notifyDataSetChanged();
                                } else if (adapterData.size() > 0 && "搜索到的设备".equals(adapterData.get(0).getHn())) {
                                    DetailItem e = new DetailItem("", "", -1, null, null);
                                    e.setHn("我的设备");
                                    e.setItemType(DetailItem.TYPE_HEADER);
                                    adapterData.add(0, e);

                                    adapterData.remove(detailItem);
                                    adapterData.add(1, detailItem);
                                    adapter.notifyDataSetChanged();
                                }
                                tagProfile(detailItem, profile);

                                SPUtil.setObject(cnnTv.getContext(), "cnn_bluetooth", detailItem);

                                //registerState(detailItem.getMac());
                            }
                        }

                        private void tagProfile(DetailItem item, BleGattProfile profile) {
                            List<BleGattService> services = profile.getServices();
                            for (BleGattService service : services) {
                                if (service.getUUID().toString().toUpperCase().startsWith("0000FFFA")) {
                                    item.setItgCounter(true);
                                    break;
                                }
                            }
                        }
                    });
                }
            }
        });

        listRv.setAdapter(mAdapter);

        SharedPreferences preferences = getSharedPreferences("ble_profile", MODE_PRIVATE);
        boolean hasLastDev = preferences.getBoolean("hasLastDev", false);
        if (hasLastDev) {
            DetailItem e1 = SPUtil.getObject(this, "cnn_bluetooth");
            if (e1 != null) {
                DetailItem e = new DetailItem("", "", -1, null, null);
                e.setHn("我的设备");
                e.setItemType(DetailItem.TYPE_HEADER);
                mList.add(e);

                e1.setItemType(DetailItem.TYPE_BIND_DEV);
                mList.add(e1);
                mAdapter.notifyDataSetChanged();
                // Constants.STATUS_UNKNOWN
                // Constants.STATUS_DEVICE_CONNECTED
                // Constants.STATUS_DEVICE_CONNECTING
                // Constants.STATUS_DEVICE_DISCONNECTING
                // Constants.STATUS_DEVICE_DISCONNECTED
                int status = ClientManager.getClient(this).getConnectStatus(e1.getMac());
                e1.setConnected(status == Constants.STATUS_DEVICE_CONNECTED);
                if (!e1.isConnected()) {
                    ClientManager.getClient(this).disconnect(e1.getMac());
                }
            } else searchDevice();
        } else searchDevice();
    }

    private void openActivity(String path, Function1<Postcard, Unit> build) {
        Postcard postcard = ARouter.getInstance().build(path);
        build.invoke(postcard);
        postcard.navigation();
    }

    private void registerState(String mac) {
        ClientManager.getClient(this).registerConnectStatusListener(mac, new BleConnectStatusListener() {
            @Override
            public void onConnectStatusChanged(String mac, int status) {

            }
        });
    }

    protected AProgressDialog createProgressDialog() {
        try {
            AProgressDialog pd = new AProgressDialog(this, getString(R.string.connecting));
            pd.setCanceledOnTouchOutside(false);
            pd.show();
            return pd;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void stopScanUi() {
        View sc = findViewById(R.id.scanning_container);
        if (sc.getVisibility() == View.GONE) {
            return;
        }
        sc.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (scanningIv.getDrawable() instanceof GifDrawable) {
                    GifDrawable drawable = (GifDrawable) scanningIv.getDrawable();
                    if (drawable.isRunning()) {
                        //drawable.stop();
                    } else {
                        //drawable.start();
                    }
                }
                sc.setVisibility(View.GONE);
                findViewById(R.id.no_found_dev).setVisibility(View.GONE);
            }
        }, 1500);
    }

    public int conv2Qua(int val) {
        if (val < 0) {
            /* Assume dBm already; rough conversion: best = -40, worst = -100 */
            val = Math.abs(clamp(val, -100, -40) + 40);  /* normalize to 0 */
            val = 100 - (int) ((100.0 * (double) val) / 60.0);
        } else if (val > 110 && val < 256) {
            /* assume old-style WEXT 8-bit unsigned signal level */
            val -= 256;  /* subtract 256 to convert to dBm */
            val = Math.abs(clamp(val, -100, -40) + 40);  /* normalize to 0 */
            val = 100 - (int) ((100.0 * (double) val) / 60.0);
        } else {
            val = clamp(val, 0, 100);
        }

        return Math.max(val, 0);
    }

    private int clamp(int x, int min, int max) {
        if (x > max)
            return max;
        return Math.max(x, min);
    }

    public static String conv2Id(String value) {
        if (TextUtils.isEmpty(value)) return "";
        StringBuilder sbu = new StringBuilder();
        char[] chars = value.toCharArray();
        for (char aChar : chars) {
            sbu.append((int) aChar);
        }
        if (sbu.length() >= 6) {
            return sbu.reverse().substring(0, 6);
        }
        return sbu.toString();
    }

    private static class MyHandler extends Handler {
        public static final int HIDE_SCAN = 11;

        public MyHandler(@NonNull Looper looper) {
            super(looper);
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler msgHandler = new MyHandler(Looper.getMainLooper()) {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == HIDE_SCAN) {
                if (mAdapter.getItemCount() < 2)
                    findViewById(R.id.no_found_dev).setVisibility(View.VISIBLE);
                else findViewById(R.id.no_found_dev).setVisibility(View.GONE);
                findViewById(R.id.scanning_container).setVisibility(View.GONE);
            }
        }
    };

    private void searchDevice() {
        findViewById(R.id.scanning_container).setVisibility(View.VISIBLE);

        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(3000, 3)   // 先扫BLE设备3次，每次3s
                .searchBluetoothClassicDevice(5000) // 再扫经典蓝牙5s
                .searchBluetoothLeDevice(2000)      // 再扫BLE设备2s
                .build();

        ClientManager.getClient(this).search(request, new SearchResponse() {
            private TreeSet<DetailItem> rets = new TreeSet<>((o1, o2) -> o1.getDevId().compareTo(o2.getDevId()));

            @Override
            public void onSearchStarted() {
                msgHandler.removeMessages(MyHandler.HIDE_SCAN);
                findViewById(R.id.no_found_dev).setVisibility(View.GONE);
                findViewById(R.id.scanning_container).setVisibility(View.VISIBLE);
                if (rets.isEmpty()) msgHandler.sendEmptyMessageDelayed(MyHandler.HIDE_SCAN, 20000);
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDeviceFounded(SearchResult device) {
                byte[] scanRecord = device.getScanRecord();
                if (scanRecord == null || scanRecord.length < 1) return;
                String bytes = ByteUtils.byteToString(device.getScanRecord());
//                Log.d("recV", "onNotify data = " + String.format("%s", bytes));
                if (!bytes.startsWith("0201") || !bytes.contains("0BFF00") || bytes.length() < 43) {
                    return;
                }
                //Log.d(TAG, "onDeviceFounded: record = " + bytes);//DU_CT01
                //String devId = "" + device.getAddress().hashCode();
                int did = ByteUtils.bytesToInt(ByteUtils.get(scanRecord, 9, 4));
                DetailItem item = new DetailItem(device.getAddress(), "" + did, conv2Qua(device.rssi), UUIDUtils.makeUUID(0xFFFC), UUIDUtils.makeUUID(0xFFFA));
                item.setItemType(DetailItem.TYPE_DEV);
                rets.add(item);

                mList.clear();
                if (!rets.isEmpty()) {
                    DetailItem e = new DetailItem("", "", 0, null, null);
                    e.setHn(getString(R.string.str_bl_the_device_that_was_discovered));
                    e.setItemType(DetailItem.TYPE_HEADER);
                    mList.add(e);
                    mList.addAll(rets);
                    mAdapter.notifyDataSetChanged();
                }
                msgHandler.removeMessages(11);
                stopScanUi();
                if (rets.size() > 19) {
                    ClientManager.getClient(ScanBluetoothActivity.this).stopSearch();
                }
            }

            @Override
            public void onSearchStopped() {
            }

            @Override
            public void onSearchCanceled() {
            }
        });
    }
}