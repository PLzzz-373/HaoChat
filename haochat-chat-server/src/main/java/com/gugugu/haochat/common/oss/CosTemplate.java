package com.gugugu.haochat.common.oss;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.format.DatePrinter;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import com.gugugu.haochat.common.oss.domain.OssReq;
import com.gugugu.haochat.common.oss.domain.OssResp;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.Headers;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.*;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class CosTemplate {
    /**
     * Cos客户端
     */
    COSClient cosClient;
    /**
     * oss 配置类
     */
    OssProperties ossProperties;

    /**
     * 查询所有存储桶
     * @return Bucket 集合
     */
    @SneakyThrows
    public List<Bucket> listBuckets(){
        return cosClient.listBuckets();
    }

    /**
     * 桶是否存在
     * @param bucketName 桶名
     * @return 是否存在
     */
    @SneakyThrows
    public boolean bucketExists(String bucketName){
        return cosClient.doesBucketExist(bucketName);
    }
    /**
     * 创建存储桶
     * @param bucketName 桶名
     */
    @SneakyThrows
    public void createBucket(String bucketName){
        if(!bucketExists(bucketName)){
            cosClient.createBucket(bucketName);
        }
    }
    /**
     * 删除一个空桶，如果存储桶的存在对象不为空时，删除会报错
     * @Param bucketName 桶名
     */
    @SneakyThrows
    public void deleteBucket(String bucketName){
        cosClient.deleteBucket(bucketName);
    }
    /**
     * 返回临时带签名，过期时间为一天，put请求方式访问url
     */
    @SneakyThrows
    public OssResp getPreSignedObjectUrl(OssReq req){
        String absolutePath = req.isAutoPath() ? generateAutoPath(req) : req.getFilePath() + StrUtil.SLASH + req.getFileName();
        //设置签名在30天后过期
        Date expirationDate = new Date(System.currentTimeMillis() + 30L * 60 * 60 * 24 * 1000);
        //上传为PUT请求
        HttpMethodName method = HttpMethodName.PUT;
        String url = cosClient.generatePresignedUrl(ossProperties.getBucketName(), absolutePath,expirationDate,method).toString();
        return OssResp.builder()
                .uploadUrl(url)
                .downloadUrl(getDownloadUrl(ossProperties.getBucketName(),absolutePath))
                .build();
    }

    /**
     * GetObject 接口用于获取某个文件
     *
     * @param bucketName 桶名
     * @param ossFilePath Oss 文件路径
     *
     */
    @SneakyThrows
    public COSObject getObject(String bucketName, String ossFilePath){
        return cosClient.getObject(bucketName, ossFilePath);
    }



    private String generateAutoPath(OssReq req) {
        String uid = Optional.ofNullable(req.getUid()).map(String::valueOf).orElse("000000");
        cn.hutool.core.lang.UUID uuid = cn.hutool.core.lang.UUID.fastUUID();
        String suffix = FileNameUtil.getSuffix(req.getFileName());
        String yearAndMonth = DateUtil.format(new Date(), DatePattern.NORM_MONTH_PATTERN);
        return req.getFilePath() + StrUtil.SLASH + yearAndMonth + StrUtil.SLASH + uid + StrUtil.SLASH + uuid + StrUtil.DOT +suffix;
    }
    private String getDownloadUrl(String bucket, String pathFile){
        GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(bucket, pathFile, HttpMethodName.GET);
        //设置签名在30天后过期
        Date expirationDate = new Date(System.currentTimeMillis() + 30L * 60 * 60 * 24 * 1000);
        req.putCustomRequestHeader(Headers.HOST, cosClient.getClientConfig().getEndpointBuilder().buildGeneralApiEndpoint(bucket));
        return cosClient.generatePresignedUrl(req).toString();
    }
}
