package edge.slogs;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.format.DateTimeFormatter;

/**
 * 日志对象
 */
@SuppressWarnings("all")
public class Logger {

    private String name;

    private SLogManager sLogManager;

    private static final Pattern pattern = Pattern.compile("\\{\\}");


    protected Logger(String name) {
        this.name = name;
        this.sLogManager = SLogManager.getInstance();
    }


    /**
     * 致命错误
     */
    public void fatal(String input, Object... values) {
        log(LogLevelType.FATAL, input, values);
    }

    public void error(String input, Object... values) {
        log(LogLevelType.ERROR, input, values);
    }

    public void warn(String input, Object... values) {
        log(LogLevelType.WARN, input, values);
    }

    public void info(String input, Object... values) {
        log(LogLevelType.INFO, input, values);
    }

    public void debug(String input, Object... values) {
        log(LogLevelType.DEBUG, input, values);
    }


    public void trace(String input, Object... values) {
        log(LogLevelType.TRACE, input, values);
    }


    private void log(LogLevelType level, String input, Object... values) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stackTraceElements[2];
        // 提取类名、方法名和行号
        String className = caller.getClassName();
        String methodName = caller.getMethodName();
        int lineNumber = caller.getLineNumber();
        String message = format(input, values);
        // 获取当前线程
        Thread currentThread = Thread.currentThread();
        // 获取线程ID
        long threadId = currentThread.getId();
        this.sLogManager.write(level, className, methodName, lineNumber, message, threadId);
    }


    private String format(String input, Object... values) {
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return MessageFormat.format(input, values);
        }
        if (values.length == 0) {
            return input;
        }
        try {
            return String.format(input, values);
        } catch (IllegalArgumentException e) {
            // 如果格式化失败（例如，提供的值与占位符不匹配），返回原始字符串
            return input;
        }
    }
}
