package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import fyp.com.packetsniffer.R;

public class TestFragment extends Fragment {
    EditText input;
    Button btn;
    TextView output;
    int length = 0;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.test_fragment, container, false);
        input = view.findViewById(R.id.pcap_length);
        btn = view.findViewById(R.id.split_btn);
        output = view.findViewById(R.id.output_msg);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    length = Integer.parseInt(input.getText().toString());
                    try{
                        FileInputStream fileReader = new FileInputStream(new File("/storage/emulated/0/0"));
                        FileOutputStream fileWriter = new FileOutputStream(new File("/storage/emulated/0/test0"));
                        byte[] input = new byte[512];
                        int count = 0;
                        while(fileReader.read(input) != 0 && count <= length){
                            fileWriter.write(input);
                            count++;
                        }

                        fileWriter.close();
                        fileReader.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }catch (NumberFormatException e){
                    return;
                }

                try{
                    Process p = Runtime.getRuntime().exec("su");
                    DataOutputStream outputStream = new DataOutputStream(p.getOutputStream());
                    InputStream response = p.getErrorStream();
                    InputStream  normRes = p.getInputStream();
                    outputStream.writeBytes("tcpdump -r /storage/emulated/0/test0 -tttt" + "\n");
                    outputStream.flush();
                    outputStream.writeBytes("exit\n");
                    outputStream.flush();
                    BufferedReader br = new BufferedReader(new InputStreamReader(normRes));
                    BufferedReader br2 = new BufferedReader(new InputStreamReader(response));
                    String line;
                    String output1 = "";
                    String output2 = "";
                    int count = 0;
                    while((line = br.readLine()) != null){
                        output1 += line + "\n";
                        if(line.matches("\\d*-\\d*-\\d* \\d*:\\d*:\\d*.*")) {
                            count++;
                        }
                    }

                    while((line = br2.readLine()) != null){
                        output2 += line + "\n";
                    }
                    try {
                        p.waitFor();
                    } catch (InterruptedException e) {
                        Log.d("test", "Thread Interrupted closing");
                        Log.e("test", e.getMessage());
                    }

                    output.setText(output1 + "\n\n" + output2 + "\n\n" + count);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return view;
    }
}
