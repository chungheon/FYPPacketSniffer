package fyp.com.packetsniffer.Fragments.DevicesConnected;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

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

    private final int FRAG_MSG = 2;
    private final int START_SCAN = 10;
    private final int STOP_SCAN = 11;

    private RecyclerView wifiNetworkList;
    private View view;
    private Button runBtn;
    private Button updateBtn;
    private TextView wifiName;
    private TextView numDevices;
    private TextView percentText;
    private ProgressBar percentBar;


    private NetworkChangeReceiver networkReceiver;
    private IntentFilter intentFilter;

    private Handler mHandler;
    private Thread subNetThread;

    private WifiManager mWifiManager;
    private ConnectivityManager mConnectivityManager;
    private WifiInfo wifiInfo;
    private DhcpInfo dhcpInfo;
    private IPv4 networkIP;
    private Network network;
    private DeviceConnectFragment connFragment;
    private ScanSubNetRunnable scanRunnable;
    private UpdateListRunnable updateListRunnable;


    private boolean scanInProgress = false;
    private boolean foundAll;
    private int found;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_device_connect, container, false);
        initView();
        initBtn();
        return view;
    }

    private void initView(){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        wifiNetworkList = (RecyclerView) view.findViewById(R.id.recycler_view_Net);
        runBtn = (Button) view.findViewById(R.id.startScan);
        updateBtn = (Button) view.findViewById(R.id.updateDev);
        wifiName = (TextView) view.findViewById(R.id.wifiName);
        numDevices = (TextView) view.findViewById(R.id.numDevices);
        percentText = (TextView) view.findViewById(R.id.percentText);
        percentBar = (ProgressBar) view.findViewById(R.id.percentBar);

        mConnectivityManager = (ConnectivityManager)this.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        scanInProgress = false;
        runBtn.setEnabled(false);
        networkReceiver = new NetworkChangeReceiver();

        intentFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        getActivity().registerReceiver(networkReceiver, intentFilter);

        connFragment = this;
    }

    private void initBtn(){

        runBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dhcpInfo = mWifiManager.getDhcpInfo();
                String[] ipInfo = dhcpInfo.toString().split(" ");
                String subMask = ipInfo[5];
                try{
                    networkIP = new IPv4(ipInfo[1], subMask);
                }catch (NumberFormatException e) {
                    subMask = getSubMask();
                    networkIP = new IPv4(ipInfo[1], subMask);
                }
                if(networkIP.getNumberOfHosts() > (255 * 255) && !scanInProgress){
                    Toast.makeText(getContext().getApplicationContext(), "Network too big", Toast.LENGTH_LONG);
                }else if(!scanInProgress){
                    scanInProgress = true;
                    /*updateListRunnable = new UpdateListRunnable(mConnectivityManager, mWifiManager,
                            getContext().getApplicationContext(), networkIP, connFragment);
                    Thread update = new Thread(updateListRunnable);
                    update.start();*/

                    scanRunnable = new ScanSubNetRunnable(networkIP);
                    Thread scan = new Thread(scanRunnable);
                    scan.start();

                }else{
                    //updateListRunnable.stopRun();
                    scanRunnable.stopRun();
                    scanInProgress = false;
                }

            }
        });
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(networkReceiver);
        super.onDestroy();
    }

    public void updateSearch(final long numOfHosts, final ArrayList<String> hosts, final ArrayList<String> addrs,
                             final ArrayList<String> vendors, final ArrayList<String> macs){
        /*getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int percentage = (int) (hosts.size()/numOfHosts) * 100;
                percentBar.setProgress(percentage);
                percentText.setText(percentage + " %");
                ArrayList<String> foundHosts = new ArrayList<>();
                ArrayList<String> foundAddrs = new ArrayList<>();
                ArrayList<String> foundVendors = new ArrayList<>();
                ArrayList<String> foundMacs = new ArrayList<>();
                for(int i = 0; i < hosts.size(); i++){
                    if(!macs.get(i).equals("00:00:00:00:00:00")){
                        foundHosts.add(hosts.get(i));
                        foundAddrs.add(addrs.get(i));
                        foundVendors.add(vendors.get(i));
                        foundMacs.add(macs.get(i));
                    }
                }
                numDevices.setText(foundHosts.size() + " Devices");

                RecyclerViewDeviceAdapter adapter = new RecyclerViewDeviceAdapter(foundHosts,
                        foundAddrs, foundVendors, foundMacs, getContext().getApplicationContext());
                wifiNetworkList.setAdapter(adapter);
            }
        });*/
    }

    public class NetworkChangeReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction().toString();
            final WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            final DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
            if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
                if(!scanInProgress && wifiInfo.getSupplicantState().equals(SupplicantState.COMPLETED) &&
                        dhcpInfo.ipAddress != 0){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            runBtn.setEnabled(true);
                            wifiName.setText(dhcpInfo.ipAddress + "");
                        }
                    });
                }else if(dhcpInfo.ipAddress == 0){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(scanInProgress){
                                runBtn.performClick();
                            }
                            runBtn.setEnabled(false);

                        }
                    });
                }
            }
        }
    }

    /*private void updateList(IPv4 networkIP){
        wifiInfo = mWifiManager.getConnectionInfo();
        ArrayList<String> hosts = new ArrayList<>();
        ArrayList<String> addrs = new ArrayList<>();
        ArrayList<String> vendors = new ArrayList<>();
        ArrayList<String> macs = new ArrayList<>();
        if(Build.VERSION.SDK_INT >= 23){
            if(wifiInfo.getSupplicantState().equals(SupplicantState.COMPLETED)) {
                network = mConnectivityManager.getActiveNetwork();
                if (network != null) {
                    int hostsFound = readARPTableNew(hosts, addrs, vendors, macs);
                    Log.d(TAG, "hostsFound " + hostsFound + " " + hosts.size() + " " + networkIP.getNumberOfHosts());
                    if(found < hostsFound){
                        getActivity().runOnUiThread(new UpdateWifiListRunnable(hosts, addrs, vendors, macs));
                        found = hostsFound;
                    }
                    if(networkIP.getNumberOfHosts() - 2 <= hostsFound + 1){
                        foundAll = true;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                runBtn.setEnabled(true);
                            }
                        });
                    }
                }
            }
        }else{
            if(wifiInfo.getSupplicantState().equals(SupplicantState.COMPLETED)){
                int hostsFound = readARPTableOld(hosts, addrs, vendors, macs);
                if(found < hostsFound){
                    getActivity().runOnUiThread(new UpdateWifiListRunnable(hosts, addrs, vendors, macs));
                    found = hostsFound;
                }
                if(networkIP.getNumberOfHosts() - 2 <= hostsFound + 1){
                    foundAll = true;
                }
            }
        }
    }*/

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

    public class UpdateWifiListRunnable implements Runnable{
        ArrayList<String> hostnames;
        ArrayList<String> ipaddrs;
        ArrayList<String> vendors;
        ArrayList<String> macs;
        public UpdateWifiListRunnable(ArrayList<String> hosts, ArrayList<String> addrs, ArrayList<String> vendors, ArrayList<String> macs){
            this.hostnames = hosts;
            this.ipaddrs = addrs;
            this.vendors = vendors;
            this.macs = macs;
        }
        @Override
        public void run() {
            RecyclerViewDeviceAdapter adapter = new RecyclerViewDeviceAdapter(hostnames, ipaddrs, vendors, macs, getContext().getApplicationContext());
            wifiNetworkList.setAdapter(adapter);
            wifiNetworkList.setLayoutManager(new LinearLayoutManager(getContext().getApplicationContext()));
        }
    }

    public void scanDone(){
        scanInProgress = false;
        Log.d(TAG, "Scan Done");
    }
}