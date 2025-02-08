package org.lpz.usercenter.model.VO;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户封装类（脱敏）
 * @TableName user
 */
@Data
public class UserVO implements Serializable {
    /**
     * id
     */
    private long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 登录账号
     */
    private String userAccount;

    /**
     * 头像
     */
    private String avatarUrl;


    /**
     * 性别
     */
    private Integer gender;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户状态 0:正常
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    /**
     * 用户角色 0：普通用户 1：管理员
     */
    private Integer userRole;

    /**
     * 星球编号
     */
    private String planetCode;
    /**
     * 标签
     */
    private String tags;
    /**
     * 个人简历
     */
    private String profile;

    private static final long serialVersionUID = 1L;
}