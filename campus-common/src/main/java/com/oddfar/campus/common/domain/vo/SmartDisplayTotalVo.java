package com.oddfar.campus.common.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SmartDisplayTotalVo {

    private BigDecimal currentEnergy;

    private BigDecimal savingEnergy;

    private BigDecimal savingMoney;


}
