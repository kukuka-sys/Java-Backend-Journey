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
@TableName("tb_shop_type")
public class ShopType {
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private String name;
    private String icon;
    private Integer sort;
}
