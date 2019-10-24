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
            ArrayList<String> result = new ArrayList<>();
            int pageNum = 1;
            long numOfPackets = 1;
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(this.input));
                String line = null;
                String page = "Page " + pageNum + "\n";
                int count = 0;
                int pagesAdded = 0;
                boolean first = true;
                while(true){
                    if(br.ready()){
                        line = br.readLine();
                    }
                    if(line != null){
                        if(first){
                            page += line;
                            first = false;
                        }else if(line.matches("\\d*-\\d*-\\d* \\d*:\\d*:\\d*.*")) {
                            if (count > 300) {
                                count = 0;
                                page += "\n";
                                result.add(page);
                                pagesAdded++;
                                pageNum++;
                                page = "Page " + pageNum + "\n" + line;
                            }else{
                                page += "\n\n" + line;
                            }
                            numOfPackets++;
                        }else{
                            page += "\n" + line;
                        }

                        if(pagesAdded >= 1){
                            updateResult(result, numOfPackets);
                            pagesAdded = 0;
                        }
                        count++;
                    }
                    if (isInterrupted() || stopRunning) {
                        Log.d(TAG, "Reader stopping");
                        break;
                    }
                }
                if(count > 0){
                    result.add(page);
                    pagesAdded++;
                }
                if(pagesAdded > 0){
                    updateResult(result, numOfPackets);
                }
                br.close();
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
