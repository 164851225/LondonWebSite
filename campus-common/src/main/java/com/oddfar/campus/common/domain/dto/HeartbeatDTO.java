package com.oddfar.campus.common.domain.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 心跳检测请求DTO
 */
@Data
public class HeartbeatDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 访问记录ID */
    private Long visitId;
    
    /** 停留时长(秒) */
    private Integer duration;
}