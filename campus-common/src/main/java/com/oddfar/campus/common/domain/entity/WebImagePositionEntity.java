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
 * 网站图片位置实体类
 *
 * @author oddfar
 * @since 1.0.0 2024-02-09
 */
@TableName("web_image_position")
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class WebImagePositionEntity extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 位置ID */
    @TableId("position_id")
    private Long positionId;

    /** 网站ID */
    @Xss(message = "网站ID不能包含脚本字符")
    @NotBlank(message = "网站ID不能为空")
    private String webId;

    /** 位置编码 */
    @Xss(message = "位置编码不能包含脚本字符")
    @NotBlank(message = "位置编码不能为空")
    private String positionCode;

    /** 位置名称 */
    @Xss(message = "位置名称不能包含脚本字符")
    @NotBlank(message = "位置名称不能为空")
    private String positionName;

    /** 关联的文件ID */
    private Long fileId;

    /** 图片URL */
    private String imageUrl;

    /** 图片描述 */
    @Xss(message = "图片描述不能包含脚本字符")
    private String imageDesc;

    /** 排序 */
    private Integer sortOrder;

    /** 状态(0禁用 1启用) */
    @NotNull(message = "状态不能为空")
    private Integer status;

    public WebImagePositionEntity(Long positionId) {
        this.positionId = positionId;
    }
}