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
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.crypto.Mac;

import fyp.com.packetsniffer.Fragments.IPv4;
import fyp.com.packetsniffer.MainActivity;
import fyp.com.packetsniffer.R;

public class DeviceConnectFragment extends Fragment {
    private static final String TAG = "DeviceConnFrag";

    private RecyclerView wifiNetworkList;
    private View view;
    private ImageButton runBtn;
    private ImageButton historyBtn;
    private TextView wifiName;

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
    private TextView numDevices;
    private TextView percentText;
    private ProgressBar percentBar;

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
        runBtn = (ImageButton) view.findViewById(R.id.startScan);
        wifiName = (TextView) view.findViewById(R.id.wifiName);
        numDevices = (TextView) view.findViewById(R.id.numDevices);
        percentText = (TextView) view.findViewById(R.id.percentText);
        percentBar = (ProgressBar) view.findViewById(R.id.percentBar);
        historyBtn = (ImageButton) view.findViewById(R.id.historyBtn);

        mConnectivityManager = (ConnectivityManager)this.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        networkReceiver = new NetworkChangeReceiver();
        intentFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        getActivity().registerReceiver(networkReceiver, intentFilter);

        connFragment = this;
        scanInProgress = false;

        defaultDisplay();
    }

    private void defaultDisplay(){
        runBtn.setEnabled(false);
        runBtn.setVisibility(View.INVISIBLE);
        numDevices.setVisibility(View.INVISIBLE);
        percentText.setVisibility(View.INVISIBLE);
        percentBar.setVisibility(View.INVISIBLE);
    }

    private void displayPercentage(){
        runBtn.setImageResource(R.mipmap.stop_btn);
        numDevices.setText("0 Devices");
        numDevices.setVisibility(View.VISIBLE);
        percentText.setText("0%");
        percentText.setVisibility(View.VISIBLE);
        percentBar.setProgress(0);
        percentBar.setVisibility(View.VISIBLE);
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
                if(networkIP.getNumberOfHosts() > (255*255)){
                    Toast.makeText(getContext().getApplicationContext(), "Network too big use NMAP instead", Toast.LENGTH_LONG);
                }else{
                    if(!scanInProgress){
                        scanInProgress = true;
                        updateListRunnable = new UpdateListRunnable(mConnectivityManager, mWifiManager,
                                getContext().getApplicationContext(), networkIP, connFragment);
                        Thread update = new Thread(updateListRunnable);
                        update.start();

                        scanRunnable = new ScanSubNetRunnable(networkIP, updateListRunnable);
                        Thread scan = new Thread(scanRunnable);
                        scan.start();

                        displayPercentage();
                    }else{
                        updateListRunnable.stopRun();
                        scanRunnable.stopRun();
                        scanInProgress = false;
                    }
                }
            }
        });

        historyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).getSupportActionBar().setTitle("Scan History");
                ((MainActivity)getActivity()).enableViews(true);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ScanHistoryFragment(), "test")
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    public void updateSearch(final long numOfHosts, ArrayList<DeviceInformation> devices, final String userHost,
                             final String userIP, final String userMac, final String userVendor){
        DeviceInformation devInfo = new DeviceInformation();
        devInfo.setHostName(userHost);
        devInfo.setIpAddrs(userIP);
        devInfo.setMacVendor(userVendor);
        devInfo.setMacAddrs(userMac);
        ArrayList<DeviceInformation> devInfos = new ArrayList<>(devices);
        devInfos.add(0, devInfo);
        final ArrayList<DeviceInformation> devicesInfo = new ArrayList<>(devInfos);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int percentage = (int) (((double)devicesInfo.size()/(numOfHosts - 2)) * 100);
                percentBar.setProgress(percentage);
                percentText.setText(percentage + " %" + " " + (numOfHosts - 2) + "/" + devicesInfo.size());
                ArrayList<DeviceInformation> foundDev = new ArrayList<>();
                for(int i = 0; i < devicesInfo.size(); i++){
                    if(!devicesInfo.get(i).getMacAddrs().equals("00:00:00:00:00:00")){
                        foundDev.add(devicesInfo.get(i));
                    }
                }


                numDevices.setText(foundDev.size() + " Devices");

                RecyclerViewDeviceAdapter adapter = new RecyclerViewDeviceAdapter(foundDev, getContext());
                wifiNetworkList.setVisibility(View.VISIBLE);
                wifiNetworkList.setAdapter(adapter);
                wifiNetworkList.setLayoutManager(new LinearLayoutManager(getActivity()));
            }
        });
    }

    @Override
    public void onDestroy() {
        if(networkReceiver != null){
            getActivity().unregisterReceiver(networkReceiver);
        }
        if(updateListRunnable != null){
            updateListRunnable.stopRun();
        }
        if(scanRunnable != null){
            scanRunnable.stopRun();
        }
        super.onDestroy();
    }

    public class NetworkChangeReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction().toString();
            final WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            final DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
            if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
                if(!scanInProgress && dhcpInfo.ipAddress != 0){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            runBtn.setEnabled(true);
                            runBtn.setVisibility(View.VISIBLE);
                            runBtn.setImageResource(R.mipmap.refresh_scan_btn);
                            wifiName.setText(wifiInfo.getSSID().replace("\"",""));
                            wifiNetworkList.setVisibility(View.INVISIBLE);
                        }
                    });
                }else if(scanInProgress && dhcpInfo.ipAddress == 0){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            runBtn.setEnabled(false);
                            runBtn.setVisibility(View.INVISIBLE);
                            if (updateListRunnable != null) {
                                updateListRunnable.stopRun();
                            }
                            if (scanRunnable != null) {
                                scanRunnable.stopRun();
                            }
                        }
                    });
                }else if(!scanInProgress && dhcpInfo.ipAddress == 0){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            runBtn.setEnabled(false);
                            runBtn.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        }
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

    public void scanDone(ArrayList<DeviceInformation> devices, WifiInfo wifiInfo){
        scanInProgress = false;
        Log.d(TAG, "Scan Done");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                runBtn.setImageResource(R.mipmap.refresh_scan_btn);
                percentBar.setVisibility(View.INVISIBLE);
                percentText.setVisibility(View.INVISIBLE);
            }
        });

        storeCache(devices, wifiInfo);
    }

    private void storeCache(ArrayList<DeviceInformation> devices, WifiInfo wifiInfo){
        String macName = wifiInfo.getBSSID().replace(":", "");
        String fileName = wifiInfo.getSSID().replace("\"", "") + '_' + macName + ".txt";
        File newDir = new File(getActivity().getCacheDir().toString(), "ScanHistory");
        newDir.mkdir();
        String path = getActivity().getCacheDir().toString();
        Log.d("Files", "Path: " + path);
        File cacheFile = new File(path + "/ScanHistory", fileName);

        Log.d(TAG, "Cache File:" + fileName);
        ArrayList<DeviceInformation> foundDev = new ArrayList<>();
        for(int i = 0; i < devices.size(); i++){
            if(!devices.get(i).getMacAddrs().equals("00:00:00:00:00:00")){
                foundDev.add(devices.get(i));
            }
        }
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(cacheFile));
            Date currentTime = Calendar.getInstance().getTime();
            bw.write(currentTime.toString());
            bw.newLine();
            bw.write(wifiInfo.getSSID() + ":");
            bw.write(wifiInfo.getMacAddress());
            bw.newLine();
            for(DeviceInformation dev: foundDev){
                bw.write(dev.getHostName() + ":" + dev.getIpAddrs() + ":");
                bw.write(dev.getMacAddrs() + ":" + dev.getMacVendor());
                if(!dev.getIpAddrs().equals(devices.get(devices.size()-1).getIpAddrs())){
                    bw.newLine();
                }
                Log.d(TAG, dev.getHostName() + ":" + dev.getIpAddrs());
            }

            bw.close();
        } catch (IOException e) {
            Log.e(TAG, "Error storing to cache");
        }

        path = getActivity().getCacheDir().toString() + "/ScanHistory/";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++)
        {
            Log.d("Files", "FileName:" + files[i].getName());
        }

        directory = new File(getActivity().getCacheDir().toString());
        files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++)
        {
            Log.d("Files", "FileName:" + files[i].getName());
        }
    }
}