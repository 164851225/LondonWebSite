package com.oddfar.campus.common.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SmartDisplayLightAndAcInfoVO {

    private Integer gatewayCount;

    private Integer lightCount;

    private Integer acCount;

    private BigDecimal lightCurrentEnergy;

    private BigDecimal lightSavingEnergy;

}
