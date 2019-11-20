package fyp.com.packetsniffer.Fragments;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class CmdExecNormal extends Thread {
    private final String TAG = "CmdExecution";
    protected ArrayList<String> cmds;
    protected CmdExecInterface mFragment;
    protected Process p;
    protected InputStream response;
    protected DataOutputStream outputStream;
    protected ReadOutput fileReader;


    public CmdExecNormal(CmdExecInterface fragment, ArrayList<String> cmds){
        this.mFragment = fragment;
        this.cmds = cmds;
    }

    protected class ReadOutput implements Runnable{
        protected InputStream input;
        public void setInputStream(InputStream input) {
            this.input = input;
        }

        public void run() { }

        public void stopRun(){ }
    }
    public void run() {
        outputStream = null;
        Thread readOutput = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("su");
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
                p.waitFor();
            } catch (InterruptedException e) {
                Log.d(TAG, "Thread Interrupted...");
                fileReader.stopRun();
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

    public void stopRun(){   }

    protected void updateResult(String result, long numOfPackets) {
        mFragment.printResult(result, numOfPackets);
    }

    protected void printToast(String message){
        mFragment.printToast(message);
    }

    protected void cmdDone(){
        mFragment.cmdDone();
    }
}
