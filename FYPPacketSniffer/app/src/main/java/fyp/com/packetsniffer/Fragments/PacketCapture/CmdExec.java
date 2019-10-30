package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.ArrayList;

public class CmdExec extends Thread {
    private final String TAG = "CmdExecution";
    protected ArrayList<String> cmds;
    protected PacketCaptureInterface mFragment;
    protected Process p;
    protected InputStream response;
    protected DataOutputStream outputStream;
    protected ReadOutput fileReader;


    public CmdExec(PacketCaptureInterface fragment, ArrayList<String> cmds){
        this.mFragment = fragment;
        this.cmds = cmds;
    }

    protected class ReadOutput implements Runnable{
        protected InputStream input;
        protected void setInputStream(InputStream input) {
            this.input = input;
        }


        public synchronized void run() { }
    }
    public void run() {
        outputStream = null;
        Thread readOutput = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("su");
            //processBuilder.redirectErrorStream(true);
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

            outputStream.writeBytes("exit\n");
            outputStream.flush();

            try {
                p.waitFor();
            } catch (InterruptedException e) {
                Log.d(TAG, "Thread Interrupted closing");
            }

            Log.d(TAG, "Process Done");
            outputStream.close();
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

    public void stopRun(){
        if(response != null){
            try {
                response.close();
            } catch (IOException e) { }
        }
    }

    protected void updateResult(ArrayList<String> result, long numOfPackets) {
        mFragment.printResult(result, numOfPackets);
    }

    protected void printToast(String message){
        mFragment.printToast(message);
    }

    protected void cmdDone(){
        mFragment.cmdDone();
    }
}
