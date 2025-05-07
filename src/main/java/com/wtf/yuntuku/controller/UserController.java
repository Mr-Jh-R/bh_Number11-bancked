package com.wtf.yuntuku.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wtf.yuntuku.annotation.AuthCheck;
import com.wtf.yuntuku.common.BaseResponse;
import com.wtf.yuntuku.common.DeleteRequest;
import com.wtf.yuntuku.common.ResultUtils;
import com.wtf.yuntuku.exception.ErrorCode;
import com.wtf.yuntuku.exception.ThrowUtils;
import com.wtf.yuntuku.model.dto.user.*;
import com.wtf.yuntuku.model.entity.User;
import com.wtf.yuntuku.model.vo.LoginUserVO;
import com.wtf.yuntuku.model.vo.UserVO;
import com.wtf.yuntuku.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static com.wtf.yuntuku.constant.UserConstant.ADMIN_ROLE;
import static com.wtf.yuntuku.constant.UserConstant.DEFAULT_ROLE;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {

        ThrowUtils.throwif(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")

    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest , HttpServletRequest request) {

        ThrowUtils.throwif(userLoginRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);

        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    /**
     * 用户注销
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
    ThrowUtils.throwif(request == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
       return ResultUtils.success(userService.userLogout(request));

    }


    /**
     * 创建用户
     * @param userAddRequest 用户信息
     * @return  用户id
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwif(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        final String DEFAULT_PASSWORD = "12345678";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwif(!result, ErrorCode.OPERATION_ERROR, "用户创建失败");
        return ResultUtils.success(user.getId());
    }


    /**
     * 根据id获取用户 (仅管理员)
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        User user = userService.getById(id);
        ThrowUtils.throwif(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        return ResultUtils.success(user);
    }


    /**
     * 根据id获取用户 (仅管理员)
     */
    @GetMapping("/get/vo")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<UserVO> getUserVOById(long id) {
        BaseResponse<User> userResponse = getUserById(id);
        return ResultUtils.success(userService.getUserVO(userResponse.getData()));
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest ) {
        ThrowUtils.throwif(deleteRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        return ResultUtils.success(userService.removeById(deleteRequest.getId()));

    }


    /**
     * 创建用户
     * @param userUpdateRequest 用户信息
     * @return  用户id
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwif(userUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.save(user);
        ThrowUtils.throwif(!result, ErrorCode.OPERATION_ERROR, "用户创建失败");
        return ResultUtils.success(true);
    }

    /**
     * 分页查询用户信息
     * @param userQueryRequest 查询条件
     * @return 分页用户信息
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwif(userQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        List<User> records = userPage.getRecords();
        // 转换为脱敏的用户信息
        List<UserVO> userVOList = userService.getUserVOList(records);
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }
}











