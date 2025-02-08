package org.lpz.usercenter.service;

import org.lpz.usercenter.model.VO.TeamUserVO;
import org.lpz.usercenter.model.domain.UserTeam;
import com.baomidou.mybatisplus.extension.service.IService;
import org.lpz.usercenter.model.dto.TeamQuery;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author lenovo
* @description 针对表【user_team(用户队伍关系表)】的数据库操作Service
* @createDate 2025-01-25 21:53:30
*/
public interface UserTeamService extends IService<UserTeam> {

}
