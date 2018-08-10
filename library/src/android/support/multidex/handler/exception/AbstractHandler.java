package android.support.multidex.handler.exception;


import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public abstract class AbstractHandler implements ExceptionHandler {

    @Override
    public boolean handle(Context context, Throwable throwable, String stackTraceString) {
        if (!needHandle(throwable, stackTraceString)) {
            return false;
        }
        return handle(context);
    }

    public boolean needHandle(Throwable throwable, String stackTraceString) {

        if (throwable == null) {
            return false;
        }

        boolean stackTraceEmpty = TextUtils.isEmpty(stackTraceString);
        if (!stackTraceEmpty && match(stackTraceString)) {
            return true;
        }

        String message = throwable.getMessage();
        if (!TextUtils.isEmpty(message) && match(stackTraceString)) {
            return true;
        }

        if (stackTraceEmpty) {
            stackTraceString = Log.getStackTraceString(throwable);
            if (!TextUtils.isEmpty(stackTraceString) && match(stackTraceString)) {
                return true;
            }
        }
        return false;
    }

    public abstract boolean match(String msg);

    public abstract boolean handle(Context context);
}
