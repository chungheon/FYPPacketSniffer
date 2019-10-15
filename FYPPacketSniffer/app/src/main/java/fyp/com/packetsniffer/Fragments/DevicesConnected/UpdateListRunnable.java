package fyp.com.packetsniffer.Fragments.DevicesConnected;

import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fyp.com.packetsniffer.Fragments.IPv4;
import fyp.com.packetsniffer.R;

public class UpdateListRunnable implements Runnable{

    private static final String TAG = "UpdateList";
    private Context mContext;
    private Network network;
    private WifiManager mWifiManager;
    private ConnectivityManager mConnManager;
    private IPv4 mNetAddress;
    private boolean running;
    private boolean update;
    private long numOfHosts;
    private boolean lastRun = false;
    private DeviceConnectFragment mConnFragment;

    public UpdateListRunnable(ConnectivityManager connectivityManager,
                              WifiManager wifiManager, Context context, IPv4 netAddress, DeviceConnectFragment connFragment){
        this.mContext = context;
        this.mWifiManager = wifiManager;
        this.mConnManager = connectivityManager;
        this.mNetAddress = netAddress;
        this.mConnFragment = connFragment;
    }
    @Override
    public synchronized void run() {
        running = true;
        int numOfUpdates = 0;
        int numOfRefresh = 0;
        ArrayList<DeviceInformation> devices = new ArrayList<>();
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        String ip = ipAddressToString(wifiInfo.getIpAddress());
        String userMac = getMacAddr();
        String host = ip;
        if(Build.VERSION.SDK_INT >= 23){
            try {
                network = mConnManager.getActiveNetwork();
                host = (network.getByName(ip).getHostName());
            } catch (UnknownHostException e) { }
        }
        numOfHosts = mNetAddress.getNumberOfHosts();
        while(running || lastRun) {
            Log.d(TAG, "Start List Update");
            if(update) {
                Log.d(TAG, "List Updated");
                wifiInfo = mWifiManager.getConnectionInfo();
                if (Build.VERSION.SDK_INT >= 23 && !lastRun) {
                    if (wifiInfo.getSupplicantState().equals(SupplicantState.COMPLETED)) {
                        network = mConnManager.getActiveNetwork();
                        if (network != null) {
                            int hostsFound = readARPTableNew(devices);
                            for(DeviceInformation dev: devices){
                                Log.d(TAG, "Found: " + dev.getIpAddrs());
                            }
                        }
                    }
                } else {
                    if (wifiInfo.getSupplicantState().equals(SupplicantState.COMPLETED)) {
                        int hostsFound = readARPTableOld(devices);
                    }
                }
                if(lastRun && Build.VERSION.SDK_INT >= 23){
                    for(DeviceInformation dev: devices){
                        if(!dev.getMacAddrs().equals("00:00:00:00:00:00")){
                            network = mConnManager.getActiveNetwork();
                            String hostname = null;
                            try {
                                hostname = network.getByName(dev.getIpAddrs()).getHostName();
                            } catch (UnknownHostException e) {
                                hostname = dev.getIpAddrs();
                            }

                            dev.setHostName(hostname);
                            Log.d(TAG, "Get Network and hostname" + hostname);
                        }
                    }
                }
                sortList(devices);
                mConnFragment.updateSearch(numOfHosts, devices, host, ip + " (ME)",
                        userMac, getVendor(getMacVendor(userMac)));
                update = false;
                numOfUpdates++;
            }else{
                numOfRefresh++;
            }

            if(numOfHosts - 3 <= devices.size() && !lastRun){
                running = false;
                lastRun = true;
                update = true;
            }else if(numOfHosts - 3 <= devices.size() && lastRun) {
                lastRun = false;
            }else if(numOfRefresh > 2){
                update = true;
                numOfRefresh = 0;
            }else{
                try{
                    wait(100);
                }catch (InterruptedException e) { }
            }
            if(lastRun){
                try{
                    wait(1000);
                }catch (InterruptedException e) { }
            }
        }
        sortList(devices);
        DeviceInformation devInfo = new DeviceInformation();
        devInfo.setHostName(host);
        devInfo.setIpAddrs(ip + "(ME)");
        devInfo.setMacAddrs(userMac);
        devInfo.setMacVendor(getVendor(getMacVendor(userMac)));
        devices.add(devInfo);
        mConnFragment.scanDone(devices, wifiInfo);
    }

