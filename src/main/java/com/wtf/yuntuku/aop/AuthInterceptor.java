package com.wtf.yuntuku.aop;

import com.wtf.yuntuku.annotation.AuthCheck;
import com.wtf.yuntuku.exception.BusinessException;
import com.wtf.yuntuku.exception.ErrorCode;
import com.wtf.yuntuku.model.entity.User;
import com.wtf.yuntuku.model.enums.UserRoleEnum;
import com.wtf.yuntuku.service.UserService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    public UserService userService;

    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        //如果不需要权限 放行
        if (mustRoleEnum==null) {
            joinPoint.proceed();
        }
        // 以下必须有权限才能通过
        String userRole = loginUser.getUserRole();
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(userRole);
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"用户无权限");
        }
        // 要求必须要有管理员权限
        if (mustRoleEnum.equals(UserRoleEnum.ADMIN)  && !userRoleEnum.equals(UserRoleEnum.ADMIN) ) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"用户无权限");
        }
        return joinPoint.proceed();
    }

}
