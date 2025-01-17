package parser.me7log;

import contract.Me7LogFileContract;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class ClosedLoopLogParser {

    private final PublishSubject<Map<Me7LogFileContract.Header, List<Double>>> publishSubject;

    private static ClosedLoopLogParser instance;

    public static ClosedLoopLogParser getInstance() {
        if (instance == null) {
            synchronized (ClosedLoopLogParser.class) {
                if (instance == null) {
                    instance = new ClosedLoopLogParser();
                }
            }
        }

        return instance;
    }

    private ClosedLoopLogParser() {
        publishSubject = PublishSubject.create();
    }

    public void registerClosedLoopLogOnChangeObserver(Observer<Map<Me7LogFileContract.Header, List<Double>>> observer){
        SwingUtilities.invokeLater(() -> publishSubject.subscribe(observer));
    }

    public void loadDirectory(File directory) {
        if (directory.isDirectory()) {
            Me7LogParser me7LogParser = new Me7LogParser();
            Single.fromCallable(() -> me7LogParser.parseLogDirectory(Me7LogParser.LogType.CLOSED_LOOP, directory, (value, max) -> {
            })).subscribeOn(Schedulers.io()).subscribe(new SingleObserver<Map<Me7LogFileContract.Header, List<Double>>>() {
                @Override
                public void onSubscribe(@NonNull Disposable disposable) {}

                @Override
                public void onSuccess(@NonNull Map<Me7LogFileContract.Header, List<Double>> logMap) {
                    SwingUtilities.invokeLater(() -> publishSubject.onNext(logMap));
                }

                @Override
                public void onError(@NonNull Throwable throwable) {}
            });
        }
    }
}
