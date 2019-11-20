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
    private String killPath;

    public CapturePacketThread(CmdExecInterface fragment, ArrayList<String> cmds, String filePath, String killPath) {
        super(fragment, cmds);
        this.filePath = filePath;
        fileReader = new ReadCaptureOutput();
        this.killPath = killPath;
        this.cmds.add("exit");
    }

    private class ReadCaptureOutput extends ReadOutput{

        @Override
        public void run() {
            br = null;
            long numOfPackets = 0;
            String result = "";
            String keyAddr = "";
            try {
                br = new BufferedReader(new InputStreamReader(this.input));
                String line = null;
                int updateCounter = 0;
                while(true){
                    if(br.ready() && br != null){
                        line = br.readLine();
                    }
                    if(line != null){

                        if(line.matches("\\d*-\\d*-\\d* \\d*:\\d*:\\d*.*")) {
                            numOfPackets++;
                            updateCounter++;
                        }else{
                            result += line + "|";
                        }
                        if(updateCounter >= 1){
                            boolean update = false;
                            String[] data = result.split(" ");
                            for (int i = 0; i < data.length; i++) {
                                /*if (data[i].matches(".*:.*:.*:.*:.*:.*")) {
                                    if (i != data.length - 1) {
                                        if (data[i + 1].equals(">")) {
                                            String addr[] = data[i].split(":");
                                            String key = "\n" + addr[0].replace(" ", "");
                                            for (int j = 1; j < addr.length; j++) {

                                                if(j == 3 || j == 6){
                                                    key += "\n:" + addr[j];
                                                }else if( j == addr.length - 1){
                                                    String[] temp = addr[j].split("\\.");
                                                    key += ":" + temp[0];
                                                }else{
                                                    key += ":" + addr[j];
                                                }
                                            }
                                            keyAddr = key;
                                            update = true;
                                            break;
                                        }
                                    }
                                }*/

                                if (data[i].matches(".*\\d*\\.\\d*\\.\\d*\\.\\d*.*")) {
                                    if (i != data.length - 1) {
                                        if (data[i + 1].equals(">")) {
                                            String addr[] = data[i].split("\\.");
                                            String key = addr[0].replace(" ", "");
                                            for (int j = 1; j < addr.length - 1; j++) {
                                                key += "." + addr[j];
                                            }
                                            keyAddr = key;
                                            update = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            if(update){
                                updateResult(keyAddr, numOfPackets);
                            }
                            result = line;
                            updateCounter = 0;
                        }
                        line = null;
                    }
                    if(isInterrupted()){
                        break;
                    }
                }
                mFragment.cmdDone();
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
        fileReader.stopRun();
        if(br != null){
            try{
                br.close();
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
                    if(count > 500){
                        break;
                    }
                }
            } catch (IOException e) { }
            killOut.writeBytes("exit\n");
            killOut.flush();

            killOut.close();
        }catch (IOException e) {
            Log.e(TAG, "Error with stream");
        }finally {
            if(kill != null){
                kill.destroy();
            }
        }
    }

}
