package com.wtf.yuntuku.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wtf.yuntuku.config.CosClientConfig;
import com.wtf.yuntuku.exception.ErrorCode;
import com.wtf.yuntuku.exception.ThrowUtils;
import com.wtf.yuntuku.manager.CosManager;
import com.wtf.yuntuku.manager.FileManager;
import com.wtf.yuntuku.model.dto.file.UploadPictureResult;
import com.wtf.yuntuku.model.dto.picture.PictureUploadRequest;
import com.wtf.yuntuku.model.entity.Picture;
import com.wtf.yuntuku.model.entity.User;
import com.wtf.yuntuku.model.vo.PictureVO;
import com.wtf.yuntuku.service.PictureService;
import com.wtf.yuntuku.mapper.PictureMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Administrator
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-04-29 16:22:33
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {
    
    @Resource
    FileManager fileManager;

    /**
     * 上传图片
     * @param multipartFile 图片文件
     * @param pictureUploadRequest 上传路径前缀
     * @param loginUser 登录用户
     * @return
     */
    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        ThrowUtils.throwif(multipartFile == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwif(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 判断是新增还是更新图片
        Long pictureId = null;
        if (pictureUploadRequest!=null){
            pictureId = pictureUploadRequest.getId();
        }
        // 判断图片是否存在
        if (pictureId!=null){
            boolean exists = this.lambdaQuery().eq(Picture::getId, pictureId).exists();
            ThrowUtils.throwif(!exists,ErrorCode.NOT_FOUND_ERROR,"图片不存在");
        }
        // 根据用户id生成上传路径前缀
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        // 上传图片到对象存储
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
        Picture picture = getPicture(loginUser, uploadPictureResult, pictureId);
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwif(!result,ErrorCode.SYSTEM_ERROR,"上传图片失败");
        return PictureVO.objToVo(picture);
    }

    private static Picture getPicture(User loginUser, UploadPictureResult uploadPictureResult, Long pictureId) {
        Picture picture = new Picture();

        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        // 如果图片存在 表示更新
        if (pictureId !=null){
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        return picture;
    }


}




