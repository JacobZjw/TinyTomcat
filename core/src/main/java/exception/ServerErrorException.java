package exception;

import util.HttpStatus;

/**
 * @author JwZheng
 * @date 2021/7/28 13:49
 */
public class ServerErrorException extends ServletException {
    public ServerErrorException(String messages) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, messages);
    }

    @Override
    public String toString() {
        return "ServerErrorException{" +
                "status=" + status +
                ", messages='" + messages + '\'' +
                '}';
    }
}
