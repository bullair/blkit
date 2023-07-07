package com.inuker.bluetooth.library.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.blankj.utilcode.util.SizeUtils;
import com.inuker.bluetooth.library.BluetoothContext;
import com.inuker.bluetooth.library.ClientManager;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.R;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;
import com.inuker.bluetooth.library.model.DetailItem;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.inuker.bluetooth.library.utils.ByteUtils;
import com.inuker.bluetooth.library.utils.RxToast;
import com.inuker.bluetooth.library.utils.SPUtil;
import com.inuker.bluetooth.library.utils.UUIDUtils;
import com.jeremyliao.liveeventbus.LiveEventBus;
import com.permissionx.saltedfish.PermissionX;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/** ui - 我的设备 */
@Route(path = ARouterPath.Bluetooth.My_DEVICES)
public class MyDevicesActivity extends AppCompatActivity {
    private ImageView refreshIv = null;
    //我的设备
    private final ArrayList<DetailItem> myDevs = new ArrayList<>();
    private final MyDevAdapter myDevAdapter = new MyDevAdapter(R.layout.my_dev_item, myDevs);

    //其它设备
    private final ArrayList<DetailItem> otherDevs = new ArrayList<>();
    private final ArrayList<DetailItem> history = new ArrayList<>();
    private final MyOthAdapter otherAdapter = new MyOthAdapter(otherDevs);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setContentView(R.layout.activity_my_devices);
        findViewById(R.id.back_iv).setOnClickListener(v -> finish());
        findViewById(R.id.manage_devs_rl).setOnClickListener(v -> startActivity(new Intent(MyDevicesActivity.this, ManageDevicesActivity.class)));

