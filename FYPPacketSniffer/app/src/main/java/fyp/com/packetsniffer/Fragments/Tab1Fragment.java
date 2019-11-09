package fyp.com.packetsniffer.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Random;
import fyp.com.packetsniffer.R;

public class Tab1Fragment extends Fragment {
    int num = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_one, container, false);
        TextView tx = view.findViewById(R.id.fragText);
        if (this.getArguments() != null) {
            Random r = new Random();
            num = r.nextInt();
        }
        tx.setText(num + " fragment");
        test();
        testBinary();
        return view;
    }

    public void test() {
        InputStream is = null;
        FileOutputStream fileOutputStream = null;
        try {
            is = getActivity().getAssets().open("traceroute_arm");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);

            File targetFile = new File(getActivity().getFilesDir() + "/traceroute");
            fileOutputStream = new FileOutputStream(targetFile);
            fileOutputStream.write(buffer);

            is.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testBinary() {
        try {
            String libPath = getActivity().getFilesDir() + "/traceroute";

            //ProcessBuilder pb = new ProcessBuilder("chmod 777 " + libPath);
            //pb.command(libPath + " --version\n");
            // pb.command("traceroute --version\n");
            //pb.command("ls -l /data/user/0/fyp.com.packetsniffer/files");
            Process shell = Runtime.getRuntime().exec("chmod 777 " + libPath + "\n");
            DataOutputStream outputStream = new DataOutputStream(shell.getOutputStream());
            BufferedReader shell_out = new BufferedReader(new InputStreamReader(shell.getErrorStream()));
            outputStream.writeBytes(libPath + " --version | grep \'version\'\n");
            outputStream.flush();
            String line = null;
            int count = 0;
            try {
                while(true){
                    if(shell_out.ready()){
                        line = shell_out.readLine();
                    }

                    if(line != null){
                        Log.d("test", line);
                        count = 0;
                        line = null;
                    }

                    synchronized (this){
                        try{
                            wait(10);
                            count++;
                        }catch (InterruptedException e){
                            break;
                        }
                    }
                    if(count >= 300){
                        break;
                    }
                }
            } catch (IOException e) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void installAsset(String assetName, String fileName, String dirPath, ArrayList<String> cmds) {
        InputStream is = null;
        FileOutputStream fileOutputStream = null;
        try {
            is = getActivity().getAssets().open(assetName);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);

            File targetFile = new File(dirPath + "/" + fileName);
            fileOutputStream = new FileOutputStream(targetFile);
            fileOutputStream.write(buffer);

            is.close();
            fileOutputStream.close();

            cmds.add("chmod 755 " + targetFile.getPath());
            cmds.add("cp " + targetFile.getPath() + " /system/xbin/" + fileName);
            cmds.add("rm " + targetFile.getPath());
        } catch (IOException e) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                }
            }

            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException ex) {
                }
            }
        }
    }
}