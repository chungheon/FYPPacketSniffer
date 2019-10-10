package fyp.com.packetsniffer.Fragments.DevicesConnected;

import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import fyp.com.packetsniffer.Fragments.IPv4;

public class UpdateScanHandler extends HandlerThread {

    private DeviceConnectFragment mConnFragment;
    private UpdateManager mUpdateManager;

    public UpdateScanHandler(String name, DeviceConnectFragment mainFragment) {
        super(name);
        this.mConnFragment = mainFragment;
    }

    public synchronized void register(Context context, WifiManager wifiManager,
                     ConnectivityManager connectivityManager, IPv4 netAddress){
        mUpdateManager = new UpdateManager();
        Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                mUpdateManager.onReceived(msg.arg2);
            }
        };
        mUpdateManager.register(this, context, wifiManager,
                                connectivityManager, netAddress, handler);



    }
}
