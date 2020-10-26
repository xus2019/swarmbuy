[TOC]



## Restful返回参数的封装

```java
public class Result<T> {
	private int code;
	private String msg;
	private T data;

	/**
	 * 成功时候的调用
	 * */
	public static <T> Result<T> success(T data){
		return new  Result<T>(data);
	}
	
	/**
	 * 失败时候的调用
	 * */
	public static <T> Result<T> error(CodeMsg cm){
		return new  Result<T>(cm);
	}
	
	private Result(T data) {
		this.code = 0;
		this.msg = "success";
		this.data = data;
	}
	
	private Result(CodeMsg cm) {
		if(cm == null) {
			return;
		}
		this.code = cm.getCode();
		this.msg = cm.getMsg();
	}

	public int getCode() {
		return code;
	}
	public String getMsg() {
		return msg;
	}
	public T getData() {
		return data;
	}
}

```



## 登录功能实现

### 两次MD5

HTTP在网络上是用明文来传输的，必须对用户密码进行加密

1.用户端，固定salt + 明文

2.服务端，前端参数 + 随机salt



#### 为什么要做两次MD5

第一次MD5（为了防止用户的密码明文在网络上传输） ：将用户输入的密码做一次加盐，将加盐后的密码传递给服务端

```js
//在前端实现
var inputPass = $("#password").val();
var salt = g_passsword_salt; // var g_passsword_salt="1a2b3c4d"
var str = ""+salt.charAt(0)+salt.charAt(2) + inputPass +salt.charAt(5) + salt.charAt(4);
var password = md5(str);
```



第二次MD5：服务端接收到上述密码后，会生成一个随机salt并对接收到的密码再进行一次MD5，并将密码与该salt同时存储到DB中

第二次MD5是为了防止数据库泄露，不法分子能通过彩虹表等手段将MD5值反推出密码。所以在服务端有必要再做一次MD5

```java
String dbPass = user.getPassword();
String saltDB = user.getSalt();
String calcPass = MD5Util.formPassToDBPass(formPass, saltDB);
if(!calcPass.equals(dbPass)) {
	throw new GlobalException(CodeMsg.PASSWORD_ERROR);
}
```





### 参数校验

#### springboot -validation使用

导包：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
    <version>{validation.version}</version>
