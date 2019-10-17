package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
<<<<<<< Updated upstream
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
=======
import android.widget.TextView;
>>>>>>> Stashed changes

import java.nio.file.Path;

import fyp.com.packetsniffer.R;

public class PacketCaptureFragment extends Fragment {

    private final String TAG = "PCapFragment";
    private View view;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerViewPageAdapter mRVAdapter;

<<<<<<< Updated upstream
    private RecyclerView output;
    private EditText editText;
    private CmdExec cmdRunnable;
=======
    private TextView output;
    private EditText input;
>>>>>>> Stashed changes
    private Button sniffBtn;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_packet_capture, container, false);
<<<<<<< Updated upstream
        initView();
        return view;
    }

    private void initView(){
        this.output = (RecyclerView) view.findViewById(R.id.output_content);
=======
        this.output = (TextView) view.findViewById(R.id.outputText);
        this.input = (EditText) view.findViewById(R.id.input_text);
>>>>>>> Stashed changes
        this.sniffBtn = (Button) view.findViewById(R.id.sniffBtn);
        this.editText = (EditText) view.findViewById(R.id.cmd_prompt);
        mLayoutManager = new LinearLayoutManager(getContext().getApplicationContext(),
                LinearLayout.HORIZONTAL,
                false);
        mRVAdapter = new RecyclerViewPageAdapter(getContext().getApplicationContext(), new ArrayList<String>());
        output.setAdapter(mRVAdapter);
        output.setLayoutManager(mLayoutManager);

        sniffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
<<<<<<< Updated upstream
=======
                String cmd = input.getText().toString();
                String dir = "";
                if(cmd.contains("|")){
                    String[] data = cmd.split("\\|");
                    dir = Environment.getExternalStorageDirectory().getAbsolutePath();
                    dir += "/Download/" + data[1].substring(5);
                    cmd = data[0] + "| tee " + dir;
                }else if(cmd.contains(">")){
                    String[] data = cmd.split(">");
                    dir = Environment.getExternalStorageDirectory().getAbsolutePath();
                    dir += "/Download/" + data[1].substring(1);
                    cmd = data[0] + "> " + dir;
                }


                String getVersion = runCmd("su:-c:tcpdump --version", PCAP_ERROR_STREAM);
                String testPacket = runCmd("su:-c:" + cmd, PCAP_NORMAL_STREAM);
                output.setText(getVersion + "\n\n" + cmd + "\n\n" + testPacket);
>>>>>>> Stashed changes

                ArrayList<String> cmds = new ArrayList<>();
                cmds.add("tcpdump --version");
                String[] cmd = editText.getText().toString().split(":");
                for(String str: cmd){
                    cmds.add(str);
                }

                if(cmdRunnable != null){
                    cmdRunnable.stopRun();
                    final ArrayList<String> allCmd = cmds;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRVAdapter = new RecyclerViewPageAdapter(getContext().getApplicationContext(), new ArrayList<String>());
                            output.setAdapter(mRVAdapter);
                            runCmd(allCmd);
                        }
                    });
                }else{
                    runCmd(cmds);
                }
            }
        });
    }

    private void runCmd(ArrayList<String> args){
        cmdRunnable = new CmdExec(this, args);
        Thread test = new Thread(cmdRunnable);
        test.start();
    }

    public void printToast(String message){
        Toast.makeText(getContext().getApplicationContext(), message, Toast.LENGTH_LONG);
    }

    public void printResult(final ArrayList<String> result){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int prevSize = mRVAdapter.getItemCount();
                mRVAdapter.bindList(result);
                mRVAdapter.notifyItemRangeInserted(prevSize, 5);
                /*mRVAdapter =
                        new RecyclerViewPageAdapter(getContext().getApplicationContext(), result);
                output.setAdapter(mRVAdapter);
                output.setNestedScrollingEnabled(true);
                output.setLayoutManager(mLayoutManager);*/
            }
        });
    }
}