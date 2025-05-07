package com.wtf.yuntuku.service;

import com.wtf.yuntuku.model.dto.picture.PictureUploadRequest;
import com.wtf.yuntuku.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wtf.yuntuku.model.entity.User;
import com.wtf.yuntuku.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

/**
* @author Administrator
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-04-29 16:22:33
*/
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param multipartFile 图片文件
     * @param pictureUploadRequest 上传路径前缀
     * @param loginUser 登录用户
     * @return 上传结果
     */
    PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser);
}
