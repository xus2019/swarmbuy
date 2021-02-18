package com.inotee.swarmbuy.entity.vo;

import com.inotee.swarmbuy.entity.MiaoshaUser;
import lombok.*;

/**
 * @Auther: inotee
 * @Date: 2020/9/4 16:20
 * @Description:
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsDetailVo {
    private Integer miaoshaStatus;
    private Integer remainSeconds;
    private GoodsVo goods ;
    private MiaoshaUser user;
}
