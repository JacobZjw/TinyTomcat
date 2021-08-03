package filter;

import cn.hutool.core.util.ArrayUtil;
import http.Request;
import http.Response;

import java.util.List;

/**
 * @author JwZheng
 * @date 2021/8/3 14:35
 */
public class StandardFilterChain implements FilterChain {
    private final Filter[] filters;
    private int index;

    public StandardFilterChain(List<Filter> filters) {
        this.filters = ArrayUtil.toArray(filters, Filter.class);
        this.index = 0;
    }

    @Override
    public void doFilter(Request request, Response response) {
        if (index < filters.length) {
            filters[index++].doFilter(request, response, this);
        }
    }
}
