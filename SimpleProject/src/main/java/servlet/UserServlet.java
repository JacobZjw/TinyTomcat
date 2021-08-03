package servlet;

import domain.User;
import exception.ResourceNotFoundException;
import exception.ServletException;
import http.HttpSession;
import http.Request;
import http.Response;
import lombok.extern.slf4j.Slf4j;
import service.UserService;


@Slf4j
public class UserServlet extends HttpServlet {
    private final UserService userService;

    public UserServlet() {
        userService = UserService.getInstance();
    }

    @Override
    public void doGet(Request request, Response response) throws ServletException {
        HttpSession session = request.getSession();
        if (session == null){
//            response.sendRedirect("/login");
//            request.getRequestDispatcher("/login").forward(request, response);
            return;
        }
        User user = userService.findByUsername((String) session.getAttribute("username"));
        request.setAttribute("user", user);
        request.getRequestDispatcher("/views/user.html").forward(request, response);
    }

    @Override
    public void doPost(Request request, Response response) throws ServletException {
        doGet(request, response);
    }
}