</dependency>
```



在Controller入参中加上@Valid注解

```java
@RequestMapping("/do_login")
@ResponseBody
public Result<Boolean> loginRequest(HttpServletResponse response , @Valid LoginVo loginVo){
```



再去需要校验的参数上加上相应的注解

```java
@Data
public class LoginVo {
    @NotNull
    @IsMobile
    private String mobile;

    @NotNull
    @Length(min=32)
    private String password;
}
```

可使用的注解：

![](https://gitee.com/inotee/PicGoUp/raw/master/static/20201009162336.png)·



常用注解示例：

```java
@NotNull(message = ValidatorConstant.FILED_NOT_NULL)
    
@NotEmpty(message = ValidatorConstant.FILED_NOT_EMPTY)

@Size(min = 1, message = ValidatorConstant.FILED_NOT_SIZE)
List<@Valid StagePriceLineEntity> stagePriceLines;
```



#### 自定义验证器

自定义注解

```java
/**
 * 与校验器IsMobileValidator绑定 ，校验器需实现ConstraintValidator<IsMobile,String>接口 ，
 * 其中第一个泛型为注解，第二个为需要校验的参数的类型
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {IsMobileValidator.class }) 
public @interface  IsMobile {

    boolean required() default true; //是否必须

    String message() default "手机号码格式错误";  //如果校验不通过 提示什么信息

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
```



自定义校验器Validator

```java
public class IsMobileValidator implements ConstraintValidator<IsMobile,String> {

    private boolean required = false;
    /**
     * 初始化方法 ，可以拿到注解中的值
     * @param constraintAnnotation
     */
    @Override
    public void initialize(IsMobile constraintAnnotation) {
        required = constraintAnnotation.required();
    }

    /**
     * 判断数据是否有效
     * @return
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if(required) { //是否必须
            return ValidatorUtil.isMobile(value);
        }else {
            if(StringUtils.isEmpty(value)) {
                return true;
            }else {
                return ValidatorUtil.isMobile(value);
            }
        }
    }
}
```



手机号格式正则校验工具类

```java
public class ValidatorUtil {
	
	private static final Pattern mobile_pattern = Pattern.compile("1\\d{10}");
	
	public static boolean isMobile(String src) {
		if(StringUtils.isEmpty(src)) {
			return false;
		}
		Matcher m = mobile_pattern.matcher(src);
		return m.matches();
	}
}
```





### 全局异常处理器

`GlobalExceptionHandler` 拦截代码中抛出的异常

```java
@ControllerAdvice  //切面
@ResponseBody   //
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public Result<String> exceptionHandler(HttpServletRequest request, Exception e) {
        e.printStackTrace();
        if (e instanceof GlobalException) {
            GlobalException ex = (GlobalException) e;
            return Result.error(ex.getCm());
        }
        else if (e instanceof BindException) {
            BindException ex = (BindException) e;
            List<ObjectError> errors = ex.getAllErrors();
            ObjectError error = errors.get(0);
            String msg = error.getDefaultMessage();
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));
        } else {
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }
}
```



定义全局异常，根据业务需要自定义异常，出现异常情况直接抛出

```java
public class GlobalException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	
	private CodeMsg cm;
	
	public GlobalException(CodeMsg cm) {
		super(cm.toString());
		this.cm = cm;
	}

	public CodeMsg getCm() {
		return cm;
	}

}


public class CodeMsg {
	private int code;
	private String msg;
	
	//通用异常
	public static CodeMsg SUCCESS = new CodeMsg(0, "success");
	public static CodeMsg SERVER_ERROR = new CodeMsg(500100, "服务端异常");
	//登录模块 5002XX
	
	//商品模块 5003XX
	
	//订单模块 5004XX
	
	//秒杀模块 5005XX
	
	
	private CodeMsg(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
	public int getCode() {
		return code;
	}
	public String getMsg() {
		return msg;
	}
}
```

抛出之后，就会交由刚才定义的异常处理器进行处理

这样的好处是service返回值不必再带有异常信息，可以直接返回真正带业务含义的实体

举个例子：

在如下的 `loginRequest`方法中，有异常的情况都直接抛出，抛出的异常会被全局异常处理器`GlobalExceptionHandler`拦截，并输出响应信息

```java
    @RequestMapping("/do_login")
    @ResponseBody
    public Result<Boolean> loginRequest(HttpServletResponse response , @Valid LoginVo loginVo){
        log.info(JSON.toJSONString(loginVo));
        
        loginService.loginRequest(response,loginVo); //异常情况都抛出了并被全局异常处理器拦截了
        
        return Result.success(true);
    }
```



```java
@Override
public Boolean loginRequest(HttpServletResponse response, LoginVo loginVo) {
    if (Objects.isNull(loginVo)) {
        throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
    }
    String mobile = loginVo.getMobile();
    String formPass = loginVo.getPassword();
    MiaoshaUserExample example = new MiaoshaUserExample();
    example.createCriteria().andIdEqualTo(Long.valueOf(mobile));
    List<MiaoshaUser> users = userMapper.selectByExample(example);
    if (CollectionUtils.isEmpty(users)) {
        throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
    }

    MiaoshaUser user = users.get(0);
    //验证密码
    String dbPass = user.getPassword();
    String saltDB = user.getSalt();
    String calcPass = MD5Util.formPassToDBPass(formPass, saltDB);
    if (!calcPass.equals(dbPass)) {
        throw new GlobalException(CodeMsg.PASSWORD_ERROR);
    }
    //设置cookie
    String token = UUIDUtil.uuid();
    addCookie(response, token, user);
    return true;
}
```





### 分布式session

用户登录成功后，服务端生成用户的唯一token，将token信息写入cookie中并回传给客户端。以后客户端每一次访问都会将token携带在cookie中，服务端可通过该token来识别唯一用户。

实现：

> 生成用户的唯一token并写入到cookie中

```java
String token = UUIDUtil.uuid();
addCookie(response, token, user);
```

将用户的token信息存在缓存中并设置一定的过期时间(cookie的存活时间与缓存的存活时间保持一致)。用户登陆之后服务端就可以通过缓存中的token来取到用户的user信息

```java
    private void addCookie(HttpServletResponse response, 
                           String token, MiaoshaUser user) {
        redisUtil.set(token, user,TOKEN_EXPIRE); 
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setMaxAge(TOKEN_EXPIRE);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
```



> 定义接口参数解析器，在解析器中通过客户端请求中的token去缓存中获取到user信息

web配置文件

```java
/**
 * MVC的各种配置，包括拦截器，跨域处理等等 都在这里配置
 */

@Configuration
public class WebConfig  implements WebMvcConfigurer {

    @Resource
    UserArgumentResolver userArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(userArgumentResolver);
    }
}

