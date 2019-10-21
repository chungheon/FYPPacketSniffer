package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.util.Log;

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

public class CmdExecError extends Thread {
    private final String TAG = "CmdExecution";
    private ArrayList<String> cmds;
    private PacketCaptureInterface mFragment;
    private Process p;
    private Thread readOutput;
    private ReadOutput fileReader;

    public CmdExecError(PacketCaptureInterface fragment, ArrayList<String> cmds){
        this.mFragment = fragment;
        this.cmds = cmds;
    }

    public class ReadOutput implements Runnable{
        private InputStream input;
        private boolean stopRunning = false;
        public ReadOutput(InputStream input) {
            super();
            this.input = input;
        }


        public synchronized void run() {
            ArrayList<String> result = new ArrayList<>();
            BufferedReader br = null;
            int pageNum = 1;
            long numOfPackets = 0;
            try {
                while(true){
                    br = new BufferedReader(new InputStreamReader(this.input));
                    String line;
                    String page = "Page " + pageNum + "\n";
                    int count = 0;
                    int pagesAdded = 0;
                    boolean first = true;
                    while((line = br.readLine()) != null){
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
                        }else{
                            page += "\n" + line;
                        }

                        if(pagesAdded >= 2){
                            updateResult(result, numOfPackets);
                            pagesAdded = 0;
                        }
                        count++;
                    }
                    if(count > 0){
                        result.add(page);
                        pagesAdded++;
                    }
                    if(pagesAdded > 0){
                        updateResult(result, numOfPackets);
                    }

                    if (isInterrupted()|| stopRunning) {
                        Log.d(TAG, "Reader stopping");
                        break;
                    }
                    try{
                        this.wait(1000);
                        break;
                    }catch (InterruptedException e){
                        break;
                    }
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
                if(br != null){
                    try{
                        br.close();
                    } catch (IOException ex) {
                        Log.e(TAG, ex.getMessage());
                    }
                }
                printToast("Scan Interrupted");
            }
            printToast("Scan Done");
        }
    }
    public void run() {
        String output = "";
        String normOut = "";
        DataOutputStream outputStream = null;

        try {

            p = Runtime.getRuntime().exec("su");
            outputStream = new DataOutputStream(p.getOutputStream());
            InputStream response = p.getErrorStream();
            InputStream  normRes = p.getInputStream();
            fileReader = new ReadOutput(response);
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
                Log.e(TAG, e.getMessage());
            }
        } catch (IOException e){
            return;
        } finally {
           new Closer().closeSilently(outputStream);
           readOutput.interrupt();
        }
    }

    public void stopRun(){
        if(fileReader != null && readOutput != null){
            readOutput.interrupt();
        }
        this.interrupt();
    }

    public class Closer {
        // closeAll()
        public void closeSilently(Object... xs) {
            // Note: on Android API levels prior to 19 Socket does not implement Closeable
            for (Object x : xs) {
                if (x != null) {
                    try {
                        Log.d(TAG, "closing: "+x);
                        if (x instanceof Closeable) {
                            ((Closeable)x).close();
                        } else if (x instanceof Socket) {
                            ((Socket)x).close();
                        } else if (x instanceof DatagramSocket) {
                            ((DatagramSocket)x).close();
                        } else {
                            Log.d(TAG, "cannot close: "+x);
                            throw new RuntimeException("cannot close "+x);
                        }
                    } catch (Throwable e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        }
    }

    private void updateResult(ArrayList<String> result, long numOfPackets) {
        mFragment.printResult(result, numOfPackets);
    }

    private void printToast(String message){
        mFragment.printToast(message);
    }
}
