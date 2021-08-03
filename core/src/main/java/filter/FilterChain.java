package filter;

import http.Request;
import http.Response;

/**
 * @author JwZheng
 * @date 2021/7/31 16:12
 */
public interface FilterChain {
    void doFilter(Request request, Response response) ;
}
