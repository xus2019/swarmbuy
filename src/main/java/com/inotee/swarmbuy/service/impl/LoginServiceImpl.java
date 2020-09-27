package com.inotee.swarmbuy.service.impl;

import com.inotee.swarmbuy.common.exception.GlobalException;
import com.inotee.swarmbuy.common.util.MD5Util;
import com.inotee.swarmbuy.common.util.RedisUtil;
import com.inotee.swarmbuy.common.util.UUIDUtil;
import com.inotee.swarmbuy.entity.MiaoshaUser;
import com.inotee.swarmbuy.entity.MiaoshaUserExample;
import com.inotee.swarmbuy.entity.result.CodeMsg;
import com.inotee.swarmbuy.entity.vo.LoginVo;
import com.inotee.swarmbuy.mapper.MiaoshaUserMapper;
import com.inotee.swarmbuy.service.LoginService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;

/**
 * @author : iNotee
 * @date : 2020-08-23
 **/
@Service
public class LoginServiceImpl implements LoginService {

    @Resource
    MiaoshaUserMapper userMapper;

    @Resource
    RedisUtil redisUtil;

    public static final String COOKIE_NAME = "token";

    public static final Integer TOKEN_EXPIRE = 3600*24 *2;

    /**
     * @param:
     * @return:
     * @Description: 对象缓存
     */
    public MiaoshaUser getById(long id) {
        //取缓存
        MiaoshaUser user= (MiaoshaUser) redisUtil.get("user:"+id);
        if(user!=null){
           return user;
        }
        //取不到再去db
        MiaoshaUserExample example = new MiaoshaUserExample();
        example.createCriteria().andIdEqualTo(id);
        List<MiaoshaUser> miaoshaUsers = userMapper.selectByExample(example);

        //存缓存
        if(!CollectionUtils.isEmpty(miaoshaUsers)){
            MiaoshaUser miaoshaUser = miaoshaUsers.get(0);
            redisUtil.set("user:"+id,miaoshaUser);
            return miaoshaUser;
        }
        return null;
    }

    //如果使用了缓存，更新db数据的时候一定要记得更新缓存
    public boolean updatePassword(long id,String formPass,String token){
        MiaoshaUser user = getById(id);
        if(user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //更新数据库
        MiaoshaUserExample example = new MiaoshaUserExample();
        example.createCriteria().andIdEqualTo(id);
        MiaoshaUser record = new MiaoshaUser();
        String newPassword = MD5Util.formPassToDBPass(formPass, user.getSalt());
        record.setPassword(newPassword);
        userMapper.updateByExampleSelective(record,example);

        //处理缓存：删除旧缓存，更新缓存
        redisUtil.del("user:"+id);
        user.setPassword(newPassword);
        redisUtil.set(token,user);
        return true;
    }


    @Override
    public Boolean loginRequest(HttpServletResponse response, LoginVo loginVo) {
        if (Objects.isNull(loginVo)) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();
        MiaoshaUserExample example = new MiaoshaUserExample();
        example.createCriteria().andIdEqualTo(Long.valueOf(mobile));
        List<MiaoshaUser> users = userMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(users)) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }

        MiaoshaUser user = users.get(0);
        //验证密码
        String dbPass = user.getPassword();
        String saltDB = user.getSalt();
        String calcPass = MD5Util.formPassToDBPass(formPass, saltDB);
        if (!calcPass.equals(dbPass)) {
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        //设置cookie
        String token = UUIDUtil.uuid();
        addCookie(response, token, user);
        return true;
    }


    @Override
    public MiaoshaUser getUserByToken(HttpServletResponse response, String token){
        if(StringUtils.isEmpty(token)) {
            return null;
        }
        MiaoshaUser user = (MiaoshaUser)redisUtil.get(token);
        //延长有效期
        if(user!=null){
            addCookie(response,token,user);
        }
        return user;
    }

    private void addCookie(HttpServletResponse response, String token, MiaoshaUser user) {
        redisUtil.set(token, user,TOKEN_EXPIRE);
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setMaxAge(TOKEN_EXPIRE);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

}
