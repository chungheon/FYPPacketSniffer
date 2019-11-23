package fyp.com.packetsniffer.Fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

import fyp.com.packetsniffer.R;

public class FileChooser {
    private static final String PARENT_DIR = "..";

    private final Activity activity;
    private ListView list;
    private Dialog dialog;
    private File currentPath;

    // filter on file extension
    private String extension = null;
    public void setExtension(String extension) {
        this.extension = (extension == null) ? null :
                extension.toLowerCase();
    }

    // file selection event handling
    public interface FileSelectedListener {
        void fileSelected(File file);
    }
    public FileChooser setFileListener(FileSelectedListener fileListener) {
        this.fileListener = fileListener;
        return this;
    }
    private FileSelectedListener fileListener;

    public FileChooser(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity);
        list = new ListView(activity);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
                String fileChosen = (String) list.getItemAtPosition(which);
                File chosenFile = getChosenFile(fileChosen);
                if (chosenFile.isDirectory()) {
                    refresh(chosenFile);
                } else {
                    if (fileListener != null) {
                        fileListener.fileSelected(chosenFile);
                    }
                    dialog.dismiss();
                }
            }
        });
        dialog.setContentView(list);
        dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
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
            File[] files = path.listFiles(new FileFilter() {
                @Override public boolean accept(File file) {
                    if (!file.isDirectory()) {
                        if (!file.canRead()) {
                            return false;
                        } else if (extension == null) {
                            return true;
                        } else {
                            return file.getName().toLowerCase().endsWith(extension);
                        }
                    } else {
                        return false;
                    }
                }
            });

            if(dirs == null || files == null){
                return;
            }
            // convert to an array
            int i = 0;
            String[] fileList;
            if (path.getParentFile() == null) {
                fileList = new String[dirs.length + files.length];
            } else {
                fileList = new String[dirs.length + files.length + 1];
                fileList[i++] = PARENT_DIR;
            }
            Arrays.sort(dirs);
            Arrays.sort(files);
            for (File dir : dirs) { fileList[i++] = dir.getName(); }
            for (File file : files ) { fileList[i++] = file.getName(); }

            // refresh the user interface
            dialog.setTitle(currentPath.getPath());
            list.setAdapter(new ArrayAdapter(activity,
                    android.R.layout.simple_list_item_1, fileList) {
                @Override public View getView(int pos, View view, ViewGroup parent) {
                    view = super.getView(pos, view, parent);
                    ((TextView) view).setSingleLine(true);
                    return view;
                }
            });
        }
    }


    /**
     * Convert a relative filename into an actual File object.
     */
    private File getChosenFile(String fileChosen) {
        if (fileChosen.equals(PARENT_DIR)) {
            if(currentPath.getPath().equals(Environment.getExternalStorageDirectory().getPath())){
                return currentPath;
            }
            return currentPath.getParentFile();
        } else {
            return new File(currentPath, fileChosen);
        }
    }

    public class FileArrayAdapter extends BaseAdapter {
        private File[] files;
        private Context mContext;
        private String[] fileList;
        private boolean gotParent = false;
        public FileArrayAdapter(Context context, File[] files, String[] fileList) {
            this.mContext = context;
            this.files = files;
            this.fileList = fileList;
            if(files.length < fileList.length){
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
                        File chosenDir = files[position];
                        refresh(chosenDir);
                    }

                }
            });
            TextView dir = (TextView)view.findViewById(R.id.directory_text);
            dir.setSingleLine(true);
            dir.setText(this.fileList[pos]);

            return view;
        }
    }
}
