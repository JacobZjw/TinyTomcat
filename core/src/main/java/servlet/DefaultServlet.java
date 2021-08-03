package servlet;

import exception.ServletException;
import http.Request;
import http.Response;
import util.Handler;

/**
 * @author JwZheng
 * @date 2021/7/29 20:42
 */
public class DefaultServlet extends HttpServlet{
    @Override
    public void doGet(Request request, Response response) throws ServletException {
        Handler.handleResource(request,response);
    }

    @Override
    public void doPost(Request request, Response response) throws ServletException {
        doGet(request,response);
    }
}
