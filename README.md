# 伙伴匹配系统

介绍：帮助大家找到志同道合的伙伴，移动端H5网页（尽量兼容PC端）

## 需求分析

1. 用户去添加标签，标签的分类（要有哪些标签、怎么把标签进行分类）学习方向c++/java，工作/大学
2. 主动搜索：允许用户根据标签去搜索其他用户
   1. Redis缓存
3. 组队
   1. 创建队伍
   2. 加入队伍
   3. 根据标签查询队伍
   4. 邀请其他人
4. 允许用户去修改标签
5. 推荐
   1. 相似度计算算法 + 本地分布式计算

## 技术栈

### 前端

1. Vue3开发框架（提高页面开发的效率）
2. Vant UI(基于Vue的移动端组件库)(
   (React版Zent)
3. Vite 2(打包工具，快！)
4. Nginx必来单机部署

### 后端

1. Java编程语言+SpringBoot框架
2. SpringMVC MyBatis MyBatis Plus（提高开发效率）
3. MySQL数据库
4. Redis缓存
5. Swagger + Knife4j接口文档

## 前端项目初始化

#### 用脚手架初始化项目

+ Vue CLI：https://cli.vuejs.org/zh/
+ Vite脚手架：https://vitejs.cn/guide/（推荐）

1. 创建Vite项目（目录cmd下）

   ![image-20250114121533288](C:\Users\lenovo\AppData\Roaming\Typora\typora-user-images\image-20250114121533288.png)

2. webstorm打开，终端

   1. 安装依赖

      ```bash
      npm install
      ```

      

#### 整合组件库Vant（看官方文档快速入手）

1. 安装Vant

   ```bash
   npm i vant
   ```

2. **按需引入**插件

   按需引入，每次引用组件，都需在main.ts中声明，否则不起作用

   1. 安装插件

      ```bash
      npm i @vant/auto-import-resolver unplugin-vue-components unplugin-auto-import -D
      ```

   2. 配置插件

   如果是基于 Vite 的项目，在 `vite.config.js` 文件中配置插件：

   ```vue
   import vue from '@vitejs/plugin-vue';
   import AutoImport from 'unplugin-auto-import/vite';
   import Components from 'unplugin-vue-components/vite';
   import { VantResolver } from '@vant/auto-import-resolver';
   
   export default {
     plugins: [
       vue(),
       AutoImport({
         resolvers: [VantResolver()],
       }),
       Components({
         resolvers: [VantResolver()],
       }),
     ],
   };
   ```

3. 全部引入

   ```ts
   import Vant from 'vant';
   ```

   

​	

#### 开发页面经验

1. 多参考
2. 从整体到局部
3. 先想清楚页面要做成什么样子，再写代码

## 前端主页 + 组件概览

### 设计

导航条：展示当前页面名称

主页搜索框 => 搜索页 => 搜索结果页（ 标签筛选页）

内容

tab栏：

- 主页（推荐页 + **广告**）
  - 搜索框
  - banner
  - 推荐信息流
- 队伍页
- 用户页（消息-暂时考虑发邮件）

### 开发

很多页面要复用组件 / 样式，重复写很麻烦，不利于维护，所以要抽象一个通用的布局（Layout），组件化

创建`src/layouts/BasicLayout.vue`作为通用布局，引用Vant UI组件库的[导航栏](https://vant-ui.github.io/vant/#/zh-CN/nav-bar)、[标签页](https://vant-ui.github.io/vant/#/zh-CN/tab)、[标签栏](https://vant-ui.github.io/vant/#/zh-CN/tabbar)

创建`src/pages/Index.vue、``src/pages/Team.vue`页面，并在BasicLayout.vue中引入

## 数据库表设计

标签的分类（要有哪些标签、怎么把标签进行分类）

#### 新增标签页（分类表）

建议用标签，不要用分类，更灵活。

性别：男、女

方向：Java、C++、Go、前端

目标：考研、春招、秋招、社招、考公、竞赛（蓝桥杯）、转行、跳槽

段位：初级、中级、高级、王者

身份：大一、大二、大三、大四、学生、待业、已就业、研一、研二、研三

状态：乐观、有点丧、一般、单身、已婚、有对象

**用户自己定义标签？**



字段：
id int主键

标签名varchar非空 （必须唯一，加唯一索引）

上传标签的用户userld int （如果要根据userId查已上传标签的话，最好加上索引，加普通索引）

父标签id,parentld,int（分类)

是否为父标签isParent,tinyint(0不是父标签、1是父标签)

创建时间createTime,datetime

更新时间updateTime,datetime

是否删除isDelete,tinyint(0、1)

怎么查询所有标签，并且把标签分好组？按父标签id分组，能实现 √

根据父标签查询子标签？根据id查询，能实现 √



#### 修改之前的用户表

用户有哪些标签？怎么设计？

两种方法：

**根据自己的实际需求来！！！** 此处选择第一种

1. 直接在用户表补充tags字段，如**[java',男']存json字符串**

   优点：查询方便、不用新建关联表，标签是用户的固有属性（除了该系统、其他系统可能要用到，标签是用户
   的固有属性)节省开发成本

   **查询用户列表，查关系表拿到这100个用户有的所有标签id,再根据标签id去查标签表。**
   哪怕性能低，可以用缓存。

   缺点：用户表多一列，会有点

2. 加一个关联表，记录用户和标签的关系

   关联表的应用场景：查询灵活，可以正查反查

   缺点：要多建一个表、多维护一个表

   重点：企业大项目开发中尽量减少关联查询，很影响扩展性，而且会影响查询性能

## 后端初始化

