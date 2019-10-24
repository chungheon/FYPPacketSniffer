package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CapturePacketThread extends CmdExec {
    private final String TAG = "CapturePacket";

    public CapturePacketThread(PacketCaptureInterface fragment, ArrayList<String> cmds) {
        super(fragment, cmds);
        fileReader = new ReadCaptureOutput();
    }

    private class ReadCaptureOutput extends ReadOutput{
        private boolean stopRunning;

        @Override
        public synchronized void run() {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(this.input));
                String line = null;
                long numOfPackets = 1;
                int updateCounter = 0;
                while(true){
                    if(br.ready()){
                        line = br.readLine();
                    }
                    if(line != null){
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
            cmdDone();
        }
    }


}
