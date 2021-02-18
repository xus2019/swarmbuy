package com.inotee.swarmbuy.controller;

import com.alibaba.fastjson.JSON;
import com.inotee.swarmbuy.common.util.RedisUtil;
import com.inotee.swarmbuy.entity.MiaoshaUser;
import com.inotee.swarmbuy.entity.constant.MiaoShaStatusEnum;
import com.inotee.swarmbuy.entity.result.Result;
import com.inotee.swarmbuy.entity.vo.GoodsDetailVo;
import com.inotee.swarmbuy.entity.vo.GoodsVo;
import com.inotee.swarmbuy.service.GoodsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/goods")
public class GoodsController {

    private final long htmlCacheTime = 60 * 60 * 12;

    @Resource
    GoodsService goodsService;

    @Resource
    RedisUtil redisUtil;

    @Resource
    ThymeleafViewResolver thymeleafViewResolver;

    @Resource
    ApplicationContext applicationContext;


    /**
     * 功能描述:优化前 jmeter记录
     *
     * @param: QPS ： 289
     * @return: 请求数： 5000 * 10
     * @auther: inotee
     * @date: 2020/9/3 10:08
     */
    @RequestMapping(value = "/to_list", produces = "text/html")
    @ResponseBody //加上页面缓存之后注意要加上这个注解
    public String list(Model model, MiaoshaUser user,
                       HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute("user", user);

        String html = (String) redisUtil.get("html:goodsList");
        if (!StringUtils.isEmpty(html)) {
            return html;
        }

        List<GoodsVo> goodsList = goodsService.queryGoodsList();
        model.addAttribute("goodsList", goodsList);

        //手动渲染
        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list", webContext);
        if (!StringUtils.isEmpty(html)) {
            redisUtil.set("html:goodsList", html, htmlCacheTime);
        }
        return html;
    }

    /**
     * @param:
     * @return:
     * @Description: 页面缓存
     */
    @RequestMapping(value = "/to_detail2/{goodsId}", produces = "text/html")
    @ResponseBody
    public String detail2(Model model, MiaoshaUser user, @PathVariable("goodsId") long goodsId,
                          HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute("user", user);

        //取缓存
        String html = (String) redisUtil.get("goodsDetail" + goodsId);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }

        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods", goods);

        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int miaoshaStatus = 0;
        int remainSeconds = 0;
        if (now < startAt) {
            miaoshaStatus = MiaoShaStatusEnum.NOT_BEGIN.getValue();
            remainSeconds = (int) ((startAt - now) / 1000);
        } else if (now > endAt) {//秒杀已经结束
            miaoshaStatus = MiaoShaStatusEnum.ALREADY_END.getValue();
            remainSeconds = -1;
        } else {//秒杀进行中
            miaoshaStatus = MiaoShaStatusEnum.BEGIN_ING.getValue();
            remainSeconds = 0;
        }
        model.addAttribute("miaoshaStatus", miaoshaStatus);
        model.addAttribute("remainSeconds", remainSeconds);

        //手动渲染
        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", webContext);
        if (!StringUtils.isEmpty(html)) {
            redisUtil.set("goodsDetail" + goodsId, html, htmlCacheTime);
        }
        return html;
    }

    /**
     * @param:
     * @return:
     * @Description: 页面静态化 ，页面1跳转至页面2 , 页面2再请求controller获取数据
     */
    @RequestMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(MiaoshaUser user, @PathVariable("goodsId") long goodsId) {

        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int miaoshaStatus = 0;
        int remainSeconds = 0;
        if (now < startAt) {
            miaoshaStatus = MiaoShaStatusEnum.NOT_BEGIN.getValue();
            remainSeconds = (int) ((startAt - now) / 1000);
        } else if (now > endAt) {//秒杀已经结束
            miaoshaStatus = MiaoShaStatusEnum.ALREADY_END.getValue();
            remainSeconds = -1;
        } else {//秒杀进行中
            miaoshaStatus = MiaoShaStatusEnum.BEGIN_ING.getValue();
            remainSeconds = 0;
        }

        GoodsDetailVo build = GoodsDetailVo.builder()
                .goods(goods)
                .user(user)
                .remainSeconds(remainSeconds)
                .miaoshaStatus(miaoshaStatus)
                .build();
        log.info(JSON.toJSONString(build));
        return Result.success(build);
    }
}
