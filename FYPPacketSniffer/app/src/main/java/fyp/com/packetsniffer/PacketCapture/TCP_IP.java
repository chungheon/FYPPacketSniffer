package fyp.com.packetsniffer.PacketCapture;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.nio.ByteBuffer;

public class TCP_IP {

    private ByteBuffer packet;
    private String hostname;
    private String destIP;
    private String sourceIP;
    private int version;
    private int protocol;
    private int port;
    private String TAG = "T";
    public static final String debugMsg = "MSG";
    private Context mC;


    public TCP_IP(ByteBuffer pack, String a, Context c) {
        this.packet = pack;
        TAG = a;
        mC = c;
    }

    public void debug() {
        Log.d(TAG, "STARTED THREAD");
        sendMsg("Started");
        packet.flip();
        int buffer = packet.get();
        int headerlength;
        int temp;
        version = buffer >> 4;
        headerlength = buffer & 0x0F;
        headerlength *= 4;
        Log.d(TAG,"IP Version:"+version);
        sendMsg("IP Version:"+version);
        Log.d(TAG,"Header Length:"+headerlength);
        sendMsg("Header Length:"+headerlength);
        String status = "";
        status += "Header Length:"+headerlength;

        buffer = packet.get();      //DSCP + EN
        System.out.println( "1:"+buffer);
        buffer = packet.getChar();  //Total Length

        Log.d(TAG, "Total Length:"+buffer);
        sendMsg("Total Length:"+buffer);

        buffer = packet.getChar();  //Identification
        buffer = packet.getChar();  //Flags + Fragment Offset
        buffer = packet.get();      //Time to Live
        buffer = packet.get();      //Protocol

        protocol = buffer;
        Log.d(TAG, "Protocol:"+buffer);
        sendMsg("Protocol:"+buffer);

        status += "  Protocol:"+buffer;

        buffer = packet.getChar();  //Header checksum


        byte buff = (byte)buffer;

        sourceIP  = "";
        buff = packet.get();  //Source IP 1st Octet
        temp = ((int) buff) & 0xFF;
        sourceIP += temp;
        sourceIP += ".";

        buff = packet.get();  //Source IP 2nd Octet
        temp = ((int) buff) & 0xFF;
        sourceIP += temp;
        sourceIP += ".";

        buff = packet.get();  //Source IP 3rd Octet
        temp = ((int) buff) & 0xFF;
        sourceIP += temp;
        sourceIP += ".";

        buff = packet.get();  //Source IP 4th Octet
        temp = ((int) buff) & 0xFF;
        sourceIP += temp;

        Log.d(TAG, "Source IP:"+sourceIP);
        sendMsg("Source IP:"+sourceIP);

        status += "   Source IP:"+sourceIP;


        destIP  = "";


        buff = packet.get();  //Destination IP 1st Octet
        temp = ((int) buff) & 0xFF;
        destIP += temp;
        destIP += ".";

        buff = packet.get();  //Destination IP 2nd Octet
        temp = ((int) buff) & 0xFF;
        destIP += temp;
        destIP += ".";

        buff = packet.get();  //Destination IP 3rd Octet
        temp = ((int) buff) & 0xFF;
        destIP += temp;
        destIP += ".";

        buff = packet.get();  //Destination IP 4th Octet
        temp = ((int) buff) & 0xFF;
        destIP += temp;

        Log.d(TAG, "Destination IP:" + destIP);
        status += "   Destination IP:"+destIP;
        sendMsg("Destination IP:" + destIP);


    }

    private void sendMsg(String msg){
        Intent intent = new Intent(debugMsg);
        intent.putExtra("MSG", msg);
        mC.sendBroadcast(intent);

    }

    public String getDestination() {
        return destIP;
    }

    public int getProtocol() {
        return protocol;
    }

    public int getPort() {
        return port;
    }

    public String getHostname() {
        return hostname;
    }

    public int getIPversion() { return version; }

}
