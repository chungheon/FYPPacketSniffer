package fyp.com.packetsniffer.Fragments.DevicesConnected;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fyp.com.packetsniffer.Fragments.IPv4;
import fyp.com.packetsniffer.R;

public class DeviceConnectFragment extends Fragment {
    private static final String TAG = "DeviceConnFrag";
    private WifiManager mWifiManager;
    private ConnectivityManager mConnectivityManager;
    private RecyclerView wifiNetworkList;
    private View view;
    private Network network;
    private Button runBtn;
    private Button updateBtn;
    private WifiInfo wifiInfo;
    private DhcpInfo dhcpInfo;
    private IPv4 networkIP;
    private boolean scanInProgress;
    private DeviceConnectFragment connFragment = this;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_device_connect, container, false);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        wifiNetworkList = (RecyclerView) view.findViewById(R.id.recycler_view_Net);
        runBtn = (Button) view.findViewById(R.id.startScan);
        updateBtn = (Button) view.findViewById(R.id.updateDev);
        mConnectivityManager = (ConnectivityManager)this.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        dhcpInfo = mWifiManager.getDhcpInfo();
        wifiInfo = mWifiManager.getConnectionInfo();
        scanInProgress = false;
        runBtn.setEnabled(false);
        if(wifiInfo.getSupplicantState().equals(SupplicantState.COMPLETED)){
            runBtn.setEnabled(true);
        }
        runBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanInProgress = true;
                dhcpInfo = mWifiManager.getDhcpInfo();
                String[] ipInfo = dhcpInfo.toString().split(" ");
                String subMask = ipInfo[5];
                try{
                    networkIP = new IPv4(ipInfo[1], subMask);
                }catch (NumberFormatException e){
                    subMask = getSubMask();
                    networkIP = new IPv4(ipInfo[1], subMask);
                }
                runBtn.setEnabled(false);
                ScanSubNetThread scanNet = new ScanSubNetThread(networkIP, connFragment);
                Thread thread = new Thread(scanNet);
                thread.start();
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiInfo = mWifiManager.getConnectionInfo();
                if(Build.VERSION.SDK_INT >= 23){
                    if(wifiInfo.getSupplicantState().equals(SupplicantState.COMPLETED) && !scanInProgress){
                        runBtn.setEnabled(true);
                        network = mConnectivityManager.getActiveNetwork();
                        if(network != null){
                            readARPTableNew();
                        }
                    }else if(wifiInfo.getSupplicantState().equals(SupplicantState.COMPLETED)){
                        network = mConnectivityManager.getActiveNetwork();
                        if(network != null){
                            readARPTableNew();
                        }
                    }
                }else{
                    if(wifiInfo.getSupplicantState().equals(SupplicantState.COMPLETED) && !scanInProgress){
                        runBtn.setEnabled(true);
                        readARPTableOld();
                    }else if(wifiInfo.getSupplicantState().equals(SupplicantState.COMPLETED)){
                        readARPTableOld();
                    }
                }
            }
        });

        return view;
    }

    private String getSubMask(){
        if(Build.VERSION.SDK_INT >= 23){
            Network network = mConnectivityManager.getActiveNetwork();
            LinkProperties linkProp = mConnectivityManager.getLinkProperties(network);
            List<LinkAddress> linkAddress = linkProp.getLinkAddresses();
            IPv4 ipv4 = null;
            StringBuilder ipv6 = new StringBuilder("N/A");
            for (LinkAddress l : linkAddress) {
                String[] ip4 = l.toString().split("\\.");
                String[] ip6 = l.toString().split(":");
                if (ip4.length > 1) {
                    try {
                        ipv4 = new IPv4(l.toString());
                    }catch (NumberFormatException e){
                        return "255.255.255.255";
                    }
                }
            }
            if(ipv4 != null){
                return ipv4.getNetmask();
            }
        }

        return "255.255.255.255";
    }

    private void readARPTableNew(){
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            line = br.readLine();
            ArrayList<String> hostnames = new ArrayList<>();
            ArrayList<String> ipaddrs = new ArrayList<>();
            ArrayList<String> vendors = new ArrayList<>();
            ArrayList<String> macs = new ArrayList<>();

            String ip = ipAddressToString(wifiInfo.getIpAddress());
            String userMac = getMacAddr();
            if(Build.VERSION.SDK_INT >= 23){
                hostnames.add(network.getByName(ip).getHostName());
            }else{
                hostnames.add(ip);
            }
            ipaddrs.add(ip);
            vendors.add(getVendor(getMacVendor(userMac)));
            macs.add(userMac);

            while ((line = br.readLine()) != null) {
                Log.d("lod", line);
                String mac = line.substring(41, 58);
                String[] info = line.split(" ");
                Log.d("test", mac);
                if (!mac.equals("00:00:00:00:00:00")) {
                    try {
                        Log.d(TAG, "check: " + getMacVendor(mac));
                        String vendor = getVendor(getMacVendor(mac));
                        Log.d(TAG, "VENDOR: " + vendor);
                        String hostname = ip;
                        if(Build.VERSION.SDK_INT >= 23){
                            hostname = network.getByName(info[0]).getHostName();
                        }
                        Log.d(TAG, hostname);
                        if (!duplicate(ipaddrs, info[0])) {
                            hostnames.add(hostname);
                            ipaddrs.add(info[0]);
                            vendors.add(vendor);
                            macs.add(mac);
                        }
                    } catch (Exception e) {

                    }
                }
            }
            Log.d("CHECKNULL", hostnames.size() + "");
            RecyclerViewDeviceAdapter adapter = new RecyclerViewDeviceAdapter(hostnames, ipaddrs, vendors, macs, getContext().getApplicationContext());
            wifiNetworkList.setAdapter(adapter);
            wifiNetworkList.setLayoutManager(new LinearLayoutManager(getContext().getApplicationContext()));
        } catch (FileNotFoundException e) {

        } catch (IOException e){

        }
    }

    private void readARPTableOld(){
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            line = br.readLine();
            ArrayList<String> hostnames = new ArrayList<>();
            ArrayList<String> ipaddrs = new ArrayList<>();
            ArrayList<String> vendors = new ArrayList<>();
            ArrayList<String> macs = new ArrayList<>();

            String ip = ipAddressToString(wifiInfo.getIpAddress());
            String userMac = getMacAddr();
            InetAddress address = InetAddress.getByName(ip);
            hostnames.add(address.getHostName());
            ipaddrs.add(ip);
            vendors.add(getVendor(getMacVendor(userMac)));
            macs.add(userMac);

            while ((line = br.readLine()) != null) {
                Log.d("lod", line);
                String mac = line.substring(41, 58);
                String[] info = line.split(" ");
                Log.d("test", mac);
                if (!mac.equals("00:00:00:00:00:00")) {
                    try {
                        Log.d(TAG, "check: " + getMacVendor(mac));
                        String vendor = getVendor(getMacVendor(mac));
                        Log.d(TAG, "VENDOR: " + vendor);
                        InetAddress host = InetAddress.getByName(info[0]);
                        String hostname = host.getHostName();
                        Log.d(TAG, hostname);
                        if (!duplicate(ipaddrs, info[0])) {
                            hostnames.add(hostname);
                            ipaddrs.add(info[0]);
                            vendors.add(vendor);
                            macs.add(mac);
                        }
                    } catch (Exception e) {

                    }
                }
            }
            Log.d("CHECKNULL", hostnames.size() + "");
            RecyclerViewDeviceAdapter adapter = new RecyclerViewDeviceAdapter(hostnames, ipaddrs, vendors, macs, getContext().getApplicationContext());
            wifiNetworkList.setAdapter(adapter);
            wifiNetworkList.setLayoutManager(new LinearLayoutManager(getContext().getApplicationContext()));
        } catch (FileNotFoundException e) {

        } catch (IOException e){

        }
    }

    public void scanDone(){
        scanInProgress = false;
    }

    private String getVendor(String macAddr){
        String vendor = "";
        try {
            //Get the ID
            Field resourceField = R.string.class.getDeclaredField(macAddr);
            //Here we are getting the String id in R file...But you can change to R.drawable or any other resource you want...
            int resourceId = resourceField.getInt(resourceField);
            //Here you can use it as usual
            vendor = getContext().getApplicationContext().getString(resourceId);
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
            Log.e("WIFI_IP", "Unable to get host address.");
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