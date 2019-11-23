package fyp.com.packetsniffer.Fragments;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class CmdExecError extends CmdExecNormal {
    private final String TAG = "CmdExecError";
    public CmdExecError(CmdExecInterface fragment, ArrayList<String> cmds) {
        super(fragment, cmds);
    }

    @Override
    public void run() {
        DataOutputStream outputStream = null;
        Thread readOutput = null;
        try {

            p = Runtime.getRuntime().exec("su");
            outputStream = new DataOutputStream(p.getOutputStream());
            response = p.getErrorStream();
            fileReader.setInputStream(response);
            readOutput = new Thread(fileReader);
            readOutput.start();
            for(String str: cmds){
                outputStream.writeBytes(str + "\n");
                outputStream.flush();
            }

            try {
                p.waitFor();
            } catch (InterruptedException e) {
                Log.d(TAG, "Thread Interrupted...");
            }
            join();
            outputStream.close();
            cmdDone();
        } catch (IOException e){
            if(outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException ex) { }
            }
        } catch (InterruptedException e) {
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
