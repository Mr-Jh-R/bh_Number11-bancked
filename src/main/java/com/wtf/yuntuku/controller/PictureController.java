package com.wtf.yuntuku.controller;

import com.wtf.yuntuku.common.BaseResponse;
import com.wtf.yuntuku.common.ResultUtils;
import com.wtf.yuntuku.manager.FileManager;
import com.wtf.yuntuku.model.dto.picture.PictureUploadRequest;
import com.wtf.yuntuku.model.entity.User;
import com.wtf.yuntuku.model.vo.PictureVO;
import com.wtf.yuntuku.service.PictureService;
import com.wtf.yuntuku.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
}
