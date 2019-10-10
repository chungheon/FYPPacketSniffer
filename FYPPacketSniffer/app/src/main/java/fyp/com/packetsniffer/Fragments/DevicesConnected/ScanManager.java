package fyp.com.packetsniffer.Fragments.DevicesConnected;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import fyp.com.packetsniffer.Fragments.IPv4;

public class ScanManager{

    private static final String TAG = "ScanManager";
    private final int SCAN_MSG = 1;
    private final int FRAG_MSG = 2;
    private final int SCAN_DONE = 12;
    private final int START_SCAN = 10;
    private final int STOP_SCAN = 11;

    private ScanManager mScanManager = this;
    private DeviceConnectFragment mConnFragment;

    private boolean scanInProgress;

    private ScanSubNetRunnable scan;

    private Handler mScanner;

    public ScanManager(String name) {

    }

    public void register(UpdateScanHandler controller, IPv4 ipAddress,
                                      Handler handler){

    }




    public class ScanHandler extends Handler{
        private IPv4 ipAddress;
        private Thread scanner;

        boolean scanInProgress = false;
        public ScanHandler(Looper looper, IPv4 ipAddress) {
            super(looper);
            this.ipAddress = ipAddress;
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, msg.arg1 + "");
            Log.d(TAG, msg.arg2 + "");
            if(msg.arg1 == FRAG_MSG){
                if(msg.arg2 == START_SCAN && !scanInProgress){
                    scan = new ScanSubNetRunnable(ipAddress);
                    this.post(scan);
                    Log.d(TAG, "THREAD STARTED");
                    scanInProgress = true;
                }else if(msg.arg2 == STOP_SCAN && scanInProgress){
                    scan.stopRun();
                    mConnFragment.scanDone();
                }
            }else if(msg.arg1 == SCAN_MSG){
                if(msg.arg2 == SCAN_DONE){
                    scanInProgress = false;
                }
            }
        }

    }
    public void sendScanner(Message msg){
        Log.d(TAG, "Sending Scanner" + msg.arg1 + " and " + msg.arg2);
        if(mScanner != null){
            mScanner.sendMessage(msg);
        }
    }
}
