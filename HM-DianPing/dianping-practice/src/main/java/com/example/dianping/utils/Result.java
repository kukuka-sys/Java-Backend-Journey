package com.example.dianping.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result {
    private Integer status;
    private String message;
    private Object data;
    public static Result ok(Object data) {
        return new Result(1,null,data);
    }
    public static Result ok() {
        return new Result(1,null,null);
    }
    public static Result fail(String message) {
        return new Result(0,message,null);
    }




}
