package preferences.filechooser;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class BinFileChooserPreferences {

    private static final String LAST_USED_DIRECTORY_KEY = "last_used_directory_key";
    private static final Preferences PREFERENCES = Preferences.userRoot().node(BinFileChooserPreferences.class.getName());

    public static void clear() {
        try {
            PREFERENCES.clear();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    public static File getDirectory() {
        return new File(PREFERENCES.get(LAST_USED_DIRECTORY_KEY, FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath()));
    }

    public static void setDirectory(File file) {
        PREFERENCES.put(LAST_USED_DIRECTORY_KEY, file.getAbsolutePath());
    }
}
