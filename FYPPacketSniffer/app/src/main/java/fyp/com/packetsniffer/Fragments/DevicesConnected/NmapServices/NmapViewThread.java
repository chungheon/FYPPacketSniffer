package fyp.com.packetsniffer.Fragments.DevicesConnected.NmapServices;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class NmapViewThread implements Runnable{
    private Thread thread;
    private NmapMainFragment fragment;
    private String cmd;
    public NmapViewThread(NmapMainFragment fragment, String cmd){
        this.fragment = fragment;
        this.cmd = cmd;
        thread = new Thread(this);
        thread.start();
    }
    @Override
    public void run() {
        Process p = null;
        String result = "";
        try {
            p = Runtime.getRuntime().exec(cmd);
            InputStream response = p.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(response));
            String line;
            String ipAddrs = "";
            while((line = br.readLine()) != null){
                Log.d("Line", line);
                if(line.matches(".*Nmap scan report for.*")){
                    String[] nameInfo = line.split("for");
                    if(nameInfo.length >= 2){
                        ipAddrs = nameInfo[2].replace(" ", "");
                    }
                }

                if(line.matches(".*Nmap scan report for.*")){
                    String[] nameInfo = line.split("for");
                    if(nameInfo.length >= 2){
                        ipAddrs = nameInfo[2].replace(" ", "");
                    }
                }
            }

            br.close();
        } catch (IOException e) {
        }
        finally {
            if(p != null){
                p.destroy();
            }
        }
    }
}
