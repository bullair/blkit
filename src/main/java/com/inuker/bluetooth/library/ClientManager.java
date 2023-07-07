package com.inuker.bluetooth.library;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.inuker.bluetooth.library.model.DetailItem;
import com.inuker.bluetooth.library.utils.SPUtil;

import java.util.HashMap;

public class ClientManager {

    @SuppressLint("StaticFieldLeak")
    private static Context ctx;
    public static int seqIndex = 0;
    private static int totalChs = 6;
    private static volatile BluetoothClient mClient;
    @SuppressLint("UseSparseArrays")
//    private static final SparseArray<Integer> tunnelsForCid = new SparseArray<>();
    private static final HashMap<Integer, Integer> tunnelsForCid = new HashMap<>();
    private static final MutableLiveData<DetailItem> detailItemMutableLiveData = new MutableLiveData<>();

    public static BluetoothClient getClient(Context context) {
        if (mClient == null) {
            synchronized (ClientManager.class) {
                if (mClient == null) {
                    ctx = context.getApplicationContext();
                    mClient = new BluetoothClient(context.getApplicationContext());
                }
            }
        }
        return mClient;
    }

    public static void setCount(int num) {
        DetailItem item = detailItemMutableLiveData.getValue();
        if (item != null) {
            item.setCurCount(num);
        }
    }

    public static int getCount() {
        DetailItem item = detailItemMutableLiveData.getValue();
        if (item != null) {
            return item.getCurCount();
        }
        return 0;
    }

    public static void setCurChNo(int no) {
        DetailItem item = detailItemMutableLiveData.getValue();
        if (item != null) {
            if (no == 0) no = totalChs;
            item.setCurNo(no);
        }
    }

    public static int getCurChNo() {
        DetailItem item = detailItemMutableLiveData.getValue();
        if (item != null) {
            return item.getCurNo();
        }
        return 1;
    }

    public static int getTotalChs() {
        return totalChs;
    }

    public static void setTotalChs(int sum) {
        totalChs = sum;
    }

    public static void putRecord(int no, int sid) {
        if (no == -1) return;
        if (sid == -1) tunnelsForCid.remove(no);
        else tunnelsForCid.put(no, sid);
        if (ctx != null) SPUtil.setObject(ctx, "tunnels_bl", tunnelsForCid);
    }

    public static void flushRecord(HashMap<Integer, Integer> ids) {
        if (ids == null) return;
        tunnelsForCid.clear();
        tunnelsForCid.putAll(ids);
    }

    public static HashMap<Integer, Integer> getChRecords() {
        return tunnelsForCid;
    }

    public static Integer getChRecord(int chNo) {
        return tunnelsForCid.get(chNo);
    }

    public static String getCurMac() {
        DetailItem item = detailItemMutableLiveData.getValue();
        return item != null ? item.getMac() : null;
    }

    public static boolean isItgCounter() {
        DetailItem item = detailItemMutableLiveData.getValue();
        return item != null && item.isItgCounter();
    }

    public static MutableLiveData<DetailItem> getDetail() {
        return detailItemMutableLiveData;
    }

    public static void setDetail(DetailItem detail) {
        detailItemMutableLiveData.setValue(detail);
    }
}
