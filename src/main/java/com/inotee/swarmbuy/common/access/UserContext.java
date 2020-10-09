package com.inotee.swarmbuy.common.access;

import com.inotee.swarmbuy.entity.MiaoshaUser;

/**
 * @author : iNotee
 * @date : 2020-10-07
 **/
public class UserContext {
    private static ThreadLocal<MiaoshaUser> userHolder = new ThreadLocal<>();

    public static void setUser(MiaoshaUser user){
        userHolder.set(user);
    }
    public static MiaoshaUser getUser(){
        return userHolder.get();
    }
}
