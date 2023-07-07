package com.inuker.bluetooth.library.ui;

import android.widget.TextView;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.inuker.bluetooth.library.R;
import com.inuker.bluetooth.library.model.DetailItem;

import java.util.List;

public class MyBoundDevAdapter extends BaseQuickAdapter<DetailItem, BaseViewHolder> {

    public MyBoundDevAdapter(int layoutResId, @Nullable List<DetailItem> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, DetailItem detailItem) {
        TextView idTv = helper.getView(R.id.blue_id);
        TextView sValTv = helper.getView(R.id.signal_val);
        TextView cnnTv = helper.getView(R.id.connect_dev);

        sValTv.setText(String.format("%d%%", detailItem.getRssi()));
        idTv.setText(detailItem.getDevId());
        if (detailItem.isConnected()) {
            cnnTv.setText(getContext().getString(R.string.str_bl_connected));
        } else {
            cnnTv.setText(getContext().getString(R.string.str_bl_not_connected));
        }
    }
}
