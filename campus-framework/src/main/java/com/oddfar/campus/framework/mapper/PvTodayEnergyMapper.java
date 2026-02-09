package com.oddfar.campus.framework.mapper;

import com.oddfar.campus.common.core.BaseMapperX;
import com.oddfar.campus.common.domain.entity.LightEnergyEntity;
import com.oddfar.campus.common.domain.entity.PvTodayEnergyEntity;
import com.oddfar.campus.common.domain.vo.SmartDisplayCommonReq;
import com.oddfar.campus.common.domain.vo.SmartDisplayTotalVo;

public interface PvTodayEnergyMapper extends BaseMapperX<PvTodayEnergyEntity> {

    void deleteAll();
}
