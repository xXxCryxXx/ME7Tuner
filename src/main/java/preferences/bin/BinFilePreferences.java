package preferences.bin;

import io.reactivex.Observer;
import io.reactivex.subjects.BehaviorSubject;

import java.io.File;

import java.util.prefs.Preferences;

public class BinFilePreferences {

    private static final String FILE_PATH_KEY = "file_path_key";
    private static final Preferences PREFERENCES = Preferences.userRoot().node(BinFilePreferences.class.getName());
    private static BinFilePreferences instance;

    private final BehaviorSubject<File> behaviorSubject = BehaviorSubject.create();

    private BinFilePreferences() {
        behaviorSubject.onNext(getFile());
    }

    public static BinFilePreferences getInstance() {
        if (instance == null) {
            synchronized (BinFilePreferences.class) {
                if (instance == null) {
                    instance = new BinFilePreferences();
                }
            }
        }

        return instance;
    }

    public void registerObserver(Observer<File> observer) {
        behaviorSubject.subscribe(observer);
    }

    public File getFile() {
        return new File(PREFERENCES.get(FILE_PATH_KEY, ""));
    }

    public void setFile(File file) {
        PREFERENCES.put(FILE_PATH_KEY, file.getAbsolutePath());
        behaviorSubject.onNext(file);
    }
}