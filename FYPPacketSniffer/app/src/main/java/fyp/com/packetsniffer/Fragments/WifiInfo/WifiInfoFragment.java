package fyp.com.packetsniffer.Fragments.WifiInfo;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import java.lang.reflect.Field;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fyp.com.packetsniffer.R;

public class WifiInfoFragment extends Fragment {

    private WifiManager mWifiManager;
    private ListView wifiList;
    private ListView detailList;
    private Button refreshBtn;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wifiinfo, container, false);
        this.wifiList = (ListView) view.findViewById(R.id.wifiList);
        this.detailList = (ListView) view.findViewById(R.id.detailList);
        this.refreshBtn = (Button) view.findViewById(R.id.refresh);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });
        this.mWifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        getInfo();

        return view;
    }

    public void refresh(){
        getInfo();
    }

    private void getInfo(){
        WifiInfo connInfo = mWifiManager.getConnectionInfo();
        DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
        List<Pair<String, String>> connItem = new ArrayList<>();
        List<Pair<String, String>> detailItem = new ArrayList<>();
        if(connInfo.getNetworkId() != -1){
            String[] dhcpData = dhcpInfo.toString().split(" ");
            Pair<String, String> connType = new Pair<>("Connection Type", "Wi-Fi");
            Pair<String, String> ipAddr = new Pair<>("IP Address", dhcpData[1]);
            Pair<String, String> subnetMask = new Pair<>("Subnet Mask", dhcpData[5]);
            Pair<String, String> defGateway = new Pair<>("Default Gateway", dhcpData[3]);
            Pair<String, String> dnsIP = new Pair<>("DNS Server IP", dhcpData[7]);
            connItem.add(connType);
            connItem.add(ipAddr);
            connItem.add(subnetMask);
            connItem.add(defGateway);
            connItem.add(dnsIP);

            ListViewWifiInfoAdaptor connAdaptor = new ListViewWifiInfoAdaptor(this.getContext(), R.layout.layout_infoitem, (ArrayList<Pair<String, String>>) connItem);
            wifiList.setAdapter(connAdaptor);
        }else{
            Pair<String, String> connType = new Pair<>("Connection Type", "Not connected");
            ListViewWifiInfoAdaptor connAdaptor = new ListViewWifiInfoAdaptor(this.getContext(), R.layout.layout_infoitem, (ArrayList<Pair<String, String>>) connItem);
            wifiList.setAdapter(connAdaptor);
        }

        if(mWifiManager.isWifiEnabled()){
            String macStr = getMacAddr();
            Pair<String, String> connType = new Pair<>("Enabled", "Yes");
            Pair<String, String> state, ssid, bssid, vendor, mac, speed, signal;
            if(connInfo.getNetworkId() != -1){
                state = new Pair<>("Connection State", "Completed");
                ssid = new Pair<>("SSID", connInfo.getSSID());
                bssid = new Pair<>("BSSID", connInfo.getBSSID());
                vendor = new Pair<>("Vendor", getVendor(formatMAC(macStr)));
                mac = new Pair<>("MAC", macStr);
                speed = new Pair<>("Speed", connInfo.getLinkSpeed() + " Mbps");
                signal = new Pair<>("Signal Strength", connInfo.getRssi() + " dBm");

                detailItem.add(state);
                detailItem.add(connType);
                detailItem.add(ssid);
                detailItem.add(bssid);
                detailItem.add(vendor);
                detailItem.add(mac);
                detailItem.add(speed);
                detailItem.add(signal);

                ListViewWifiInfoAdaptor detailAdaptor = new ListViewWifiInfoAdaptor(this.getContext(), R.layout.layout_infoitem, (ArrayList<Pair<String, String>>) detailItem);
                detailList.setAdapter(detailAdaptor);
            }else{
                connType = new Pair<>("Connection State", "Disabled");

                ListViewWifiInfoAdaptor detailAdaptor = new ListViewWifiInfoAdaptor(this.getContext(), R.layout.layout_infoitem, (ArrayList<Pair<String, String>>) detailItem);
                detailList.setAdapter(detailAdaptor);
            }

        }
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

    private String formatMAC(String data){
        String info[] = data.split(":");
        String macLook = info[0] + info[1] + info[2];
        return "RE" + macLook.toUpperCase();
    }

    private String getVendor(String macAddr){
        String vendor = "";
        try {
            //Get the ID
            Field resourceField = R.string.class.getDeclaredField(macAddr);
            //Here we are getting the String id in R file...But you can change to R.drawable or any other resource you want...
            int resourceId = resourceField.getInt(resourceField);
            //Here you can use it as usual
            vendor = this.getContext().getString(resourceId);
        }catch(NoSuchFieldException e){
            vendor = "Unidentified";
        }catch(IllegalAccessException e){
            vendor = "Unidentified";
        }

        return vendor;
    }
}