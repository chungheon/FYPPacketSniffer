package fyp.com.packetsniffer.Fragments.NetworkUtils;

import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class PingShell extends Thread {
    private String host;
    private String libPath;
    private PingActivity fragment;
    private boolean exit = false;
    public PingShell(PingActivity fragment, String libPath, String host){
        this.fragment = fragment;
        this.libPath = libPath;
        this.host = host;
    }
    public void run(){
        ping(libPath, host);
    }
    public void ping(String libPath, String host){
        Process p = null;
        BufferedReader br = null;
        try {
            p = Runtime.getRuntime().exec("ping -c 10 " + host);
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            String pingResult = "";
            ArrayList<String> result = new ArrayList<>();
            while(!exit && (line = br.readLine()) != null){
                if(line.matches("PING.*")){
                    String[] startPing = line.split(" ");
                    if(startPing.length >= 7
                            && startPing[2].matches(".*\\d*\\.\\d*\\.\\d*\\.\\d*.*")){

                        fragment.printHostIP(startPing[2].replace("(","")
                                .replace(")", ""));

                    }else if(startPing[1].matches(".*\\d*\\.\\d*\\.\\d*\\.\\d*.*")){
                        fragment.printHostIP(startPing[1]);
                    }else{
                        fragment.printHostIP("-");
                    }
                }else if(line.matches(".*from.*\\d*\\.\\d*\\.\\d*\\.\\d*.*")){
                    result.add(line);
                    fragment.updateDetails(result);
                }else if(line.matches(".*transmitted.*received.*")){
                    pingResult += line;
                }else if(line.matches("rtt.*")){
                    pingResult += "\n" + line;
                    fragment.printResult(pingResult);
                }

                if(isInterrupted()){
                    break;
                }
            }

            p.waitFor();
            fragment.stopProgressBar();
            if(result.isEmpty()){
                fragment.printToast("Unable to ping " + host);
            }
        } catch (IOException e) {
        } catch (InterruptedException e) {
            if(br != null){
                try{
                    br.close();
                } catch (IOException ex) {}
            }
        }finally {
            if(p != null){
                p.destroy();
            }
        }
    }

    public void stopRun(){
        interrupt();
        exit = true;
    }
}
