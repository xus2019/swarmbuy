package com.inotee.swarmbuy.entity.vo;

import com.inotee.swarmbuy.common.validator.IsMobile;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author : iNotee
 * @date : 2020-08-23
 **/
@Data
public class LoginVo {
    @NotNull
    @IsMobile
    private String mobile;

    @NotNull
    @Length(min=32)
    private String password;
}
