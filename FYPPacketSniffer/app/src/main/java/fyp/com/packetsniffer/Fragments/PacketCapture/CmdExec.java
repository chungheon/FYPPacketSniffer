package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CmdExec {
    private final String TAG = "CmdExecution";

    public String executeCMD(String[] command){
        Process p;

        StringBuilder output = new StringBuilder("");
        try{
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n\n");
            }
        }catch(Exception e){
            Log.e(TAG, e.getMessage());
            output.append("Error Occured");
        }

        if(output.toString() == null || output.toString().isEmpty()){
            output = new StringBuilder("No output from command");
        }

        return output.toString();
    }
}
