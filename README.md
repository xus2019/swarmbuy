# 1. 登录功能实现

## 1.1 两次MD5

HTTP在网络上是用明文来传输的，必须对用户密码进行加密

1.用户端，固定salt + 明文

2.服务端，前端参数 + 随机salt



### 为什么要做两次MD5

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



## 1.2 参数校验

### springboot-validation使用

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



### 自定义验证器

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





# 2. 全局异常处理器

Springboot对于异常的处理也做了不错的支持，它提供了一个 **@ControllerAdvice**注解以及 **@ExceptionHandle**r注解，前者是用来**开启全局的异常捕获**，后者则是说明**捕获哪些异常**，对那些异常进行处理。

## 2.1 全局异常捕获处理

```java
@ControllerAdvice  //切面，开启全局的异常捕获
@ResponseBody   //
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class) //捕获哪些异常
    public Result<String> exceptionHandler(HttpServletRequest request, Exception e) {
        e.printStackTrace();
        if (e instanceof GlobalException) {//如果是自定义异常
            GlobalException ex = (GlobalException) e;
            return Result.error(ex.getCm());
        }
        else if (e instanceof BindException) { //如果是其他异常
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



## 2.2 Restful返回参数的封装

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



## 2.3 自定义异常处理类

```java

//自定义异常处理类
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
```

## 2.4 全局异常定义

```java
//CodeMsg 全局异常，可以定义为枚举类
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

# 3. 分布式session

用户登录成功后，服务端生成用户的唯一token，**将token信息写入cookie中并回传给客户端**。以后客户端每一次访问都会将token携带在cookie中，服务端可通过该token来识别唯一用户。

## 3.1 生成唯一token并写入到cookie中

```java
String token = UUIDUtil.uuid();
addCookie(response, token, user);
```

将用户的token信息存在缓存中并设置一定的过期时间(cookie的存活时间与缓存的存活时间保持一致)。用户登陆之后服务端就可以通过缓存中的token来取到用户的user信息

```java
    private void addCookie(HttpServletResponse response, String token, MiaoshaUser user) {
        redisUtil.set(token, user,TOKEN_EXPIRE); 
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setMaxAge(TOKEN_EXPIRE);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
```



## 3.2 拦截器和接口参数解析器

web配置文件

```java
/**
 * MVC的各种配置，包括拦截器，跨域处理等等 都在这里配置
 */

@Configuration
public class WebConfig  implements WebMvcConfigurer {

    @Resource
    UserArgumentResolver userArgumentResolver;

    @Resource
    AccessInterceptor accessInterceptor;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(userArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessInterceptor);
    }
}
```



使用拦截器

```java
@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {

    @Resource
    LoginService loginService;

    @Resource
    RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            //获取用户信息并设置到UserContext中
            MiaoshaUser user = this.getUser(request, response);
            UserContext.setUser(user);
        }
        return true;
    }

    private MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response) {
        //用户token信息可能存放在cookie ， 也可能存放在参数中(兼容手机端)
        String paramToken = request.getParameter(LoginServiceImpl.COOKIE_NAME);
        String cookieToken = getCookieValue(request, LoginServiceImpl.COOKIE_NAME);

        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        //优先取参数中的token信息
        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
        return loginService.getUserByToken(response, token);
    }

    private String getCookieValue(HttpServletRequest request, String cookiName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length <= 0) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookiName)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
```



使用接口参数解析器，在解析器中通过客户端请求中的token去缓存中获取到user信息

```java
/**
 * 自定义接口参数解析器
 */
@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * 判断是否是支持的参数类型
     * @return clazz== MiaoshaUser.class 如果是MiaoshaUser 做解析参数，则必须走下面的解析方法
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> clazz = parameter.getParameterType();
        return clazz== MiaoshaUser.class;
    }
    /**
     * 如果是支持的参数类型，则调用此方法解析参数，将返回值赋值给Controller层中的这个参数
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        return UserContext.getUser();
    }
}

```

SpringMvc中的HandlerAdapter会对Controller层方法的参数执行 HandlerMethodArgumentResolver(对参数的解析器)中的方法，将返回值赋值给Controller层中的对应参数。

**以上执行顺序为：拦截器------>HandlerMethodArgumentResolver ------->controller。**



## 3.3 延长有效期

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



## 3.4 ThreadLocal在一个线程内传递状态

上面将MiaoshaUser传入到一个UserContext中，在另一个地方又通过UserContext去获取，是通过ThreadLocal实现的。

```java
public class UserContext {
    private static ThreadLocal<MiaoshaUser> userHolder = new ThreadLocal<>();

    public static void setUser(MiaoshaUser user){
        userHolder.set(user);
    }
    public static MiaoshaUser getUser(){
        return userHolder.get();
    }
}
```

### ThreadLocal应用

