package com.inuker.bluetooth.library.ui;

import android.graphics.Color;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.inuker.bluetooth.library.R;
import com.inuker.bluetooth.library.model.DetailItem;

import java.util.List;

public class MyDevAdapter extends BaseQuickAdapter<DetailItem, BaseViewHolder> {

    public MyDevAdapter(int layoutResId, @Nullable List<DetailItem> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, DetailItem detailItem) {
        TextView idTv = helper.getView(R.id.blue_id);
        TextView sValTv = helper.getView(R.id.signal_val);
        TextView cnnTv = helper.getView(R.id.connect_dev);
        cnnTv.setBackgroundResource(R.drawable.blu_click_bound_selector);
        cnnTv.setTextColor(Color.rgb(168, 145, 106));

        sValTv.setText(String.format("%d%%", detailItem.getRssi()));
        idTv.setText(detailItem.getDevId());
        if (detailItem.isConnected()) {
            cnnTv.setText(getContext().getString(R.string.str_bl_connected));
        } else {
            cnnTv.setText(getContext().getString(R.string.str_bl_not_connected));
        }
    }
}
