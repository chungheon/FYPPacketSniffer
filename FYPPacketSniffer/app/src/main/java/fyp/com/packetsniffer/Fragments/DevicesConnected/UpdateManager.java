package fyp.com.packetsniffer.Fragments.DevicesConnected;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;

import fyp.com.packetsniffer.Fragments.IPv4;

public class UpdateManager {
    private static final String TAG = "UpdateManager";

    private final int FRAG_MSG = 2;
    private final int UPDATE_MSG = 3;
    private final int UPDATE_DONE = 12;
    private final int START_UPDATE = 10;
    private final int STOP_UPDATE = 11;

    private WifiManager mWifiManager;
    private ConnectivityManager mConnectivityManager;
    private Context mContext;
    private UpdateManager mUpdateManager = this;
    private UpdateScanHandler mMsgController;
    private IPv4 mNetAddress;

    private Handler mUpdate;
    private UpdateListRunnable updateThread;

    public UpdateManager() {

    }

    public void register(UpdateScanHandler controller, Context context,
                WifiManager wifiManager, ConnectivityManager connectivityManager, IPv4 netAddress,
                                Handler handler){
        this.mMsgController = controller;
        this.mWifiManager = wifiManager;
        this.mConnectivityManager = connectivityManager;
        this.mContext = context;
        this.mNetAddress = netAddress;
        this.mUpdate = handler;
    }


    public void onReceived(int msg){

    }
}
