package fyp.com.packetsniffer.Fragments.DevicesConnected;

/*Device Information Class
This class is used to represent the information the devices in a network

This class contains methods to get and set information to each object.
Implements comparable interface to sort an array of device information.
 */

public class DeviceInformation implements Comparable<DeviceInformation>{

    private String hostName; //The host name of the Device
    private String ipAddrs; //The local ip address of the Device
    private String macAddrs; //The mac address of the Device
    private String macVendor; //The vendor of the Device derived from the mac address


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

    //Override parameters for class object to be compared
    //The compared using the ip address numeral value
    @Override
    public int compareTo(DeviceInformation deviceInfo){
        String[] userInfo = this.ipAddrs.split("\\.");
        String[] devInfo = deviceInfo.getIpAddrs().split("\\.");
        try{

            if(userInfo.length == devInfo.length){
                for(int i = 0; i < devInfo.length; i++){
                    int userVal = Integer.parseInt(userInfo[i]);
                    int devVal = Integer.parseInt(devInfo[i]);
                    if(userVal != devVal){
                        return userVal - devVal;
                    }
                }
            }
        }catch(NumberFormatException e){
            return 0;
        }


        return 0;
    }
}
