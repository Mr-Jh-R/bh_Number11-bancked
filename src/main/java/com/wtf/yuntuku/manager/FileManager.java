package com.wtf.yuntuku.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.wtf.yuntuku.config.CosClientConfig;
import com.wtf.yuntuku.exception.BusinessException;
import com.wtf.yuntuku.exception.ErrorCode;
import com.wtf.yuntuku.exception.ThrowUtils;
import com.wtf.yuntuku.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class FileManager {

    @Resource
    private CosManager cosManager;

    @Resource
    private CosClientConfig cosClientConfig;

    /**
     * 上传图片
     *
     * @param multipartFile    图片文件
     * @param uploadPathPrefix 上传路径前缀
     * @return
     */

    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {

        validPicture(multipartFile);
        // 获取文件的原始文件名
        String originalFilename = multipartFile.getOriginalFilename();
        String uuid = RandomUtil.randomString(16);
        // 使用时间戳+uuid+原始文件名后缀拼接上传文件名
        String uploadFileName = String.format("%s/%s_%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        // 拼接完整的上传路径
        String uploadPath = String.format("%s/%s", uploadPathPrefix, uploadFileName);
        File file = null;
        try {
            // 创建临时文件
            file = File.createTempFile(uploadPath, null);
            // 上传图片到对象存储
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 封装返回结果
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicWidth(imageInfo.getWidth());
            uploadPictureResult.setPicHeight(imageInfo.getHeight());
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("上传图片失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传图片失败");
        } finally {
            // 删除临时文件
            this.deleteTempFile(file);
        }

    }

    /**
     * 验证图片
     *
     * @param multipartFile 图片文件
     */

    private void validPicture(MultipartFile multipartFile) {
        if (multipartFile == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片为空");
        }
        final long ONE_MB = 1024*1024;
        if (multipartFile.getSize() > 2*ONE_MB) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片大小不能超过2MB");
        }
        // 检验文件后缀
        String suffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        // 检验文件类型
        final List<String> ALLOW_FORMAT = Arrays.asList("jpg", "jpeg", "png", "gif");
        ThrowUtils.throwif(!ALLOW_FORMAT.contains(suffix), ErrorCode.PARAMS_ERROR, "图片格式不支持");
    }

    /**
     * 删除临时文件
     *
     * @param file 临时文件
     */
    private void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        boolean delete = file.delete();
        if (!delete) {
            log.error("删除临时文件失败,{}", file.getAbsolutePath());
        }
    }
}
