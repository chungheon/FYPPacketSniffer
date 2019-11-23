package fyp.com.packetsniffer.Fragments.Aircrack;

import android.renderscript.ScriptGroup;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AircrackThread implements Runnable {

    private String cmd;
    private AircrackFragment fragment;
    private boolean found = false;

    public AircrackThread(AircrackFragment fragment, String cmd) {
        this.fragment = fragment;
        this.cmd = cmd;
    }

    @Override
    public void run() {
        Process p = null;
        DataOutputStream outputStream = null;
        String result = "";
        try{
            p = Runtime.getRuntime().exec("su");
            outputStream = new DataOutputStream(p.getOutputStream());
            InputStream response = p.getInputStream();
            outputStream.writeBytes(cmd + "\n");
            outputStream.flush();
            String keyFound = "";
            BufferedReader br = new BufferedReader(new InputStreamReader(response));
            String line;

            while((line = br.readLine()) != null){
                if(line.matches(".*Tested \\d* keys.*")){
                    String[] info = line.split("keys")[0].split(" ");
                    String key = info[info.length - 1];
                    if(key.matches("\\d*")){
                        result = "Tested " + key + " keys...";
                        fragment.updateResult(result);
                        result += "\n";
                    }
                }

                String[] keyLine = line.split(" ");
                if(keyLine.length >= 4){
                    if(keyLine[1].equals("FOUND!")){
                        keyFound = keyLine[3];
                    }
                }

                if(line.matches(".*Decrypted correctly: 100%.*")){
                    result += "KEY: \n" + keyFound;
                    fragment.updateResult(result);
                    found = true;
                    break;
                }

                if(line.matches(".*Quitting.*")){
                    found = true;
                    result = "Unable to read this file";
                    fragment.updateResult(result);
                    break;
                }
            }
            if(!found){
                result += "Key not found";
                fragment.updateResult(result);
            }

            fragment.scanDone();
            br.close();
        } catch (IOException e) {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    e.printStackTrace();
                }
            }
        }finally {
            if(p != null){
                p.destroy();
            }
        }
    }
}
