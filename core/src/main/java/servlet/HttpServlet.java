package servlet;

import exception.ServletException;
import http.Request;
import http.Response;
import util.HttpStatus;
import util.RequestMethod;

/**
 * @author JwZheng
 * @date 2021/7/23 15:36
 */
public abstract class HttpServlet implements Servlet {
    public abstract void doGet(Request request, Response response) throws ServletException;

    public abstract void doPost(Request request, Response response) throws ServletException;

    @Override
    public void service(Request request, Response response) throws ServletException {
        if (RequestMethod.GET.equals(request.getMethod())) {
            doGet(request, response);
        } else if (RequestMethod.POST.equals(request.getMethod())) {
            doPost(request, response);
        } else {
            throw new ServletException(HttpStatus.METHOD_NOT_ALLOWED, "不支持的方法");
        }
    }

    @Override
    public void init() throws ServletException {

    }

    @Override
    public void destroy() throws ServletException {

    }
}
