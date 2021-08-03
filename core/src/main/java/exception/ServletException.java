package exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import util.HttpStatus;

/**
 * @author JwZheng
 * @date 2021/7/28 13:06
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServletException extends Exception{
    protected HttpStatus status;
    protected String messages;

    @Override
    public String toString() {
        return "ServletException{" +
                "status=" + status +
                ", messages='" + messages + '\'' +
                '}';
    }
}
