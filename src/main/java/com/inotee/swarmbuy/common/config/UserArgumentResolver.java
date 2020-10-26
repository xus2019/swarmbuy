package com.inotee.swarmbuy.common.config;

import com.inotee.swarmbuy.common.access.UserContext;
import com.inotee.swarmbuy.entity.MiaoshaUser;
import com.inotee.swarmbuy.service.LoginService;
import com.inotee.swarmbuy.service.impl.LoginServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义接口参数解析器
 */
@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * 判断是否是支持的参数类型
     * @return clazz== MiaoshaUser.class 如果是MiaoshaUser 做解析参数，则必须走下面的解析方法
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> clazz = parameter.getParameterType();
        return clazz== MiaoshaUser.class;
    }
    /**
     * 如果是支持的参数类型，则调用此方法解析参数
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        return UserContext.getUser();
    }
}
