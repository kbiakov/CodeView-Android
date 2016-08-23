package io.github.kbiakov.codeviewexample;

import android.app.Application;

import io.github.kbiakov.codeview.classifier.CodeProcessor;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // train classifier on app start
        CodeProcessor.init(this);
    }
}
