package fyp.com.packetsniffer.Fragments.UpdateVersion;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import fyp.com.packetsniffer.Fragments.CmdExecInterface;
import fyp.com.packetsniffer.Fragments.CmdExecMix;

public class UpdateVersionThread extends CmdExecMix {

    private final String TAG = "UpdateVersion";

    private BufferedReader br;

    public UpdateVersionThread(CmdExecInterface fragment, ArrayList<String> cmds) {
        super(fragment, cmds);
        fileReader = new ReadUpdateOutput();
    }

    private class ReadUpdateOutput extends ReadOutput{
        @Override
        public synchronized void run() {
            br = null;
            String output = "";
            ArrayList<String> result = new ArrayList<>();
            try {
                br = new BufferedReader(new InputStreamReader(this.input));
                String line = null;
                while((line = br.readLine()) != null){
                    output += line + "\n";
                }
                result.add(output);
                updateResult(result, 0);
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
        }
    }
}
