package fyp.com.packetsniffer.Fragments;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class CmdExecMix extends CmdExecNormal {
    private final String TAG = "CmdExecMix";
    public CmdExecMix(CmdExecInterface fragment, ArrayList<String> cmds) {
        super(fragment, cmds);
    }

    @Override
    public void run() {
        DataOutputStream outputStream = null;
        Thread readOutput = null;
        try {
            Log.d(TAG, mode);
            ProcessBuilder processBuilder = new ProcessBuilder("su");
            processBuilder.redirectErrorStream(true);
            p = processBuilder.start();
            outputStream = new DataOutputStream(p.getOutputStream());
            response = p.getInputStream();
            fileReader.setInputStream(response);

            readOutput = new Thread(fileReader);
            readOutput.start();

            for(String str: cmds){
                outputStream.writeBytes(str + "\n");
                outputStream.flush();
            }

            try {
                int res = p.waitFor();
            } catch (InterruptedException e) {
                Log.d(TAG, "Thread Interrupted...");
            }

            Log.d(TAG, "Cmd Done");
        } catch (IOException e){
            if(outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException ex) { }
            }
        } finally {
            if(p != null){
                p.destroy();
            }
        }
    }
}