直接复制用户中心的后端文件，用IDEA打开，删除.deal（上一个项目的信息），并在pom.xml中全局修改user-center => yupao-backend



纠正：由于是在用户中心的基础上添加标签，所以直接在上个项目基础上开发即可



用mybatisx插件生成tag的实体类,并加上逻辑删除注解，将生成的文件移到对应的包下，修改TagMapper.xml，不需要TagService和TagServiceImpl

## 开发后端接口

### 搜索标签

1. 允许用户传入多个标签，多个标签都存在才能搜索出来and。like'%java%'and like'%C++%'。

2. 允许用户传入多个标签，有任何一个标签存在就能搜索出来or。like'%java%'or like'%C++%

两种方式：

1. SQL查询（实现简单，可以通过拆分查询进一步优化）
2. 内存查询（灵活，可以通过并发进一步优化）



- 如果参数可以分析，根据用户的参数去选择查询方式，比如标签数
- 如果参数不可分析，并且数据库连接足够、内存空间足够，可以并发同时查询，谁先返回用谁
- 还可以SQL查询与内存计算相结合，比如先用SQL过滤掉部分tag

建议通过实际测试来分析那种查询比较快，数据量大的时候验证效果更明显！



解析JSON字符串：

1. 序列化：把java对象转成json
2. 反序列化：把json对象转为java对象



java json序列化库有很多：

1. gson（google的，在次使用gson）
2. fastjson alibaba（ali出品，快，但是漏洞太多）
3. jackson
4. kryo



在UserService添加searchUsersByTags接口，并实现，最后进行单元测试



mybatis输出日志（更方便调试）：

```xml
# MyBatis配置
mybatis:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```



添加tags字段：

修改UserMapper.xml、User、并在Impl中getSavetyUser方法内新加设置Tags



两种方法都测试一遍



**用户中心来集中提供用户的检索、操作、注册、登录、鉴权**



**Java 8：**

1. stream / parallelStream流失处理
2. Optional可选类



## 前端整合路由

使用Vue-Router：https://router.vuejs.org/zh/installation.html，直接看官方文档

Vue—Router：其实就是帮助你根据不同的url来展示 不同的页面（组件），不用自己写if / else

路由配置影响整个项目，所以建议单独用config目录、单独的配置文件去集中定义和管理

有些组件可能自带了和Vue-Router的整合，所以尽量先看组件文档，省去自己写的时间，如van-tabbar就自带路由模式



安装Vue-Router：

```bash
yarn add vue-router@4
```

官方文档创建路由器实例：

```ts
import { createMemoryHistory, createRouter } from 'vue-router'

import HomeView from './HomeView.vue'
import AboutView from './AboutView.vue'

const routes = [
  { path: '/', component: HomeView },
  { path: '/about', component: AboutView },
]

const router = createRouter({
  history: createMemoryHistory(),
  routes,
})
```

新建src/config/route.ts：

```ts
import Index from "../pages/Index.vue";
import Team from "../pages/Team.vue";
import User from "../pages/User.vue";

//定义一些路由
const routes = [
    { path: '/', component: Index },
    { path: '/team', component: Team },
    { path: '/user', component: User },
]

export default routes;
```

修改main.ts，添加代码：

```ts
const router = createRouter({
    //使用hash模式，即使用#区分不同页面
    history: createWebHashHistory(),
    routes,
})
app.use(router)
```

## 搜索页面开发

searchPage.vue，增添路由

**使用到的组件：**

搜索框、分割线、标签、分类选择

已选标签使用布局van-row展示

搜索过滤



## 个人页面开发

使用单元格组件（展示箭头）

定义用户类别：

models/user.d.ts

### 编辑信息页面开发

使用表单组件

在用户页面信息框绑定事件toEdit

## 后端整合Swagger + Knife4j接口文档

什么是接口文档？写接口信息的文档，每条接口包括：

- 请求参数

- 响应参数

  - 错误码

- 接口地址

- 接口名称

- 请求类型

- 请求格式

- 备注

  

who谁用？一般是后端或者负责人来提供，后端和前端都要使用

为什么需要接口文档？

- 有个书面内容（背书或者归档），便于大家参考和查阅，便于沉淀和维护，拒绝口口相传
- 接口文档便于前端和后端开发对接，前后端联调的介质。后端=>接口文档<=前端
- 好的接口文档支持在线调试、在线测试，可以作为工具提高我们的开发测试效率

怎么做接口文档？

- 手写（比如腾讯文档、Markdown笔记）
- 自动化接口文档生成：自动根据项目代码生成完整的文档或在线调试的网页。Swagger,
  Postman(侧重接口管理)；apifox、apipost、.eolink(国产)

接口文档有哪些技巧？



Swagger原理：

1. 引入依赖（Swagger或Knife4j）

2. 自定义Swagger配置类

3. 定义需要生成接口文档的代码位置（Controller）

4. 千万注意：线上环境不要把接口暴露出去！！！可以通过SwaggerConfig配置文件开头加上`@Profile({"dev","test"})`限定配置仅在部分环境开启

5. 启动即可

6. 可以通过在controller方法上添加@Api、@ApilmplicitParam(name="name",va
   lue="姓名",required=true)、@ApiOperation(value="向客人问好")等注解来自定义生成的接口描述信息

7. 如果sptingboot version  >= 2.6,如果报错，需要添加如下配置：

   ```yaml
   spring:
     mvc:
     	pathmatch:
         matching-strategy: ANT_PATH_MATCHER
   
   ```

   

    访问地址：http://项目实际地址/swagger-ui.html

   或者Knife4j：http://localhost:8080/api/doc.html

## 页面和功能开发

### 搜索页面

