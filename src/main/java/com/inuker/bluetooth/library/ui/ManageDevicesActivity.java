package com.inuker.bluetooth.library.ui;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SizeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.inuker.bluetooth.library.ClientManager;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.R;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;
import com.inuker.bluetooth.library.model.DetailItem;
import com.inuker.bluetooth.library.utils.RxToast;
import com.inuker.bluetooth.library.utils.SPUtil;
import com.jeremyliao.liveeventbus.LiveEventBus;
import com.lxj.xpopup.XPopup;

import java.util.ArrayList;
import java.util.List;

public class ManageDevicesActivity extends AppCompatActivity {
    private ArrayList<DetailItem> mList = new ArrayList<>();
    private MyBoundDevAdapter myBoundDevAdapter = new MyBoundDevAdapter(R.layout.bound_blue_item, mList);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setContentView(R.layout.activity_manage_devices);
        TextView titleTv = findViewById(R.id.title);
        titleTv.setText(getString(R.string.str_bl_device_management));
        findViewById(R.id.back_iv).setOnClickListener(v -> finish());
        RecyclerView boundRv = findViewById(R.id.my_bound_devs);
        boundRv.setOutlineProvider(new ViewOutlineProvider() {
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
        boundRv.setClipToOutline(true);

        //我绑定的设备
        RecyclerView myDevRv = findViewById(R.id.my_bound_devs);
        myDevRv.setLayoutManager(new LinearLayoutManager(this));
        myDevRv.setHasFixedSize(true);
        myDevRv.setAdapter(myBoundDevAdapter);
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
        View header = LayoutInflater.from(this).inflate(R.layout.my_bound_dev_item_header, null);
        myBoundDevAdapter.addHeaderView(header);
        myBoundDevAdapter.setNewInstance(mList);
        myBoundDevAdapter.addChildClickViewIds(R.id.unbind, R.id.connect_dev);
        myBoundDevAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                List<DetailItem> adapterData = adapter.getData();
                if (adapterData.isEmpty()) return;
                DetailItem detailItem = (DetailItem) adapterData.get(position);
                int id = view.getId();
                if (id == R.id.unbind) {
                    new XPopup.Builder(ManageDevicesActivity.this)
                            .dismissOnTouchOutside(false)
                            .asConfirm("", String.format(getString(R.string.str_bl_you_are_sure_to_unbind_the_device), detailItem.getDevId()), () -> unbindDev(adapter, view, detailItem)).show();
                } else if (id == R.id.connect_dev) {
                    if (detailItem.isConnected()) {
                        detailItem.setConnected(false);
                        myBoundDevAdapter.notifyDataSetChanged();
                        ClientManager.getClient(view.getContext()).disconnect(detailItem.getMac());
                    } else startConnectBlu(view, detailItem);
                }
            }
        });
        recoverLastDev();
    }

    private void unbindDev(@NonNull BaseQuickAdapter adapter, @NonNull View view, DetailItem detailItem) {
        mList.remove(detailItem);
        adapter.notifyDataSetChanged();
        RxToast.success(getString(R.string.str_bl_you_have_success_unbound_the_device));
        ClientManager.getClient(view.getContext()).disconnect(detailItem.getMac());
        if (mList.isEmpty()) {
            SharedPreferences preferences = view.getContext().getSharedPreferences(from+"ble_profile", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("hasLastDev", false);
            editor.apply();
        }
        SPUtil.setObject(view.getContext(), "bound_" + from + "bluetooth", mList);
        LiveEventBus
                .get("remove_bound_dev")
                .post(detailItem.getDevId());
        LiveEventBus
                .get("remove_bound_dev")
                .post(detailItem.getMac());
    }
    String from = "";
    @SuppressLint("NotifyDataSetChanged")
    private void recoverLastDev() {
        if (getIntent() != null && getIntent().getStringExtra("from") != null) {
            from = getIntent().getStringExtra("from");
        }
        SharedPreferences preferences = getSharedPreferences(from+"ble_profile", MODE_PRIVATE);
        boolean hasLastDev = preferences.getBoolean("hasLastDev", false);
        if (hasLastDev) {
            ArrayList<DetailItem> its = SPUtil.getObject(this, "bound_" + from + "bluetooth");
            if (its != null) {
                mList.clear();
                mList.addAll(its);
                myBoundDevAdapter.notifyDataSetChanged();

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

    private void startConnectBlu(View view, DetailItem detailItem) {
        if (detailItem == null || view == null) return;
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

                    detailItem.setConnected(true);
                    detailItem.setItemType(DetailItem.TYPE_BIND_DEV);
                    myBoundDevAdapter.notifyDataSetChanged();
                    RxToast.success(getString(R.string.str_bl_the_connection_was_successful));
                    tagProfile(detailItem, profile);
                } else {
                    RxToast.error(getString(R.string.str_bl_the_connection_was_fail));
                }
            });
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

    private void tagProfile(DetailItem item, BleGattProfile profile) {
        List<BleGattService> services = profile.getServices();
        for (BleGattService service : services) {
            if (service.getUUID().toString().toUpperCase().startsWith("0000FFFA")) {
                item.setItgCounter(true);
                break;
            }
        }
    }
}