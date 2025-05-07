package com.wtf.yuntuku.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.wtf.yuntuku.config.CosClientConfig;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

@Component
public class CosManager {

    @Resource
    private COSClient cosClient;
    @Resource
    private CosClientConfig cosClientConfig;

    // 将本地文件上传到 COS
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectResult = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectResult);
    }
    public COSObject getObject(String key){
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }


    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectResult = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        PicOperations picOperations = new PicOperations();
        picOperations.setIsPicInfo(1);
        putObjectResult.setPicOperations(picOperations);
        return cosClient.putObject(putObjectResult);
    }


}