```

自定义接口参数解析器

```java
@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * 判断是否是支持的参数类型
     *
     * @return clazz== MiaoshaUser.class 如果接口参数中有MiaoshaUser 类型，则必须走下面的解析方法
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> clazz = parameter.getParameterType();
        return clazz == MiaoshaUser.class;
    }

    /**
     * 如果是支持的参数类型，则调用此方法解析参数
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, 
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, 
                                  WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = 
            webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = 
            webRequest.getNativeResponse(HttpServletResponse.class);
        String paramToken = request.getParameter(MiaoshaUserService.COOKI_NAME_TOKEN);
        String cookieToken = getCookieValue(
            request, MiaoshaUserService.COOKI_NAME_TOKEN);
        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
        return userService.getByToken(response, token);
    }

    private String getCookieValue(HttpServletRequest request, String cookiName) {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookiName)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
```



> 延长有效期

这里的有效期应该是用户最后一次登录的时间+过期时间，所以在用户每一次通过token获取登录信息之后，需要更新缓存和cookie的过期时间

```java
public MiaoshaUser getUserByToken(HttpServletResponse response, String token){
    if(StringUtils.isEmpty(token)) {
        return null;
    }
    MiaoshaUser user = (MiaoshaUser)redisUtil.get(token);
    //延长有效期
    if(user!=null){
        addCookie(response,token,user);
    }
    return user;
}
```



### TODO

#### 单点登录



#### ThreadLocal



## 缓存

并发大的主要的瓶颈主要在数据库，所以最有效的办法是加缓存

### 页面缓存

> 修改前code

```java
    @RequestMapping("/to_list")
    public String list(Model model , MiaoshaUser user){
        model.addAttribute("user",user);
        List<GoodsVo> goodsList = goodsService.queryGoodsList();
        model.addAttribute("goodsList",goodsList);
        return "goods_list";
    }
```



> 修改后

使用Thymeleaf 提供的页面渲染工具：ThymeleafViewResolver 

```java
@Resource
ThymeleafViewResolver thymeleafViewResolver;    


	@RequestMapping(value = "/to_list",produces = "text/html")
	@ResponseBody //加上页面缓存之后注意要加上@ResponseBody注解
    public String list(Model model , MiaoshaUser user,
                       HttpServletRequest request, HttpServletResponse response){
        model.addAttribute("user",user);

        String html = (String) redisUtil.get("html:goodsList");
        if(!StringUtils.isEmpty(html)){ //如果能取到直接返回
            return html;
        }
        List<GoodsVo> goodsList = goodsService.queryGoodsList();
        model.addAttribute("goodsList",goodsList);

        //手动渲染
        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list", webContext);
        if(!StringUtils.isEmpty(html)){
            redisUtil.set("html:goodsList",html，htmlCacheTime);
        }
        return html;
    }
```





### 页面静态化

加了页面缓存后会出现问题，也就是页面不能及时更新，特别是像秒杀这种，需要精确到秒的，页面不能刷新，秒杀状态不能及时变更，所以就引入了页面静态化 --> 静态资源直接返回，只有动态资源，每一次都在变的数据才从服务端获取，也减轻了服务端的压力

也就是说，原来是客户端直接访问服务端，服务端将数据放入前端，再将前端返回给客户端，页面静态化之后，客户端在页面上直接跳转页面，由页面对服务端发起一个请求获取数据。



```java
    /**
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
        return Result.success(build);//直接返回需要的数据，而不是返回缓存中的html
    }
}
```

页面静态化就是通俗意义上的前后端分离，利用浏览器的缓存来减轻服务器的压力。



### 对象缓存

分布式session就是基于对象缓存

```java
    public MiaoshaUser getById(long id) {
        //取缓存
        MiaoshaUser user= (MiaoshaUser) redisUtil.get("user:"+id);
        if(user!=null){
           return user;
        }
        //取不到再去db
        MiaoshaUserExample example = new MiaoshaUserExample();
        example.createCriteria().andIdEqualTo(id);
        List<MiaoshaUser> miaoshaUsers = userMapper.selectByExample(example);

        //存缓存
        if(!CollectionUtils.isEmpty(miaoshaUsers)){
            MiaoshaUser miaoshaUser = miaoshaUsers.get(0);
            redisUtil.set("user:"+id,miaoshaUser);
            return miaoshaUser;
        }
        return null;
    }

    //如果使用了缓存，更新db数据的时候一定要记得更新缓存
    public boolean updatePassword(long id,String formPass,String token){
        MiaoshaUser user = getById(id);
        if(user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //更新数据库
        MiaoshaUserExample example = new MiaoshaUserExample();
        example.createCriteria().andIdEqualTo(id);
        MiaoshaUser record = new MiaoshaUser();
        String newPassword = MD5Util.formPassToDBPass(formPass, user.getSalt());
        record.setPassword(newPassword);
        userMapper.updateByExampleSelective(record,example);

        //处理缓存：删除旧缓存，更新缓存
        redisUtil.del("user:"+id);
        user.setPassword(newPassword);
        redisUtil.set(token,user);
        return true;
    }
```

> PS: 一定要先更新DB再去刷新缓存，考虑以下场景: 若先更新了缓存，此时用户又做了一次读操作(此时还没有更新数据库) , 那么旧的数据会被加载进缓存，就造成了DB与缓存数据不一致。



## 接口优化

### 优化目标

1.系统初始化，把商品库存数量加载到Redis

2.收到秒杀请求，Redis预减库存，若库存不足，直接返回，否则进入3

3.请求入队，立即返回排队中

4.请求出队，生成订单，减少库存

5.客户端轮询，是否秒杀成功



### Redis预减库存 & 内存标记

在系统初始化的时候将秒杀商品的库存数量存入redis

```java
public class MiaoshaController implements InitializingBean {
...
    //库存预热
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.queryGoodsList();
        if (goodsList == null) {
            return;
        }
        goodsList.forEach(goods -> {
            redisUtil.set("preStock" + goods.getId(), goods.getStockCount());
            localOverMap.put(goods.getId(), false);
        });
    }
...
}
```



在秒杀之前先判断对应秒杀商品是否还有库存，如果stock < 0 直接返回；

由于商品秒杀完之后仍会有大量请求落在redis上，为了减轻redis压力，需要进行内存标记。

```java
        //内存标记
        boolean over = localOverMap.get(goodsId);
        if (over) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        //预减库存，减到0之后过来的请求还是会判断redis，所以要加上内存标记
        long stock = redisUtil.decr("preStock" + goodsId, 1);
        if (stock < 0) {
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);

        }
```





### RabbitMQ异步下单

```java
@Configuration
public class MQConfig {
   
    public static final String MIAOSHA_QUEUE="miaosha.queue";


    @Bean
    public Queue miaoshaQueue(){
        return new Queue(MIAOSHA_QUEUE,true);
    }
    ...
```

异步下单

```java
public class MiaoshaController implements InitializingBean {
...
//入队
        mqSender.sendMiaoshaMessage(
                MiaoshaMessage.builder()
                        .user(user)
                        .goodsId(goodsId)
                        .build());

        return Result.success(0);
        
        
.....
```

接收消息

```java
@Slf4j
@Service
public class MQReceiver {
....
    @RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
    public void receiveMiaosha(String message){
        MiaoshaMessage messages =  JSONObject.parseObject(message, MiaoshaMessage.class);
        MiaoshaUser user = messages.getUser();
        long goodsId = messages.getGoodsId();

        //判断库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if(stock <= 0) {
            return;
        }
        //判断是否已经秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaOrderInfoByUserIdAndGoodsId(user.getId(), goodsId);
        if(order != null) {
            return;
        }
        miaoshaService.miaosha(user, goods);
    }
    
.....
```







### 秒杀路径隐藏

前端点击立即秒杀，在服务端会发起一个获取path的请求，获取到path并存入缓存（存入userId + goodsId）后返回前端，才会发起一个秒杀请求，并且将path携带在参数中，秒杀请求验证path正确后才会进行秒杀流程



### 图形验证码

在原来秒杀进行中、倒计时的地方加上一个请求验证码，每当用户来到商品详情 要秒杀的时候，就会请求服务端生成一个图形验证码返回页面 并存在缓存中（userId goodsId + verifyCode）。用户输入完后点击立即秒杀，会将验证码带入path请求，并验证（从缓存中取）是否和之前的结果一致，如果不一致则返回错误，一致则可以请求秒杀接口

图形验证码既可以防止机器人刷请求，也可以起到很好的减轻服务器压力的作用，10秒钟请求10000次与1秒请求10000次，对服务器的压力相差非常大。



### 接口限流防刷

自定义注解 & 自定义拦截器







## 解决超卖问题

SQL语句修改，防止库存变成负数

```
and stock_count > 0 
修改后：
update miaosha_goods set stock_count = stock_count - 1 where goods_id = ? and stock_count > 0 
数据库会对这句SQL加锁
```



## 解决重复秒杀的问题

在秒杀订单表上建立唯一索引



## TODO

### Nginx水平扩展



### 分库分表

推荐分库分表中间件 mycat



### CDN优化(待补充)



### 缓存数据不一致解决



```
页面使用	 bootstrap
表单数据验证 query-validator  
页面弹框  layer
```

