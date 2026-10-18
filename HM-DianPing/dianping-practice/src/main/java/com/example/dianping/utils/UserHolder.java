package com.example.dianping.utils;

import com.example.dianping.dto.UserDTO;

public class UserHolder  {

    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public UserHolder(){}

    public static void set(UserDTO user){
        tl.set(user);
    }
    public static UserDTO get(){
        return tl.get();
    }

    public static void remove(){
        tl.remove();
    }

}
