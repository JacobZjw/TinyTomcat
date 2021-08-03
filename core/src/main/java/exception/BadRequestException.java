package exception;

import util.HttpStatus;

/**
 * @author JwZheng
 * @date 2021/7/28 13:10
 */
public class BadRequestException extends ServletException {
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST,message);
    }

    @Override
    public String toString() {
        return "BadRequestException{" +
                "status=" + status +
                ", messages='" + messages + '\'' +
                '}';
    }
}
