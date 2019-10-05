package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CmdExec {
    private final String TAG = "CmdExecution";
    private final int PCAP_NORMAL_STREAM = 1;
    private final int PCAP_ERROR_STREAM = 2;

    public String executeCMD(String[] command, int mode){
        Process p;

        StringBuilder output = new StringBuilder("");
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = null;
            String line = "";
            if (mode == PCAP_NORMAL_STREAM) {
                reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    output.append(line + "\n\n");
                }
            } else if (mode == PCAP_ERROR_STREAM) {
                reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    output.append(line + "\n\n");
                }
            }
        }catch (IOException e){
            Log.e(TAG, e.getMessage());
            if(e.getMessage().split(":")[1].equals(" error=13, Permission denied")){
                output.append("Permission Denied: User may not have root access");
            }else{
                output.append("No output available");
            }

        }catch(Exception e){
            Log.e(TAG, e.getMessage());
            output.append("No output available");
        }

        if(output.toString() == null || output.toString().isEmpty()){
            output = new StringBuilder("No output from command");
        }

        return output.toString();
    }
}
