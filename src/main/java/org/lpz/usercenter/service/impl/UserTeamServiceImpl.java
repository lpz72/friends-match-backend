package org.lpz.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lpz.usercenter.model.VO.TeamUserVO;
import org.lpz.usercenter.model.domain.User;
import org.lpz.usercenter.model.domain.UserTeam;
import org.lpz.usercenter.model.dto.TeamQuery;
import org.lpz.usercenter.service.TeamService;
import org.lpz.usercenter.service.UserService;
import org.lpz.usercenter.service.UserTeamService;
import org.lpz.usercenter.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author lenovo
* @description 针对表【user_team(用户队伍关系表)】的数据库操作Service实现
* @createDate 2025-01-25 21:53:30
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




