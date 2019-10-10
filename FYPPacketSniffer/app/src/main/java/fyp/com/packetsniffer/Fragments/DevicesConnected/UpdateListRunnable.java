package fyp.com.packetsniffer.Fragments.DevicesConnected;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Message;
import android.util.Log;

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
import java.util.Collections;
import java.util.List;

import fyp.com.packetsniffer.Fragments.IPv4;
import fyp.com.packetsniffer.R;

public class UpdateListRunnable implements Runnable{

    private static final String TAG = "UpdateList";
    private final int UPDATE_MSG = 3;
    private final int STOP_UPDATE = 11;
    private Context mContext;
    private Network network;
    private WifiManager mWifiManager;
    private ConnectivityManager mConnManager;
    private DeviceConnectFragment mConnFragment;
    private ArrayList<String> hosts, addrs, vendors, macs;
    private IPv4 mNetAddress;
    private boolean running;

    public UpdateListRunnable(ConnectivityManager connectivityManager, WifiManager wifiManager,
                       Context context, IPv4 netAddress, DeviceConnectFragment connectFragment){
        this.hosts = new ArrayList<>();
        this.addrs = new ArrayList<>();
        this.vendors = new ArrayList<>();
        this.macs = new ArrayList<>();
        this.mContext = context;
        this.mWifiManager = wifiManager;
        this.mConnManager = connectivityManager;
        this.mNetAddress = netAddress;
        this.mConnFragment = connectFragment;
    }
    @Override
    public synchronized void run() {
        running = true;
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        while(running) {
            Log.d(TAG, "Start Update");
            wifiInfo = mWifiManager.getConnectionInfo();
            ArrayList<String> hosts = new ArrayList<>();
            ArrayList<String> addrs = new ArrayList<>();
            ArrayList<String> vendors = new ArrayList<>();
            ArrayList<String> macs = new ArrayList<>();
            if(Build.VERSION.SDK_INT >= 23){
                if(wifiInfo.getSupplicantState().equals(SupplicantState.COMPLETED)) {
                    network = mConnManager.getActiveNetwork();
                    if (network != null) {
                        int hostsFound = readARPTableNew(hosts, addrs, vendors, macs);

                        for(String h: hosts){
                            Log.d(TAG, h);
                        }
                        Log.d(TAG, "Found: " + hostsFound);
                    }
                }
            }else{
                if(wifiInfo.getSupplicantState().equals(SupplicantState.COMPLETED)){
                    int hostsFound = readARPTableOld(hosts, addrs, vendors, macs);
                    for(String h: hosts){
                        Log.d(TAG, h);
                    }
                    Log.d(TAG, "Found: " + hostsFound);
                }
            }
            /*long numOfHosts = mNetAddress.getNumberOfHosts();
            mConnFragment.updateSearch(numOfHosts, hosts, addrs, vendors, macs);*/

                try{
                    wait(500);
                }catch (InterruptedException e) { }
        }
    }

    public synchronized void stopRun(){
        running = false;
        Log.d(TAG, "Stop Update Thread");
    }

    private int readARPTableNew(ArrayList<String> hosts, ArrayList<String> addrs, ArrayList<String> vendors, ArrayList<String> macs){
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            line = br.readLine();

            String ip = ipAddressToString(mWifiManager.getConnectionInfo().getIpAddress());
            String userMac = getMacAddr();
            if(Build.VERSION.SDK_INT >= 23){
                hosts.add(network.getByName(ip).getHostName());
            }else{
                hosts.add(ip);
            }
            addrs.add(ip);
            vendors.add(getVendor(getMacVendor(userMac)));
            macs.add(userMac);
            int numOfHost = 1;
            while ((line = br.readLine()) != null) {
                String mac = line.substring(41, 58);
                String[] info = line.split(" ");
                try {
                    String vendor = getVendor(getMacVendor(mac));
                    String hostname = info[0];
                    if(Build.VERSION.SDK_INT >= 23){
                        hostname = network.getByName(info[0]).getHostName();
                    }
                    if (!duplicate(addrs, info[0])) {
                        hosts.add(hostname);
                        addrs.add(info[0]);
                        vendors.add(vendor);
                        macs.add(mac);
                    }else if (!mac.equals("00:00:00:00:00:00")){
                        int index = getHost(addrs, info[0]);
                        if(index != -1 && index < hosts.size()){
                            hosts.set(index, hostname);
                            addrs.set(index, info[0]);
                            vendors.set(index, vendor);
                            macs.set(index, mac);
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

    private int readARPTableOld(ArrayList<String> hosts, ArrayList<String> addrs, ArrayList<String> vendors, ArrayList<String> macs){
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;

            String ip = ipAddressToString(mWifiManager.getConnectionInfo().getIpAddress());
            String userMac = getMacAddr();
            InetAddress address = InetAddress.getByName(ip);
            hosts.add(address.getHostName());
            addrs.add(ip);
            vendors.add(getVendor(getMacVendor(userMac)));
            macs.add(userMac);
            int numOfHost = 0;

            while ((line = br.readLine()) != null) {
                Log.d(TAG, line);
                String mac = line.substring(41, 58);
                String[] info = line.split(" ");
                try {
                    String vendor = getVendor(getMacVendor(mac));
                    InetAddress host = InetAddress.getByName(info[0]);
                    String hostname = host.getHostName();
                    if (!duplicate(addrs, info[0])) {
                        hosts.add(hostname);
                        addrs.add(info[0]);
                        vendors.add(vendor);
                        macs.add(mac);
                    }else if (!mac.equals("00:00:00:00:00:00")) {
                        int index = getHost(addrs, info[0]);
                        if(index != -1 && index < hosts.size()){
                            hosts.set(index, hostname);
                            addrs.set(index, info[0]);
                            vendors.set(index, vendor);
                            macs.set(index, mac);
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

    private int getHost(ArrayList<String> addrs, String address){
        int count = 0;
        for(String addr: addrs){
            if(addr.equals(address)){
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

    private boolean duplicate(ArrayList<String> ipaddrs, String ipaddr){
        for(String ip: ipaddrs){
            if(ip.equals(ipaddr)){
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
