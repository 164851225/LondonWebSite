package com.oddfar.campus.common.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class TimeDataVo {

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date time;

    private BigDecimal value;

    public TimeDataVo(Date time, BigDecimal value) {
        this.time = time;
        this.value = value;
    }
    public TimeDataVo() {
    }
}
