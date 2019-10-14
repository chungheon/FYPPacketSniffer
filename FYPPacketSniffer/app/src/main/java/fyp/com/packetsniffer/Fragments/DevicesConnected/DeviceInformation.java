package fyp.com.packetsniffer.Fragments.DevicesConnected;

import android.util.Log;

public class DeviceInformation implements Comparable<DeviceInformation>{

    private String hostName;
    private String ipAddrs;
    private String macAddrs;
    private String macVendor;


    public void setHostName(String hostName){
        this.hostName = hostName;
    }

    public void setIpAddrs(String ipAddrs){
        this.ipAddrs = ipAddrs;
    }

    public void setMacAddrs(String macAddrs){
        this.macAddrs = macAddrs;
    }

    public void setMacVendor(String macVendor){
        this.macVendor = macVendor;
    }

    public String getHostName(){
        return this.hostName;
    }

    public String getIpAddrs(){
        return this.ipAddrs;
    }

    public String getMacAddrs(){
        return this.macAddrs;
    }

    public String getMacVendor(){
        return this.macVendor;
    }

    @Override
    public int compareTo(DeviceInformation deviceInfo){
        String[] userInfo = this.ipAddrs.split("\\.");
        String[] devInfo = deviceInfo.getIpAddrs().split("\\.");
        int userVal = Integer.parseInt(userInfo[3]);
        int devVal = Integer.parseInt(devInfo[3]);

        return userVal - devVal;
    }
}
