package com.inuker.bluetooth.library.ui;

import static android.content.Context.BLUETOOTH_SERVICE;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.inuker.bluetooth.library.R;
import com.inuker.bluetooth.library.model.DetailItem;

import java.util.List;

public class MyOthAdapter extends BaseMultiItemQuickAdapter<DetailItem, BaseViewHolder> {
    private boolean reqScan = false;

    public MyOthAdapter(@Nullable List<DetailItem> data) {
        super(data);
        addItemType(1, R.layout.my_oth_dev_item_header);
        addItemType(3, R.layout.my_dev_item);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, DetailItem detailItem) {
        if (detailItem.getItemType() == 3) {
            TextView idTv = helper.getView(R.id.blue_id);
            TextView sValTv = helper.getView(R.id.signal_val);
            TextView cnnTv = helper.getView(R.id.connect_dev);

            sValTv.setText(String.format("%d%%", detailItem.getRssi()));
            idTv.setText(detailItem.getDevId());

            cnnTv.setText(getContext().getString(R.string.str_bl_connect));
        } else if (!reqScan && detailItem.getItemType() == 1 && isOpenBluetooth()) {
            reqScan = true;
            ImageView refreshIv = helper.getView(R.id.refresh_iv);
            refreshIv.clearAnimation();
            Animation mAnimation = AnimationUtils.loadAnimation(refreshIv.getContext(), R.anim.rotate_img);
            LinearInterpolator lin = new LinearInterpolator();
            mAnimation.setInterpolator(lin);
            refreshIv.startAnimation(mAnimation);
        }
    }

    private boolean isOpenBluetooth() {
        if (getContext() == null) return false;
        BluetoothManager manager = (BluetoothManager) getContext().getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager.getAdapter();
        return adapter != null && adapter.isEnabled();
    }
}
