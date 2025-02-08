package org.lpz.usercenter.service;

import org.lpz.usercenter.model.VO.TeamUserVO;
import org.lpz.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import org.lpz.usercenter.model.domain.User;
import org.lpz.usercenter.model.dto.TeamQuery;
import org.lpz.usercenter.model.request.TeamJoinRequest;
import org.lpz.usercenter.model.request.TeamQuitRequest;
import org.lpz.usercenter.model.request.TeamUpdateRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author lenovo
* @description 针对表【team(队伍表)】的数据库操作Service
* @createDate 2025-01-25 21:53:10
*/
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 搜索队伍
     * @param teamQuery
     * @param isAdmin
     * @param type
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery,boolean isAdmin,String type);

    /**
     * 更新队伍
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest,User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 解散（删除）队伍
     * @param id
     * @return
     */
    boolean deleteTeam(long id,User loginUSer);

    /**
     * 查询用户是否已加入队伍和加入各个队伍的人数
     * @param teamQuery
     * @param isAdmin
     * @param request
     * @param type
     * @return
     */
    List<TeamUserVO> hasJoinAndHasJoinNum(TeamQuery teamQuery, boolean isAdmin, HttpServletRequest request,String type);
}
