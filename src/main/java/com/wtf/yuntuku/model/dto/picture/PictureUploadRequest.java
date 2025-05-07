package com.wtf.yuntuku.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {

    private static final long serialVersionUID = 6077830269633903071L;
    /**
     * 图片 id
     */
    private Long id;

}
