package org.lpz.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lpz.usercenter.common.BaseResponse;
import org.lpz.usercenter.common.ErrorCode;
import org.lpz.usercenter.common.ResultUtils;
import org.lpz.usercenter.exception.BusinessException;
import org.lpz.usercenter.model.VO.UserVO;
import org.lpz.usercenter.model.domain.User;
import org.lpz.usercenter.model.request.UserLoginRequest;
import org.lpz.usercenter.model.request.UserRegisterRequest;
import org.lpz.usercenter.service.UserService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.lpz.usercenter.constant.UserConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/user")
//@CrossOrigin(origins = {"http://localhost:3000"},allowCredentials = "true")
//@CrossOrigin(origins = {"http://39.107.143.21:80"},allowCredentials = "true")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 用户注册
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){

        if (userRegisterRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();

        if (StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        long id = userService.userRegister(userAccount,userPassword,checkPassword,planetCode);
        return ResultUtils.success(id);
    }

    /**
     * 用户登录
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){

        if (userLoginRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        if (StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 用户注销
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request){

        if (request == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int i = userService.userLogout(request);
        return ResultUtils.success(i);
    }

    /**
     * 获取当前用户信息
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        Object object = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) object;
        if(user == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // todo 校验用户是否合法
        long id = user.getId();
        User user1 = userService.getById(id);
        User savetyUser = userService.getSavetyUser(user1);
        return ResultUtils.success(savetyUser);
    }

    /**
     * 搜索所有用户
     * @param username
     * @param request
     * @return
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username,HttpServletRequest request){
        //HttpServletRequest request是获取用户登录态
        // 鉴权，仅管理员可查询
        if (!userService.isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)){
            queryWrapper.like("username",username);
        }

        List<User> userList = userService.list(queryWrapper);
        List<User> collect = userList.stream().map(user -> userService.getSavetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(collect);

    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request){
        if (!userService.isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        boolean b = userService.removeById(id);//已经配置过逻辑删除，所以mybatis-plus会自动改为逻辑删除
        return ResultUtils.success(b);

    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    @PostMapping("/update")
    //因为前端的请求是json数据类型，所以需要使用@RequestBody注解，前提是post方式才会生效
    public BaseResponse<Integer> updateUser(@RequestBody User user,HttpServletRequest request){
        //检验数据是否为空
        if (user == null){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        User loginUSer = userService.getLoginUser(request);
        int result = userService.updateUser(user, loginUSer);
        return ResultUtils.success(result);
    }

    //todo 推荐多个，未实现
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        String key = String.format("yupao:user:recommend:%s",loginUser.getId());
        ValueOperations<String,Object> valueOperations = redisTemplate.opsForValue();

        //如果有缓存，直接读缓存
        Page<User> userPage = (Page<User>) valueOperations.get(key);
        if (userPage != null) {
            return ResultUtils.success(userPage);
        }

        //无缓存，查数据库，设置缓存
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //使用分页
         userPage = userService.page(new Page<>(pageNum,pageSize),queryWrapper);
        try {
            valueOperations.set(key,userPage,30000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis set key error",e);
        }
        return ResultUtils.success(userPage);
    }


    /**
     * 匹配最相似的用户
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request){
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);
        List<User> list = userService.matchUsers(num,loginUser);
        return ResultUtils.success(list);

    }

}
