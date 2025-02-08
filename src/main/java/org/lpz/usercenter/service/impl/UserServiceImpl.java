package org.lpz.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lpz.usercenter.common.ErrorCode;
import org.lpz.usercenter.exception.BusinessException;
import org.lpz.usercenter.model.VO.UserVO;
import org.lpz.usercenter.model.domain.User;
import org.lpz.usercenter.service.UserService;
import org.lpz.usercenter.mapper.UserMapper;
import org.lpz.usercenter.utils.AlgorithmUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.lpz.usercenter.constant.UserConstant.ADMIN_ROLE;
import static org.lpz.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 * @author lpz
 */
@Service
@Slf4j //记录日志
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 盐值，混淆密码
     */
    private static final String salt = "lpz";


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword,String planetCode) {
        //1.校验
        if (StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }

        //星球编号长度在1 - 5
        if (planetCode.length() > 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"星球编号过长");
        }

        //账户长度不小于4位
        if (userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户长度过短");
        }
        //密码长度不小于8位
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度过短");
        }
        //账户不能包含特殊字符
        String validPattern = "[^a-zA-Z0-9_]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户包含特殊字符");
        }
        //密码和校验密码相同
        if (!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次密码不一致");
        }
        //账户不能重复  涉及到查询数据库，可以把该条判断放到最后，防止资源浪费，避免并不必要的查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);//设置查询条件
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户重复");
        }

        //星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode",planetCode);//设置查询条件
         count = userMapper.selectCount(queryWrapper);
        if (count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"星球编号重复");
        }

        //2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((salt + userPassword).getBytes());

        //3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword); //保存加密后的密码
        user.setPlanetCode(planetCode);
        int result = userMapper.insert(user);
        if (result != 1){
            throw new BusinessException(ErrorCode.INSERT_ERROR);
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验
        if (StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        //账户长度不小于4位
        if (userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户长度过短");
        }
        //密码长度不小于8位
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度过短");
        }
        //账户不能包含特殊字符
        String validPattern = "[^a-zA-Z0-9_]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户包含特殊字符");
        }

        //2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((salt + userPassword).getBytes());

        //查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);//设置查询条件
        queryWrapper.eq("userPassword",encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        //用户不存在
        if (user == null){
            log.info("user login failed,userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.NULL_USER,"用户不存在");
        }

        //3.用户脱敏
        User saftyUser = getSavetyUser(user);

        //4.记录用户登录态  Attribute相当于一个map
        request.getSession().setAttribute(USER_LOGIN_STATE,saftyUser);
        return saftyUser;
    }


    /**
     * 用户脱敏
     * @param user
     * @return
     */
    @Override
    public User getSavetyUser(User user){
        if (user == null){
            throw new BusinessException(ErrorCode.NULL_USER,"用户不存在");
        }
        User saftyUser = new User();
        saftyUser.setId(user.getId());
        saftyUser.setUsername(user.getUsername());
        saftyUser.setUserAccount(user.getUserAccount());
        saftyUser.setAvatarUrl(user.getAvatarUrl());
        saftyUser.setGender(user.getGender());
        saftyUser.setPhone(user.getPhone());
        saftyUser.setEmail(user.getEmail());
        saftyUser.setUserRole(user.getUserRole());
        saftyUser.setPlanetCode(user.getPlanetCode());
        saftyUser.setUserStatus(user.getUserStatus());
        saftyUser.setCreateTime(user.getCreateTime());
        saftyUser.setTags(user.getTags());
        saftyUser.setProfile(user.getProfile());

        return saftyUser;
    }

    /**
     * 用户注销
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        //用户退出登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签查询用户(内存过滤)
     * @param tagNameList 所用到的查询标签
     * @return
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (tagNameList == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //法二：内存查询
        //1.先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        //2.在内容中判断是否包含要求的标签
        return userList.stream().filter(user -> {
            String tagStr = user.getTags();
            if (StringUtils.isBlank(tagStr)){
                return false;
            }
            Set<String> tempTagNameSet = gson.fromJson(tagStr, new TypeToken<Set<String>>(){}.getType());
            //如果 tempTagNameSet 为 null，则将其初始化为一个新的空 HashSet；
            // 如果不为 null，则保持原值。这样可以确保 tempTagNameSet 不会为 null。
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tag : tagNameList) {
                if (!tempTagNameSet.contains(tag)){
                    return false;
                }
            }
            return true;
        }).map(this::getSavetyUser).collect(Collectors.toList());
    }

    /**
     * 是否为管理员
     * @param request 获取登录态
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        if (user == null || user.getUserRole() != ADMIN_ROLE){
            return false;
        }

        return true;
    }

    @Override
    public boolean isAdmin(User loginUser){
        if (loginUser.getUserRole() != ADMIN_ROLE){
            return false;
        }
        return true;
    }

    @Override
    public int updateUser(User user, User loginUser) {
        long id = user.getId();
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //todo 补充校验，如果用户没有任何需要更新的值，就直接报错，不用执行update语句
        //如果是管理员，则允许修改任意用户
        //如果不是管理员，只允许修改当前（自己的）信息
        if (!isAdmin(loginUser) && id != loginUser.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        User oldUser = userMapper.selectById(id);
        if (oldUser == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        return userMapper.updateById(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if(request == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        return (User) userObj;
    }

    @Override
    public List<User> matchUsers(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id","tags");
        queryWrapper.isNotNull("tags");

        List<User> userList = this.list(queryWrapper);
        String userTags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagsList = gson.fromJson(userTags, new TypeToken<List<String>>() {
        }.getType());

        //用户列表的下标 => 相似度
        List<Pair<User,Long>> list = new ArrayList<>();
        //依次计算所有用户和当前用户的相似度
        for (int i = 0;i < userList.size();i ++) {
            User user = userList.get(i);
            String tags = user.getTags();
            //无标签或者为当前用户自己
            if (StringUtils.isBlank(tags) || user.getId() == loginUser.getId()) {
                continue;
            }

            List<String> userTagList = gson.fromJson(tags, new TypeToken<List<String>>() {
            }.getType());

            //计算相似度
            long distance = AlgorithmUtils.minDistance(tagsList, userTagList);
            list.add(new Pair<>(user,distance));
        }

        //按编辑距离由小到大排序
        List<Pair<User, Long>> topUserPairList = list.stream().sorted((a, b) -> (int) (a.getValue() - b.getValue())).limit(num).collect(Collectors.toList());
        //原本顺序的userId列表
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id",userIdList);
        //拼接in查询条件之后，原本顺序就被打乱了，根据上面已经排好序的id映射user
        Map<Long,List<User>> userIdUseListMap= this.list(userQueryWrapper)
                .stream()
                .map(user -> getSavetyUser(user))
                .collect(Collectors.groupingBy(User::getId));
        List<User> finalUserList = new ArrayList<>();
        for (Long id : userIdList) {
            finalUserList.add(userIdUseListMap.get(id).get(0));
        }

        return finalUserList;
    }

    /**
     * 根据标签查询用户 (SQL 查询版)
     * @param tagNameList 所用到的查询标签
     * @return
     */
    @Deprecated //表示过时
    public List<User> searchUsersByTagsBySQL(List<String> tagNameList) {
        if (tagNameList == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //法一：SQL查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        //拼接 and 查询
        //如 like '%java%' and like '%python%'
        for (String tag : tagNameList) {
            queryWrapper = queryWrapper.like("tags",tag);
        }

        List<User> userList = userMapper.selectList(queryWrapper);

        return userList.stream().map(this::getSavetyUser).collect(Collectors.toList());
    }

}




