package fyp.com.packetsniffer.Fragments.DevicesConnected;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static java.lang.Thread.interrupted;

public class ScanSubNetThread implements Runnable {

    private static final String TAG = "ScanningSubNet";
    private IPv4 ipAddress;
    private DeviceConnectFragment mFragment;

    public ScanSubNetThread(IPv4 ipAddress, DeviceConnectFragment fragment){
        this.ipAddress = ipAddress;
        this.mFragment = fragment;
    }

    @Override
    public void run() {
        scanSubNet();
    }

    private void scanSubNet(){
        String hostAddr = ipAddress.getFirstHostAddress();
        String[] hostIP = hostAddr.split("\\.");
        int firstBit = Integer.parseInt(hostIP[0]);
        int secondBit = Integer.parseInt(hostIP[1]);
        int thirdBit = Integer.parseInt(hostIP[2]);
        int lastBit = Integer.parseInt(hostIP[3]);

        String endAddr = ipAddress.getLastHostAddress();
        InetAddress inetAddress = null;
        try {
            for(int i = 0 ; i < (ipAddress.getNumberOfHosts() - 2); i++){
                String fullBit = firstBit + "." + secondBit + "." + thirdBit + "." + lastBit;
                Log.d(TAG, "Trying: " + fullBit);
                inetAddress = InetAddress.getByName(fullBit);
                inetAddress.isReachable(10);
                lastBit++;
                if(lastBit > 255){
                    thirdBit++;
                    lastBit = 0;
                }
                if(thirdBit > 255){
                    secondBit++;
                    thirdBit = 0;
                }
                if(secondBit > 255){
                    firstBit++;
                    secondBit = 0;
                }
                if(firstBit > 255) {
                    break;
                }
                if(fullBit.equals(endAddr)){
                    break;
                }
                if(interrupted()){
                    break;
                }
            }

            mFragment.scanDone();
        } catch (UnknownHostException e) {

        } catch (IOException e) {

        }
    }
}
