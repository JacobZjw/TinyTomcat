package catalina.connector;

import catalina.Context;
import catalina.Host;
import catalina.ServletWrapper;
import exception.ServletException;
import filter.Filter;
import filter.StandardFilterChain;
import http.Request;
import http.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import util.Handler;
import util.HttpStatus;

import java.util.List;
import java.util.Map;

/**
 * @author JwZheng
 * @date 2021/7/27 15:52
 */
@Data
@Slf4j
@AllArgsConstructor
public class CoyoteAdapter {
    private Connector connector;

    public void doDispatch(Request request, Response response) throws ServletException {
        for (Host host : connector.getEngine().getHosts()) {
            if (!host.getName().equals(request.getHost())) {
                continue;
            }
            String url = request.getUrl();
            for (Map.Entry<String, Context> entry : host.getContextMap().entrySet()) {
                String path = entry.getKey();
                Context context = entry.getValue();
                if (url.startsWith(path)) {
                    //TODO:多个context的选择问题
                    request.setContext(context);
                    response.setContext(context);
                    break;
                }
            }
            if (request.getContext() != null) {
                break;
            }
        }
        service(request, response);
    }


    private void service(Request request, Response response) throws ServletException {
        Context context = request.getContext();
        if (context != null) {
            String urlPattern = request.getUrl();
            if (!"/".equals(context.getPath())) {
                urlPattern = urlPattern.replaceFirst(context.getPath(), "");
            }

            List<Filter> filters = context.getMatchFilters(urlPattern);
            if (!filters.isEmpty()) {
                StandardFilterChain filterChain = new StandardFilterChain(filters);
                filterChain.doFilter(request, response);
            }

            ServletWrapper servletWrapper = context.findServletWrapper(urlPattern);
            if (servletWrapper != null) {
                response.setStatus(HttpStatus.OK);
                servletWrapper.getServlet().service(request, response);
            } else {
                Handler.handleResource(request, response);
            }
        }
    }
}
