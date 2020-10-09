package com.inotee.swarmbuy.common.access;

import com.alibaba.fastjson.JSON;
import com.inotee.swarmbuy.common.util.RedisUtil;
import com.inotee.swarmbuy.entity.MiaoshaUser;
import com.inotee.swarmbuy.entity.result.CodeMsg;
import com.inotee.swarmbuy.entity.result.Result;
import com.inotee.swarmbuy.service.LoginService;
import com.inotee.swarmbuy.service.impl.LoginServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

/**
 * @author : iNotee
 * @date : 2020-10-07
 * /**
 *   TODO 做什么的，什么原理
 *
 **/
@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {

    @Resource
    LoginService loginService;

            @Resource
            RedisUtil redisUtil;


            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                if(handler instanceof HandlerMethod){
                    MiaoshaUser user = this.getUser(request, response);
                    UserContext.setUser(user);
                    HandlerMethod handlerMethod = (HandlerMethod) handler;
                    AccessLimit accessLimit = handlerMethod.getMethodAnnotation(AccessLimit.class);
            if(accessLimit==null){
                return true;
            }
            int seconds =  accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();

            String key = request.getRequestURI();
            if(needLogin) {
                if(user == null) {
                    render(response, CodeMsg.SESSION_ERROR);
                    return false;
                }
                key += "_" + user.getId();
            }else {
                //do nothing
            }

            Integer count = (Integer) redisUtil.get(key);
            if(count == null){
                redisUtil.set(key,1,seconds);
            }else if(count <maxCount){
                redisUtil.incr(key,1);
            }else{
                render(response, CodeMsg.ACCESS_LIMIT_REACHED);
                return  false;
            }
        }
        return true;
    }

    private void render(HttpServletResponse response, CodeMsg cm)throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        String str  = JSON.toJSONString(Result.error(cm));
        out.write(str.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

    private MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response) {
        String paramToken = request.getParameter(LoginServiceImpl.COOKIE_NAME);
        String cookieToken = getCookieValue(request, LoginServiceImpl.COOKIE_NAME);

        if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        return loginService.getUserByToken(response,token);
    }

    private String getCookieValue(HttpServletRequest request, String cookiName) {
        Cookie[]  cookies = request.getCookies();
        if(cookies == null || cookies.length <= 0){
            return null;
        }
        for(Cookie cookie : cookies) {
            if(cookie.getName().equals(cookiName)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
