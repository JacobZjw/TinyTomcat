package servlet;


import exception.ServletException;
import http.Request;
import http.Response;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class LogoutServlet extends HttpServlet {

    @Override
    public void doGet(Request request, Response response) throws ServletException {
        request.getRequestDispatcher("/views/logout.html").forward(request, response);
    }

    @Override
    public void doPost(Request request, Response response) throws ServletException {
        request.getSession().removeAttribute("username");
        request.getSession().invalidate();
        response.sendRedirect("/login");
    }

}
