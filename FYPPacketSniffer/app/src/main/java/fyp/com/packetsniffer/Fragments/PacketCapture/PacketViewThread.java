package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class PacketViewThread extends CmdExecNormal {
    private final String TAG = "PacketView";
    private BufferedReader br;
    private ArrayList<String> result;
    private int lineAdded = 0;
    private int pageNum = 1;
    private long numOfPackets;
    private String page;
    private int mode;

    public PacketViewThread(PacketCaptureInterface fragment, ArrayList<String> cmds, int mode) {
        super(fragment, cmds);
        fileReader = new ReadCaptureOutput();
        this.mode = mode;
    }

    private class ReadCaptureOutput extends ReadOutput{
        private boolean stopRunning;

        @Override
        public synchronized void run() {
            br = null;
            numOfPackets = 1;
            boolean first = true;
            page = "Page 1";
            int waitCounter = 0;
            result = new ArrayList<>();
            try {
                br = new BufferedReader(new InputStreamReader(this.input));
                String line = null;

                while(true){
                    if(br == null){
                        break;
                    }

                    if(br.ready()){
                        line = br.readLine();
                    }
                    if(line != null){
                        if(first) {
                            page += "\n" + line;
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
                        waitCounter = 0;
                    }else{
                        try{
                            this.wait(100);
                        }catch (InterruptedException e){
                            break;
                        }
                        waitCounter++;
                    }
                    if(waitCounter > 3){
                        break;
                    }
                }
                if(lineAdded > 0){
                    result.add(page);
                    updateResult(result, numOfPackets);
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
            if( lineAdded >= 300){
                pageNum++;
                lineAdded = 0;
                result.add(page);
                page = "Page " + pageNum + "\n" + line;
                updateResult(result, numOfPackets);
            }else{
                page += "\n\n" + line;
            }
            numOfPackets++;
        }else{
            page += "\n" + line;
        }
    }

    private void webFilter(String line){
        if(line.matches("\\d*-\\d*-\\d* \\d*:\\d*:\\d*.*")){
            if( lineAdded >= 300){
                pageNum++;
                lineAdded = 0;
                result.add(page);
                page = "Page " + pageNum + "\n" + line;
                updateResult(result, numOfPackets);
            }else{
                page += "\n\n" + line;
            }
            numOfPackets++;
        }else if(line.matches(".*AAAAAAAAA.*")){
            if( lineAdded >= 300){
                pageNum++;
                lineAdded = 0;
                result.add(page);
                page = "Page " + pageNum + "\n" + line;
                updateResult(result, numOfPackets);
            }else{
                page += "\n\n" + line;
            }
        }else{
            page += "\n" + line;
        }
    }

    private void lineFilter(String line){
        if (lineAdded >= 300) {
            pageNum++;
            lineAdded = 0;
            result.add(page);
            page = "Page " + pageNum + "\n" + line;
            updateResult(result, 0);
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
            killOut.writeBytes("killall -q 2 tcpdump\n");
            killOut.flush();
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
