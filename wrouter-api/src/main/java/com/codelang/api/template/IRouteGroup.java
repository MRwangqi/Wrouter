package com.codelang.api.template;

import java.util.Map;

/**
 * @author wangqi
 * @since 2018/6/5 11:00
 */

public interface IRouteGroup {

    void loadMap(Map<String, Class> routes);
}
