package com.wtf.yuntuku.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wtf.yuntuku.model.dto.user.UserQueryRequest;
import com.wtf.yuntuku.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wtf.yuntuku.model.vo.LoginUserVO;
import com.wtf.yuntuku.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Administrator
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-03-24 23:07:28
*/
public interface UserService extends IService<User> {

    /**
     *
     * 用户注册
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);


    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request      请求对象
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户注销
     * @param request  请求对象
     * @return  注销结果
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取加密的密码
     * @param userPassword 明文密码
     * @return 加密的密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 获取脱敏的登录用户信息
     * @param user 登录用户信息
     * @return 脱敏的登录用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     * @param user 用户信息
     * @return 脱敏的用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息列表
     * @param userList 用户信息列表
     * @return 脱敏的用户信息列表
     */
    List<UserVO> getUserVOList(List<User> userList);


    /**
     *
     * 获取查询条件
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);


    /**
     * 是否为管理员
     * @param user 用户信息
     * @return 是否为管理员
     */
    boolean isAdmin(User user);
}
