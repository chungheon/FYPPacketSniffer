package fyp.com.packetsniffer.Fragments.DevicesConnected;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import fyp.com.packetsniffer.Fragments.IPv4;

import static java.lang.Thread.interrupted;

public class ScanSubNetRunnable implements Runnable {

    private static final String TAG = "ScanningSubNet";

    private final int SCAN_MSG = 1;
    private final int SCAN_DONE = 12;

    private IPv4 ipAddress;
    private boolean stopThread;

    public ScanSubNetRunnable(IPv4 ipAddress){
        this.ipAddress = ipAddress;
    }

    @Override
    public void run() { scanSubNet(); }

    private void scanSubNet(){
        Log.d(TAG, "Start Scan");
        String hostAddr = ipAddress.getFirstHostAddress();
        String[] hostIP = hostAddr.split("\\.");
        int firstBit = Integer.parseInt(hostIP[0]);
        int secondBit = Integer.parseInt(hostIP[1]);
        int thirdBit = Integer.parseInt(hostIP[2]);
        int lastBit = Integer.parseInt(hostIP[3]);
        stopThread = false;

        String endAddr = ipAddress.getLastHostAddress();
        InetAddress inetAddress = null;
        int i = 0;
        try {
            while(i < (ipAddress.getNumberOfHosts() - 2) && !stopThread){
                String fullBit = firstBit + "." + secondBit + "." + thirdBit + "." + lastBit;
                Log.d(TAG, "Trying: " + fullBit);
                inetAddress = InetAddress.getByName(fullBit);
                inetAddress.isReachable(1);
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
                i++;
            }

        } catch (UnknownHostException e) {

        } catch (IOException e) {

        }
    }

    public synchronized void stopRun(){
        stopThread = true;
        Log.d(TAG, "Scan stopped");
    }
}