        //我的设备
        RecyclerView myDevRv = findViewById(R.id.my_devices_rv);
        myDevRv.setLayoutManager(new LinearLayoutManager(this));
        myDevRv.setHasFixedSize(true);
        myDevRv.setAdapter(myDevAdapter);
        myDevRv.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(
                        0,
                        0,
                        view.getWidth() - view.getPaddingLeft(),
                        view.getHeight() - view.getPaddingTop(),
                        SizeUtils.dp2px(4)
                );
            }
        });
        myDevRv.setClipToOutline(true);
        View header = LayoutInflater.from(this).inflate(R.layout.my_dev_item_header, null);
        myDevAdapter.addHeaderView(header);
        myDevAdapter.setNewInstance(myDevs);
        recoverLastDev();

        //其它设备
        RecyclerView otherDevRv = findViewById(R.id.other_devices_rv);
        otherDevRv.setLayoutManager(new LinearLayoutManager(this));
        otherDevRv.setHasFixedSize(true);
        otherDevRv.setAdapter(otherAdapter);
        otherDevRv.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(
                        0,
                        0,
                        view.getWidth() - view.getPaddingLeft(),
                        view.getHeight() - view.getPaddingTop(),
                        SizeUtils.dp2px(4)
                );
            }
        });
        otherDevRv.setClipToOutline(true);
        DetailItem item = new DetailItem("", "", 0, null, null);
        item.setItemType(1);
        otherDevs.add(item);
        otherAdapter.setNewInstance(otherDevs);
        setClickListener();

        BluetoothContext.set(this);
        String[] perms = null;

        if (Build.VERSION.SDK_INT >= 31) {
            perms = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN};
        } else {
            perms = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
        }

        try {
            PermissionX.INSTANCE.requestPermissions(this, perms, (stringBooleanMap, all) -> {
                if (all) {
                    startScan();
                } else {
                    RxToast.info(getString(R.string.str_bl_please_allow_the_relevant_permission_locate_and_bluetooth_to_operate));
                }
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
            PermissionX.INSTANCE.requestPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION, all -> {
                if (all) {
                    startScan();
                } else {
                    RxToast.info(getString(R.string.str_bl_please_allow_the_relevant_permission_locate_and_bluetooth_to_operate));
                }
                return null;
            });
        }

        if (!isOpenBluetooth()) {
            RxToast.info(getString(R.string.str_bl_your_phone_s_bluetooth_isnot_turned_on_you_cant_find_or_connect_devices_please_turn_it_on_manually));
        }

        LiveEventBus
                .get("remove_bound_dev", String.class)
                .observe(this, s -> {
                    if (!TextUtils.isEmpty(s)) {
                        for (DetailItem e :
                                myDevs) {
                            if (s.equals(e.getDevId())) {
                                history.remove(e);
                                myDevs.remove(e);
                                myDevAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }

    private void startRefresh() {
        Animation mAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_img);
        LinearInterpolator lin = new LinearInterpolator();
        mAnimation.setInterpolator(lin);
        if (refreshIv != null) {
            if (refreshIv.getAnimation() != null && refreshIv.getAnimation().hasEnded()) {
                refreshIv.startAnimation(mAnimation);
            } else {
                refreshIv.startAnimation(mAnimation);
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void recoverLastDev() {
        SharedPreferences preferences = getSharedPreferences("ble_profile", MODE_PRIVATE);
        boolean hasLastDev = preferences.getBoolean("hasLastDev", false);
        if (hasLastDev) {
            ArrayList<DetailItem> its = SPUtil.getObject(this, "bound_bluetooth");
            if (its != null) {
                history.clear();
                history.addAll(its);
                myDevs.addAll(its);
                myDevAdapter.notifyDataSetChanged();

                for (DetailItem it :
                        its) {
                    int status = ClientManager.getClient(this).getConnectStatus(it.getMac());
                    it.setConnected(status == Constants.STATUS_DEVICE_CONNECTED);
                    if (!it.isConnected()) {
                        ClientManager.getClient(this).disconnect(it.getMac());
                    }
                }
            }
        }
    }

    private long lastClick = 0;
    private boolean started = false;

    @SuppressLint("NotifyDataSetChanged")
    private void setClickListener() {
        myDevAdapter.addChildClickViewIds(R.id.connect_dev);
        myDevAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            List<DetailItem> adapterData = adapter.getData();
            if (adapterData.isEmpty()) {
                return;
            }
            DetailItem detailItem = adapterData.get(position);
            if (detailItem.isConnected()) {
                detailItem.setConnected(false);
                ClientManager.getClient(view.getContext()).disconnect(detailItem.getMac());
                myDevAdapter.notifyDataSetChanged();
            } else {
                startConnectBlu(view, detailItem);
            }
        });
        otherAdapter.addChildClickViewIds(R.id.connect_dev, R.id.refresh_ll);
        otherAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId() == R.id.connect_dev) {
                List<DetailItem> adapterData = adapter.getData();
                if (adapterData.isEmpty()) {
                    return;
                }
                DetailItem detailItem = adapterData.get(position);
                startConnectBlu(view, detailItem);
            } else if (view.getId() == R.id.refresh_ll) {
                refreshIv = view.findViewById(R.id.refresh_iv);
                refreshIv.clearAnimation();
                startRefresh();
                if (started) {
                    ClientManager.getClient(view.getContext()).stopSearch();
                }
                startScan();
            }
        });
    }

    private void startConnectBlu(View view, DetailItem detailItem) {
        if (detailItem == null || view == null) {
            return;
        }
        if (view.getId() == R.id.connect_dev) {
            if (lastClick != 0 && System.currentTimeMillis() - lastClick < 1200) {
                return;
            }
            lastClick = System.currentTimeMillis();
            final AProgressDialog progressDialog = createProgressDialog();
            BleConnectOptions options = new BleConnectOptions.Builder()
                    .setConnectRetry(3)
                    .setConnectTimeout(20000)
                    .setServiceDiscoverRetry(3)
                    .setServiceDiscoverTimeout(10000)
                    .build();
            ClientManager.getClient(view.getContext()).connect(detailItem.getMac(), options, (code, profile) -> {
                progressDialog.dismiss();
                if (code == Constants.REQUEST_SUCCESS) {
                    ClientManager.setDetail(detailItem);
                    otherDevs.remove(detailItem);
                    otherAdapter.notifyDataSetChanged();

                    for (DetailItem e :
                            myDevs) {
                        if (e.isConnected()) {
                            e.setConnected(false);
                            ClientManager.getClient(this).disconnect(e.getMac());
                        }
                    }
                    detailItem.setConnected(true);
                    detailItem.setItemType(DetailItem.TYPE_BIND_DEV);
                    myDevs.remove(detailItem);
                    myDevs.add(0, detailItem);
                    myDevAdapter.notifyDataSetChanged();
                    RxToast.success(getString(R.string.str_bl_the_connection_was_successful));

                    SharedPreferences preferences = getSharedPreferences("ble_profile", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("hasLastDev", true);
                    editor.apply();

                    tagProfile(detailItem, profile);

                    if (history.size() < 11) {
                        history.remove(detailItem);
                        history.add(detailItem);
                    } else {
                        history.remove(10);
                        history.add(detailItem);
                    }
                    SPUtil.setObject(view.getContext(), "bound_bluetooth", history);
                } else {
                    RxToast.error(getString(R.string.str_bl_the_connection_was_fail));
                }
            });
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

    private void startScan() {
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(3000, 3)   // 先扫BLE设备3次，每次3s
                .searchBluetoothClassicDevice(5000) // 再扫经典蓝牙5s
                .searchBluetoothLeDevice(2000)      // 再扫BLE设备2s
                .build();

        ClientManager.getClient(this).search(request, new SearchResponse() {
            private TreeSet<DetailItem> rets = new TreeSet<>((o1, o2) -> o1.getDevId().compareTo(o2.getDevId()));
            public static final boolean DEBUG = false;

            @Override
            public void onSearchStarted() {
                started = true;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDeviceFounded(SearchResult device) {
                byte[] scanRecord = device.getScanRecord();
                if (scanRecord == null || scanRecord.length < 1) {
                    return;
                }
                String bytes = ByteUtils.byteToString(device.getScanRecord());
//                LogUtil.d("recV", "onNotify data = " + String.format("%s", bytes));
                if (!bytes.startsWith("0201") || (!bytes.contains("0BFF00") && !bytes.contains("0FFF"))
                        || bytes.length() < 43) {
                    return;//DU_CT01
                }
                int did = ByteUtils.bytesToInt(ByteUtils.get(scanRecord, 9, 4));
                DetailItem item = new DetailItem(device.getAddress(), "" + did, conv2Qua(device.rssi), UUIDUtils.makeUUID(0xFFFC), UUIDUtils.makeUUID(0xFFFA));
                item.setItemType(DetailItem.TYPE_DEV);

                if (!contains(did)) {
                    rets.add(item);
                }

                otherDevs.clear();
                DetailItem header = new DetailItem("", "", 0, null, null);
                header.setItemType(1);
                otherDevs.add(header);
                if (!rets.isEmpty()) {
                    otherDevs.addAll(rets);
                    otherAdapter.notifyDataSetChanged();
                }
                if (rets.size() > 19) {
                    ClientManager.getClient(MyDevicesActivity.this).stopSearch();
                }
            }

            private boolean contains(int id) {
                for (DetailItem item :
                        myDevs) {
                    if (("" + id).equals(item.getDevId())) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void onSearchStopped() {
                started = false;
                if (refreshIv != null) {
                    refreshIv.clearAnimation();
                }
            }

            @Override
            public void onSearchCanceled() {
                started = false;
            }
        });
    }

    public int conv2Qua(int val) {
        if (val < 0) {
            /* Assume dBm already; rough conversion: best = -40, worst = -100 */
            val = Math.abs(clamp(val, -100, -40) + 40);  /* normalize to 0 */
            val = 100 - (int) ((100.0 * (double) val) / 60.0);
        } else if (val > 110 && val < 256) {
            /* assume old-style NEXT 8-bit unsigned signal level */
            val -= 256;  /* subtract 256 to convert to dBm */
            val = Math.abs(clamp(val, -100, -40) + 40);  /* normalize to 0 */
            val = 100 - (int) ((100.0 * (double) val) / 60.0);
        } else {
            val = clamp(val, 0, 100);
        }

        return Math.max(val, 0);
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        return Math.max(x, min);
    }

    /**
     * 是否开启蓝牙
     */
    private boolean isOpenBluetooth() {
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager.getAdapter();
        return adapter != null && adapter.isEnabled();
    }
}