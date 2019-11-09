package fyp.com.packetsniffer.Fragments.NetworkUtils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TraceRouteShell extends Thread{
    private final String TAG = "TraceRoute";
    private TraceActivity traceActivity;
    private String hostName;

    public TraceRouteShell(String hostName, TraceActivity traceActivity){
        this.hostName = hostName;
        this.traceActivity = traceActivity;
    }

    public void run(){
        Process p = null;
        try{
            p = Runtime.getRuntime().exec("traceroute -q 1 " + hostName);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            int count = 0;
            while(true){
                if(br == null){
                    break;
                }

                if(br.ready()){
                    line = br.readLine();
                }

                if(line != null){
                    line = " " + line;
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
                    line = null;
                    count = 0;
                }
                synchronized (this){
                    try{
                        wait(10);
                        count++;
                    }catch (InterruptedException e){
                        break;
                    }
                }
                if(count >= 300){
                    break;
                }
            }

            p.waitFor();
            traceActivity.refreshList();
            traceActivity.stopProgressBar();

        } catch (IOException e) {

        } catch (InterruptedException e) {

        } finally {
            if(p != null){
                p.destroy();
            }
        }
    }
}
