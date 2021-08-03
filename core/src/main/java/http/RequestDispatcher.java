package http;

import catalina.connector.CoyoteAdapter;
import exception.ServletException;
import lombok.AllArgsConstructor;

/**
 * @author JwZheng
 * @date 2021/7/29 16:14
 */
@AllArgsConstructor
public class RequestDispatcher {
    private CoyoteAdapter coyoteAdapter;

    public void forward(Request request, Response response) throws ServletException {
        coyoteAdapter.doDispatch(request, response);
    }
}
