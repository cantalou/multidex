package android.support.multidex.handler.exception;

import android.content.Context;

/**
 * handler for exception thrown when call MultiDex.install()
 */
public interface ExceptionHandler {

    boolean handle(Context context, Throwable throwable, String stackTraceString);
}
