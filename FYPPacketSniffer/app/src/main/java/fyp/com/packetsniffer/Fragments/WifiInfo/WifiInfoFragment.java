package fyp.com.packetsniffer.Fragments.WifiInfo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.RouteInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fyp.com.packetsniffer.Fragments.IPv4;
import fyp.com.packetsniffer.R;

public class WifiInfoFragment extends Fragment {

    private WifiManager mWifiManager;
    private ConnectivityManager mConnManager;
    private TelephonyManager mTeleManager;
    private View view;
    private ListView wifiList;
    private ListView detailList;
    private ListView cellList;
    private TextView networkType;
    private Button refreshBtn;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_wifiinfo, container, false);
        this.wifiList = (ListView) view.findViewById(R.id.wifiList);
        this.detailList = (ListView) view.findViewById(R.id.detailList);
        this.cellList = (ListView) view.findViewById(R.id.cellList);
        this.refreshBtn = (Button) view.findViewById(R.id.refresh);
        this.networkType = (TextView) view.findViewById(R.id.networkType);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });
        this.mWifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.mConnManager = (ConnectivityManager) getContext().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        this.mTeleManager = (TelephonyManager) getContext().getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        refresh();
        return view;
    }

    public void refresh(){
        if(Build.VERSION.SDK_INT >= 23){
            Network network = mConnManager.getActiveNetwork();
            NetworkCapabilities capabilities = mConnManager.getNetworkCapabilities(network);
            if(capabilities == null){
                displayEmpty();
            }else if(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)){
                displayWifi();
            }else if(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)){
                displayCell(network);
            }
        }else{
            if(mConnManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null){
                displayWifi();
            }else if(mConnManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null){
                displayCell();
            }else{
                displayEmpty();
            }
        }

        getNetworkState();
    }
    private void displayEmpty(){
        networkType.setText("NO CONNECTION");
        List<Pair<String, String>> connItem = new ArrayList<>();
        Pair<String, String> connType = new Pair<>("Connection Type", "DISCONNECTED");

        ListViewWifiInfoAdaptor connAdaptor = new ListViewWifiInfoAdaptor(this.getContext(), R.layout.layout_infoitem, (ArrayList<Pair<String, String>>) connItem);
        wifiList.setAdapter(connAdaptor);
    }

    private void displayWifi(){
        //WifiInfo connInfo = mWifiManager.getConnectionInfo();
        DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
        networkType.setText("WI-FI CONNECTION");
        List<Pair<String, String>> connItem = new ArrayList<>();
        String[] dhcpData = dhcpInfo.toString().split(" ");
        Pair<String, String> connType = new Pair<>("Connection Type", "Wi-Fi");
        Pair<String, String> ipAddr = new Pair<>("IP Address", dhcpData[1]);
        String subMask = dhcpData[5];
        try{
            IPv4 ipv4 = new IPv4(dhcpData[1], subMask);
        }catch (NumberFormatException e){
            subMask = getSubMask();
        }
        Pair<String, String> subnetMask = new Pair<>("Subnet Mask", subMask);
        Pair<String, String> defGateway = new Pair<>("Default Gateway", dhcpData[3]);
        Pair<String, String> dnsIP = new Pair<>("DNS Server IP", dhcpData[7]);
        StringBuilder ipv6 = new StringBuilder("N/A");
        if(Build.VERSION.SDK_INT > 23){
            Network network = mConnManager.getActiveNetwork();
            List<LinkAddress> linkAddress = mConnManager.getLinkProperties(network).getLinkAddresses();
            int count = 0;
            for(LinkAddress l: linkAddress){
                String[] info = l.toString().split(":");
                if(info.length > 1 && count == 0){
                    ipv6 = new StringBuilder("\n").append(l.toString());
                    count++;
                }else if(info.length > 1){
                    ipv6.append("\n").append(l.toString());
                }
            }
        }

        Pair<String, String> ipv6Address = new Pair<>("IPv6 Addresses", ipv6.toString());
        connItem.add(connType);
        connItem.add(ipAddr);
        connItem.add(subnetMask);
        connItem.add(defGateway);
        connItem.add(dnsIP);
        connItem.add(ipv6Address);

        ListViewWifiInfoAdaptor connAdaptor = new ListViewWifiInfoAdaptor(this.getContext(), R.layout.layout_infoitem, (ArrayList<Pair<String, String>>) connItem);
        wifiList.setAdapter(connAdaptor);
    }

    private String getSubMask(){
        if(Build.VERSION.SDK_INT >= 23){
            Network network = mConnManager.getActiveNetwork();
            LinkProperties linkProp = mConnManager.getLinkProperties(network);
            List<LinkAddress> linkAddress = linkProp.getLinkAddresses();
            IPv4 ipv4 = null;
            StringBuilder ipv6 = new StringBuilder("N/A");
            int count = 0;
            for (LinkAddress l : linkAddress) {
                String[] ip4 = l.toString().split("\\.");
                String[] ip6 = l.toString().split(":");
                if (ip4.length > 1) {
                    try {
                        ipv4 = new IPv4(l.toString());
                    }catch (NumberFormatException e){
                        return "N/A";
                    }
                }
            }
            if(ipv4 != null){
                return ipv4.getNetmask();
            }
        }

        return "N/A";
    }

    private void displayCell(){
        networkType.setText("CELL CONNECTION");
        List<Pair<String, String>> connItem = new ArrayList<>();
        String connInfo = mConnManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).toString();
        String[] info = connInfo.split(",");
        Pair<String, String> connType = new Pair<>("Connection Type", "Cellular");
        String[] roam = info[6].split(":");
        String roaming  = roam[1].substring(0,roam[1].length() - 2);
        Pair<String, String> roamFlag = new Pair<>("Roaming", roaming);
        Pair<String, String> ipAddr = new Pair<>("IP Address", getMobileIPAddress());

        connItem.add(connType);
        connItem.add(roamFlag);
        connItem.add(ipAddr);

        ListViewWifiInfoAdaptor connAdaptor = new ListViewWifiInfoAdaptor(this.getContext(), R.layout.layout_infoitem, (ArrayList<Pair<String, String>>) connItem);
        wifiList.setAdapter(connAdaptor);
    }

    private String getMobileIPAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        return  addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }

    private void displayCell(Network network){
        networkType.setText("CELL CONNECTION");
        if(Build.VERSION.SDK_INT >= 23) {
            LinkProperties linkProp = mConnManager.getLinkProperties(network);
            List<Pair<String, String>> connItem = new ArrayList<>();
            Pair<String, String> connType = new Pair<>("Connection Type", "Cellular");
            List<LinkAddress> linkAddress = linkProp.getLinkAddresses();
            IPv4 ipv4 = null;
            StringBuilder ipv6 = new StringBuilder("N/A");
            int count = 0;
            for (LinkAddress l : linkAddress) {
                String[] ip4 = l.toString().split("\\.");
                String[] ip6 = l.toString().split(":");
                if (ip4.length > 1) {
                    ipv4 = new IPv4(l.toString());
                }
                if (ip6.length > 1 && count == 0) {
                    ipv6 = new StringBuilder("\n").append(l.toString());
                    count++;
                } else if (ipv6.length() > 1) {
                    ipv6.append("\n").append(l.toString());
                }
            }
            Pair<String, String> ipAddress = new Pair<>("IP address", "N/A");
            Pair<String, String> netmask = new Pair<>("Subnet mask", "N/A");
            if (ipv4 != null) {
                ipAddress = new Pair<>("IP address", ipv4.getIP());
                netmask = new Pair<>("Subnet mask", ipv4.getNetmask());
            }

            Pair<String, String> ipv6Address = new Pair<>("IPv6 Addresses", ipv6.toString());

            List<RouteInfo> routeInfo = linkProp.getRoutes();

            String defaultGateway = "N/A";
            for (RouteInfo r : routeInfo) {
                String[] route = r.toString().split("\\.");
                if (route.length > 1) {
                    String[] routeAddr = r.getGateway().toString().split("/");
                    if (routeAddr.length > 1) {
                        if (routeAddr[1] != "0.0.0.0") {
                            defaultGateway = routeAddr[1];
                        }
                    }
                }
            }
            Pair<String, String> gateway = new Pair<>("Default Gateway IP", defaultGateway);

            List<InetAddress> dnsAddress = linkProp.getDnsServers();
            StringBuilder ip4Str = new StringBuilder("N/A");
            StringBuilder ip6Str = new StringBuilder("N/A");
            int count4 = 0;
            int count6 = 0;
            for (InetAddress d : dnsAddress) {
                String[] ip4 = d.toString().split("\\.");
                String[] ip6 = d.toString().split(":");
                if (ip4.length > 1 && count4 == 0) {
                    String[] dnsAddr = d.toString().split("/");
                    if (dnsAddr.length > 1) {
                        ip4Str = new StringBuilder(dnsAddr[1]);
                    } else {
                        ip4Str = new StringBuilder(d.toString());
                    }
                    count4++;
                } else if (ip4.length > 1) {
                    String[] dnsAddr = d.toString().split("/");
                    if (dnsAddr.length > 1) {
                        ip4Str.append("\n").append(dnsAddr[1]);
                    } else {
                        ip4Str.append("\n").append(d.toString());
                    }
                }

                if (ip6.length > 1 && count6 == 0) {
                    String[] dnsAddr = d.toString().split("/");
                    if (dnsAddr.length > 1) {
                        ip6Str = new StringBuilder("\n").append(dnsAddr[1]);
                    } else {
                        ip6Str = new StringBuilder("\n").append(d.toString());
                    }
                    count++;
                } else if (ipv6.length() > 1) {
                    String[] dnsAddr = d.toString().split("/");
                    if (dnsAddr.length > 1) {
                        ip6Str.append("\n").append(dnsAddr[1]);
                    } else {
                        ip6Str.append("\n").append(d.toString());
                    }
                }
            }
            Pair<String, String> dnsIP4 = new Pair<>("DNS Server IP", ip4Str.toString());
            Pair<String, String> dnsIP6 = new Pair<>("DNS Server IPv6", ip6Str.toString());
            connItem.add(connType);
            connItem.add(ipAddress);
            connItem.add(netmask);
            connItem.add(gateway);
            connItem.add(dnsIP4);
            connItem.add(ipv6Address);
            connItem.add(dnsIP6);

            ListViewWifiInfoAdaptor connAdaptor = new ListViewWifiInfoAdaptor(this.getContext(), R.layout.layout_infoitem, (ArrayList<Pair<String, String>>) connItem);
            wifiList.setAdapter(connAdaptor);
        }
    }

    private void getNetworkState(){
        WifiInfo connInfo = mWifiManager.getConnectionInfo();
        DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
        List<Pair<String, String>> detailItem = new ArrayList<>();

        if(mWifiManager.isWifiEnabled()){
            String macStr = getMacAddr();
            Pair<String, String> enabled = new Pair<>("Enabled", "Yes");
            Pair<String, String> state, ssid, bssid, vendor, mac, speed, signal;
            if(connInfo.getNetworkId() != -1){
                state = new Pair<>("Connection State", "Connected");
                ssid = new Pair<>("SSID", connInfo.getSSID());
                bssid = new Pair<>("BSSID", connInfo.getBSSID());
                vendor = new Pair<>("Vendor", getVendor(formatMAC(macStr)));
                mac = new Pair<>("MAC", macStr);
                speed = new Pair<>("Speed", connInfo.getLinkSpeed() + " Mbps");
                signal = new Pair<>("Signal Strength", connInfo.getRssi() + " dBm");

                detailItem.add(enabled);
                detailItem.add(state);
                detailItem.add(ssid);
                detailItem.add(bssid);
                detailItem.add(vendor);
                detailItem.add(mac);
                detailItem.add(speed);
                detailItem.add(signal);

                //detailList.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 360));
                ListViewWifiInfoAdaptor detailAdaptor = new ListViewWifiInfoAdaptor(this.getContext(), R.layout.layout_infoitem, (ArrayList<Pair<String, String>>) detailItem);
                detailList.setAdapter(detailAdaptor);
            }else{
                state = new Pair<>("Connection State", "Disconnected");

                detailItem.add(enabled);
                detailItem.add(state);

                ListViewWifiInfoAdaptor detailAdaptor = new ListViewWifiInfoAdaptor(this.getContext(), R.layout.layout_infoitem, (ArrayList<Pair<String, String>>) detailItem);
                //detailList.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 90));
                detailList.setAdapter(detailAdaptor);
            }
        }else{
            Pair<String, String> enabled = new Pair<>("Enabled", "No");
            detailItem.add(enabled);
            //detailList.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            ListViewWifiInfoAdaptor detailAdaptor = new ListViewWifiInfoAdaptor(this.getContext(), R.layout.layout_infoitem, (ArrayList<Pair<String, String>>) detailItem);
            detailList.setAdapter(detailAdaptor);
        }

        ArrayList<Pair<String, String>> cellItem = new ArrayList<>();
        int cellDataState = mTeleManager.getDataState();
        if(cellDataState == TelephonyManager.DATA_CONNECTED){
            Pair<String, String> dataState = new Pair<>("Data State", "Connected");
            cellItem.add(dataState);
        }else{
            Pair<String, String> dataState = new Pair<>("Data State", "Disconnected");
            cellItem.add(dataState);
        }

        int simState = mTeleManager.getSimState();
        if(simState == TelephonyManager.SIM_STATE_READY){
            Pair<String, String> simCard = new Pair<> ("SIM State", "Ready");
            Pair<String, String> networkOperator = new Pair<>("Cell Network Operator", mTeleManager.getNetworkOperatorName());
            String netMCC = mTeleManager.getNetworkOperator() + " (" + mTeleManager.getNetworkCountryIso()+ ")";
            Pair<String, String> networkMCC = new Pair<>("Network MCC/MNC", netMCC);
            Pair<String, String> simOperator = new Pair<>("SIM Operator", mTeleManager.getSimOperatorName());
            String cellMCC = mTeleManager.getSimOperator() + " (" + mTeleManager.getSimCountryIso() + ")";
            Pair<String, String> simMCC = new Pair<>("SIM MCC/MNC", cellMCC);
            Pair<String, String> networkType = new Pair<>("Network Type", getCellNetworkType());

            cellItem.add(simCard);
            cellItem.add(networkOperator);
            cellItem.add(networkMCC);
            cellItem.add(simOperator);
            cellItem.add(simMCC);
            cellItem.add(networkType);

        }else{
            Pair<String, String> simCard = new Pair<> ("SIM State", "Not Ready");
            cellItem.add(simCard);
        }

        ListViewWifiInfoAdaptor cellAdaptor = new ListViewWifiInfoAdaptor(this.getContext(), R.layout.layout_infoitem, (ArrayList<Pair<String, String>>) cellItem);
        cellList.setAdapter(cellAdaptor);

    }

    private String getCellNetworkType(){
        int networkType = mTeleManager.getNetworkType();
        String netType = "N/A";
        if(networkType == TelephonyManager.NETWORK_TYPE_1xRTT){
            netType = "1xRTT";
        }else if(networkType == TelephonyManager.NETWORK_TYPE_CDMA) {
            netType = "CDMA";
        }else if(networkType == TelephonyManager.NETWORK_TYPE_EDGE){
            netType = "EDGE";
        }else if(networkType == TelephonyManager.NETWORK_TYPE_EHRPD){
            netType = "EHRPD";
        }else if(networkType == TelephonyManager.NETWORK_TYPE_EVDO_0){
            netType = "EVDO_0";
        }else if(networkType == TelephonyManager.NETWORK_TYPE_EVDO_A){
            netType = "EVDO_A";
        }else if(networkType == TelephonyManager.NETWORK_TYPE_EVDO_B){
            netType = "EVDO_B";
        }else if(networkType == TelephonyManager.NETWORK_TYPE_GPRS){
            netType = "GPRS";
        }else if(networkType == TelephonyManager.NETWORK_TYPE_GSM){
            netType = "GSM";
        }else if(networkType == TelephonyManager.NETWORK_TYPE_HSDPA){
            netType = "HSDPA";
        }else if(networkType == TelephonyManager.NETWORK_TYPE_HSPA){
            netType = "HSPA";
        }else if(networkType == TelephonyManager.NETWORK_TYPE_HSPAP){
            netType = "HSPAP";
        }else if(networkType == TelephonyManager.NETWORK_TYPE_HSUPA){
            netType = "HSUPA";
        }else if(networkType == TelephonyManager.NETWORK_TYPE_IDEN){
            netType = "IDEN";
        }else if(networkType == TelephonyManager.NETWORK_TYPE_IWLAN){
            netType = "IWLAN";
        }else if(networkType == TelephonyManager.NETWORK_TYPE_LTE){
            netType = "LTE";
        }else if(networkType == TelephonyManager.NETWORK_TYPE_TD_SCDMA){
            netType = "TD_SCDMA";
        }else if(networkType == TelephonyManager.NETWORK_TYPE_UMTS){
            netType = "UMTS";
        }else{
            netType = "N/A";
        }
        return netType;
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