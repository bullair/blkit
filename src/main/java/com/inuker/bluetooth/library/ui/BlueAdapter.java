package com.inuker.bluetooth.library.ui;

import android.annotation.SuppressLint;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.inuker.bluetooth.library.R;
import com.inuker.bluetooth.library.model.DetailItem;

import java.util.List;

public class BlueAdapter extends BaseMultiItemQuickAdapter<DetailItem, BaseViewHolder> {
    public BlueAdapter(@Nullable List<DetailItem> data) {
        super(data);
        addItemType(DetailItem.TYPE_HEADER, R.layout.blue_header);
        addItemType(DetailItem.TYPE_BIND_DEV, R.layout.cnn_blue_item);
        addItemType(DetailItem.TYPE_DEV, R.layout.blue_item);
    }

    @SuppressLint({"DefaultLocale", "NotifyDataSetChanged"})
    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, DetailItem detailItem) {
        if (detailItem.getItemType() == DetailItem.TYPE_HEADER) {
            TextView view = baseViewHolder.getView(R.id.header);
            if (getContext().getString(R.string.str_bl_the_device_that_was_discovered).equals(detailItem.getHn())) {
                int p = getItemPosition(detailItem) != 0 ? 35 : 26;
                RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
                lp.topMargin = ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, p, view.getResources().getDisplayMetrics()));
                view.setLayoutParams(lp);
            }
            view.setText(detailItem.getHn());
        } else {
            TextView idTv = baseViewHolder.getView(R.id.blue_id);
            TextView sValTv = baseViewHolder.getView(R.id.signal_val);
            TextView cnnTv = baseViewHolder.getView(R.id.connect_dev);
//            TextView unbindTv = baseViewHolder.getViewOrNull(R.id.unbind);
            sValTv.setText(String.format("%d%%", detailItem.getRssi()));
            idTv.setText(detailItem.getDevId());
            if (detailItem.isConnected()) {
                cnnTv.setText(getContext().getString(R.string.str_bl_connected));
            } else {
                cnnTv.setText(getContext().getString(R.string.str_bl_connect));
            }
//            if (unbindTv != null) unbindTv.setOnClickListener(v -> {
//
//            });
//            cnnTv.setOnClickListener(v -> {});
        }
    }
}
