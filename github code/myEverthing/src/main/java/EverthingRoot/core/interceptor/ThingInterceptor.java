package EverthingRoot.core.interceptor;

import EverthingRoot.core.model.Thing;

/**
 * 检索结果thing的拦截器
 */

public interface ThingInterceptor {

    void apply(Thing thing);
}
