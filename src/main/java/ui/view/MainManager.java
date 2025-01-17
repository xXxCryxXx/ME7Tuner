package ui.view;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import preferences.MapPreferenceManager;
import preferences.bin.BinFilePreferences;
import preferences.filechooser.BinFileChooserPreferences;
import preferences.filechooser.XdfFileChooserPreferences;
import preferences.logheaderdefinition.LogHeaderPreference;
import preferences.xdf.XdfFilePreferences;
import ui.view.closedloopfueling.ClosedLoopFuelingView;
import ui.view.configuration.ConfigurationView;
import ui.view.kfmiop.KfmiopView;
import ui.view.kfmirl.KfmirlView;
import ui.view.kfvpdksd.KfvpdksdView;
import ui.view.kfzw.KfzwView;
import ui.view.kfzwop.KfzwopView;
import ui.view.krkte.KrkteView;
import ui.view.ldrpid.LdrpidView;
import ui.view.listener.OnTabSelectedListener;
import ui.view.openloopfueling.OpenLoopFuelingView;
import ui.view.plsol.PlsolView;
import ui.view.wdkugdn.WdkugdnView;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainManager {
    private final List<OnTabSelectedListener> tabSelectedListeners = new ArrayList<>();

    private final JFrame frame = new JFrame();
    private File binFile = new File("");
    private File xdfFile = new File("");

    public MainManager() {
        BinFilePreferences.getInstance().registerObserver(new Observer<File>() {
            @Override
            public void onSubscribe(@NonNull Disposable disposable) {}

            @Override
            public void onNext(@NonNull File file) {
                if(file.exists() && file.isFile()) {
                    binFile = file;
                    updateTitle();
                }
            }

            @Override
            public void onError(@NonNull Throwable throwable) {}

            @Override
            public void onComplete() {}
        });

        XdfFilePreferences.getInstance().registerObserver(new Observer<File>() {
            @Override
            public void onSubscribe(@NonNull Disposable disposable) {}

            @Override
            public void onNext(@NonNull File file) {
                if(file.exists() && file.isFile()) {
                    xdfFile = file;
                    updateTitle();
                }
            }

            @Override
            public void onError(@NonNull Throwable throwable) {}

            @Override
            public void onComplete() {}
        });
    }

    public void start() {
        updateTitle();

        frame.setJMenuBar(getMenuBar());
        frame.setSize(1480, 920);
        frame.add(getTabbedPane());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private JTabbedPane getTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();

        ConfigurationView configurationView = new ConfigurationView();
        tabbedPane.addTab("Configuration", null, new JScrollPane(configurationView.getPanel()), "Table Definition Configuration");

        KrkteView krkteView = new KrkteView();
        tabSelectedListeners.add(krkteView);
        tabbedPane.addTab("KRKTE", null, new JScrollPane(krkteView.getPanel()), "KRKTE Calculator");

        ClosedLoopFuelingView closedLoopFuelingView = new ClosedLoopFuelingView();
        // first tab is selected
        closedLoopFuelingView.onTabSelected(true);
        tabSelectedListeners.add(closedLoopFuelingView);
        tabbedPane.addTab("Closed Loop Fueling", null, closedLoopFuelingView.getPanel(), "Closed Loop MLHFM Compensation");

        OpenLoopFuelingView openLoopFuelingView = new OpenLoopFuelingView();
        tabSelectedListeners.add(openLoopFuelingView);
        tabbedPane.addTab("Open Loop Fueling", null, openLoopFuelingView.getPanel(), "Open Loop MLHFMCompensation");

        PlsolView plsolView = new PlsolView();
        tabbedPane.addTab("PLSOL", null, plsolView.getPanel(), "Requested Boost");

        KfmiopView kfmiopView = new KfmiopView();
        tabSelectedListeners.add(kfmiopView);
        tabbedPane.addTab("KFMIOP", null, new JScrollPane(kfmiopView.getPanel()), "KFMIOP Calculator");

        KfmirlView kfmirlView = new KfmirlView();
        tabSelectedListeners.add(kfmirlView);
        tabbedPane.addTab("KFMIRL", null, new JScrollPane(kfmirlView.getPanel()), "KFMIRL Calculator");

        KfzwopView kfzwopView = new KfzwopView();
        tabSelectedListeners.add(kfzwopView);
        tabbedPane.addTab("KFZWOP", null, new JScrollPane(kfzwopView.getPanel()), "KFZWOP Calculator");

        KfzwView kfzwView = new KfzwView();
        tabSelectedListeners.add(kfzwView);
        tabbedPane.addTab("KFZW", null, new JScrollPane(kfzwView.getPanel()), "KFZW Calculator");

        KfvpdksdView kfvpdksdView = new KfvpdksdView();
        tabSelectedListeners.add(kfvpdksdView);
        tabbedPane.addTab("KFVPDKSD/E", null, new JScrollPane(kfvpdksdView.getPanel()), "KFVPDKSD/E Calculator");

        WdkugdnView wdkugdnView = new WdkugdnView();
        tabbedPane.addTab("WDKUGDN", null, wdkugdnView.getPanel(), "KFURL");

        LdrpidView ldrpidView = new LdrpidView();
        tabbedPane.addTab("LDRPID", null, new JScrollPane(ldrpidView.getPanel()), "LDRPID");

        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int selectedIndex = tabbedPane.getSelectedIndex();
                for (int i = 0; i < tabSelectedListeners.size(); i++) {
                    tabSelectedListeners.get(i).onTabSelected(selectedIndex == i);
                }
            }
        });

        return tabbedPane;
    }

    private JMenuBar getMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem openBinMenuItem = new JMenuItem("Open Bin...");
        openBinMenuItem.addActionListener(e -> {
            final JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new BinFileFilter());
            fc.setCurrentDirectory(BinFileChooserPreferences.getDirectory());

            int returnValue = fc.showOpenDialog(frame);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File binFile = fc.getSelectedFile();
                BinFilePreferences.getInstance().setFile(binFile);
                BinFileChooserPreferences.setDirectory(binFile);
            }
        });
        fileMenu.add(openBinMenuItem);

        JMenu xdfMenu = new JMenu("XDF");
        menuBar.add(xdfMenu);

        JMenuItem selectXdfMenuItem = new JMenuItem("Select XDF...");
        selectXdfMenuItem.addActionListener(e -> {
            final JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new XdfFileFilter());
            fc.setCurrentDirectory(XdfFileChooserPreferences.getDirectory());

            int returnValue = fc.showOpenDialog(frame);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                MapPreferenceManager.clear();
                File xdfFile = fc.getSelectedFile();
                XdfFilePreferences.getInstance().setFile(xdfFile);
                XdfFileChooserPreferences.setDirectory(xdfFile);
            }
        });
        xdfMenu.add(selectXdfMenuItem);

        JMenu preferencesMenu = new JMenu("Preferences");
        menuBar.add(preferencesMenu);

        JMenuItem clearPreferencesMenuItem = new JMenuItem("Reset Preferences");
        clearPreferencesMenuItem.addActionListener(e -> {
            MapPreferenceManager.clear();
            LogHeaderPreference.clear();
            XdfFilePreferences.clear();
            BinFilePreferences.clear();
            XdfFileChooserPreferences.clear();
            BinFileChooserPreferences.clear();

            binFile = new File("");
            xdfFile = new File("");

            updateTitle();
        });
        preferencesMenu.add(clearPreferencesMenuItem);

        return menuBar;
    }

    private void updateTitle() {
        frame.setTitle("ME7 Tuner - " + binFile.getName() + " | XDF File - " + xdfFile.getName());
    }

    private static class BinFileFilter extends FileFilter {
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String name = f.getName();
            String[] parts = name.split("\\.");
            if (parts.length > 0) {
                String ext = parts[parts.length - 1];
                return ext.trim().equalsIgnoreCase("bin");
            }

            return false;
        }

        @Override
        public String getDescription() {
            return "bin";
        }
    }

    private static class XdfFileFilter extends FileFilter {
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String name = f.getName();
            String[] parts = name.split("\\.");
            if (parts.length > 0) {
                String ext = parts[parts.length - 1];
                return ext.trim().equalsIgnoreCase("xdf");
            }

            return false;
        }

        @Override
        public String getDescription() {
            return "xdf";
        }
    }


}
