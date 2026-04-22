package com.shangcheng.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductRequest {
    @NotBlank(message = "商品名称不能为空")
    private String name;
    
    private String description;
    
    @NotNull(message = "积分价格不能为空")
    @Min(value = 1, message = "积分价格必须大于0")
    private Integer price;
    
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock = 1;
}
