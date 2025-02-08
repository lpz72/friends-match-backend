package org.lpz.usercenter.service;

import org.lpz.usercenter.model.VO.UserVO;
import org.lpz.usercenter.model.domain.Tag;
import org.lpz.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static org.lpz.usercenter.constant.UserConstant.ADMIN_ROLE;
import static org.lpz.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务接口
 * @author lpz
 */
public interface UserService extends IService<User> {


    /**
     * 用户注册
     * @param userAccount 账户
     * @param userPassword 密码
     * @param checkPassword 校验密码
     * @param planetCode 星球编号
     * @return 用户id
     */
    long userRegister (String userAccount,String userPassword,String checkPassword,String planetCode);

    /**
     * 用户登录
     *
     * @param userAccount  账户
     * @param userPassword 密码
     * @param request 请求体
     * @return 脱敏用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param user
     * @return
     */
    User getSavetyUser(User user);

    /**
     * 用户注销
     * @param request
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据标签查询用户
     * @param tagNameList 所用到的查询标签
     * @return
     */
    List<User> searchUsersByTags(List<String> tagNameList);
    /**
     * 是否为管理员
     * @param request 获取登录态
     * @return
     */
     boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);

    /**
     * 更新用户信息
     * @param user
     * @param loginUser
     * @return
     */
    int updateUser(User user, User loginUser);

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
     User getLoginUser(HttpServletRequest request);

    /**
     * 匹配用户
     * @param num
     * @param loginUser
     * @return
     */
    List<User> matchUsers(long num, User loginUser);
}