#### 前端

1. 新建SearchResultPage页面，添加路由配置中
2. 添加搜索按钮，并绑定事件
3. 编写事件逻辑，进行跳转、传参等
4. 在 SearchResultPage处理传过来的数据，并显示
5. 使用商品卡片（带标签）的展示用户信息，由于涉及到个人简历，即新增需求，所以user表中新增profile（个人简历）字段，后端修改userMapper.xml、user类等文件以及safetyUser方法中新增获取profile的语句，前端在用户类型中新增profile字段

#### 后端

controller中编写接口

#### 前后端联调

使用swagger接口文档进行调试

**注意跨域**

前端使用Axios发送请求（安装Axios）

新建plugins/myAxios.ts，添加axios实例和拦截器

在SearchResultPage中使用钩子进行请求，获取数据

### 前端页面跳转传值

1. query=>url searchParams,url后附加参数，传递的值长度有限
2. vuex(全局状态管理)，搜索页将关键词塞到状态中，搜索结果页从状态取值

### Session共享

**改造用户中心，把单机登录改为分布式session登录**

种session的时候注意范围，cookie.domain

比如两个域名：

aaa.lpz.com

bbb.lpz.com

如果要共享cookie。可以种一个更高层的公共域名，比如lpz.com

#### 为什么服务器A登录后，请求发到服务器B上，不认识该用户？

用户在A登录，所以session
用户登录信息)存在了A上

结果请求B时，B没有用户信息，所以不认识。

![image-20250119214028138](C:\Users\lenovo\AppData\Roaming\Typora\typora-user-images\image-20250119214028138.png)

解决方案：**共享存储**，而不是数据放到单台服务器内存中

![image-20250119214048831](C:\Users\lenovo\AppData\Roaming\Typora\typora-user-images\image-20250119214048831.png)

如何共享存储？

1. Redis（基于K / V键值对的数据库），此处选择Redis，因为用户信息读取 / 是否登录的判断**极其频繁**，Redis基于内存，读写性能很高，简单的数据单机qps 5w - 10w
2. MySQL
3. 文件服务器ceph

#### Session共享实现-Redis实现

1. 安装Redis

官网：https://redis.io/

windows下载Redis 5.0.14

链接：https://pan.baidu.com/share/init?surl=XcsAIrdeesQAyQU2lE3cOg

提取码：vkoi

默认端口为6379，通过win + R 输入services.msc启动

2. 引入redis依赖（最好与springboot的版本一致）：

```xml
<!-- https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-data-redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
    <version>2.6.13</version>
</dependency>

```

3. 引入spring-session和redis的整合，使得自动将session存储到redis中

   ```xml
   <!-- https://mvnrepository.com/artifact/org.springframework.session/spring-session-data-redis -->
   <dependency>
       <groupId>org.springframework.session</groupId>
       <artifactId>spring-session-data-redis</artifactId>
       <version>2.6.3</version>
   </dependency>
   
   ```

   

在后端配置文件中添加redis配置：

```xml
  # redis配置
  redis:
    port: 6979
    host: localhost
    database: 0
```

redis管理工具：quick redis：https://quick123.net/

4. 修改spring-session存储配置`spring.session.store-type`,

   默认是none，表示存储在单台服务器

   store-type:redis，表示从redis读写session

5. 测试

   后端用8080端口启动，并且打包后，再用8081端口启动

   ```bash
   java -jar .\user-center-0.0.1-SNAPSHOT.jar --server.port=8081
   
   ```

   分别用8080,8081的接口文档进行登录、获取信息等进行测试是否共享session

   #### 其他单点登录方案

   常用的就是JWT

   Redis Session对比JWT的优缺点：https://zhuanlan.zhihu.com/p/108999941



### 用户信息修改页面

#### 后端

controller层编写updateUser接口，并将一些方法isAdmin、updateUser、getLoginUser抽象为service的接口，便于以后用到时方便调用 

#### 前端

将有关用户请求的函数（获取当前用户）都提取到services/user.ts中

或将当前用户存储下来，在states/user.ts中

### 登录页面

新建UserLoginPage页面，添加路由，使用表单组件，绑定事件

### 用户页面获取信息

使用钩子

在myAxios.ts中加入

```ts
//前端每次向后端发送请求时，都携带上凭证，即cookie
myAxios.defaults.withCredentials = true;
```

如果出现跨域报错，则后端跨域注解需改为：

```java
@CrossOrigin(origins = {"http://localhost:3000"},allowCredentials = "true")
```

### 主页开发

#### 前端

直接list列表

直接复用SearchResultPage.vue页面，在此基础上修改

由于主页和搜索结果页都用到了卡片，所以可以抽象出一个通用组件UserCardList.vue

#### 后端

编写**推荐**接口



模拟1000万用户，再去查询

### 导入数据

1. 用可视化界面：适合一次性导入、数据量可控
2. 写程序：for循环，建议分批，不要一把梭哈（可以用接口来控制），**要保证可控、幂等，注意线上环境和测试环境是有区别的**
3. 执行sql语句：适合小数据量

##### 编写一次性任务

在UserCenterApplication上加入定时任务注解@EnableScheduling

编写类/once/InsertUsers，最后实现不了一次性插入，只能定时倒计时插入，所以可用测试插入

用测试插入，用完后记得注释，否则打包时都会执行一遍单元测试



for循环插入数据的问题：

1. 建立和释放数据库链接（批量查询解决）
2. for循环是绝对线性的（并发）

并发要注意执行的先后顺序无所谓，不要用到非并发类的集合

```java
//自己定义线程池    
private ExecutorService executorService = new ThreadPoolExecutor(40,1000,10000, TimeUnit.MINUTES,new ArrayBlockingQueue<>(10000));

```

