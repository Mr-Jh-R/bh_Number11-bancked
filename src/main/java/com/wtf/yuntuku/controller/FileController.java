package com.wtf.yuntuku.controller;

import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import com.wtf.yuntuku.annotation.AuthCheck;
import com.wtf.yuntuku.common.BaseResponse;
import com.wtf.yuntuku.common.ResultUtils;
import com.wtf.yuntuku.constant.UserConstant;
import com.wtf.yuntuku.exception.BusinessException;
import com.wtf.yuntuku.exception.ErrorCode;
import com.wtf.yuntuku.manager.CosManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private CosManager cosManager;

    /**
     * 文件上传
     *
     * @param multipartFile 文件
     * @return 文件路径
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUploadFile(@RequestParam("file") MultipartFile multipartFile) {

        // 1. 校验文件是否为空
        if (multipartFile.isEmpty()) {
            throw new RuntimeException("文件为空");
        }
        String originalFilename = multipartFile.getOriginalFilename();
        String filepath = String.format("test/%s", originalFilename);
        // 转换文件类型
        File file = null;
        try {
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            return ResultUtils.success(filepath);
        } catch (Exception e) {
            log.error("文件上传失败,filepath,{}", filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            // 删除临时文件
            if (file != null) {
                boolean delete = file.delete();
                if (!delete) {
                    log.error("文件删除失败,filepath,{}", filepath);
                }
            }
        }


    }


    /**
     * 文件下载
     *
     * @param filepath
     * @param response
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download")
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {


        COSObjectInputStream cosObjectInput = null;
        try {
            COSObject cosObject = cosManager.getObject(filepath);
            cosObjectInput = cosObject.getObjectContent();
            // 处理下载到的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            // 写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }


}
