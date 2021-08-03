package exception;

import util.HttpStatus;

/**
 * @author JwZheng
 * @date 2021/7/28 13:08
 */
public class ResourceNotFoundException extends ServletException {
    public ResourceNotFoundException(String messages) {
        super(HttpStatus.NOT_FOUND, messages);
    }

    @Override
    public String toString() {
        return "ResourceNotFoundException{" +
                "status=" + status +
                ", messages='" + messages + '\'' +
                '}';
    }
}