CPU密集型：分配的核心线程数 = CPU - 1

IO密集型：分配的核心线程数可以大于CPU核数



##### 使用页面显示数据

前端加上records：

```ts
response?.data?.records
```

后端引入分页配置：

```java
@Configuration
@MapperScan("scan.your.mapper.package")
public class MybatisPlusConfig {

    /**
     * 添加分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL)); // 如果配置多个插件, 切记分页最后添加
        // 如果有多数据源可以不配具体类型, 否则都建议配上具体的 DbType
        return interceptor;
    }
}
```



数据库慢？预先把数据查出来，放到一个更快读取的地方，不用再查数据库了。（缓存）

预加载缓存，定时更新缓存。（定时任务）

多个机器都要执行任务吗？（分布式锁：控制同一时间只有一台机器去执行定时任务，其他机器不用重复执行了）

### 数据查询慢怎么办？

用缓存：提前把数据取出来保存好（通常保存到读写更快的介质，比如内存），就可以更快地读写。

#### 缓存的实现

- Redis（分布式缓存）
- memcached（分布式）
- Etcd（云原生架构的一个分布式存储，**存储配置**，扩容能力）

- ehcache（单机）
- 本地缓存（Java内存Map）
- Caffeine（Java内存缓存，高性能）
- Google Guava

### Redis

> NoSQL数据库
> 

key-value存储系统（区别于MySQL，他存储的是键值对）

#### Redis数据结构

String字符串类型：name:"yupi"

List列表：names:["yupi","dogyupi","yupi"]

Set集合：names:["yupi","dogyupi"]（值不能重复）

Hash哈希：nameAge:{ "yupi":1, "dogyupi":2 }
Zset集合：names:{ yupl-9, dogyupi-12 }(适合做排行榜)



bloomfilter(布隆过滤器，主要从大量的数据中快速过滤值，比如邮件黑名单拦
截)

