package fyp.com.packetsniffer.Fragments.DevicesConnected.NmapServices;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import fyp.com.packetsniffer.Fragments.DevicesConnected.DeviceConnectFragment;
import fyp.com.packetsniffer.Fragments.IPv4;
import fyp.com.packetsniffer.R;

/*Nmap suite for android
Nmap provides port scanning, and host discovery services

However, for the current version we will not be implementing the function
as there is another similar solution

*/

public class NmapMainFragment extends Fragment {

    private String libPath;
    private View view;
    private TextView wifiText;
    private ImageButton historyBtn;
    private ImageButton startBtn;
    private TextView numDevices;
    private ProgressBar progressBar;
    private RecyclerView deviceList;

    private boolean scanInProgress = false;

    //WifiManager class to retrieve wifi network related information
    private WifiManager mWifiManager;
    //Broadcast Receiver to receive intent (network changes)
    private NetworkChangeReceiver networkReceiver;
    //ConnectivityManager class to retrieve active network related information
    private ConnectivityManager mConnectivityManager;
    //The intent filter for intents to be caught
    private IntentFilter intentFilter;

    private NmapViewThread nmapThread;
    private NmapMainFragment fragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_nmap_main, container, false);
        /*loadAssets();
        initView();
        initListener();*/
        return view;
    }

    private void initView(){
        this.wifiText = (TextView) view.findViewById(R.id.nmap_wifi_name);
        this.historyBtn = (ImageButton) view.findViewById(R.id.nmap_history_btn);
        this.startBtn = (ImageButton) view.findViewById(R.id.nmap_start_scan);
        this.numDevices = (TextView) view.findViewById(R.id.nmap_num_devices);
        this.progressBar = (ProgressBar) view.findViewById(R.id.aircrack_progress_bar);

        mConnectivityManager = (ConnectivityManager)this.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.networkReceiver = new NetworkChangeReceiver();
        intentFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        getActivity().registerReceiver(networkReceiver, intentFilter);

        defaultDisplay();
    }

    private void initListener(){
        startBtn.setOnClickListener(new View.OnClickListener() {
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
                    String cmd = libPath + " -Pn -v " + networkIP.getCIDR();
                    nmapThread = new NmapViewThread(fragment, cmd);
                }
            }
        });
    }

    private String getSubMask(){
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

        return "1";
    }

    private void defaultDisplay(){
        wifiText.setText("-");
        startBtn.setEnabled(false);
        startBtn.setVisibility(View.INVISIBLE);
        numDevices.setVisibility(View.INVISIBLE);
    }


    private void loadAssets(){
        installAsset("nmap-os-db_arm", "nmap-os-db",getActivity().getFilesDir().toString());
        installAsset("nmap-services_arm", "nmap-services",getActivity().getFilesDir().toString());
        installAsset("nmap-payloads_arm", "nmap-payloads",getActivity().getFilesDir().toString());
        installAsset("nmap-mac-prefixes_arm", "nmap-mac-prefixes",getActivity().getFilesDir().toString());
        installAsset("nmap_arm", "nmap",getActivity().getFilesDir().toString());
    }

    private void installAsset(String assetName, String fileName, String dirPath) {
        InputStream is = null;
        FileOutputStream fileOutputStream = null;
        try {
            is = getActivity().getAssets().open(assetName);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);

            File targetFile = new File(dirPath + "/" + fileName);
            libPath = targetFile.getPath();
            fileOutputStream = new FileOutputStream(targetFile);
            fileOutputStream.write(buffer);

            is.close();
            fileOutputStream.close();
            setPermissions();
        } catch (IOException e) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                }
            }

            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    private void setPermissions() {
        try {
            Process shell = Runtime.getRuntime().exec("chmod 777 " + libPath + "\n");
            shell.waitFor();
        } catch (IOException e) {
        } catch (InterruptedException e) { }
    }

    //Extends BroadcastReceiver to receive broadcast intents for network changes
    public class NetworkChangeReceiver extends BroadcastReceiver {
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
                            startBtn.setEnabled(true);
                            startBtn.setVisibility(View.VISIBLE);
                            startBtn.setImageResource(R.mipmap.refresh_scan_btn);
                            wifiText.setText(wifiInfo.getSSID().replace("\"",""));
                        }
                    });
                }else if(scanInProgress && dhcpInfo.ipAddress == 0){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startBtn.setEnabled(false);
                            wifiText.setText("-");
                            /*if (updateListRunnable != null) {
                                updateListRunnable.stopRun();
                            }
                            if (scanRunnable != null) {
                                scanRunnable.stopRun();
                            }*/
                        }
                    });
                }else if(!scanInProgress && dhcpInfo.ipAddress == 0){
                    wifiText.setText("-");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startBtn.setEnabled(false);
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        if(networkReceiver != null){
            getActivity().unregisterReceiver(networkReceiver);
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        if(networkReceiver != null){
            getActivity().unregisterReceiver(networkReceiver);
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        if(networkReceiver != null && intentFilter != null){
            getActivity().registerReceiver(networkReceiver, intentFilter);
        }
        super.onResume();
    }

    //For Future development to cater to both ARM and x86 Architecture
    /*private void getArchitecture(){
        arch = System.getProperty("os.arch");
        if(arch.equals("") || arch == null){
            final String[] architectures = {"aarch64", "x86"};

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext().getApplicationContext());
            builder.setTitle("Pick device's architecture");
            builder.setItems(architectures, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case 0: arch = "aarch64";
                            break;
                        case 1: arch = "x86";
                            break;
                    }
                }
            });
            builder.show();
        }
        Log.d("ARCH", arch);
    }*/
}
