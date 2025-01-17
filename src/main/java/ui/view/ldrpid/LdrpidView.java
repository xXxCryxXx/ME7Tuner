package ui.view.ldrpid;

import com.sun.tools.javac.util.Pair;
import contract.Me7LogFileContract;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import math.map.Map3d;
import model.ldrpid.LdrpidCalculator;
import parser.me7log.Me7LogParser;
import parser.xdf.TableDefinition;
import preferences.bin.BinFilePreferences;
import preferences.kfldimx.KfldimxPreferences;
import preferences.kfldrl.KfldrlPreferences;
import preferences.ldrpid.LdrpidPreferences;
import ui.map.axis.MapAxis;
import ui.map.map.MapTable;
import writer.BinWriter;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class LdrpidView {
    private LdrpidCalculator.LdrpidResult ldrpidResult;

    private final MapTable nonLinearTable = MapTable.getMapTable(new Double[0], new Double[0], new Double[0][0]);
    private final MapTable linearTable = MapTable.getMapTable(new Double[0], new Double[0], new Double[0][0]);
    private final MapTable kfldrlTable = MapTable.getMapTable(new Double[0], new Double[0], new Double[0][0]);
    private final MapTable kfldimxTable = MapTable.getMapTable(new Double[0], new Double[0], new Double[0][0]);
    private final MapAxis kfldimxXAxis = MapAxis.getMapAxis(new Double[1][0]);

    private JLabel logFileLabel;

    private final JProgressBar dpb = new JProgressBar();
    private JPanel panel;

    public JPanel getPanel() {
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.BOTH;

        constraints.weightx = 0.5;
        constraints.weighty = 0.5;

        constraints.gridx = 0;
        constraints.gridy = 0;

        panel.add(getNonLinearMapPanel(), constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;

        panel.add(getLinearMapPanel(), constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.insets.top = 84;

        panel.add(getKflDrlMapPanel(), constraints);

        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.insets.top = 16;

        panel.add(getKfldimxMapPanel(), constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.insets.top = 0;

        panel.add(getLogsButton(panel), constraints);

        return panel;
    }

    private JPanel getHeader(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;

        JLabel label = new JLabel(title);
        panel.add(label, c);

        return panel;
    }

    private void initKfldimxAxis() {
        Double[][] kfldImxXAxisValues = new Double[1][];

        Pair<TableDefinition, Map3d> tableDefinition = KfldimxPreferences.getInstance().getSelectedMap();
        if(tableDefinition != null && tableDefinition.snd != null) {
            kfldImxXAxisValues[0] = tableDefinition.snd.xAxis;
        }
        kfldimxXAxis.setTableData(kfldImxXAxisValues);
    }

    private JPanel getKfldimxMapPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;

        panel.add(new JLabel("KFLDIMX X-Axis"), constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.insets.top = 16;

        initKfldimxAxis();

        panel.add(kfldimxXAxis.getScrollPane(), constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;

        panel.add(getHeader("KFLDIMX"), constraints);

        constraints.gridx = 0;
        constraints.gridy = 3;

        initKflimxMap();

        panel.add(kfldimxTable.getScrollPane(), constraints);

        constraints.gridx = 0;
        constraints.gridy = 4;

        panel.add(getWriteKfldimxFileButton(), constraints);

        return panel;
    }

    private JButton getWriteKfldimxFileButton() {
        JButton button = new JButton("Write KFLDIMX");

        button.addActionListener(e -> {
            int returnValue = JOptionPane.showConfirmDialog(
                    panel,
                    "Are you sure you want to write KFLDIMX to the binary?",
                    "Write KFLDIMX",
                    JOptionPane.YES_NO_OPTION);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                try {
                    BinWriter.getInstance().write(BinFilePreferences.getInstance().getFile(), KfldimxPreferences.getInstance().getSelectedMap().fst, kfldimxTable.getMap3d());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        return button;
    }

    private JPanel getKflDrlMapPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;

        panel.add(getHeader("KFLDRL"), constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.insets.top = 16;

        initKfldrlMap();

        panel.add(kfldrlTable.getScrollPane(), constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        panel.add(getWriteKfldrlFileButton(), constraints);

        return panel;
    }

    private JButton getWriteKfldrlFileButton() {
        JButton button = new JButton("Write KFLDRL");

        button.addActionListener(e -> {
            int returnValue = JOptionPane.showConfirmDialog(
                    panel,
                    "Are you sure you want to write KFLDRL to the binary?",
                    "Write KFLDRL",
                    JOptionPane.YES_NO_OPTION);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                try {
                    BinWriter.getInstance().write(BinFilePreferences.getInstance().getFile(), KfldrlPreferences.getInstance().getSelectedMap().fst, kfldrlTable.getMap3d());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        return button;
    }

    private JPanel getLogsButton(JPanel parent) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        JButton button = new JButton("Load ME7 Logs");
        button.addActionListener(e -> {
            final JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setCurrentDirectory(LdrpidPreferences.getDirectory());

            int returnValue = fc.showOpenDialog(parent);

            if (returnValue == JFileChooser.APPROVE_OPTION) {

                SwingUtilities.invokeLater(() -> {
                    LdrpidPreferences.setDirectory(fc.getSelectedFile().getParentFile());

                    logFileLabel.setText(fc.getSelectedFile().getPath());

                    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                        @Override
                        public Void doInBackground() {
                            Me7LogParser parser = new Me7LogParser();
                            Map<Me7LogFileContract.Header, List<Double>> values = parser.parseLogDirectory(Me7LogParser.LogType.LDRPID, fc.getSelectedFile(), (value, max) -> {
                                SwingUtilities.invokeLater(() -> {
                                    dpb.setMaximum(max);
                                    dpb.setValue(value);
                                    dpb.setVisible(value < max - 1);
                                });
                            });
                            Pair<TableDefinition, Map3d> kfldimxTableDefinition = KfldimxPreferences.getInstance().getSelectedMap();
                            Pair<TableDefinition, Map3d> kfldrlTableDefinition = KfldrlPreferences.getInstance().getSelectedMap();

                            ldrpidResult = LdrpidCalculator.caclulateLdrpid(values, kfldrlTableDefinition.snd, kfldimxTableDefinition.snd);
                            return null;
                        }

                        @Override
                        public void done() {
                            nonLinearTable.setMap(ldrpidResult.nonLinearOutput);
                            linearTable.setMap(ldrpidResult.linearOutput);
                            kfldrlTable.setMap(ldrpidResult.kfldrl);

                            Double[][] kfldImxXAxisValues = new Double[1][];
                            kfldImxXAxisValues[0] = ldrpidResult.kfldimx.xAxis;
                            kfldimxXAxis.setTableData(kfldImxXAxisValues);
                            kfldimxTable.setMap(ldrpidResult.kfldimx);
                        }
                    };

                    worker.execute();
                });
            }
        });

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.CENTER;

        panel.add(button, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;

        dpb.setIndeterminate(false);
        dpb.setVisible(false);
        panel.add(dpb, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;

        logFileLabel = new JLabel("No File Selected");
        panel.add(logFileLabel, constraints);

        return panel;
    }

    private JPanel getNonLinearMapPanel() {

        JPanel panel = new JPanel();

        panel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;

        panel.add(getHeader("Non Linear Boost"), constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.insets.top = 16;

        initNonLinearMap();

        JScrollPane scrollPane = nonLinearTable.getScrollPane();

        panel.add(scrollPane, constraints);

        return panel;
    }

    private JPanel getLinearMapPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;

        panel.add(getHeader("Linear Boost"), constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.insets.top = 16;

        initLinearMap();

        JScrollPane kfmiopMapScrollPane = linearTable.getScrollPane();

        panel.add(kfmiopMapScrollPane, constraints);

        return panel;
    }

    private void initKflimxMap() {
        Pair<TableDefinition, Map3d> tableDefinition = KfldimxPreferences.getInstance().getSelectedMap();
        kfldimxTable.setEditable(false);
        if(tableDefinition != null && tableDefinition.snd != null) {
            kfldimxTable.setMap(tableDefinition.snd);
        }
    }

    private void initKfldrlMap() {
        Pair<TableDefinition, Map3d> tableDefinition = KfldrlPreferences.getInstance().getSelectedMap();
        kfldrlTable.setEditable(false);
        if(tableDefinition != null && tableDefinition.snd != null) {
            kfldrlTable.setMap(tableDefinition.snd);
        }
    }

    private void initNonLinearMap() {
        Pair<TableDefinition, Map3d> tableDefinition = KfldrlPreferences.getInstance().getSelectedMap();

        if(tableDefinition != null && tableDefinition.snd != null) {
            nonLinearTable.setMap(tableDefinition.snd);
        }

        nonLinearTable.getPublishSubject().subscribe(new Observer<Map3d>() {
            @Override
            public void onNext(@NonNull Map3d map3d) {

                Pair<TableDefinition, Map3d> kfldimxTableDefinition = KfldimxPreferences.getInstance().getSelectedMap();
                Pair<TableDefinition, Map3d> kfldrlTableDefinition = KfldrlPreferences.getInstance().getSelectedMap();

                Map3d linearMap3d = LdrpidCalculator.calculateLinearTable(map3d.zAxis, kfldrlTableDefinition.snd);
                Map3d kfldrlMap3d = LdrpidCalculator.calculateKfldrl(map3d.zAxis, linearMap3d.zAxis, kfldrlTableDefinition.snd);
                Map3d kfldimxMap3d = LdrpidCalculator.calculateKfldimx(map3d.zAxis, linearMap3d.zAxis, kfldrlTableDefinition.snd, kfldimxTableDefinition.snd);

                linearTable.setMap(linearMap3d);
                kfldrlTable.setMap(kfldrlMap3d);

                Double[][] kfldImxXAxisValues = new Double[1][];
                kfldImxXAxisValues[0] = kfldimxMap3d.xAxis;
                kfldimxXAxis.setTableData(kfldImxXAxisValues);
                kfldimxTable.setMap(kfldimxMap3d);
            }

            @Override
            public void onSubscribe(@NonNull Disposable disposable) {
            }

            @Override
            public void onError(@NonNull Throwable throwable) {
            }

            @Override
            public void onComplete() {
            }
        });
    }

    private void initLinearMap() {
        Pair<TableDefinition, Map3d> tableDefinition = KfldrlPreferences.getInstance().getSelectedMap();

        linearTable.setEditable(false);

        if(tableDefinition != null && tableDefinition.snd != null) {
            linearTable.setMap(tableDefinition.snd);
        }
    }
}