geo(计算地理位置〉

hyperloglog (pv/uv)

pub/sub(发布订阅，类似消息队列)

BitMap（100101010101010，二进制）



#### 自定义序列化

为了防止写入Redis的数据乱码、浪费空间等，可以自定义序列化器：

```java
package com.yupi.yupao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisTemplateConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(RedisSerializer.string());
        return redisTemplate;
    }
}

```

> 引入一个库时，记得写测试类
>
> 编写测试类RedisTest测试增删改查

#### 设计缓存key

不同用户看到的数据不同

systemId:moduleId:func:options（不要和别人冲突）

yupao:user:recommend:userId

redis内存不能无限增加，一定要设置过期时间！！！

#### 缓存预热

问题：第一个用户访问还是很慢（假如第一个是老板），也能一定程度上保护数据库

缓存预热的优点：

1. 解决上面的问题，可以让用户始终访问很快

缺点：

1. 增加开发成本（要额外的开发和设计）
2. 预热的时机和时间如果错了，有可能你缓存的数据不对或者太老
3. 需要占用额外的空间

> 分析优缺点的时候，要打开思路，从整个项目从0到1的链路上去分析

##### 怎么缓存预热？

1. 定时
2. 模拟触发（手动触发）

###### 实现

用定时任务，每天刷新所有用户的推荐列表

注意点：

1. 缓存预热的意义（新增少、总用户多）
2. 缓存的空间不能太大，要预留给其他缓存空间
3. 缓存数据的周期（此处每天一次）



三种方式实现：

1. Spring Scheduler（spring boot默认整合了）

   1. 主类开启@EnableScheduling
   2. 给要定时执行的方法添加@Scheduled注解，指定cron表达式或者执行频率（这里创建了job/PreCachejob类）
   3. 不要去背cron表达式！！！！用现成的工具即可：
      - https://cron.qqe2.com/
      - https://www.matools.com/crontab/

   

2. Quartz（独立于Spring存在的定时任务框架）

3. XXL-Job之类的分布式任务调度平台（界面+sdk）

#### 控制定时任务的执行

为啥？

1. 浪费资源，想象10000台服务器同时“打鸣”
2. 脏数据，比如重复插入

**要控制定时任务在同一时间只有1个服务器能执行**

怎么做？

1. 分离定时任务程序和主程序，只在1个服务器运行定时任务。成本太大

2. 写死配置，每个服务器都执行定时任务，但是只有符合配置的服务器才真实执行业务逻辑，其他的直接返回。成本最低：但是我
   们的P可能是不固定的，把P写的太死了

3. 动态配置，配置是可以轻松的、很方便地更新的（代码无需重启），但是只有p符合配置的服务器才真实执行业务逻辑。

   - 数据库
   - Redis
   - 配置中心(Nacos、Apollo、Spring Cloud Config)

   问题：服务器多了、IP不可控还是很麻烦，还是要人工修改

4. 分布式锁，只有抢到锁的服务器才能执行业务逻辑。

   坏处：增加成本

   好处：不用手动配置，多少服务器都一样

**单机就会存在单点故障**

##### 锁

有限资源的情况下，控制同一时间（段）只有某些线程（用户/服务器）能访问到资源。

Java实现锁：synchronized关键字、并发包的类
问题：只对单个VM有效

##### 分布式锁

为啥需要分布式锁？

1. 有限资源的情况下，控制同一时间（段）只有某些线程（用户/服务器）能访问到资源。
2. 单个锁只对单个JVM有效

###### 分布式锁实现的关键

抢锁机制：

怎么保证同一时间只有1个服务器能抢到锁？

 核心思想就是：先来的人先把数据改成自己的标识（服务器），后来的人发现标识已存在，就抢锁失败，继续等待。

等先来的人执行方法结束，把标识清空，其他的人继续抢锁。



MySQL数据库：select for update行级锁（最简单）
(乐观锁)

√Redis实现：内存数据库，**读写速度快**，支持**setnx**、lua脚本，比较方便我们实现分布式锁

​	setnx：set if not exists如果不存在，则设置；只有设置成功才会返回true，否则返回false



Zookeeper实现（不推荐）

###### Redis实现注意事项

1. 用完锁要释放（腾地方）

2. **锁一定要加过期时间**

3. 如果方法执行时间过长，锁提前过期了？

   问题：

   1. 连锁效应：释放掉别人的锁
   2. 这样还是会存在多个方法同时执行的情况

解决方案：

- 续期

  ```java
  boolean end = false;
  new Thread(() -> {
  	if (!end){
  	续期
  	}
  });
  end = true;
  ```

4. 释放锁的时候，有可能先判断出是自己的锁，但这时锁过期了，最后还是释放了别人的锁

   ```java
   //原子操作
   if (get lock == A){
   	//这时可能set lock B
   	del lock //此时删除的就是B设置的锁
   }
   ```

   Redis + lua脚本实现

5. Redis如果是集群（而不是只有一个Redis），如果分布式锁的数据不同步怎么办？

https://blog.csdn.net/feiying0canglang/article/details/113258494

###### Redission实现分布式锁

**拒绝自己写！！！**

Java客户端，数据网格

实现了很多java里支持的接口和数据结构



Redisson是一个java操作Redis的客户端，**提供了大量的分布式数据集来简化对Redis的操作和使用，可以让开发者像使用本地集合一**
**样使用Redis,完全感知不到Redis的存在。**



**2种引入方式**

1. spring boot starter引入（不推荐，版本迭代太快，容易冲突）https://github.com/redisson/redisson/tree/master/redisson-spring-boot-starter
2. 直接引入：https://redisson.org/docs/getting-started/
   1. 引入依赖
   2. 新建配置类RedissonConfig（根据官方文档）
   3. 进行单元测试，新建测试类
   4. 在PreCacheJob.java中加入分布式锁
      1. waitTime设置为0，只抢一次，抢不到就放弃
      2. 注意释放锁要写在finally中

###### 看门狗机制

> redisson中提供的续期机制

开一个监听线程，如果方法还没执行完，就帮你重置redis的过期时间。



原理：

1. 监听当前线程，默认过期时间是30秒，每10秒续期一次（补到30秒）
2. 如果线程挂掉（注意debug模式也会被它当成服务器宕机），则不会续期

https://blog.csdn.net/qq_26222859/article/details/79645203

实现代码示例：

```java
@Test
    void testWatchDog(){
        RLock rLock = redissonClient.getLock("yupao:precachejob:docache:lock");
        try {
            //只有一个线程能获得锁
            if (rLock.tryLock(0,-1, TimeUnit.MILLISECONDS)){
                //Thread.sleep(30000);
                //实际要做的事情:doSomething();
                System.out.println("getLock:" + Thread.currentThread().getId());
                
            }

        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }finally {
            //只能释放自己的锁
            if (rLock.isHeldByCurrentThread()){
                System.out.println("unlock:" + Thread.currentThread().getId());
                rLock.unlock();
            }
        }

    }
```

#### Java里的实现方式

##### Spring Data Redis（推荐）

Spring Data：通用的数据访问框架，定义了一组**增删改查**的接口，mysql、redis、jpa

1. 引入

   ```xml
   <dependency>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-starter-data-redis</artifactId>
               <version>2.6.13</version>
   </dependency>
   ```

2. 配置Redis地址

   ```yml
   spring:
     # redis配置
     redis:
       port: 6379
       host: localhost
       database: 0
   ```


##### Jedis

独立于Spring操作Redis的Java客户端

要配合Jedis Pool使用

##### Lettuce

高阶的操作Redis的Java客户端

异步、连接池

##### Redission

分布式操作Redis的Java客户端，让你在使用本地的集合一样操作Redis（分布式Redis数据网格）

##### JetCache



对比：

1. 如果你用的是Spring,并且没有过多的定制化要求，可以用Spring Data
   Redis,最方便
2. 如果你用的不是Spring,并且追求简单，并且没有过高的性能要求，可以用
   Jedis Jedis Pool
3. 如果你的项目不是Spring,并且追求高性能、高定制化，可以用Lettuce,支持异步、连接池
4. 如果你的项目是分布式的，需要用到一些分布式的特性（比如分布式锁、分布式集合），推荐用redission

### 组队功能

理解为王者

#### 理想的应用场景

我要个别人一起参加竞赛或做项目，可以发起队伍或者加入别人的队伍

#### 需求分析

用户可以**创建**一个队伍，设置队伍的人数、队伍名称（标题）、描述、超时时间  **P0**

> 队长、剩余的人数
>
> 聊天？
>
> 公开 或 private 或加密
>
> 用户最多创建5个队伍

展示队伍列表，根据标签或名称搜索队伍 **P0** 信息流中不展示已过期的队伍

修改队伍信息

用户可以加入队伍（其他人、未满、未过期），允许加入多个队伍，但是要有个上限 **P0**

> 是否需要队长同意？筛选审批？

用户可以退出队伍（如果队长退出，权限转移给第二早加入的用户——先来后到） **P1**

队长可以解散队伍 **P0**

分享队伍 -> 邀请其他用户加入队伍 **P1**

队伍人满后发送消息通知 **P1 **

#### 系统（接口）设计

##### 创建队伍

用户可以**创建**一个队伍，设置队伍的人数、队伍名称（标题）、描述、超时时间  **P0**

> 队长、剩余的人数
>
> 聊天？
>
> 公开 或 private 或加密
>
> 信息流中不展示已过期的队伍
>
> 用户最多创建5个队伍

1. 请求参数是否为空？
2. 是否登录，未登录不允许创建
3. 校验信息
   1. 队伍人数 > 1 且 <= 20
   2. 队伍标题 <= 20
   3. 描述 <= 512
   4. status是否公开（int），不传默认为0（公开）（**创建枚举类，更方便**）
   5. 如果status是加密状态，一定要有密码，且密码 <= 32
   6. 超时时间 > 当前时间
   7. 校验用户最多创建5个队伍
4. 插入队伍信息到队伍表
5. 插入用户 => 队伍关系表到队伍表

**编写接口，在Impl里实现**

事务注解（最后两个校验涉及到）：

```java
@Transactional(rollbackFor = Exception.class)
```

要么数据操作都成功，要么都失败

##### 查询队伍列表

分页展示队伍列表，根据标签或名称、最大人数搜索队伍 **P0** 信息流中不展示已过期的队伍、

1. 从请求参数中取出队伍名称等查询条件，如果存在则作为查询条件
2. 不展示已过期的队伍（根据过期时间筛选）
3. 可以通过某个**关键词**同时对名称和描述查询
4. **只有管理员才能查看加密还有非公开的房间**
5. 关联查询已加入队伍的用户信息
6. todo : 关联查询已加入队伍的用户信息（可能会很耗费性能，建议用自己写SQL的方式实现）

新建model / VO：后端返回给前端的封装类

查询实现方式：

1. 自己写SQL

   ```sql
   // 1. 自己写 SQL
   // 查询队伍和创建人的信息
   // select * from team t left join user u on t.userId = u.id
   // 查询队伍和已加入队伍成员的信息
   // select *
   // from team t
   //         left join user_team ut on t.id = ut.teamId
   //         left join user u on ut.userId = u.id;
   ```

2. 用MyBatis Plus构造查询



编写service，在Impl里实现，controller里方法listTeams调用service

##### 修改队伍信息

1. 判断请求参数是否为空
2. 查询队伍是否存在
3. 只有管理员或者队伍的创建者可以修改
4. 如果用户传入的新值和老值一致，就不用update了（自行实现，降低数据库使用次数）
5. 更新成功



从controller开始写，由于userId等不能修改，所有封装成一个新的请求类`TeamUpdateRequest`

前面已经写好controller的update方法，现在对其修改完善 -> 编写service，Impl中实现，调用service

在接口文档进行调试

##### 用户可以加入队伍

其他人、未满、未过期，允许加入多个队伍，但是要有个上限 **P0**

1. 用户最多加入5个队伍
2. 队伍必须存在，只能加入未满、未过期的队伍
3. 不能重复加入已加入的队伍（幂等性）
4. 禁止加入私有的队伍
5. 如果加入的队伍是加密的，必须密码匹配才可以
6. 新增队伍 - 用户关联信息



 controller中编写方法`joinTeam`，新建封装请求体`TeamJoinRequest`

编写service

在Impl中实现

> 注意，并发请求时可能会出现错误

##### 用户可以退出队伍

请求参数：队伍id

1. 校验请求参数

2. 校验队伍是否存在

3. 校验我是否已加入队伍

4. 如果队伍
   1. 只剩一人，队伍解散
   
   2. 还有其他人
      1. 如果是队长退出队伍，权限转移给第二早加入的用户 ——先来后到（取数据记录的id最小的数据）
      
         > 只用取id最小的2条数据
      
      2. 非队长，自己退出队伍

controller中编写方法`quitTeam`,新建封装请求体`TeamQuitRequest`

编写service

在Impl中实现

接口文档进行调试

##### 队长可以解散队伍

请求参数：队伍id

业务流程：

1. 校验请求参数
2. 校验队伍是否存在
3. 校验你是不是队伍的队长
4. 移出所有加入队伍的关联信息
5. 删除队伍

controller中编写方法`deleteTeam`

编写service

在Impl中实现

接口文档进行调试

**注意，一定要加上事务注解！！！**

> 注意，并发请求时可能出现问题

#### 数据库表设计

队伍表Team

字段：

- id主键 bigint（最简单、连续，放url上比较简短，但缺点是爬虫）
- name 队伍名称
- description 描述
- maxNum 最大人数
- expireTime 过期时间
- userId 创建人id
- status 0 - 公开，1 - 私有，2 - 加密
- password 密码
- createTime 创建时间
- updateTime 更新时间
- isDelete 是否删除 

```sql
create table team
(
    id           bigint auto_increment comment 'id'
        primary key,
    name     varchar(256)                     not null comment '队伍名称',
    description   varchar(1024)                 null comment '描述',
    maxNum       int  default 1         not null comment '最大人数',
    expireTime   datetime  null comment '过期时间',
    userId		bigint						not null comment '创建人id',
    status   int  default  0             not null comment '0 - 公开，1 - 私有，2 - 加密',
  	password varchar(256)                       null comment '密码',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '逻辑删除',
) comment '队伍表';


```



用户 - 队伍表user_team

字段：

- id 主键
- userId 用户id
- teamId 队伍id
- joinTime 加入时间
- createTime 创建时间
- updateTime 更新时间
- isDelete 是否删除

```sql
create table user_team
(
    id           bigint auto_increment comment 'id'
        primary key,
    userId     bigint                   not null comment '用户id',
    teamId     bigint                   not null comment '队伍id',
    joinTime   datetime  null comment '加入时间',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '逻辑删除',
) comment '用户队伍关系表';


```

两个关系：

1. 用户加入了哪些队伍？
2. 队伍有哪些用户？

方式：

1. 建立用户 - 队伍表teamId userId（便与修改，查询性能高一点，可以选择这个不用全表遍历）
2. 用户表补充已加入的队伍字段，队伍表补充已加入的用户字段（便与查询，不用写多对多的代码，可以直接根据队伍查用户、根据用户查队伍）



比如以下形式：

用户表

1 【a,b,c】

2 【b,c,d】

队伍表

a  1

b  1,2

c  1,2

d  2



最后：

使用MybatisX插件生成实体类代码，移到对应目录下，修改Mapper.xml里的包路径，实体类中给逻辑删除字段加上逻辑删除的注解

#### 后端

1. 编写TeamCtroller，队伍的增删改查

2. 新建model/dto：dto里存放业务封装类

- **为什么需要请求参数包装类？**
- 1. 请求参数名称 / 类型和实体类不一样
  2. 有一些参数用不到，如果要自动生成接口文档，会增加理解成本
  3. 对单个实体类映射到同一个对象
- **为什么需要包装类？**
- 1. 可能有些字段需要隐藏，不能返回给前端
  2. 或者有些字段某些方法是不关心的

3. 编写common/PageRequest：通用分页请求类

3. 使用接口文档进行调试

#### 前端

##### 添加队伍

编写TeamPage,点击加入队伍，跳转到TeamAddPage页面（添加路由，按钮绑定事件）

使用表单组件和输入框组件编写创建队伍的表单

提交按钮绑定事件，编写事件

##### 展示队伍列表

封装队伍展示卡片：TeamCardList，并且引入到TeamPage（使用onMounted，当页面加载时查询出所有team）

定义队伍类型：team.d.ts

创建constants/team.ts：存放status的枚举

##### 加入队伍

在队伍列表卡片，加入队伍按钮绑定事件doJoinTeam

##### 搜索队伍

TeamPage上端添加搜索框，绑定事件

##### 获取当前用户已加入的队伍

##### 获取当前用户创建的队伍

复用listTeam方法，只新增查询条件，不做修改（开闭原则）

修改个人页面，使用单元格组件新增UserUpdatePage、UserTeamCreatePage、UserTeamJoinPage页面，并添加至路由



UserTeamJoinPage：只需修改请求地址，即可获取用户已加入的队伍（这就是组件复用的好处，修改的地方少）

##### 更新队伍（队伍创始人可见）

新建新页面（新表单）TeamUpdatePage，添加路由

给更新队伍按钮绑定

##### 解散队伍、退出队伍

TeamCardList组件新增两个按钮，并绑定事件

后端/team/delete接口中，也统一将id也封装成通用请求参数

##### 随机匹配

> 为了帮大家更快地发现和自己兴趣相同的朋友

匹配一个还是多个？

答：匹配多个，并且按照匹配的相似度从高到低排序



怎么匹配？（根据什么匹配）

答：标签 tags

> 还可以根据user_team匹配加入相同队伍的用户

本质：找到有相似标签的用户

举例：

用户A：[Java,大一,男]

用户B：[Java,大二,男]

用户C：[Python,大二,女]

用户D：[Java,大一,女]



###### **怎么匹配**

1. 找到有共同标签最多的用户（TopN）
2. 共同标签越多，分数越高，越排在前面
3. 如果没有匹配的用户，随机推荐几个（降级方案）

>  每个标签优先级？

两种算法：

1. 编辑距离算法：https://blog.csdn.net/DBC_121/article/details/104198838

   > 最小编辑距离：字符串1通过最少多少次增删改字符的操作可以变成字符串2

2. 余弦相似度算法：（如果需要带权重计算，比如学什么方向最重要，性别相对次要）

###### 怎么对所有用户匹配，取TOP

直接取出所有用户，依次和当前用户计算分数，取TOP N（耗费时间久）

优化方法：

1. 切忌不要在数据量大的时候循环输出日志（取消掉日志后更快）

2. Map存了所有分数的信息，占用内存

   解决：维护一个固定长度的有序集合（sortedSet，未用），只保留分数最高的几个用户

   如【3,4,5,6,7】取TOP 5，id为1的用户就不用放进去了

3. 细节：剔除自己

4. 尽量只查需要的数据：

   1. 过滤掉标签为空的用户
   2. 根据部分标签取用户（前提是能区分出来哪个标签比较重要）
   3. 只查需要的数据（比如id和tags）（**更快**）

5. 提前查？（定时任务）

   1. 提前把所有用户缓存（不适用于经常更新的数据）
   2. 提前运算出来结果，缓存（针对一些重点用户，提前缓存）



大数据推荐，比如说有几亿个商品，难道要查出所有的商品？难道要对所有的数据计算一遍相似度？



检索 => 召回 => 粗排 => 精排 => 重排序等等

检索：尽可能多地查符合要求的数据（比如按照记录查）

召回：查询可能要用到的数据（不做运算）

粗排：粗略排序，简单地运算（运算相对轻量）

精排：精细排序，确定固定排位

###### 前端匹配功能

在index页面加上`匹配用户`按钮，绑定事件，发送请求

进一步优化：

使用开关进行选择

显示加载特效：

​	使用骨架屏组件套在卡片外面，在(UserCardList页面)

###### 分表学习建议

mycat、sharding sphere 框架

一致性hash

##### 队伍权限控制

在TeamCardList页面中

加入队伍：仅非队伍创建人、且未加入队伍的用户可见

更新队伍：仅创建人可见

退出退伍：非创建人，且已经加入队伍的用户可见

解散队伍：仅创建人可见

#### 前端不同页面怎么传递数据？

1. URL  querystring（xxx?id=1）比较适用于页面跳转
2. url (/team/:id,xxx/1)
3. hash(/team#1)
4. localStorage
5. **context（全局变量，同页面或整个项目要访问公共变量）**



## todo待优化

#### 退出登录 √  

用户页添加退出登录选项，监听点击后所触发的事件

#### 注册 、忘记密码√  

使用表单组件

新建页面，添加路由信息

#### 使用阿里云存储对象存储图片

修改个人信息头像

队伍头像

#### 登录或注册加载状态  √

使用加载按钮

#### 编辑用户标签

##### 前端

UserTagsEdit页面，使用分类选择组件，具体参考SearchResultPage.vue页面的设计



将所有标签抽出为一个全局变量

##### 后端

post请求通过请求体传递参数，封装一个请求体UpdateTagsRequest

#### 心动模式、登录、注册规则提示 √	

心动模式：使用icon图标+气泡弹出框提示

注册：使用表单的检验函数返回错误提示

#### 心动模式匹配有bug？

#### 主页用户分页实现 √

使用分页组件

后端给不同的页设置redis缓存

后端获取总用户数

#### 队伍分页实现 ？

#### 联系我

使用气泡弹出框组件，点击联系我之后弹出气泡，显示手机号和邮箱（使用图标组件表示手机和邮箱）

#### 加载loading特效：使用骨架屏组件√  

解决：van-skeleton组件

#### 仅加入队伍和创建队伍的人能看到操作按钮（listTeam接口要能获取我加入的队伍状态）√

​	方案一：前端查询我加入了哪些队伍列表，然后判断每个队伍id是否在列表中（前端要多发一次请求）

​	**方案二：在后端去做上述事情（推荐）**

TeamUserVo再加入是否加入队伍字段：hasJoin

后端controller  listTeams方法中编写判断用户是否已加入队伍

前端team.d.ts中加入hasJoin字段

在TeamCardList的加入队伍、退出队伍中使用hasJoin字段

#### 前端导航栏死【标题】问题 √	

解决：使用router.beforeEach，根据要跳转页面的url路径匹配config/routes配置的title字段

#### 前端全局响应拦截，自动跳转到登录页 √

解决：myAxios.js全局配置，如何response.data.code ===40100，则跳转到登录页，登录完成后重定向到之前的页面

UserLogin页面：登录完成后，获取页面信息，跳转到之前的页面

#### 创建队伍改为加号 √

给按钮一个类名

在global.css中设置样式，并在main.ts中引入global.css

#### 加入有密码的房间，要指定密码 √

队伍页使用标签页组件区分公开和加密

监听标签页切换，绑定事件，listTeams函数新增status参数，根据status发送不同的请求

TeamCardList中使用弹出框组件弹出密码输入框，绑定的事件

#### 展示队伍已加入人数 √

TeamUserVO中新增队伍人数字段`hasJoinNum`

后端team controller接口中listTeams添加获取已加入队伍人数的逻辑

前端team.d.ts中添加hasJoinNum字段

TeamCardList页面中展示人数

#### 重复加入队伍的问题（加锁、分布式锁）并发请求时可能出现问题 √

分布式锁

#### 前端拦截器统一输出日志 √

myAxios中输出response.data（可以全部输出，也可以只输出一部分，如输出response.data.code）

#### 查不出已加入的队伍 ，只能查到自己创建的队伍 √

在Team Impl的listTeams方法中区分是查询自己创建的队伍，还是查询队伍列表，不同的查询对应不同的条件

查询自己创建的队伍：需根据自己的id查询，加密的队伍也需显示

查询队伍列表：不需要根据自己的id查询，区分公开和加密

查询自己加入的队伍：不需根据创建人（当前登录用户）的id查询，也需显示出加密队伍

上面三个都需判断自己是否已经加入队伍，以及各个队伍已加入的总人数

> 如果查询数据库后的队伍列表为空，需判断且终止，否则会报错

## 部署上线

先区分多环境：前端区分开发和线上接口，后端prod改为用线上公网可访问的数据库

使用宝塔

前端：http://friends.project-learn.site/

后端：http://friends-backend.project-learn.site:8080/api

添加反向代理，访问后端时不需要加:8080 :http://friends-backend.project-learn.site/api

宝塔安装mysql，redis，后端线上环境进行更改（mysql的url）



跨域问题：

添加新站点，绑定后端域名，创建反向代理

用的是WebMvcConfig后端去解决跨域

```java
package org.lpz.usercenter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
 
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //设置允许跨域的路径
        registry.addMapping("/**")
                //设置允许跨域请求的域名
                //当 **Credentials为true时，** Origin不能为星号，需为具体的ip地址【如果接口不带cookie,ip无需设成具体ip】
                .allowedOriginPatterns("*") //当 Credentials为true时，Origin为星号时，使用allowedOriginPatterns
                //是否允许证书 不再默认开启
                .allowCredentials(true)
                //设置允许的方法
                .allowedMethods("*")
                .allowedHeaders("*")
                //跨域允许时间
                .maxAge(3600);
    }
}

```



免备案上线方案

前端：Vercel(免费) https://vercel.com/

后端：微信云托管（部署容器的平台，付费）https://cloud.weixin.qq.com/cloudrun/service

## 如何改成小程序

cordova、跨段开发框架taro、uniapp

## axios

- **GET 请求**：通过 `params` 发送参数（查询字符串）。
- **POST 请求**：通过请求体发送参数。
- **PUT/DELETE 请求**：参数通过请求体（`data`）发送。
