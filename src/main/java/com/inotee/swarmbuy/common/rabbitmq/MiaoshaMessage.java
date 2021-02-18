package com.inotee.swarmbuy.common.rabbitmq;

import com.inotee.swarmbuy.entity.MiaoshaUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Auther: inotee
 * @Date: 2020/9/11 20:22
 * @Description:
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MiaoshaMessage {
    private MiaoshaUser user;
    private long goodsId;
}
