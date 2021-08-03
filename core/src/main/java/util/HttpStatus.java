package util;

/**
 * @author JwZheng
 * @date 2021/7/27 21:11
 */
public enum HttpStatus {
    OK(200),
    ACCEPTED(202),
    MOVED_TEMP(302),
    BAD_REQUEST(400),
    NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),
    INTERNAL_SERVER_ERROR(500);
    private int code;

    HttpStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
