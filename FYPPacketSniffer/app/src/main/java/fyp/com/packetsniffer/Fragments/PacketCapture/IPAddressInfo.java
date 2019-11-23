package fyp.com.packetsniffer.Fragments.PacketCapture;

import java.util.ArrayList;

public class IPAddressInfo {
    private ArrayList<String> packetSent;
    private long numOfPackets;
    private String ipAddr;

    public IPAddressInfo(String ipAddr){
        this.ipAddr = ipAddr;
        packetSent = new ArrayList<>();
        numOfPackets = 0;
    }
}
