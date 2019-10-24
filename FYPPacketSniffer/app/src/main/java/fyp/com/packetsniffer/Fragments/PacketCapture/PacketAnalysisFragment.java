package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.os.Bundle;
import android.os.Environment;
import android.renderscript.ScriptGroup;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import fyp.com.packetsniffer.R;

public class PacketAnalysisFragment extends Fragment implements PacketCaptureInterface {

    private final String TAG = "PCapFragment";
    private View view;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerViewPageAdapter mRVAdapter;

    private RecyclerView output;
    private Button jumpBtn;
    private EditText pageJump;
    private TextView pageText;
    private CmdExec cmdRunnable;
    private Button analyseBtn;
    private long movement = 1050;

    private boolean inProgress;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_packet_analysis, container, false);
        initView();
        initListener();
        return view;
    }

    private void initView(){
        this.output = (RecyclerView) view.findViewById(R.id.output_content);
        this.analyseBtn = (Button) view.findViewById(R.id.analyse_btn);
        this.jumpBtn = (Button) view.findViewById(R.id.jump_btn);
        this.pageJump = (EditText) view.findViewById(R.id.jump_page);
        this.pageText = (TextView) view.findViewById(R.id.num_page);
        mLayoutManager = new LinearLayoutManager(getContext().getApplicationContext(),
                LinearLayout.HORIZONTAL,
                false);
        mRVAdapter = new RecyclerViewPageAdapter(getContext().getApplicationContext(), new ArrayList<String>());
        output.setAdapter(mRVAdapter);
        output.setLayoutManager(mLayoutManager);
        this.pageText.setText("0/0");
        this.inProgress = false;
    }

    private void initListener(){

        analyseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> cmds = new ArrayList<>();
                cmds.add("tcpdump --version");
                String filePath = getActivity().getFilesDir().toString();
                String[] cmd = (pageJump.getText().toString())
                        .split(":");
                for(String str: cmd){
                    cmds.add(str);
                }

                if(cmdRunnable != null){
                    if(inProgress){
                        cmdRunnable.stopRun();
                        inProgress = false;
                    }else{
                        final ArrayList<String> allCmd = cmds;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mRVAdapter = new RecyclerViewPageAdapter(getContext().getApplicationContext(), new ArrayList<String>());
                                output.setAdapter(mRVAdapter);
                                pageText.setText("0/0");
                                runCmd(allCmd);
                            }
                        });

                        inProgress = true;
                    }
                }else{
                    runCmd(cmds);
                    inProgress = true;
                }
            }
        });

        output.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dx > 0) {
                    final String[] pageInfo = pageText.getText().toString().split("/");
                    long totalpages = Integer.parseInt(pageInfo[1]) * 1050;
                    if(movement <= totalpages && pageInfo.length == 3){
                        movement += dx;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pageText.setText((((movement + 200)/1050)) + "/" + pageInfo[1] + "/" + pageInfo[2]);
                            }
                        });
                    }
                    if(movement > totalpages){
                        movement = totalpages;
                    }
                } else {
                    final String[] pageInfo = pageText.getText().toString().split("/");
                    if((movement-1050)<= (dx * -1)){
                        movement = 1050;
                    }else{
                        movement += dx;
                        if(pageInfo.length == 3){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pageText.setText((((movement + 600)/1050)) + "/" + pageInfo[1] + "/" + pageInfo[2]);
                                }
                            });
                        }
                    }

                    if(movement == 1050 && pageInfo.length == 3){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pageText.setText("1/" + pageInfo[1] + "/" + pageInfo[2]);
                            }
                        });
                    }

                }
            }
        });

        jumpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pageNum = pageJump.getText().toString();
                String[] totalPage = pageText.getText().toString().split("/");
                try{
                    int num = Integer.parseInt(pageNum);
                    if(totalPage.length >= 2){
                        int totalPages = Integer.parseInt(totalPage[1]);
                        if(totalPages < num){
                            Toast.makeText(getContext().getApplicationContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }else{
                        return;
                    }
                    if(num <= 0){
                        Toast.makeText(getContext().getApplicationContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
                    }else{
                        output.scrollToPosition(num - 1);
                        movement = (num) * 1050;
                    }

                }catch (NumberFormatException e){
                    Toast.makeText(getContext().getApplicationContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void runCmd(ArrayList<String> args){
        cmdRunnable = new CmdExec(this, args);
        Thread test = new Thread(cmdRunnable);
        test.start();
    }

    public void printToast(final String message){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext().getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void printResult(final ArrayList<String> result, final long numOfPackets){
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int prevSize = mRVAdapter.getItemCount();
                    int diff = result.size() - prevSize;
                    mRVAdapter.bindList(result);
                    final String[] pageInfo = pageText.getText().toString().split("/");
                    mRVAdapter.notifyItemRangeInserted(prevSize - 1, 2);
                    String text = pageInfo[0] + "/" + result.size() + "/" + numOfPackets + " packets";
                    pageText.setText(text);
                }
            });
        }
    }

    @Override
    public void cmdDone() {
        this.inProgress = false;
    }

    @Override
    public void onDestroy() {
        if(cmdRunnable != null){
            cmdRunnable.stopRun();
        }
        super.onDestroy();
    }
}