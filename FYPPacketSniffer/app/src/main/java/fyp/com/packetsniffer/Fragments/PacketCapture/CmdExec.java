package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.os.AsyncTask;
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
    private ArrayList<String> cmds;
    private PacketCaptureFragment mFragment;
    private Process p;
    private ReadOutput fileReader;

    public CmdExec(PacketCaptureFragment fragment, ArrayList<String> cmds){
        this.mFragment = fragment;
        this.cmds = cmds;
    }

    public class ReadOutput extends AsyncTask<String, Integer, String>{
        private InputStream input;
        private boolean stopRunning = false;
        public ReadOutput(InputStream input) {
            super();
            this.input = input;
        }

        @Override
        protected String doInBackground(String...strings) {
            ArrayList<String> result = new ArrayList<>();
            BufferedReader br = null;
            try {
                while(true){
                    br = new BufferedReader(new InputStreamReader(this.input));
                    String line;
                    String page = "";
                    int count = 0;
                    int pagesAdded = 0;
                    boolean first = true;
                    while((line = br.readLine()) != null){
                        if(first){
                            page += line;
                            first = false;
                        }else if(line.matches("\\d*-\\d*-\\d* \\d*:\\d*:\\d*.*")){
                            page += "\n\n" + line;
                        }else{
                            page += "\n" + line;
                        }

                        if(count < 100 && count > 90){
                            if(line.matches("\\d*-\\d*-\\d* \\d*:\\d*:\\d*.*")){
                                count = 0;
                                page += "\n";
                                result.add(page);
                                pagesAdded++;
                                page = line;
                                try{
                                    Thread.sleep(100);
                                }catch (InterruptedException e){
                                    stopRunning = true;
                                    break;
                                }
                            }
                        }else if(count >= 100){
                            count = 0;
                            page += "\n";
                            result.add(page);
                            page = "";
                            first = true;
                            pagesAdded++;
                        }
                        if(pagesAdded == 5){
                            updateResult(result);
                            pagesAdded = 0;
                            try{
                                Thread.sleep(100);
                            }catch (InterruptedException e){
                                stopRunning = true;
                                break;
                            }

                        }
                        count++;
                    }
                    if(count < 100 && count > 0){
                        result.add(page);
                        updateResult(result);
                    }
                    if (isCancelled() || stopRunning) {
                        br.close();
                        Log.d(TAG, "AsyncTask closing");
                        break;
                    }
                }

            printToast("Scan Done");

            } catch (IOException e) {
                if(br != null){
                    try{
                        br.close();
                    } catch (IOException ex) { }
                }
                return "Error";
            }
            return "Success";
        }
    }
    public void run() {
        String output = "";
        String normOut = "";
        DataOutputStream outputStream = null;
        InputStream response = null;
        try {

            p = Runtime.getRuntime().exec("su");
            outputStream = new DataOutputStream(p.getOutputStream());
            response = p.getErrorStream();
            InputStream  normRes = p.getInputStream();
            fileReader = new ReadOutput(normRes);
            fileReader.execute();
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

            Log.d(TAG, output);
        } catch (IOException e){
            return;
        } finally {
           new Closer().closeSilently(outputStream, response);
        }
    }

    public void stopRun(){
        fileReader.cancel(true);
        this.interrupt();
    }
    public static String readFully(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos.toString("UTF-8");
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

    private void updateResult(ArrayList<String> result) {
        mFragment.printResult(result);
    }

    private void printToast(String message){
        mFragment.printToast(message);
    }
}
