package fyp.com.packetsniffer.Fragments.DevicesConnected;

/*Device Connect Fragment class
This class is in-charge of the passing live data retrieved to be displayed
on the UI for the purpose of scanning the network for devices connected.

 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
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
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import fyp.com.packetsniffer.Fragments.IPv4;
import fyp.com.packetsniffer.MainActivity;
import fyp.com.packetsniffer.R;

public class DeviceConnectFragment extends Fragment {
    private static final String TAG = "DeviceConnFrag"; //Tag used for debugging purposes

    private RecyclerView deviceList; //RecyclerView to display all devices found on network
    private View view; //View object reference to pass data to UI
    private ImageButton runBtn; //Button to run scan
    private ImageButton historyBtn; //Button to open history of past scans
    private TextView wifiName; //TextView displays wifi network SSID
    private TextView numDevices; //TextView displays number of devices found on network
    private TextView percentText; //TextView displays the percentage of scan done
    private ProgressBar percentBar; //ProgressBar visual representation of percentage of scan done

    //Broadcast Receiver to receive intent (network changes)
    private NetworkChangeReceiver networkReceiver;
    //The intent filter for intents to be caught
    private IntentFilter intentFilter;

    //WifiManager class to retrieve wifi network related information
    private WifiManager mWifiManager;
    //ConnectivityManager class to retrieve active network related information
    private ConnectivityManager mConnectivityManager;
    //Instance of this object to be passed to model for reference
    private DeviceConnectFragment connFragment;
    //ScanSubNetRunnable reference for the thread scanning the network
    private ScanSubNetRunnable scanRunnable;
    //UpdateListRunnable reference for the thread collecting the results
    private UpdateListRunnable updateListRunnable;

    //Boolean tracking if a scan is in progress
    private boolean scanInProgress = false;

    //Setup the view and listeners for when fragment is created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_device_connect, container, false);
        initView();
        initListener();
        return view;
    }

    //Initialization of all objects and UI components
    private void initView(){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        deviceList = (RecyclerView) view.findViewById(R.id.recycler_view_Net);
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

    //The default display settings at start up.
    private void defaultDisplay(){
        wifiName.setText("-");
        runBtn.setEnabled(false);
        runBtn.setVisibility(View.INVISIBLE);
        numDevices.setVisibility(View.INVISIBLE);
        percentText.setVisibility(View.INVISIBLE);
        percentBar.setVisibility(View.INVISIBLE);
    }

    //Display the percentage when scanning
    private void displayPercentage(){
        runBtn.setImageResource(R.mipmap.stop_btn);
        numDevices.setText("0 Devices");
        numDevices.setVisibility(View.VISIBLE);
        percentText.setText("0%");
        percentText.setVisibility(View.VISIBLE);
        percentBar.setProgress(0);
        percentBar.setVisibility(View.VISIBLE);
    }

    //Initialize all necessary listeners for buttons etc
    private void initListener(){

        runBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
                String[] ipInfo = dhcpInfo.toString().split(" ");
                String subMask = ipInfo[5];
                IPv4 networkIP = null;
                try{
                    networkIP = new IPv4(ipInfo[1], subMask);
                }catch (NumberFormatException e) {
                    subMask = getSubMask();
                    networkIP = new IPv4(ipInfo[1], subMask);
                }
                if(networkIP == null){
                    Toast.makeText(getContext().getApplicationContext(),
                            "Not Connected to Network", Toast.LENGTH_SHORT).show();
                    return;
                }
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
        });

        historyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).getSupportActionBar().setTitle("Scan History");
                ((MainActivity)getActivity()).enableViews(true, 1, "Scan Network", "");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ScanHistoryFragment(), "ScanHistory")
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    //Called by UpdateListRunnable to update the View with data retrieved
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

        if(getActivity() != null){
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

                    RecyclerViewDeviceAdapter adapter = new RecyclerViewDeviceAdapter(foundDev);
                    deviceList.setVisibility(View.VISIBLE);
                    deviceList.setAdapter(adapter);
                    deviceList.setLayoutManager(new LinearLayoutManager(getActivity()));
                }
            });
        }

    }

    //Override for when the fragment is destroyed
    @Override
    public void onDestroy() {
        if(networkReceiver != null){
            try{
                getActivity().unregisterReceiver(networkReceiver);
            }catch(IllegalArgumentException e){ }

        }
        if(updateListRunnable != null){
            updateListRunnable.stopRun();
        }
        if(scanRunnable != null){
            scanRunnable.stopRun();
        }
        Log.d(TAG, "destroyed devConn");
        super.onDestroy();
    }

    //Override for when fragment is paused
    @Override
    public void onPause() {
        if(networkReceiver != null){
            try{
                getActivity().unregisterReceiver(networkReceiver);
            }catch(IllegalArgumentException e){ }
        }

        if(updateListRunnable != null){
            updateListRunnable.stopRun();
        }
        if(scanRunnable != null){
            scanRunnable.stopRun();
        }
        super.onPause();
    }

    //Override for when fragment is resumed after a pause
    @Override
    public void onResume() {
        if(networkReceiver != null && intentFilter != null){
            getActivity().registerReceiver(networkReceiver, intentFilter);
        }
        super.onResume();
    }

    //Extends BroadcastReceiver to receive broadcast intents for network changes
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
                            deviceList.setVisibility(View.INVISIBLE);
                        }
                    });
                }else if(scanInProgress && dhcpInfo.ipAddress == 0){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            runBtn.setEnabled(false);
                            wifiName.setText("-");
                            if (updateListRunnable != null) {
                                updateListRunnable.stopRun();
                            }
                            if (scanRunnable != null) {
                                scanRunnable.stopRun();
                            }
                        }
                    });
                }else if(!scanInProgress && dhcpInfo.ipAddress == 0){
                    wifiName.setText("-");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            runBtn.setEnabled(false);
                        }
                    });
                }
            }
        }
    }

    //Called to retrieve submask when DhcpInfo does not return a valid submask
    private String getSubMask(){
        if(Build.VERSION.SDK_INT >= 23){
            Network network = mConnectivityManager.getActiveNetwork();
            LinkProperties linkProp = mConnectivityManager.getLinkProperties(network);
            List<LinkAddress> linkAddress = linkProp.getLinkAddresses();
            IPv4 ipv4 = null;
            for (LinkAddress l : linkAddress) {
                String[] ip4 = l.toString().split("\\.");
                if (ip4.length > 1) {
                    try {
                        ipv4 = new IPv4(l.toString());
                    }catch (NumberFormatException e){
                        return "1";
                    }
                }
            }
            if(ipv4 != null){
                return ipv4.getNetmask();
            }
        }

        return "1";
    }

    //Called by UpdateListRunnable when scan is completed and no further update would be done
    public void scanDone(ArrayList<DeviceInformation> devices, WifiInfo wifiInfo){
        scanInProgress = false;
        Log.d(TAG, "Scan Done");
        if(getActivity() != null){
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
    }

    //Store data of the scan done to a file for future reference
    private void storeCache(ArrayList<DeviceInformation> devices, WifiInfo wifiInfo){
        String macName = wifiInfo.getBSSID().replace(":", "");
        String fileName = wifiInfo.getSSID().replace("\"", "") + '_' + macName;
        File newDir = new File(getActivity().getFilesDir().toString(), "ScanHistory");
        newDir.mkdir();
        String path = getActivity().getFilesDir().toString();
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
                bw.write(dev.getHostName() + "," + dev.getIpAddrs() + ",");
                bw.write(dev.getMacAddrs() + "," + dev.getMacVendor());
                if(!dev.getIpAddrs().equals(devices.get(devices.size()-1).getIpAddrs())){
                    bw.newLine();
                }
                Log.d(TAG, dev.getHostName() + ":" + dev.getIpAddrs());
            }

            bw.close();
        } catch (IOException e) {
            Log.e(TAG, "Error storing to cache");
        }
    }
}