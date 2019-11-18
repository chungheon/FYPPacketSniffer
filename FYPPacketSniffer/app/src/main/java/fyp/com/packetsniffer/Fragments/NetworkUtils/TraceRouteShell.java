package fyp.com.packetsniffer.Fragments.NetworkUtils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TraceRouteShell extends Thread{
    private final String TAG = "TraceRoute";
    private String tracePath;
    private TraceActivity traceActivity;
    private String hostName;

    public TraceRouteShell(String hostName, TraceActivity traceActivity, String tracePath){
        this.hostName = hostName;
        this.traceActivity = traceActivity;
        this.tracePath = tracePath;
    }

    public void run(){
        Process p = null;
        BufferedReader br = null;
        try{
            p = Runtime.getRuntime().exec( tracePath + " -q 1 " + hostName);
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while((line = br.readLine()) != null){
                line = " " + line;
                Log.d("line", line);
                if(line.matches("\\s+\\d+.*")){
                    String[] trace = line.split("\\s+");
                    TracerouteContainer traceRoute = null;
                    if(trace.length >= 2 && trace.length <= 3){
                        traceRoute = new TracerouteContainer(trace[2], "", 0);
                    }else if(trace.length >= 4){
                        String ipAddr = trace[3].replace("(", "").replace(")", "");
                        float ms = 0;
                        try{
                            ms = Float.parseFloat(trace[4]);
                        }catch (NumberFormatException e) { }
                        traceRoute = new TracerouteContainer(trace[2], ipAddr, ms);
                    }
                    if(traceRoute != null){
                        traceActivity.printResult(traceRoute);
                    }
                }
            }

            p.waitFor();
            traceActivity.refreshList();
            traceActivity.stopProgressBar();

        } catch (IOException e) {

        } catch (InterruptedException e) {
            if(br != null){
                try{
                    br.close();
                } catch (IOException ex) {}
            }
        } finally {
            if(p != null){
                p.destroy();
            }
        }
    }
}
