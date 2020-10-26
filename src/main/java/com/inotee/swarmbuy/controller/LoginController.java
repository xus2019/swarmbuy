package com.inotee.swarmbuy.controller;

import com.alibaba.fastjson.JSON;
import com.inotee.swarmbuy.entity.result.Result;
import com.inotee.swarmbuy.entity.vo.LoginVo;
import com.inotee.swarmbuy.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * @author : iNotee
 * @date : 2020-08-23
 **/
@Slf4j
@Controller
@RequestMapping("/login")
public class LoginController{

    @Resource
    LoginService loginService;

    @RequestMapping("/to_login")
    public String toLogin() {
        return "login";
    }

    @RequestMapping("/do_login")
    @ResponseBody
    public Result<Boolean> loginRequest(HttpServletResponse response , @Valid LoginVo loginVo){
        log.info(JSON.toJSONString(loginVo));
        loginService.loginRequest(response,loginVo);
        return Result.success(true);
    }

/*    @RequestMapping("/do_login")
    @ResponseBody
    public Result<String> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) {
        log.info(loginVo.toString());
        //登录
        String token = loginService.loginRequest(response,loginVo);
        return Result.success(token);
    }*/
}
