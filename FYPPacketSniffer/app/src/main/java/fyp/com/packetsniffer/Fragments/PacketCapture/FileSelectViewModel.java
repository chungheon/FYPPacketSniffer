package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class FileSelectViewModel extends ViewModel {
    private final MutableLiveData<String> selected = new MutableLiveData<>();

    public void select(String fileName){
        selected.setValue(fileName);
    }

    public LiveData<String> getSelected(){
        return selected;
    }
}
