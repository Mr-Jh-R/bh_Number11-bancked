package com.wtf.yuntuku.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wtf.yuntuku.exception.BusinessException;
import com.wtf.yuntuku.exception.ErrorCode;
import com.wtf.yuntuku.exception.ThrowUtils;
import com.wtf.yuntuku.model.dto.user.UserQueryRequest;
import com.wtf.yuntuku.model.entity.User;
import com.wtf.yuntuku.model.enums.UserRoleEnum;
import com.wtf.yuntuku.model.vo.LoginUserVO;
import com.wtf.yuntuku.model.vo.UserVO;
import com.wtf.yuntuku.service.UserService;
import com.wtf.yuntuku.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.wtf.yuntuku.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author Administrator
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-03-24 23:07:28
*/
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    /**
     * 用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 用户id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验参数
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        ThrowUtils.throwif(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号过短");
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不一致");
        }
        // 2. 检查用户账号是否在数据库中存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        Long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号已存在");
        }
        // 3. 对密码进行加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 4. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户注册失败");
        }
        return user.getId();
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        ThrowUtils.throwif(StrUtil.hasBlank(userAccount, userPassword), ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwif(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号过短");
        if (userPassword.length() < 8 ) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 2.对用户密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 3. 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 不存在,抛异常
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或者密码错误");
        }
        //4. 保存用户信息到session中
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        // 5. 返回脱敏后的用户信息
        return this.getLoginUserVO(user  );
    }

    /**
     * 用户注销
     * @param request http请求
     * @return 用户信息
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null ) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未登录");
        }
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 获取登录用户信息
     * @param request  http请求
     * @return 登录用户信息
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        currentUser = this.getById(currentUser.getId());
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }


    /**
     * 获取加密的密码
     * @param userPassword 用户密码
     * @return 加密的密码
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        // 加密逻辑
        final String SALT = "jinitaimei";
        return DigestUtil.md5Hex((userPassword+SALT).getBytes());
    }

    /**
     * 脱敏用户信息
     * @param user 用户信息
     * @return 脱敏后的用户信息
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 获取脱敏的用户信息
     * @param user 用户信息
     * @return 脱敏后的用户信息
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO UserVO = new UserVO();
        BeanUtils.copyProperties(user, UserVO);
        return UserVO;
    }

    /**
     * 获取脱敏的用户信息列表
     * @param userList 用户信息列表
     * @return 脱敏的用户信息列表
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if(CollUtil.isEmpty(userList)){
            return new ArrayList<>();
        }
        return userList.stream()
                .map(this::getUserVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取查询用户的条件
     * @param userQueryRequest 查询用户的条件
     * @return 查询用户的条件
     * */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "查询用户的条件为空");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        // 拼接查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField), sortOrder.equals("ascend") , sortField);
        return queryWrapper;
    }

    @Override
    public boolean isAdmin(User user) {
        if (user == null) {
            return false;
        }
        return UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

}




