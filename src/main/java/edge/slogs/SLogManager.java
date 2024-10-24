package edge.slogs;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 日志主类
 */
public class SLogManager {

    private static final String DEFAULT_LOG_PATH = "./logs";
    private static final String DEFAULT_LOG_NAME = "slog";

    private static SLogManager instance;

    public static ExecutorService SCHEDULED_SERVICE;


    /**
     * 创建一个默认的日志经理人
     * @return
     */
    protected static void build() {
        if (instance != null) {
            throw new SLogException("A log model has been initialized!");
        }
        instance = new SLogManager(DEFAULT_LOG_PATH, DEFAULT_LOG_NAME, 0, LogSplitType.NONE, 0, 0, true, false, true, true, LogLevelType.ALL, LogLevelType.ALL, 0);
    }


    /**
     * 自定义一个日志
     * @param logPath 日志地址
     * @param fileName 日志文件名称前缀
     * @param fileMaxSize 单文件大小，单位MB
     * @param splitType 分割格式
     * @param layout 最多保存几个文件备份
     * @param packageSize  压缩包设置，多久打包；基于分割格式打包，可以理解成打包的数量
     * @param console  是否输出到控制台， 默认输出
     * @param splitByLogLevel  是否按照日志级别拆分成文件
     * @param locationInfo  是否输出类名和行数
     * @param threadPrinting  是否输出线程名
     * @param consoleLogLevel  控制台最低日志级别
     * @param logFileLevel  日志文件最低日志级别
     */
    protected static void build(String logPath, String fileName, int fileMaxSize, LogSplitType splitType, int layout, int packageSize, boolean console, boolean splitByLogLevel, boolean locationInfo, boolean threadPrinting, LogLevelType consoleLogLevel, LogLevelType logFileLevel, int poolSize) {
        if (instance != null) {
            throw new SLogException("A log model has been initialized!");
        }
        instance = new SLogManager(logPath, fileName, fileMaxSize, splitType, layout, packageSize, console, splitByLogLevel, locationInfo, threadPrinting, consoleLogLevel, logFileLevel, poolSize);
    }

    protected SLogManager(String logPath, String fileName, int fileMaxSize, LogSplitType splitType, int layout, int packageSize, boolean console, boolean splitByLogLevel, boolean locationInfo, boolean threadPrinting, LogLevelType consoleLogLevel, LogLevelType logFileLevel, int poolSize) {
        this.logPath = logPath;
        this.fileName = fileName;
        this.fileMaxSize = fileMaxSize;
        this.splitType = splitType;
        this.layout = layout;
        this.packageSize = packageSize;
        this.console = console;
        this.splitByLogLevel = splitByLogLevel;
        this.locationInfo = locationInfo;
        this.threadPrinting = threadPrinting;
        this.consoleLogLevel = consoleLogLevel;
        this.logFileLevel = logFileLevel;
        SCHEDULED_SERVICE = poolSize == 0 ? null : Executors.newFixedThreadPool(poolSize);
        init();
    }



    private void init() {
        this.inJar = checkIfRunningFromJar();
        //判断是否存在目录，没有就创建
        File directory = new File(this.logPath);
        if (directory.exists()) {
           return;
        }
        boolean isCreated = directory.mkdirs();
        if (isCreated) {
            throw new SLogException("create " + this.logPath + " fail!");
        }
    }

    private static boolean checkIfRunningFromJar() {
        // 获取当前运行类的类加载器
        ClassLoader classLoader = SLogManager.class.getClassLoader();

        // 获取URLs组成的数组，该数组代表了查找类和资源的搜索路径
        URL[] urls = ((URLClassLoader) classLoader).getURLs();

        for (URL url : urls) {
            String protocol = url.getProtocol();
            // 如果协议是"jar"，则说明这个URL指向的是JAR文件中的内容
            if ("jar".equals(protocol)) {
                return true;
            }
        }
        return false;
    }

    public static SLogManager getInstance() {
        if (instance == null) {
            throw new SLogException("The log model has not been initialized!");
        }
        return instance;
    }

    /**
     * 日志地址
     */
    private String logPath;

    /**
     * 日志文件名称
     */
    private String fileName;


    /**
     * 单文件大小，单位MB
     */
    private int fileMaxSize;

    /**
     * 分割格式
     */
    private LogSplitType splitType;


    /**
     * 最多保存几个文件备份
     */
    private int layout;

    /**
     * 压缩包设置，多久打包；基于分割格式打包，可以理解成打包的数量
     */
    private int packageSize;

    /**
     * 是否输出到控制台， 默认输出
     */
    private boolean console;

    /**
     * 是否按照日志级别拆分成文件
     */
    private boolean splitByLogLevel;


    /**
     * 是否输出类名和行数
     */
    private boolean locationInfo;

    /**
     * 是否输出线程名
     */
    private boolean threadPrinting;

    /**
     * 控制台最低日志级别
     */
    private LogLevelType consoleLogLevel;

    /**
     * 日志文件最低日志级别
     */
    private LogLevelType logFileLevel;


    /**
     * 当前是不是jar包
     */
    private boolean inJar;


    private String now() {// 获取当前时间
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        // 定义时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SS");
        // 格式化当前时间
        return now.format(formatter);
    }

    /**
     * 写入日志
     */
    protected synchronized void write(LogLevelType level, String className, String methodName, int lineNumber, String message, long threadId) {
        //拼接fullMessage
        String fullMessage;
        if (this.locationInfo && this.threadPrinting) {
            fullMessage = String.format("%s [%s] %s:%s line:%d Thread-ID: %d %s", now(), level.getName(), className, methodName, lineNumber, threadId, message);
        } else {
            fullMessage = String.format("%s [%s] %s", now(), level.getName(), message);
        }
        if (SCHEDULED_SERVICE != null) {
            SCHEDULED_SERVICE.submit(() ->{
                //写入控制台
                writeToConsole(level, fullMessage);
                //写入日志文件
                writeToLogFile(level, fullMessage);
            });
            return;
        }
        //写入控制台
        writeToConsole(level, fullMessage);
        //写入日志文件
        writeToLogFile(level, fullMessage);
    }

    //写入控制台
    private void writeToConsole(LogLevelType level, String fullMessage) {
        if (!this.console || this.consoleLogLevel == LogLevelType.OFF || level.getCode() < this.consoleLogLevel.getCode() || this.inJar) {
            return;
        }
        //写入控制台
        System.out.println(fullMessage);
    }

    //写入日志文件
    private void writeToLogFile(LogLevelType level, String fullMessage) {
        //判断日志级别
        if (this.logFileLevel == LogLevelType.OFF || level.getCode() < this.logFileLevel.getCode()) {
            return;
        }
        //是否输出到一个统一的日志文件， 如果输出到一个统一的日志文件
        if (!this.splitByLogLevel) {
            //String fileName =


            return;
        }
        //是否按照日志级别拆分成文件
        //判断分割形式
        //判断单文件大小
        //判断压缩包设置

    }
}
