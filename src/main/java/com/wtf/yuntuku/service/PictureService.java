package com.wtf.yuntuku.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wtf.yuntuku.model.dto.picture.PictureQueryRequest;
import com.wtf.yuntuku.model.dto.picture.PictureUploadRequest;
import com.wtf.yuntuku.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wtf.yuntuku.model.entity.User;
import com.wtf.yuntuku.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

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

    /**
     * 获取查询条件
     * @param pictureQueryRequest 查询条件
     * @return 查询条件
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片VO类
     * @param picture 图片
     * @return 图片VO
     */
    PictureVO getPictureVO(Picture picture);


    /**
     * 分页获取图片列表（封装类）
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 校验
     * @param picture 图片
     */
    void validPicture(Picture picture);
}
