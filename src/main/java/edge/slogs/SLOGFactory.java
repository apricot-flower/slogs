package edge.slogs;

/**
 * 日志工厂
 */
public class SLOGFactory {



    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    public static Logger getLogger(String name) {
        return new Logger(name);
    }

}
