package edge.slogs;

/**
 * 日志级别
 */
public enum LogLevelType {


    OFF("OFF",-1, "最高级别，关闭所有日志记录"),
    FATAL("FATAL",100, "致命错误，表示系统无法继续运行的情况"),
    ERROR("ERROR",5, "错误事件，但可能允许系统继续运行"),
    WARN("WARN",4, "警告，指潜在的问题或不期望发生的情况"),
    INFO("INFO",3, "信息消息，用来报告应用程序正常运行时的状态信息"),
    DEBUG("DEBUG",2, "调试信息，通常用于开发阶段，帮助追踪问题"),
    TRACE("TRACE",1, "追踪信息，比 DEBUG 级别更低，提供了更加详细的程序执行流程信息"),
    ALL("ALL",0, "最低级别，打开所有级别的日志记录");

    private String name;

    private int code;
    private String mark;

    LogLevelType() {
    }

    LogLevelType(String name, int code, String mark) {
        this.name = name;
        this.code = code;
        this.mark = mark;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

    public String getMark() {
        return mark;
    }
}
