package fyp.com.packetsniffer.Fragments.DevicesConnected;

import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

public class ScanSimulation implements Runnable{
    private static final String TAG = "ScanSimulation";
    public ArrayList<String> foundDev;
    boolean running;
    public synchronized void run(){
        Random r = new Random();
        foundDev = new ArrayList<>();
        running = true;
        while(running){
            int mac = (r.nextBoolean()) ? 1 : 2;
            int rNum = r.nextInt() % 256;
            String test = "192.168.1." + ((rNum < 0) ? rNum*-1 : rNum);
            foundDev.add(test + " " + mac);
            Log.d(TAG, "found " + test);
            try {
                wait(10);
            } catch (InterruptedException e) {
            }
        }
    }

    public synchronized ArrayList<String> getDevices(){
        return new ArrayList<>(foundDev);
    }

    public synchronized void stopRun(){
        running = false;
    }
}