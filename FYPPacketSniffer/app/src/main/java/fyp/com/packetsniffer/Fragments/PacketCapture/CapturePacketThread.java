package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CapturePacketThread extends CmdExecNormal {
    private final String TAG = "CapturePacket";

    private BufferedReader br;
    private String filePath;

    public CapturePacketThread(PacketCaptureInterface fragment, ArrayList<String> cmds, String filePath) {
        super(fragment, cmds);
        this.filePath = filePath;
        fileReader = new ReadCaptureOutput();
    }

    private class ReadCaptureOutput extends ReadOutput{
        private boolean stopRunning;

        @Override
        public synchronized void run() {
            br = null;
            long numOfPackets = 0;
            try {
                br = new BufferedReader(new InputStreamReader(this.input));
                String line = null;
                int updateCounter = 0;
                while(true){
                    if(br == null){
                        break;
                    }

                    if(br.ready()){
                        line = br.readLine();
                    }
                    if(line != null){
                        Log.d(TAG, line);
                        Log.d(TAG, numOfPackets + "");
                        if(line.matches(".*\\d*:\\d*:\\d*.*")) {
                            numOfPackets++;
                            updateCounter++;
                        }
                        if(updateCounter > 10){
                            updateResult(null, numOfPackets);
                            updateCounter = 0;
                        }
                    }
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
            killOut.writeBytes("tcpdump -r " + filePath + " | wc -l\n");
            killOut.flush();
            killOut.writeBytes("exit\n");
            killOut.flush();


            BufferedReader reader = new BufferedReader(new InputStreamReader(kill.getInputStream()));
            String line;
            try{
                while((line = reader.readLine()) != null){
                    Log.d(TAG, line);
                    if(line.matches("\\d*")){
                        try{
                            long packets = Long.parseLong(line);
                            updateResult(null, packets);
                            break;
                        }catch (NumberFormatException e){
                            break;
                        }
                    }
                }
            } catch (IOException e) { }
            kill.waitFor();
        }catch (IOException e) {
            Log.e(TAG, "Error with stream");
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted");
        }
    }

}
