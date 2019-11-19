package fyp.com.packetsniffer.Fragments.Aircrack;

import android.renderscript.ScriptGroup;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AircrackThread implements Runnable {

    private Thread thread;
    private String cmd;
    AircrackFragment fragment;

    public AircrackThread(AircrackFragment fragment, String cmd) {
        this.fragment = fragment;
        this.cmd = cmd;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        Process p = null;
        DataOutputStream outputStream = null;

        try{
            p = Runtime.getRuntime().exec("su");
            outputStream = new DataOutputStream(p.getOutputStream());
            InputStream response = p.getInputStream();
            outputStream.writeBytes(cmd + "\n");
            outputStream.flush();\

            p.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
