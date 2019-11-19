package fyp.com.packetsniffer.Fragments.DevicesConnected.NmapServices;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import fyp.com.packetsniffer.R;

public class NmapMainFragment extends Fragment {

    private String libPath;
    private String arch;

    private View view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_nmap_main, container, false);
        loadAssets();
        initView();

        return view;
    }

    private void initView(){

    }


    private void loadAssets(){
        installAsset("nmap-os-db_arm", "nmap-os-db",getActivity().getFilesDir().toString());
        installAsset("nmap-services_arm", "nmap-services",getActivity().getFilesDir().toString());
        installAsset("nmap-payloads_arm", "nmap-payloads",getActivity().getFilesDir().toString());
        installAsset("nmap-mac-prefixes_arm", "nmap-mac-prefixes",getActivity().getFilesDir().toString());
        installAsset("nmap_arm", "nmap",getActivity().getFilesDir().toString());
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

    private void setPermissions() {
        try {
            Process shell = Runtime.getRuntime().exec("chmod 777 " + libPath + "\n");
            shell.waitFor();
        } catch (IOException e) {
        } catch (InterruptedException e) { }
    }

    //For Future development to cater to both ARM and x86 Architecture
    /*private void getArchitecture(){
        arch = System.getProperty("os.arch");
        if(arch.equals("") || arch == null){
            final String[] architectures = {"aarch64", "x86"};

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext().getApplicationContext());
            builder.setTitle("Pick device's architecture");
            builder.setItems(architectures, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case 0: arch = "aarch64";
                            break;
                        case 1: arch = "x86";
                            break;
                    }
                }
            });
            builder.show();
        }
        Log.d("ARCH", arch);
    }*/
}
