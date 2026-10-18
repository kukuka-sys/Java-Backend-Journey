package com.example.dianping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_shop")
public class Shop {
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private Long typeId;
    private String name;
    private String images;
    private String area;
    private String address;
    private Long avgPrice;
    private Integer score;
    private Integer comments;
    private String openHours;


}
