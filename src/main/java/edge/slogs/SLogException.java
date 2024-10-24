package edge.slogs;

/**
 * 日志错误信息
 */
public class SLogException extends RuntimeException {

    public SLogException() {
        super("slog module error!");
    }

    public SLogException(String message) {
        super(message);
    }
}
