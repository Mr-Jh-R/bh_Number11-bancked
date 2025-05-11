package com.wtf.yuntuku.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wtf.yuntuku.annotation.AuthCheck;
import com.wtf.yuntuku.common.BaseResponse;
import com.wtf.yuntuku.common.DeleteRequest;
import com.wtf.yuntuku.common.ResultUtils;
import com.wtf.yuntuku.exception.BusinessException;
import com.wtf.yuntuku.exception.ErrorCode;
import com.wtf.yuntuku.exception.ThrowUtils;
import com.wtf.yuntuku.manager.FileManager;
import com.wtf.yuntuku.model.dto.picture.PictureEditRequest;
import com.wtf.yuntuku.model.dto.picture.PictureQueryRequest;
import com.wtf.yuntuku.model.dto.picture.PictureUpdateRequest;
import com.wtf.yuntuku.model.dto.picture.PictureUploadRequest;
import com.wtf.yuntuku.model.dto.user.UserQueryRequest;
import com.wtf.yuntuku.model.dto.user.UserUpdateRequest;
import com.wtf.yuntuku.model.entity.Picture;
import com.wtf.yuntuku.model.entity.User;
import com.wtf.yuntuku.model.vo.PictureTagCategory;
import com.wtf.yuntuku.model.vo.PictureVO;
import com.wtf.yuntuku.model.vo.UserVO;
import com.wtf.yuntuku.service.PictureService;
import com.wtf.yuntuku.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.wtf.yuntuku.constant.UserConstant.ADMIN_ROLE;

@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    UserService userService;

    @Resource
    PictureService pictureService;

    @PostMapping("/upload")
    public BaseResponse<PictureVO> uploadPicture(@RequestParam("file") MultipartFile multipartFile,
                                                 PictureUploadRequest pictureUploadRequest,
                                                 HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 删除图片
     * 仅本人或管理员可删除
     *
     * @param deleteRequest id
     * @param request       请求
     * @return true
     */
    @PostMapping("/delete")

    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwif(deleteRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        User loginUser = userService.getLoginUser(request);
        Long id = deleteRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwif(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        // 仅本人和管理员可以删除
        if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除");
        }
        boolean result = pictureService.removeById(id);
        ThrowUtils.throwif(!result, ErrorCode.OPERATION_ERROR, "删除失败");
        return ResultUtils.success(true);
    }

    /**
     * 更新图片 (仅管理员)
     *
     * @param pictureUpdateRequest 更新信息
     * @return true
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest) {
        ThrowUtils.throwif(pictureUpdateRequest == null && pictureUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        // 将list转为string
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        // 图片校验
        pictureService.validPicture(picture);
        Long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwif(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        // 更新图片
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwif(!result, ErrorCode.OPERATION_ERROR, "更新失败");
        return ResultUtils.success(true);
    }


    /**
     * 根据id获取图片 (仅管理员)
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {

        ThrowUtils.throwif(id <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwif(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        return ResultUtils.success(picture);
    }

    /**
     * 根据id获取图片封装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwif(id <= 0, ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwif(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        return ResultUtils.success(pictureService.getPictureVO(picture));
    }

    /**
     * 分页查询图片信息 (仅管理员)
     *
     * @param pictureQueryRequest 查询条件
     * @return 分页图片信息
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        ThrowUtils.throwif(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        long current = pictureQueryRequest.getCurrent();
        long pageSize = pictureQueryRequest.getPageSize();
        Page<Picture> userPage = pictureService.page(new Page<>(current, pageSize),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(userPage);
    }

    /**
     * 分页查询图片信息 (封装类)
     *
     * @param pictureQueryRequest 查询条件
     * @return 分页图片信息
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = ADMIN_ROLE)
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwif(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        long current = pictureQueryRequest.getCurrent();
        long pageSize = pictureQueryRequest.getPageSize();
        ThrowUtils.throwif(pageSize > 20, ErrorCode.PARAMS_ERROR, "请求参数过大");
        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize),
                pictureService.getQueryWrapper(pictureQueryRequest));
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 编辑图片 (用户使用)
     * @param pictureEditRequest 编辑信息
     * @param request 请求
     * @return true
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 实体类和DTO装换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        picture.setEditTime(new Date());
        User loginUser = userService.getLoginUser(request);
        Long id = pictureEditRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwif(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        // 仅本人和管理员可以编辑
        if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限编辑");
        }
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwif(!result, ErrorCode.OPERATION_ERROR, "编辑失败");
        return ResultUtils.success(true);
    }

    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }


}