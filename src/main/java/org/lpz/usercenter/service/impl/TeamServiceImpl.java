package org.lpz.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lpz.usercenter.common.ErrorCode;
import org.lpz.usercenter.exception.BusinessException;
import org.lpz.usercenter.model.VO.TeamUserVO;
import org.lpz.usercenter.model.VO.UserVO;
import org.lpz.usercenter.model.domain.Team;
import org.lpz.usercenter.model.domain.User;
import org.lpz.usercenter.model.domain.UserTeam;
import org.lpz.usercenter.model.dto.TeamQuery;
import org.lpz.usercenter.model.enums.TeamStatusEnum;
import org.lpz.usercenter.model.request.TeamJoinRequest;
import org.lpz.usercenter.model.request.TeamQuitRequest;
import org.lpz.usercenter.model.request.TeamUpdateRequest;
import org.lpz.usercenter.service.TeamService;
import org.lpz.usercenter.mapper.TeamMapper;
import org.lpz.usercenter.service.UserService;
import org.lpz.usercenter.service.UserTeamService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.file.OpenOption;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author lenovo
* @description 针对表【team(队伍表)】的数据库操作Service实现
* @createDate 2025-01-25 21:53:10
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class) //启用事务，最后两个校验涉及到事务
    public long addTeam(Team team, User loginUser) {
        //1. 请求参数是否为空？
        if (team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2. 是否登录，未登录不允许创建
        if (loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = loginUser.getId();
        //3. 校验信息
        //   1. 队伍人数 > 1 且 <= 20
        int num = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (num > 20 || num <= 1){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数不满足要求");
        }
        //   2. 队伍标题 <= 20
        String teamName = team.getName();
        if (StringUtils.isBlank(teamName) || teamName.length() > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍标题不满足要求");
        }
        //   3. 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍描述过长");
        }
        //   4. status是否公开（int），不传默认为0（公开）
        Integer status = Optional.ofNullable(team.getStatus()).orElse(0);
        //由于status只有三种情况，且可能常用，所以设置成枚举类
        TeamStatusEnum enumByValue = TeamStatusEnum.getEnumByValue(status);
        if (enumByValue == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //   5. 如果status是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(enumByValue)){
            if (StringUtils.isBlank(password) || password.length() > 32){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码设置不正确");
            }
        }
        //   6. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"超时时间 < 当前时间");
        }
        //   7. 校验用户最多创建5个队伍
        // todo 有bug，可能同时创建100个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        int count = this.count(queryWrapper);
        if (count >= 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户最多创建5个队伍");
        }
        //4. 插入队伍信息到队伍表
        team.setId(null); //使用数据库设置的自增
        team.setUserId(userId);
        boolean save = this.save(team);
        Long teamId = team.getId();
        if (!save || teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"创建队伍失败");
        }
        //5.插入用户 => 队伍关系表到队伍表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        boolean save1 = userTeamService.save(userTeam);
        if (!save1){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"创建队伍失败");
        }


        return teamId;
    }

    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery,boolean isAdmin,String type) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        // 1.组合查询条件
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id",id);
            }


            //可以通过某个关键词同时对名称和描述查询
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name",searchText).or().like("description",searchText));
            }

            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.eq("name",name);
            }

            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.eq("description",description);
            }
            //查询最大人数相等的
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum",maxNum);
            }

            if (StringUtils.isNotBlank(type)) {
                if ("create".equals(type)) {
                    //根据创建人来查询
                    Long userId = teamQuery.getUserId();
                    if (userId != null && userId > 0) {
                        queryWrapper.eq("userId",userId);
                    }
                } else if ("list".equals(type)) {
                    //根据状态查询
                    Integer status = teamQuery.getStatus();
                    TeamStatusEnum enumByValue = TeamStatusEnum.getEnumByValue(status);
                    if (enumByValue == null){
                        enumByValue = TeamStatusEnum.PUBLIC;
                    }

                    if (!isAdmin && enumByValue.equals(TeamStatusEnum.PRIVATE)) {
                        throw new BusinessException(ErrorCode.NO_AUTH);
                    }
                    queryWrapper.eq("status",enumByValue.getValue());
                } else {
                    List<Long> idList = teamQuery.getIdList();
                    if (CollectionUtils.isNotEmpty(idList)) {
                        queryWrapper.in("id",idList);
                    } else {
                        return null;
                    }

                }
            }



        }

        //不展示已过期的队伍（根据过期时间筛选）
        //expireTime is Null or  < expireTime > now
        queryWrapper.and(qw -> qw.gt("expireTime",new Date()).or().isNull("expireTime"));

