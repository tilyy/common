package com.tyq.common.uitl;

import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;

import com.tyq.common.BuildConfig;
import com.tyq.common.CommonInit;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * @author zhengyongfa
 * @date 2021/1/4 16:45
 * @description 简述类的作用
 */
public class LogFileTree extends Timber.Tree {

    public static boolean isShow = false;

    public static void setIsShow(boolean isShow) {
        LogFileTree.isShow = isShow;
    }

    public static void log(String tag, final String message) {
        log(tag, message, null);
    }

    public static void log(final String tag, final String message, final Throwable t) {
        logcat(Log.ERROR, tag, message, t);
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) {
                try {
                    saveLogcat(tag, message, t);
                    emitter.onNext(true);
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io()).subscribe(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
            }

            @Override
            public void onError(Throwable e) {
                Log.e("TAG", "onError: " + e.getMessage(), e);
            }

            @Override
            public void onComplete() {
            }
        });
    }

    @Override
    protected void log(int priority, @Nullable String tag, @NotNull String message, @Nullable Throwable t) {
        if (!BuildConfig.DEBUG || !isShow) {
            return;
        }

        logcat(priority, tag, message, t);
        log(tag, message, t);
    }

    private static void logcat(int priority, String tag, String message, Throwable t) {
        String tag1 = tag == null ? "TAG" : tag;
        switch (priority) {
            case Log.VERBOSE:
                if (t == null) {
                    Log.v(tag1, message);
                } else {
                    Log.v(tag1, message, t);
                }
                break;
            case Log.DEBUG:
                if (t == null) {
                    Log.d(tag1, message);
                } else {
                    Log.d(tag1, message, t);
                }
                break;
            case Log.INFO:
                if (t == null) {
                    Log.i(tag1, message);
                } else {
                    Log.i(tag1, message, t);
                }
                break;
            case Log.WARN:
                if (t == null) {
                    Log.w(tag1, message);
                } else {
                    Log.w(tag1, message, t);
                }
                break;
            case Log.ERROR:
            default:
                if (t == null) {
                    Log.e(tag1, message);
                } else {
                    Log.e(tag1, message, t);
                }
                break;
        }
    }

    /**
     * 保存日志
     */
    private static void saveLogcat(@Nullable String tag, @NotNull String message, @Nullable Throwable t) throws Exception {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sb.append(sdf.format(new Date())).append("  日志：");
        sb.append("[").append(tag).append("] {").append(message).append("}\n");

        Writer writer = null;
        PrintWriter pw = null;
        FileOutputStream fos = null;

        if (t != null) {
            try {
                writer = new StringWriter();
                pw = new PrintWriter(writer);
                t.printStackTrace(pw);

                Throwable cause = t.getCause();
                // 循环着把所有的异常信息写入writer中
                while (cause != null) {
                    cause.printStackTrace(pw);
                    cause = cause.getCause();
                }
                String result = writer.toString();
                sb.append("错误信息：").append("\n").append(result);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (pw != null) {
                    pw.close();
                }
                if (writer != null) {
                    writer.close();
                }
            }
        }

        // 目录
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + "_Log/" + CommonInit.getContext().getPackageName() + File.separator);

        if (!dir.exists()) {
            dir.mkdirs();
        }
        SimpleDateFormat fileFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            fos = new FileOutputStream(new File(dir, fileFormat.format(new Date())), true);
            fos.write(sb.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                fos.flush();
                fos.close();
            }
        }
    }
}