在Web应用中，每个应用请求页面时，我们都会创建一个任务，类似：

```java
public void process(User user) {
    checkPermission();
    doWork();
    saveStatus();
    sendResponse();
}
```

观察`process()`方法，它内部需要调用若干其他方法，同时，我们遇到一个问题：如何在一个线程内传递状态？

`process()`方法需要传递的状态就是`User`实例。有的童鞋会想，简单地传入`User`就可以了：

```java
public void process(User user) {
    checkPermission(user);
    doWork(user);
    saveStatus(user);
    sendResponse(user);
}
```

但是往往一个方法又会调用其他很多方法，这样会导致`User`传递到所有地方：

```java
void doWork(User user) {
    queryStatus(user);
    checkStatus();
    setNewStatus(user);
    log();
}
```

这种**在一个线程中，横跨若干方法调用，需要传递的对象，我们通常称之为上下文（Context），它是一种状态，可以是用户身份、任务信息等。**

给每个方法增加一个context参数非常麻烦，而且有些时候，如果调用链有无法修改源码的第三方库，`User`对象就传不进去了。

Java标准库提供了一个特殊的`ThreadLocal`，**它可以在一个线程中传递同一个对象。**

`ThreadLocal`实例通常总是以静态字段初始化如下：

```java
static ThreadLocal<User> context = new ThreadLocal<>();
```

通过设置一个`User`实例关联到`ThreadLocal`中，在移除之前，所有方法都可以随时获取到该`User`实例：

```java
void step1() {
    User u = threadLocalUser.get();
    log();
    printUser();
}

void log() {
    User u = threadLocalUser.get();
    println(u.name);
}

void step2() {
    User u = threadLocalUser.get();
    checkUser(u.id);
}
```



### 关闭ThreadLocal

如果使用线程池，注意`ThreadLocal`一定要在`finally`中清除：

```java
try {
    threadLocalUser.set(user);
    ...
} finally {
    threadLocalUser.remove();
}
```

这是**因为当前线程执行完相关代码后，很可能会被重新放入线程池中，如果`ThreadLocal`没有被清除，该线程执行其他代码时，会把上一次的状态带进去。**

为了保证能释放`ThreadLocal`关联的实例，我们可以通过`AutoCloseable`接口配合`try (resource) {...}`结构，让编译器自动为我们关闭。例如，一个保存了当前用户名的`ThreadLocal`可以封装为一个`UserContext`对象：

```java
public class UserContext implements AutoCloseable {

    static final ThreadLocal<String> ctx = new ThreadLocal<>();

    public UserContext(String user) {
        ctx.set(user);
    }

    public static String currentUser() {
        return ctx.get();
    }

    @Override
    public void close() {
        ctx.remove();
    }
}
```

使用的时候，我们借助`try (resource) {...}`结构，可以这么写：

```java
try (var ctx = new UserContext("inotee")) {
    // 可任意调用UserContext.currentUser():
    String currentUser = UserContext.currentUser();
} // 在此自动调用UserContext.close()方法释放ThreadLocal关联对象
```

这样就在`UserContext`中完全封装了`ThreadLocal`，外部代码在`try (resource) {...}`内部可以随时调用`UserContext.currentUser()`获取当前线程绑定的用户名。



## TODO单点登录

# 4. 缓存

并发大的主要的瓶颈主要在数据库，所以最有效的办法是加缓存

## 4.1 页面缓存

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
        //取不到再去db
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





## 4.2 页面静态化

加了页面缓存后会出现问题，也就是页面不能及时更新，特别是像秒杀这种，需要精确到秒的，页面不能刷新，秒杀状态不能及时变更，所以就引入了页面静态化 --> 静态资源直接返回，只有动态资源，每一次都在变的数据才从服务端获取，也减轻了服务端的压力

也就是说，**原来是客户端直接访问服务端，服务端将数据放入前端，再将前端返回给客户端，页面静态化之后，客户端在页面上直接跳转页面，由页面对服务端发起一个请求获取数据。**



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

页面静态化**就是通俗意义上的前后端分离，利用浏览器的缓存来减轻服务器的压力。**



## 4.3 对象缓存

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



# 5. 接口优化

**优化目标**

1.系统初始化，把商品库存数量加载到Redis

2.收到秒杀请求，Redis预减库存，若库存不足，直接返回，否则进入3

3.请求入队，立即返回排队中

4.请求出队，生成订单，减少库存

5.客户端轮询，是否秒杀成功



## 5.1 Redis预减库存 & 内存标记

为了实现在系统初始化的时候将秒杀商品的库存数量存入redis，我们可以实现`InitializingBean`接口，在spring初始化bean的时候，如果bean实现了`InitializingBean`接口，会自动调用`afterPropertiesSet`方法。

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





## 5.2 RabbitMQ异步下单

