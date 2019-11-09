package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import fyp.com.packetsniffer.Fragments.CmdExecInterface;
import fyp.com.packetsniffer.Fragments.CmdExecNormal;

public class CapturePacketThread extends CmdExecNormal {
    private final String TAG = "CapturePacket";

    private BufferedReader br;
    private String filePath;

    public CapturePacketThread(CmdExecInterface fragment, ArrayList<String> cmds, String filePath) {
        super(fragment, cmds);
        this.filePath = filePath;
        fileReader = new ReadCaptureOutput();
        this.cmds.add("exit");
    }

    private class ReadCaptureOutput extends ReadOutput{
        private boolean stopRunning;

        @Override
        public void run() {
            br = null;
            long numOfPackets = 0;
            try {
                br = new BufferedReader(new InputStreamReader(this.input));
                String line = null;
                int updateCounter = 0;
                while(true){
                    if(br.ready()){
                        line = br.readLine();
                    }
                    if(line != null){
                        if(line.matches("\\d*-\\d*-\\d* \\d*:\\d*:\\d*.*")) {
                            numOfPackets++;
                            updateCounter++;
                        }
                        if(updateCounter >= 1){
                            updateResult(null, numOfPackets);
                            updateCounter = 0;
                        }
                        line = null;
                    }
                    if(interrupted()){
                        break;
                    }
                }
            } catch (IOException e) { }finally {
                if(br != null){
                    try{
                        br.close();
                    } catch (IOException ex) {
                        Log.e(TAG, "Error closing stream");
                    }
                }
            }
        }

        public void stopRun(){
            interrupt();
        }
    }

    @Override
    public void stopRun() {
        /*if(br != null){
            try {
                br.close();
                br = null;
            } catch (IOException e) { }
        }*/
        fileReader.stopRun();
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

                            wait(10);
                            count++;
                        }catch (InterruptedException e){
                            break;
                        }
                    }
                    if(count > 50){
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
            Log.e(TAG, "Interrupted");
        }finally {
            if(kill != null){
                kill.destroy();
            }
        }
    }

}
