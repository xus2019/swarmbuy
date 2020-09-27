package com.inotee.swarmbuy.service;

import com.inotee.swarmbuy.entity.MiaoshaUser;
import com.inotee.swarmbuy.entity.vo.LoginVo;

import javax.servlet.http.HttpServletResponse;

public interface LoginService {
    Boolean loginRequest(HttpServletResponse response, LoginVo loginVo);
    //String loginRequest(HttpServletResponse response, LoginVo loginVo);

    MiaoshaUser getUserByToken(HttpServletResponse response,String token);
}