总结步骤就是：配置队列、Service层由直接下单改为发送消息、接收消息的类监听队列并在接收消息的方法中调用下单逻辑。

秒杀Message

```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MiaoshaMessage {
    private MiaoshaUser user;
    private long goodsId;
}
```

配置

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

发送消息

```java
@Slf4j
@Service
public class MQSender {

    @Resource
    AmqpTemplate amqpTemplate;

    public void sendMiaoshaMessage(MiaoshaMessage message){
        String messages = JSONObject.toJSONString(message);
        log.info("-----sen message:" + messages);
        amqpTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE,messages);

    }
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





## 5.3 图形验证码

在原来商品详情页面（秒杀进行中、倒计时的地方）加上一个请求验证码，每当用户来到商品详情要秒杀的时候，就会请求服务端生成一个图形验证码返回页面 并存在缓存中（userId goodsId + verifyCode）。用户输入完后点击立即秒杀，会将验证码带入path请求，并验证（从缓存中取）是否和之前的结果一致，如果不一致则返回错误，一致则可以请求秒杀接口

图形验证码既可以防止机器人刷请求，也可以起到很好的减轻服务器压力的作用。例如，10秒钟请求10000次与1秒请求10000次，对服务器的压力相差非常大。



## 5.4 秒杀路径隐藏

前端点击立即秒杀，**在服务端会发起一个获取path的请求**，获取到path并存入缓存（存入userId + goodsId）后返回前端，**才会发起一个秒杀请求，并且将path携带在参数中**，秒杀请求验证path正确后才会进行秒杀流程



## 5.5 接口限流防刷

自定义注解

```java
@Retention(RUNTIME)
@Target(METHOD)
public @interface AccessLimit {
    int seconds();
    int maxCount();
    boolean needLogin() default true;
}
```



使用注解限流

```java
 @AccessLimit(seconds=5, maxCount=5, needLogin=true)//限流
 @RequestMapping(value = "/path", method = RequestMethod.GET)
 @ResponseBody
 public Result<String> getMiaoshaPath(HttpServletRequest request, MiaoshaUser user,
                                    @RequestParam("goodsId") long goodsId,
                                    @RequestParam(value = "verifyCode", defaultValue = "0") int verifyCode) {
```



自定义拦截器实现限流，判断要访问的接口是否包含@AccessLimit注解 ， 如果不包含，直接返回true，如果包含，则实现相应逻辑

```java
    //判断要访问的接口是否包含@AccessLimit注解，如果不包含，直接返回true
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            //获取用户信息并设置到UserContext中
            MiaoshaUser user = this.getUser(request, response);
            UserContext.setUser(user);
          
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            AccessLimit accessLimit = handlerMethod.getMethodAnnotation(AccessLimit.class);
            if (accessLimit == null) {
                return true;  //如果不包含，直接返回true
            }
            //如果包含@AccessLimit注解，则先获取注解中的值
            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();

            //判断是否必须登录，如果需要则生成对应的登录次数的缓存key
            String key = request.getRequestURI();
            if (needLogin) {
                if (user == null) {
                    render(response, CodeMsg.SESSION_ERROR);
                    return false;
                }
                key += "_" + user.getId();
            } 
            //判断登陆次数
            Integer count = (Integer) redisUtil.get(key);
            if (count == null) {
                redisUtil.set(key, 1, seconds);
            } else if (count < maxCount) {
                redisUtil.incr(key, 1);
            } else {
                render(response, CodeMsg.ACCESS_LIMIT_REACHED);//访问太频繁
                return false;
            }
        }
        return true;
    }
```



# 6. 其他

## 6. 1 解决超卖问题

SQL语句修改，防止库存变成负数

```
加上 and stock_count > 0 

修改后：
update miaosha_goods set stock_count = stock_count - 1 where goods_id = ? and stock_count > 0 
数据库会对这句SQL加锁
```



## 6.2 解决重复秒杀的问题

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



# 参考

[全局异常处理](https://www.cnblogs.com/swzx-1213/p/12781836.html)

[HandlerMethodArgumentResolver使用](https://blog.csdn.net/xxwd12/article/details/90693140?utm_medium=distribute.pc_relevant_t0.none-task-blog-BlogCommendFromMachineLearnPai2-1.control&depth_1-utm_source=distribute.pc_relevant_t0.none-task-blog-BlogCommendFromMachineLearnPai2-1.control)

[使用ThreadLocal](https://www.liaoxuefeng.com/wiki/1252599548343744/1306581251653666)

[Spring中的InitializingBean接口的使用](https://blog.csdn.net/djrm11/article/details/84526012?utm_medium=distribute.pc_relevant_t0.none-task-blog-BlogCommendFromMachineLearnPai2-1.control&depth_1-utm_source=distribute.pc_relevant_t0.none-task-blog-BlogCommendFromMachineLearnPai2-1.control)