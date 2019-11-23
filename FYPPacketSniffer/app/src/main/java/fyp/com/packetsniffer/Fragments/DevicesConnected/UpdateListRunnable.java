package fyp.com.packetsniffer.Fragments.DevicesConnected;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
        int numOfRefresh = 0;
        HashMap<String, DeviceInformation> devicesMap = new HashMap<>();
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
            if(update) {
                wifiInfo = mWifiManager.getConnectionInfo();
                if (wifiInfo.getSupplicantState().equals(SupplicantState.COMPLETED)) {
                    network = mConnManager.getActiveNetwork();
                    if (network != null) {
                        int hostsFound = readARPTableNew(devicesMap);
                    }
                }
                if(lastRun){
                    for(String key: devicesMap.keySet() ){
                        if(!devicesMap.get(key).getMacAddrs().equals("00:00:00:00:00:00")){
                            network = mConnManager.getActiveNetwork();
                            String hostname = devicesMap.get(key).getIpAddrs();
                            if(network != null) {
                                try {
                                    hostname = network.getByName(devicesMap.get(key).getIpAddrs()).getHostName();
                                } catch (UnknownHostException e) { }
                            }

                            devicesMap.get(key).setHostName(hostname);
                        }
                    }
                }
                List<DeviceInformation> devices = sortList(devicesMap);
                mConnFragment.updateSearch(numOfHosts, (ArrayList) devices, host, ip + " (ME)",
                        userMac, getVendor(getMacVendor(userMac)));
                update = false;
            }else{
                numOfRefresh++;
            }

            if(numOfHosts - 3 <= devicesMap.keySet().size() && !lastRun){
                running = false;
                lastRun = true;
                update = true;
            }else if(numOfHosts - 3 <= devicesMap.keySet().size() && lastRun) {
                lastRun = false;
            }else if(numOfRefresh > 1){
                update = true;
                numOfRefresh = 0;
            }else{
                try{
                    wait(10);
                }catch (InterruptedException e) { }
            }
            if(lastRun){
                try{
                    wait(1000);
                }catch (InterruptedException e) { }
            }
        }
        List<DeviceInformation> devices = sortList(devicesMap);
        DeviceInformation devInfo = new DeviceInformation();
        devInfo.setHostName(host);
        devInfo.setIpAddrs(ip + "(ME)");
        devInfo.setMacAddrs(userMac);
        devInfo.setMacVendor(getVendor(getMacVendor(userMac)));
        devices.add(devInfo);
        mConnFragment.scanDone((ArrayList) devices, wifiInfo);
    }

    public synchronized void stopRun(){
        running = false;
    }

    private List<DeviceInformation> sortList(HashMap<String, DeviceInformation> devices){
        List<DeviceInformation> deviceInfo = new ArrayList<>();
        for(String key: devices.keySet()){
            deviceInfo.add(devices.get(key));
        }
        Collections.sort(deviceInfo);
        return deviceInfo;
    }

    private int readARPTableNew(HashMap<String, DeviceInformation> devicesMap){
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            line = br.readLine();

            int numOfHost = 0;
            while ((line = br.readLine()) != null) {
                String mac = line.substring(41, 58);
                String[] info = line.split(" ");
                try {
                    String vendor = getVendor(getMacVendor(mac));
                    String hostname = info[0];
                    if (!devicesMap.containsKey(info[0])) {
                        DeviceInformation devInfo = new DeviceInformation();
                        devInfo.setHostName(hostname);
                        devInfo.setIpAddrs(info[0]);
                        devInfo.setMacVendor(vendor);
                        devInfo.setMacAddrs(mac.toUpperCase());
                        devicesMap.put(info[0], devInfo);
                        numOfHost++;
                    }else if (!mac.equals("00:00:00:00:00:00")) {
                        if(devicesMap.containsKey(info[0])){
                            DeviceInformation devInfo = new DeviceInformation();
                            devInfo.setHostName(hostname);
                            devInfo.setIpAddrs(info[0]);
                            devInfo.setMacVendor(vendor);
                            devInfo.setMacAddrs(mac.toUpperCase());
                            devicesMap.put(info[0], devInfo);
                        }
                    }
                } catch (Exception e) { }
            }
            return numOfHost;
        } catch (FileNotFoundException e) {

        } catch (IOException e){

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
