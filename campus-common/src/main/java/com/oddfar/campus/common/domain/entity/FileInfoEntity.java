package com.oddfar.campus.common.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.oddfar.campus.common.domain.BaseEntity;
import com.oddfar.campus.common.validator.Xss;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 文件信息实体类
 *
 * @author oddfar
 * @since 1.0.0 2024-02-09
 */
@TableName("file_info")
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class FileInfoEntity extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 文件ID */
    @TableId("file_id")
    private Long fileId;

    /** 原始文件名 */
    @Xss(message = "文件名不能包含脚本字符")
    @NotBlank(message = "文件名不能为空")
    private String fileName;

    /** 文件存储路径 */
    @NotBlank(message = "文件存储路径不能为空")
    private String filePath;

    /** 文件大小(字节) */
    private Long fileSize;

    /** 文件类型 */
    private String fileType;

    /** 文件扩展名 */
    private String fileExtension;

    /** 实际存储路径 */
    @NotBlank(message = "实际存储路径不能为空")
    private String storagePath;

    /** 访问URL */
    private String accessUrl;

    public FileInfoEntity(Long fileId) {
        this.fileId = fileId;
    }
}