package servlet;

import exception.ServletException;
import http.Request;
import http.Response;
import lombok.extern.slf4j.Slf4j;
import service.UserService;


@Slf4j
public class LoginServlet extends HttpServlet {

    private UserService userService;

    public LoginServlet() {
        userService = UserService.getInstance();
    }

    @Override
    public void init() {
        log.info("LoginServlet init...");
    }

    @Override
    public void destroy() {
        log.info("LoginServlet destroy...");
    }

    @Override
    public void doGet(Request request, Response response) throws ServletException {
        String username = (String) request.getSession(true).getAttribute("username");
        if (username != null) {
            log.info("已经登录，跳转至success页面");
            response.sendRedirect("/views/success.html");
        } else {
            request.getRequestDispatcher("/views/login.html").forward(request, response);
        }
    }

    @Override
    public void doPost(Request request, Response response) throws ServletException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        if (userService.login(username, password)) {
            log.info("{} 登录成功", username);
            request.getSession(true).setAttribute("username", username);
            response.sendRedirect("/views/success.html");
        } else {
            log.info("登录失败");
            response.sendRedirect("/views/errors/400.html");
        }
    }
}
