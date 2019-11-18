package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import fyp.com.packetsniffer.Fragments.CmdExecInterface;
import fyp.com.packetsniffer.Fragments.CmdExecNormal;

public class PacketViewThread extends CmdExecNormal {
    private final String TAG = "PacketView";
    private BufferedReader br;
    private int lineAdded = 0;
    private int pageNum = 1;
    private long numOfPackets;
    private String page;
    private int mode;
    private PacketAnalysisFragment fragment;

    public PacketViewThread(CmdExecInterface fragment, ArrayList<String> cmds, int mode) {
        super(fragment, cmds);
        fileReader = new ReadCaptureOutput();
        this.mode = mode;
    }

    private class ReadCaptureOutput extends ReadOutput{
        private boolean stopRunning;

        @Override
        public synchronized void run() {
            br = null;
            numOfPackets = 0;
            boolean first = true;
            page = "Page 1|";
            int waitCounter = 0;
            try {
                br = new BufferedReader(new InputStreamReader(this.input));
                String line = null;

                while(true){
                    if(br == null){
                        break;
                    }

                    if(br.ready()){
                        line = br.readLine();
                        if(line == null){
                            break;
                        }
                    }
                    if(line != null){
                        if(first) {
                            page += "\n" + line;
                            if(line.matches("\\d*-\\d*-\\d* \\d*:\\d*:\\d*.*")){
                                numOfPackets++;
                            }
                            first = false;
                        }else {
                            switch (mode){
                                case 0: normalFilter(line);
                                        break;
                                case 1: webFilter(line);
                                        break;
                                case 2: lineFilter(line);
                                        break;
                                default: normalFilter(line);
                                         break;
                            }
                        }
                        lineAdded++;
                        line = null;
                        waitCounter = 0;
                    }else{
                        try{
                            this.wait(10);
                        }catch (InterruptedException e){
                            break;
                        }
                        waitCounter ++;
                    }
                    if(waitCounter > 100){
                        break;
                    }
                }
                if(lineAdded > 0){
                    updateResult(page, numOfPackets);
                }
                if(br != null){
                    br.close();
                }
            } catch (IOException e) {
                if(br != null){
                    try{
                        br.close();
                    } catch (IOException ex) {
                        Log.e(TAG, "Error closing stream");
                    }
                }
            }
            cmdDone();
        }
    }

    private void normalFilter(String line){
        if(line.matches("\\d*-\\d*-\\d* \\d*:\\d*:\\d*.*")){
            if(page.length() >= 8000){
                pageNum++;
                updateResult(page, numOfPackets);
                /*if(numOfPackets > 270000){
                    this.stopRun();
                    printToast("Stopping Run... Unable to read more than " + numOfPackets + " packets");
                    return;
                }*/
                lineAdded = 0;
                page = "Page " + pageNum + "|\n" + line;
            }else{
                page += "|\n" + line;
            }
            numOfPackets++;
        }else{
            page += "|" + line;
        }
    }

    private void webFilter(String line){
        if(line.matches("\\d*-\\d*-\\d* \\d*:\\d*:\\d*.*")){
            if(page.length() >= 8000){
                pageNum++;
                updateResult(page, numOfPackets);
                lineAdded = 0;
                page = "Page " + pageNum + "\n" + line;
            }else{
                page += "|\n" + line;
            }
            numOfPackets++;
        }else if(line.matches(".*AAAAAAAAA.*")){
            if(page.length() >= 8000){
                pageNum++;
                updateResult(page, numOfPackets);
                lineAdded = 0;
                page = "Page " + pageNum + "\n" + line;
            }else{
                page += "|\n" + line;
            }
        }else{
            page += "|" + line;
        }
    }

    private void lineFilter(String line){
        if (lineAdded >= 300) {
            pageNum++;
            updateResult(page, 0);
            lineAdded = 0;
            page = "Page " + pageNum + "\n" + line;
        } else {
            page += "\n\n" + line;
        }
    }

    @Override
    public void stopRun() {
        if(br != null){
            try {
                br.close();
                br = null;
            } catch (IOException e) { }
        }

        Process kill = null;
        try {
            kill = Runtime.getRuntime().exec("su");
            DataOutputStream killOut = new DataOutputStream(kill.getOutputStream());
            killOut.writeBytes("ps\n");
            killOut.flush();
            BufferedReader reader = new BufferedReader(new InputStreamReader(kill.getInputStream()));
            String line = null;
            try{
                int count = 0;
                while(true){
                    if(reader.ready()){
                        line = reader.readLine();
                    }
                    if(line != null){
                        if(line.matches(".*tcpdump.*")){
                            String killStr[] = line.substring(10).split(" ");
                            killOut.writeBytes("kill -2 " + killStr[0] + "\n");
                            killOut.flush();
                        }
                        line = null;
                        count = 0;
                    }
                    synchronized (this){
                        try{

                            wait(1);
                            count++;
                        }catch (InterruptedException e){
                            break;
                        }
                    }
                    if(count > 1000){
                        break;
                    }
                }
            } catch (IOException e) { }

            killOut.writeBytes("exit\n");
            killOut.flush();

            kill.waitFor();
        }catch (IOException e) {
            Log.e(TAG, "Error with stream");
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted kill process");
        }finally{
            if(kill != null){
                kill.destroy();
            }
        }
    }
}