    public synchronized void stopRun(){
        running = false;
    }

    private void sortList(ArrayList<DeviceInformation> devices){
        Collections.sort(devices);
    }

    private int readARPTableNew(ArrayList<DeviceInformation> devices){
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            line = br.readLine();

            int numOfHost = 1;
            while ((line = br.readLine()) != null) {
                String mac = line.substring(41, 58);
                String[] info = line.split(" ");
                try {
                    String vendor = getVendor(getMacVendor(mac));
                    String hostname = info[0];
                    if (!duplicate(devices, info[0])) {
                        DeviceInformation devInfo = new DeviceInformation();
                        devInfo.setHostName(hostname);
                        devInfo.setIpAddrs(info[0]);
                        devInfo.setMacVendor(vendor);
                        devInfo.setMacAddrs(mac);
                        devices.add(devInfo);
                    }else if (!mac.equals("00:00:00:00:00:00")) {
                        int index = getHost(devices,info[0]);
                        if(index != -1 && index < devices.size()){
                            DeviceInformation devInfo = new DeviceInformation();
                            devInfo.setHostName(hostname);
                            devInfo.setIpAddrs(info[0]);
                            devInfo.setMacVendor(vendor);
                            devInfo.setMacAddrs(mac);
                            devices.set(index, devInfo);
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                }
                numOfHost++;
            }
            return numOfHost - 1;
        } catch (FileNotFoundException e) {

        } catch (IOException e){

        }

        return -1;
    }

    private int readARPTableOld(ArrayList<DeviceInformation> devices){
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;

            int numOfHost = 1;

            while ((line = br.readLine()) != null) {
                String mac = line.substring(41, 58);
                String[] info = line.split(" ");
                try {
                    String vendor = getVendor(getMacVendor(mac));
                    String hostname = info[0];
                    if (!duplicate(devices, info[0])) {
                        DeviceInformation devInfo = new DeviceInformation();
                        devInfo.setHostName(hostname);
                        devInfo.setIpAddrs(info[0]);
                        devInfo.setMacVendor(vendor);
                        devInfo.setMacAddrs(mac);
                        devices.add(devInfo);;
                    }else if (!mac.equals("00:00:00:00:00:00")) {
                        int index = getHost(devices, info[0]);
                        if(index != -1 && index < devices.size()){
                            DeviceInformation devInfo = new DeviceInformation();
                            devInfo.setHostName(hostname);
                            devInfo.setIpAddrs(info[0]);
                            devInfo.setMacVendor(vendor);
                            devInfo.setMacAddrs(mac);
                            devices.set(index, devInfo);
                        }
                    }
                } catch (Exception e) {

                }
                numOfHost++;
            }

            return numOfHost - 1;
        } catch (FileNotFoundException e) {

        } catch (IOException e){

        }

        return -1;
    }

    private int getHost(ArrayList<DeviceInformation> devices, String address){
        int count = 0;
        for(DeviceInformation dev: devices){
            if(dev.getIpAddrs().equals(address)){
                return count;
            }
            count++;
        }

        return -1;
    }

    private String getVendor(String macAddr){
        String vendor = "";
        try {
            //Get the ID
            Field resourceField = R.string.class.getDeclaredField(macAddr);
            //Here we are getting the String id in R file...But you can change to R.drawable or any other resource you want...
            int resourceId = resourceField.getInt(resourceField);
            //Here you can use it as usual
            vendor = mContext.getString(resourceId);
        }catch(NoSuchFieldException e){
            vendor = "Unidentified";
        }catch(IllegalAccessException e){
            vendor = "Unidentified";
        }

        return vendor;
    }

    private String ipAddressToString(int ipAddress) {

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray)
                    .getHostAddress();
        } catch (UnknownHostException ex) {
            ipAddressString = "NaN";
        }

        return ipAddressString;
    }

    private boolean duplicate(ArrayList<DeviceInformation> devices, String ipaddr){
        for(DeviceInformation dev: devices){
            if(dev.getIpAddrs().equals(ipaddr)){
                return true;
            }
        }
        return false;
    }

    private String getMacVendor(String data){
        String info[] = data.split(":");
        String macLook = info[0] + info[1] + info[2];
        return "RE" + macLook.toUpperCase();
    }

    private String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }
}
