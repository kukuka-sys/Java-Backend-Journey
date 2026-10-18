package com.example.dianping.service;

import com.example.dianping.dto.LoginFromDTO;
import com.example.dianping.utils.Result;

public interface UserService {
    Result sendCode(String phone);

    Result login(LoginFromDTO userDTO);
}
