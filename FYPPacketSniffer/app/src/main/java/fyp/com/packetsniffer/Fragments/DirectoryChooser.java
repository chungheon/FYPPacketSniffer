package fyp.com.packetsniffer.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;

import fyp.com.packetsniffer.MainActivity;
import fyp.com.packetsniffer.R;

public class DirectoryChooser {
    private static final String PARENT_DIR = "..";

    private final Activity activity;
    private ListView list;
    private Dialog dialog;
    private File currentPath;
    private AlertDialog.Builder alertDialog;

    // filter on file extension
    private String extension = null;
    public void setExtension(String extension) {
        this.extension = (extension == null) ? null :
                extension.toLowerCase();
    }

    // file selection event handling
    public interface DirectorySelectedListener {
        void fileSelected(File file);
    }
    public DirectoryChooser setDirectoryListener(DirectorySelectedListener dirListener) {
        this.dirListener = dirListener;
        return this;
    }
    private DirectorySelectedListener dirListener;

    public DirectoryChooser(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity);
        list = new ListView(activity);
        dialog.setContentView(list);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        refresh(Environment.getExternalStorageDirectory());
    }

    public void showDialog() {
        dialog.show();
    }


    /**
     * Sort, filter and display the files for the given path.
     */
    private void refresh(File path) {
        this.currentPath = path;
        if (path.exists()) {
            File[] dirs = path.listFiles(new FileFilter() {
                @Override public boolean accept(File file) {
                    return (file.isDirectory() && file.canRead());
                }
            });
            if(dirs == null){
                return;
            }
            int i = 0;
            final String[] fileList;
            if (path.getParentFile() == null) {
                fileList = new String[dirs.length];
            } else {
                fileList = new String[dirs.length + 1];
                fileList[i++] = PARENT_DIR;
            }
            Arrays.sort(dirs);
            for (File dir : dirs) { fileList[i++] = dir.getName(); }

            // refresh the user interface
            dialog.setTitle(currentPath.getName());
            list.setAdapter(new DirectoryArrayAdapter(activity.getApplicationContext(), dirs, fileList));

        }


    }

    public class DirectoryArrayAdapter extends BaseAdapter {
        private File[] dirs;
        private Context mContext;
        private String[] fileList;
        private boolean gotParent = false;
        public DirectoryArrayAdapter(Context context, File[] dirs, String[] fileList) {
            this.mContext = context;
            this.dirs = dirs;
            this.fileList = fileList;
            if(dirs.length < fileList.length){
                gotParent = true;
            }
        }

        @Override
        public int getCount() {
            return fileList.length;
        }

        @Override
        public Object getItem(int position) {
            return fileList[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int pos, View view, ViewGroup parent) {
            if (view == null) {
                view = LayoutInflater.from(activity.getApplicationContext()).inflate(R.layout.layout_directory_list, parent, false);
            }
            ConstraintLayout dirItem = view.findViewById(R.id.directory_item);
            dirItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = pos;
                    if(gotParent){
                        position--;
                    }
                    if(position < 0){
                        if(!currentPath.equals(Environment.getExternalStorageDirectory())){
                            refresh(currentPath.getParentFile());
                        }
                    }else{
                        File chosenDir = dirs[position];
                        refresh(chosenDir);
                    }

                }
            });
            TextView dir = (TextView)view.findViewById(R.id.directory_text);
            dir.setSingleLine(true);
            dir.setText(this.fileList[pos]);
            Button selectBtn = (Button) view.findViewById(R.id.directory_select_btn);
            if(gotParent && pos == 0){
                selectBtn.setVisibility(View.GONE);
            }else{
                selectBtn.setVisibility(View.VISIBLE);
                selectBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = pos;
                        if(gotParent){
                            position--;
                        }
                        File dirSelected = dirs[position];
                        dirListener.fileSelected(dirSelected);
                        dialog.dismiss();
                    }
                });
            }

            return view;
        }
    }
}
