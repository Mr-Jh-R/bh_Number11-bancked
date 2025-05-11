package com.wtf.yuntuku.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wtf.yuntuku.exception.BusinessException;
import com.wtf.yuntuku.exception.ErrorCode;
import com.wtf.yuntuku.exception.ThrowUtils;
import com.wtf.yuntuku.manager.FileManager;
import com.wtf.yuntuku.model.dto.file.UploadPictureResult;
import com.wtf.yuntuku.model.dto.picture.PictureQueryRequest;
import com.wtf.yuntuku.model.dto.picture.PictureUploadRequest;
import com.wtf.yuntuku.model.entity.Picture;
import com.wtf.yuntuku.model.entity.User;
import com.wtf.yuntuku.model.vo.PictureVO;
import com.wtf.yuntuku.model.vo.UserVO;
import com.wtf.yuntuku.service.PictureService;
import com.wtf.yuntuku.mapper.PictureMapper;
import com.wtf.yuntuku.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    @Resource
    UserService userService;

    /**
     * 上传图片
     *
     * @param multipartFile        图片文件
     * @param pictureUploadRequest 上传路径前缀
     * @param loginUser            登录用户
     * @return
     */
    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        ThrowUtils.throwif(multipartFile == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwif(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 判断是新增还是更新图片
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        // 判断图片是否存在
        if (pictureId != null) {
            boolean exists = this.lambdaQuery().eq(Picture::getId, pictureId).exists();
            ThrowUtils.throwif(!exists, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        // 根据用户id生成上传路径前缀
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        // 上传图片到对象存储
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
        Picture picture = getPicture(loginUser, uploadPictureResult, pictureId);
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwif(!result, ErrorCode.SYSTEM_ERROR, "上传图片失败");
        return PictureVO.objToVo(picture);
    }

    /**
     * 组装图片信息静态方法
     *
     * @param loginUser           登录用户
     * @param uploadPictureResult 上传图片结果
     * @param pictureId           图片id
     * @return 返回图片信息
     */
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
        if (pictureId != null) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        return picture;
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        // 拼接查询条件
        // searchText 支持同时从 name 和 introduction 中检索，用 queryWrapper 的 or 语法构造查询条件。
        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("name", searchText).or().like("introduction", searchText));
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        // JSON数组查询
        // 由于 tags 在数据库中存储的是 JSON 格式的字符串，如果前端要传多个 tag（必须同时存在才查出），
        // 需要遍历 tags 数组，每个标签都使用 like 模糊查询，将这些条件组合在一起。
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField),
                sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public PictureVO getPictureVO(Picture picture) {
        if (picture == null) {
            return null;
        }
        PictureVO pictureVO = PictureVO.objToVo(picture);
        // 关联图片用户信息
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }

        return pictureVO;
    }

    /**
     * 分页获取图片列表（封装类）
     *
     * @param picturePage 分页查询条件
     * @param request     http请求
     * @return 分页图片列表
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }

//        List<PictureVO> pictureVOList = pictureList.stream()
//                .map((picture)->{
//                    return PictureVO.objToVo(picture);})
//
//                .collect(Collectors.toList()); 可简写成以下格式
        /** 遍历pictureList,把每个 picture 对象转换为 pictureVO 对象 */
        List<PictureVO> pictureVOList = pictureList.stream()
                .map(PictureVO::objToVo)
                .collect(Collectors.toList());
        // 关联查询用户信息
//        Set<Long> userIdSet = pictureList.stream().map((picture)->{
//            return picture.getUserId();}).collect(Collectors.toSet());; 可简写成以下格式
        /** 遍历pictureList,把每个 picture 对象的 userId 提取出来,放到 userIdSet 集合中 (set集合会自动去重)*/
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet)
                .stream()
                .collect(Collectors.groupingBy(User::getId));
        // 把查询到的用户信息设置到 pictureVO 对象中
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));

        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwif(picture == null, ErrorCode.PARAMS_ERROR);
        Long id = picture.getId();
        String introduction = picture.getIntroduction();
        String url = picture.getUrl();
        ThrowUtils.throwif(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "图片id为空");
        if (StrUtil.isNotBlank(introduction) && introduction.length() > 800) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片简介过长");
        }
        if (StrUtil.isNotBlank(url) && url.length() > 1024) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片url过长");
        }
    }

}




