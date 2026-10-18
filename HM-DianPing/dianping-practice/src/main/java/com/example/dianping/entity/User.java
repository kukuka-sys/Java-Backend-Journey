package com.example.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_user")
public class User {
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;
    private String nickName;
    private String loginName;
    private String introduceSign;
    private String address;
    private Integer isDeleted;
    private Integer lockedFlag;
    private LocalDateTime createTime;

}
