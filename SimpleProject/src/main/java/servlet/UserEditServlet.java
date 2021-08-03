package servlet;

import domain.User;
import exception.ServletException;
import http.Request;
import http.Response;
import lombok.extern.slf4j.Slf4j;
import service.UserService;


@Slf4j
public class UserEditServlet extends HttpServlet {
    private UserService userService;

    public UserEditServlet() {
        userService = UserService.getInstance();
    }


    @Override
    public void doGet(Request request, Response response) throws ServletException {
        User user = userService.findByUsername((String) request.getSession().getAttribute("username"));
        request.setAttribute("user", user);
        request.getRequestDispatcher("/views/userEdit.html").forward(request, response);
    }

    @Override
    public void doPost(Request request, Response response) throws ServletException {
        User user = new User();
        user.setUsername((String) request.getSession(false).getAttribute("username"));
        user.setRealName(request.getParameter("realName"));
        user.setAge(Integer.valueOf(request.getParameter("age")));
        userService.update(user);

        request.setAttribute("user", user);
        request.getRequestDispatcher("/views/user.html").forward(request, response);
    }
}
