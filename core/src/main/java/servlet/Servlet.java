package servlet;

import exception.ServletException;
import http.Request;
import http.Response;

/**
 * @author JwZheng
 * @date 2021/7/23 15:31
 */
public interface Servlet {
    void init() throws ServletException;
    void destroy() throws ServletException;
    void service(Request request, Response response) throws ServletException;

}