//        List<Team> teamList = this.list(queryWrapper);
        List<Team> teamList = Optional.ofNullable(this.list(queryWrapper)).orElse(new ArrayList<>());
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        // 关联查询创建人的用户信息
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }

            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team,teamUserVO);

            //脱敏用户信息
            UserVO userVO = new UserVO();
            if (user != null) {
                BeanUtils.copyProperties(user,userVO);
                teamUserVO.setCreateUser(userVO);
            }

            teamUserVOList.add(teamUserVO);
        }

        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginuser) {

        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        Team oldTeam = getTeamById(id);

        //只有管理员或者队伍的创建者可以修改
        if (oldTeam.getUserId() != loginuser.getId() && !userService.isAdmin(loginuser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        TeamStatusEnum enumByValue = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if (enumByValue.equals(TeamStatusEnum.SECRET)){
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"加密房间必须要有密码");
            }
        }

        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest,updateTeam);
        return this.updateById(updateTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest,User loginUser) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }


        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);
        //禁止加入私有队伍
        Integer status = team.getStatus();
        TeamStatusEnum enumByValue = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(enumByValue)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"禁止加入私有队伍");
        }

        //如果队伍是加密的，密码必须匹配
        if (TeamStatusEnum.SECRET.equals(enumByValue)) {
            String password = teamJoinRequest.getPassword();
            if (StringUtils.isBlank(password) || !team.getPassword().equals(password)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码错误");
            }

        }

        //队伍已过期
        Date expireTime = team.getExpireTime();
        if (expireTime != null && new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍已过期");
        }

        long userId = loginUser.getId();
        //只有一个线程能获得锁
        RLock rLock = redissonClient.getLock("yupao:join_team");
        try {
            //抢到锁并执行，只抢5次，抢不到就失败
            int cnt = 5;
            while(cnt -- > 0) {
                if (rLock.tryLock(0,-1, TimeUnit.MILLISECONDS)){
                    System.out.println("getLock:" + Thread.currentThread().getId());
                    QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("userId",userId);
                    int count = userTeamService.count(queryWrapper);
                    //最多加入5个队伍
                    if (count >= 5) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户最多加入5个队伍");
                    }

                    //已经加入队伍的人数
                    int num = countTeamUserByTeamId(team.getId());
                    if (num >= team.getMaxNum()) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数已满");
                    }

                    //不能重复加入已加入的队伍
                    queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("userId",userId);
                    queryWrapper.eq("teamId",team.getId());
                    int count1 = userTeamService.count(queryWrapper);
                    if (count1 > 0) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户已加入该队伍");
                    }

                    //修改队伍信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }
            }

        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error",e);
            return false;
        } finally {
            //只能释放自己的锁
            if (rLock.isHeldByCurrentThread()){
                System.out.println("unLock:" + Thread.currentThread().getId());
                rLock.unlock();
            }
        }
        return false;
    }

    @Override
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);

        //校验我自己是否已加入队伍
        long userId = loginUser.getId();
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(userId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(userTeam);
        int count = userTeamService.count(queryWrapper);
        if (count == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"未加入队伍");
        }

        int teamHasJoinNum = countTeamUserByTeamId(teamId);
        //队伍只剩一人，解散
        if (teamHasJoinNum == 1) {
            //删除队伍
            this.removeById(teamId);
            //删除所有已加入队伍的关系
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("teamId", teamId);
            userTeamService.remove(queryWrapper);

        } else {
            //队伍还剩至少2人
            //是为队长
            if (team.getUserId() == userId) {
                //把队伍转移给第二早加入队伍的用户
                //查询已加入队伍的所有用户和加入时间，实际上只需要id最小的两条数据即可
                QueryWrapper<UserTeam> teamQueryWrapper = new QueryWrapper<>();
                teamQueryWrapper.eq("teamId",teamId);
                teamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> list = userTeamService.list(teamQueryWrapper);
                if (CollectionUtils.isEmpty(list) || list.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }

                UserTeam nextUserTeam = list.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                //更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                boolean result = this.updateById(updateTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR,"更新队伍队长失败");
                }


            }
            //移出该条记录
            return userTeamService.remove(queryWrapper);
        }

        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long id,User loginUser) {
        //1. 校验请求参数
        //2. 校验队伍是否存在
        Team team = getTeamById(id);
        //3. 校验你是不是队伍的队长
        Long userId = team.getUserId();
        if (userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH,"无访问权限");
        }
        //4. 移出所有加入队伍的关联信息
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId",id);
//        boolean result = userTeamService.removeById(team.getId());
        boolean result = userTeamService.remove(queryWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"删除队伍关联信息失败");
        }
        //5. 删除队伍
        return this.removeById(team.getId());
    }

    @Override
    public List<TeamUserVO> hasJoinAndHasJoinNum(TeamQuery teamQuery, boolean isAdmin, HttpServletRequest request,String type) {
        //1.查询队伍列表
        List<TeamUserVO> teamList = this.listTeams(teamQuery,isAdmin,type);
        //2.判断当前用户是否已加入队伍
        if (CollectionUtils.isEmpty(teamList)) {
            return null;
        }
        List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        try {
            User loginUser = userService.getLoginUser(request);
            queryWrapper.eq("userId",loginUser.getId());
            queryWrapper.in("teamId",teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
            //已加入队伍的id集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });

        } catch (Exception e) {}
        //3.查询已加入队伍的人数
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId",teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        //队伍id => 加入该队伍的用户列表
        Map<Long, List<UserTeam>> listMap = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));

        teamList.forEach(teamUserVO -> {
            teamUserVO.setHasJoinNum(listMap.getOrDefault(teamUserVO.getId(),new ArrayList<>()).size());
        });
        return teamList;
    }

    /**
     * 根据id获取队伍信息
     * @param teamId
     * @return
     */

    private Team getTeamById(Long teamId) {
        //校验参数
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //校验队伍是否存在
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        return team;
    }

    /**
     * 获取某队伍当前人数
     * @param teamId
     * @return
     */

    private int countTeamUserByTeamId(long teamId) {
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        //已经加入队伍的人数
        int num = userTeamService.count(queryWrapper);
        return num;
    }
}




