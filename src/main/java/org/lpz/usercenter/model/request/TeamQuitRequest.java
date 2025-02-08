package org.lpz.usercenter.model.request;


import lombok.Data;

import java.util.Date;

/**
 * 用户退出队伍请求体
 */
@Data
public class TeamQuitRequest {


    /**
     *  队伍id
     */
    private Long teamId;



}
