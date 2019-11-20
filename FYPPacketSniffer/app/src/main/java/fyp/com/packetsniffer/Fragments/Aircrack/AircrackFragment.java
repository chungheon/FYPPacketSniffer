package fyp.com.packetsniffer.Fragments.Aircrack;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import fyp.com.packetsniffer.Fragments.FileChooser;
import fyp.com.packetsniffer.R;

public class AircrackFragment extends Fragment {

    private View view;
    private String libPath;

    private TextView selectedFile;
    private TextView resultText;
    private Button selectFileBtn;
    private Button crackBtn;
    private ImageButton viewMode;
    private RadioGroup attackGroup;
    private ProgressBar progressBar;

    private FileChooser fileChooser;
    private String mode = "";
    private String fileName = "";
    private AircrackThread aircrackThread;
    private boolean inProgress = false;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_aircrack_suite, container, false);
        installAsset("aircrack-ng", "aircrack-ng", getActivity().getFilesDir().toString());
        initView();
        initListener();
        return view;
    }

    private void initView(){
        this.selectedFile = (TextView) view.findViewById(R.id.aircrack_selected_file);
        this.resultText = (TextView) view.findViewById(R.id.aircrack_result_text);
        this.selectFileBtn = (Button) view.findViewById(R.id.aircrack_select_file);
        this.crackBtn = (Button) view.findViewById(R.id.aircrack_crack_btn);
        this.viewMode = (ImageButton) view.findViewById(R.id.aircrack_view_mode_btn);
        this.attackGroup = (RadioGroup) view.findViewById(R.id.aircrack_attack_group);
        this.progressBar = (ProgressBar) view.findViewById(R.id.aircrack_progress_bar);

        attackGroup.setVisibility(View.GONE);
        attackGroup.setEnabled(false);
        attackGroup.check(R.id.aircrack_mode_2);
        resultText.setVisibility(View.GONE);
        fileChooser = new FileChooser(getActivity());
        stopProgressBar();
    }

    private void initListener(){
        selectFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileChooser = new FileChooser(getActivity());
                fileChooser.setFileListener(new FileChooser.FileSelectedListener() {
                    @Override
                    public void fileSelected(File file) {
                        selectedFile.setText(file.getName());
                        fileName = file.getPath();
                    }
                });
                fileChooser.showDialog();
            }
        });


        attackGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId){
                String selected = "";
                switch(checkedId){
                    case R.id.aircrack_mode_1: selected = "PTW 64 bits Attack";
                    mode = "-n 64 ";
                    break;
                    case R.id.aircrack_mode_2: selected = "PTW 128 bits Attack";
                    mode = "-n 128 ";
                    break;
                    case R.id.aircrack_mode_3: selected = "Korek Attack";
                    mode = "-K ";
                    break;
                    default: selected = "PTW 128 bits Attack";
                        mode = "-n 128";
                }
                showMessage("SELECTED " + selected);
            }
        });

        crackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(fileName);

                if(!file.exists()){
                    showMessage("Please select a file");
                    return;
                }

                if(file.getName().matches(".*ivs")){
                    if(mode.equals("") || mode.matches("-n.*")){
                        showMessage("Unable to use PTW on old ivs files");
                        return;
                    }
                }
                if(file.getName().matches(".*cap") || file.getName().matches(".*pcap")){
                    if(mode.equals("-K ")){
                        showMessage("Use PTW for pcap files");
                        return;
                    }
                }

                if(!inProgress){
                    runCrack(fileName);
                    inProgress = true;
                    startProgressBar();
                }else{
                    showMessage("Still Running...");

                }
            }
        });

        viewMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(attackGroup.isEnabled()){
                    attackGroup.setEnabled(false);
                    attackGroup.setVisibility(View.GONE);
                }else{
                    attackGroup.setEnabled(true);
                    attackGroup.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void runCrack(String fileName){
        String cmd = libPath + " " + mode + fileName;
        aircrackThread = new AircrackThread(this, cmd);
        Thread thread = new Thread(aircrackThread);
        thread.start();
    }

    public void scanDone(){
        inProgress = false;
        stopProgressBar();
    }

    public void showMessage(String message){
        if(getActivity() != null){
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    public void updateResult(final String result){
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    resultText.setVisibility(View.VISIBLE);
                    resultText.setText(result);
                }
            });
        }

    }

    private void setPermissions() {
        try {
            Process shell = Runtime.getRuntime().exec("chmod 777 " + libPath + "\n");
            shell.waitFor();
        } catch (IOException e) {
        } catch (InterruptedException e) { }
    }

    private void installAsset(String assetName, String fileName, String dirPath) {
        InputStream is = null;
        FileOutputStream fileOutputStream = null;
        try {
            is = getActivity().getAssets().open(assetName);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);

            File targetFile = new File(dirPath + "/" + fileName);
            libPath = targetFile.getPath();
            fileOutputStream = new FileOutputStream(targetFile);
            fileOutputStream.write(buffer);

            is.close();
            fileOutputStream.close();
            setPermissions();
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

    public void startProgressBar() {
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    public void stopProgressBar() {
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }
}
