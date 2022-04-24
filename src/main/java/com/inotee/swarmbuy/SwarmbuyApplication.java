package com.inotee.swarmbuy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * change test
 */
@MapperScan("com.inotee.swarmbuy.mapper")
@SpringBootApplication
public class SwarmbuyApplication /*extends SpringBootServletInitializer*/ {

    public static void main(String[] args) {
        SpringApplication.run(SwarmbuyApplication.class, args);
    }

/*    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        //return super.configure(builder);
        return builder.sources(SwarmbuyApplication.class);
    }*/
}

