//package com.oddfar.campus.business.task;
//
//import cn.hutool.core.bean.BeanUtil;
//import cn.hutool.core.collection.CollUtil;
//import cn.hutool.core.date.DateTime;
//import cn.hutool.core.date.DateUtil;
//import com.alibaba.fastjson2.JSON;
//import com.alibaba.fastjson2.JSONObject;
//import com.oddfar.campus.common.core.LambdaQueryWrapperX;
//import com.oddfar.campus.common.core.RedisLock;
//import com.oddfar.campus.common.domain.entity.DbStatisticsDataEntity;
//import com.oddfar.campus.common.domain.entity.LightEnergyEntity;
//import com.oddfar.campus.common.domain.entity.PvHisEnergyEntity;
//import com.oddfar.campus.common.domain.entity.PvTodayEnergyEntity;
//import com.oddfar.campus.common.model.api.LoginResult;
//import com.oddfar.campus.common.model.api.MeterData;
//import com.oddfar.campus.common.model.api.MeterDataRequest;
//import com.oddfar.campus.common.model.api.MeterDataResponse;
//import com.oddfar.campus.common.model.api.db.ElectricChartDataResponse;
//import com.oddfar.campus.common.model.api.db.ElectricDayTotalDataResponse;
//import com.oddfar.campus.common.model.api.pv.*;
//import com.oddfar.campus.common.model.api.pv.his.MeterHisPvDataRequest;
//import com.oddfar.campus.common.model.api.pv.his.MeterHisPvDataResponse;
//import com.oddfar.campus.common.service.api.ApiService;
//import com.oddfar.campus.common.utils.DateUtils;
//import com.oddfar.campus.framework.mapper.DbStatisticsDataMapper;
//import com.oddfar.campus.framework.mapper.LightEnergyMapper;
//import com.oddfar.campus.framework.mapper.PvHisEnergyMapper;
//import com.oddfar.campus.framework.mapper.PvTodayEnergyMapper;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//import java.util.*;
//
///**
// * 带分布式锁的定时任务示例
// * 演示如何在多实例部署环境中确保定时任务只执行一次
// *
// * @author AI Assistant
// */
//@Component
//public class DistributedScheduledTask {
//
//    private static final Logger logger = LoggerFactory.getLogger(DistributedScheduledTask.class);
//
//    @Autowired
//    private RedisLock redisLock;
//    @Autowired
//    private ApiService apiService;
//
//    @Autowired
//    private LightEnergyMapper lightEnergyMapper;
//    @Autowired
//    private PvTodayEnergyMapper pvTodayEnergyMapper;
//    @Autowired
//    private PvHisEnergyMapper pvHisEnergyMapper;
//    @Autowired
//    private DbStatisticsDataMapper dbStatisticsDataMapper;
//
//    // 锁的过期时间（秒），建议设置为任务执行时间的2-3倍
//    private static final int LOCK_EXPIRE_TIME = 30;
//
//    // 示例定时任务：每分钟执行一次
//    @Scheduled(cron = "0 0 15 * * ?")
//    public void executeTaskWithLockForDxck() {
//        // 生成唯一的请求ID，用于标识当前实例
//        String requestId = UUID.randomUUID().toString();
//        String parkId = "743192761155654";
//        String meterId = "";
//        String timeQueryEnum = "0";
//        String lockKey = "scheduled:task:example" + parkId;
//
//
//        boolean locked = false;
//        try {
//            // 尝试获取分布式锁
//            locked = redisLock.acquireLock(lockKey, requestId, LOCK_EXPIRE_TIME);
//
//            if (locked) {
//                logger.info("获取分布式锁成功，开始执行定时任务");
//                LoginResult login = apiService.login("聂平聂平", "e10adc3949ba59abbe56e057f20f883e");
//                String accessToken = login.getAccessToken();
//                MeterDataRequest meterDataRequest = new MeterDataRequest();
//                meterDataRequest.setParkId(parkId);
//                meterDataRequest.setMeterId(meterId);
//                String format = DateUtil.format(new Date(), "YYYY-MM-dd");
//                meterDataRequest.setChartTime(format);
//                meterDataRequest.setTimeQueryEnum(timeQueryEnum);
//                MeterDataResponse dxck = apiService.getMeterData(meterDataRequest, accessToken);
//                List<MeterData> liulouResult = dxck.getResult();
//                List<LightEnergyEntity> saveList = new java.util.ArrayList<>();
//                List<Long> deleteList = new ArrayList<>();
//                liulouResult.parallelStream().forEach(meterData -> {
//                    Date chartTime = DateUtils.parseDate(meterData.getChartTime());
//                    List<LightEnergyEntity> lightEnergyEntities = lightEnergyMapper.selectList(new LambdaQueryWrapperX<LightEnergyEntity>().eqIfPresent(LightEnergyEntity::getParkId, parkId).eqIfPresent(LightEnergyEntity::getMeterId, meterId).eqIfPresent(LightEnergyEntity::getChartTime, chartTime));
//                    if (CollUtil.isNotEmpty(lightEnergyEntities)) {
//                        deleteList.add(lightEnergyEntities.get(0).getId());
//                    }
//                    LightEnergyEntity lightEnergyEntity = new LightEnergyEntity();
//                    lightEnergyEntity.setMeterReading(meterData.getMeterReading());
//                    lightEnergyEntity.setCurrentEnergy(meterData.getCurrentEnergy());
//                    lightEnergyEntity.setOriginalEnergy(meterData.getOriginalEnergy());
//                    lightEnergyEntity.setSavingEnergy(meterData.getSavingEnergy());
//                    lightEnergyEntity.setChartTime(chartTime);
//                    lightEnergyEntity.setParkId(meterDataRequest.getParkId());
//                    lightEnergyEntity.setMeterId(meterDataRequest.getMeterId());
//                    lightEnergyEntity.setTimeQueryEnum(meterDataRequest.getTimeQueryEnum());
//                    lightEnergyEntity.setCreateTime(new Date());
//                    saveList.add(lightEnergyEntity);
//                });
//
//                if (CollUtil.isNotEmpty(deleteList)) {
//                    lightEnergyMapper.deleteBatchIds(deleteList);
//                }
//
//                lightEnergyMapper.insertBatch(saveList);
//
//
//                logger.info("定时任务执行完成");
//            } else {
//                // 未获取到锁，说明其他实例正在执行此任务
//                logger.info("未获取到分布式锁，任务已被其他实例执行");
//            }
//        } catch (Exception e) {
//            logger.error("定时任务执行异常: {}", e.getMessage(), e);
//        } finally {
//            // 无论执行成功与否，都要释放锁
//            if (locked) {
//                boolean released = redisLock.releaseLock(lockKey, requestId);
//                if (released) {
//                    logger.info("分布式锁释放成功");
//                } else {
//                    logger.warn("分布式锁释放失败，可能已过期");
//                }
//            }
//        }
//    }
//
//
//    @Scheduled(cron = "0 0 15 * * ?")
//    public void executeTaskWithLockForLiulou() {
//        // 生成唯一的请求ID，用于标识当前实例
//        String requestId = UUID.randomUUID().toString();
//        String parkId = "744325910970437";
//        String meterId = "";
//        String timeQueryEnum = "0";
//
//        String lockKey = "scheduled:task:example" + parkId;
//
//        boolean locked = false;
//        try {
//            // 尝试获取分布式锁
//            locked = redisLock.acquireLock(lockKey, requestId, LOCK_EXPIRE_TIME);
//
//            if (locked) {
//                logger.info("获取分布式锁成功，开始执行定时任务");
//                LoginResult login = apiService.login("聂平聂平", "e10adc3949ba59abbe56e057f20f883e");
//                String accessToken = login.getAccessToken();
//                MeterDataRequest meterDataRequest = new MeterDataRequest();
//                meterDataRequest.setParkId(parkId);
//                meterDataRequest.setMeterId(meterId);
//                String format = DateUtil.format(new Date(), "YYYY-MM-dd");
//                meterDataRequest.setChartTime(format);
//                meterDataRequest.setTimeQueryEnum(timeQueryEnum);
//                MeterDataResponse liulou = apiService.getMeterData(meterDataRequest, accessToken);
//                List<MeterData> liulouResult = liulou.getResult();
//                List<LightEnergyEntity> saveList = new java.util.ArrayList<>();
//                List<Long> deletelist = new ArrayList<>();
//                liulouResult.parallelStream().forEach(meterData -> {
//                    Date chartTime = DateUtils.parseDate(meterData.getChartTime());
//                    List<LightEnergyEntity> lightEnergyEntities = lightEnergyMapper.selectList(new LambdaQueryWrapperX<LightEnergyEntity>().eqIfPresent(LightEnergyEntity::getParkId, parkId).eqIfPresent(LightEnergyEntity::getMeterId, meterId).eqIfPresent(LightEnergyEntity::getChartTime, chartTime));
//                    if (CollUtil.isNotEmpty(lightEnergyEntities)) {
//                        deletelist.add(lightEnergyEntities.get(0).getId());
//                    }
//                    LightEnergyEntity lightEnergyEntity = new LightEnergyEntity();
//                    lightEnergyEntity.setMeterReading(meterData.getMeterReading());
//                    lightEnergyEntity.setCurrentEnergy(meterData.getCurrentEnergy());
//                    lightEnergyEntity.setOriginalEnergy(meterData.getOriginalEnergy());
//                    lightEnergyEntity.setSavingEnergy(meterData.getSavingEnergy());
//                    lightEnergyEntity.setChartTime(chartTime);
//                    lightEnergyEntity.setParkId(meterDataRequest.getParkId());
//                    lightEnergyEntity.setMeterId(meterDataRequest.getMeterId());
//                    lightEnergyEntity.setTimeQueryEnum(meterDataRequest.getTimeQueryEnum());
//                    lightEnergyEntity.setCreateTime(new Date());
//                    saveList.add(lightEnergyEntity);
//                });
//                if (CollUtil.isNotEmpty(deletelist)) {
//                    lightEnergyMapper.deleteBatchIds(deletelist);
//                }
//                lightEnergyMapper.insertBatch(saveList);
//
//
//                logger.info("定时任务执行完成");
//            } else {
//                // 未获取到锁，说明其他实例正在执行此任务
//                logger.info("未获取到分布式锁，任务已被其他实例执行");
//            }
//        } catch (Exception e) {
//            logger.error("定时任务执行异常: {}", e.getMessage(), e);
//        } finally {
//            // 无论执行成功与否，都要释放锁
//            if (locked) {
//                boolean released = redisLock.releaseLock(lockKey, requestId);
//                if (released) {
//                    logger.info("分布式锁释放成功");
//                } else {
//                    logger.warn("分布式锁释放失败，可能已过期");
//                }
//            }
//        }
//    }
//
//
//        @Scheduled(cron = "0 0 * * * ?")
//    public void executeTaskWithLockForPvToday() {
//        // 生成唯一的请求ID，用于标识当前实例
//        String requestId = UUID.randomUUID().toString();
//        String meterId = "";
//        String timeQueryEnum = "0";
//
//        String lockKey = "scheduled:task:example:pv:today";
//
//        boolean locked = false;
//        try {
//            // 尝试获取分布式锁
//            locked = redisLock.acquireLock(lockKey, requestId, LOCK_EXPIRE_TIME);
//
//            if (locked) {
//                logger.info("获取分布式锁成功，开始执行定时任务");
//                ResultData login = apiService.loginForPv();
//
//                List<PvTodayEnergyEntity> saveList = new java.util.ArrayList<>();
//                MeterTodayPvDataResponse meterTodayPvDataResponse = apiService.getTodayPvMeterData(null, login.getToken());
//                if (meterTodayPvDataResponse != null) {
//                    MeterTodayPvResultData resultData = meterTodayPvDataResponse.getResult_data();
//                    if (resultData != null) {
//                        List<TodayDevicePointWrap> devicePointList = resultData.getDevice_point_list();
//                        if (CollUtil.isNotEmpty(devicePointList)) {
//                            for (TodayDevicePointWrap todayDevicePointWrap : devicePointList) {
//                                TodayDevicePoint todayDevicePoint = todayDevicePointWrap.getDevice_point();
//                                //TodayDevicePoint转成PvTodayEnergyEntity
//                                PvTodayEnergyEntity pvTodayEnergyEntity = new PvTodayEnergyEntity();
//                                pvTodayEnergyEntity.setDeviceName(todayDevicePoint.getDevice_name());
//                                pvTodayEnergyEntity.setDevFaultStatus(todayDevicePoint.getDev_fault_status());
//                                pvTodayEnergyEntity.setPsKey(todayDevicePoint.getPs_key());
//                                pvTodayEnergyEntity.setDeviceSn(todayDevicePoint.getDevice_sn());
//                                pvTodayEnergyEntity.setDevStatus(todayDevicePoint.getDev_status());
//                                pvTodayEnergyEntity.setPsId(todayDevicePoint.getPs_id());
//                                pvTodayEnergyEntity.setCommunicationDevSn(todayDevicePoint.getCommunication_dev_sn());
//                                pvTodayEnergyEntity.setUuid(todayDevicePoint.getUuid());
//
//                                pvTodayEnergyEntity.setP83024(todayDevicePoint.getP83024() == null ? BigDecimal.ZERO : new BigDecimal(todayDevicePoint.getP83024()));
//                                pvTodayEnergyEntity.setP83022(todayDevicePoint.getP83022() == null ? BigDecimal.ZERO : new BigDecimal(todayDevicePoint.getP83022()));
//                                pvTodayEnergyEntity.setP83033(todayDevicePoint.getP83033() == null ? BigDecimal.ZERO : new BigDecimal(todayDevicePoint.getP83033()));
//                                pvTodayEnergyEntity.setDeviceTime(todayDevicePoint.getDevice_time());
//                                pvTodayEnergyEntity.setCreateTime(new Date());
//                                pvTodayEnergyEntity.setUpdateTime(new Date());
//                                saveList.add(pvTodayEnergyEntity);
//                            }
//                        }
//                    }
//                }
//
//                pvTodayEnergyMapper.deleteAll();
//                pvTodayEnergyMapper.insertBatch(saveList);
//
//                logger.info("定时任务执行完成");
//            } else {
//                // 未获取到锁，说明其他实例正在执行此任务
//                logger.info("未获取到分布式锁，任务已被其他实例执行");
//            }
//        } catch (Exception e) {
//            logger.error("定时任务执行异常: {}", e.getMessage(), e);
//        } finally {
//            // 无论执行成功与否，都要释放锁
//            if (locked) {
//                boolean released = redisLock.releaseLock(lockKey, requestId);
//                if (released) {
//                    logger.info("分布式锁释放成功");
//                } else {
//                    logger.warn("分布式锁释放失败，可能已过期");
//                }
//            }
//        }
//    }
//
//        @Scheduled(cron = "0 0 15 * * ?")
//    public void executeTaskWithLockForPvHis() {
//        // 生成唯一的请求ID，用于标识当前实例
//        String requestId = UUID.randomUUID().toString();
//
//        String lockKey = "scheduled:task:example:pv:his";
//
//        boolean locked = false;
//        try {
//            // 尝试获取分布式锁
//            locked = redisLock.acquireLock(lockKey, requestId, LOCK_EXPIRE_TIME);
//
//            if (locked) {
//                logger.info("获取分布式锁成功，开始执行定时任务");
//                ResultData login = apiService.loginForPv();
//                String asd = "{\n" + "    \"appkey\": \"653D19EFBA5E957EEADAD6262086A52F\",\n" + "    \"token\": \"1100159_4e41tvqt4t59wubbjgnp4zdyh35v4fqzq3ctpsebghh4itmfyr4jeazv72x9ca2gn976wmbfgs9v9zynpcc4inpgg3sudc9x7u8u1u34z2vsp6kke3tu37qc8wnaeu2j\",\n" + "    \"data_point\":\"p83022\",\n" + "  \t\"end_time\":\"20251130\",\n" + "  \t\"query_type\":\"1\",\n" + "  \t\"start_time\":\"20251101\",\n" + "  \t\"ps_key_list\":[\n" + "  \t\t\"2181093_11_0_0\"\n" + "  \t],\n" + "  \t\"data_type\":\"2\",\n" + "  \t\"order\":\"0\"\n" + "}\n";
//
//                DateTime startDate = DateUtil.beginOfMonth(new Date());
//                String startDateStr = startDate.toString("yyyyMMdd");
//
//                DateTime endDate = DateUtil.endOfMonth(new Date());
//                String endDateStr = endDate.toString("yyyyMMdd");
//
//
//                MeterHisPvDataRequest meterHisPvDataRequest = JSONObject.parseObject(asd, MeterHisPvDataRequest.class);
//                meterHisPvDataRequest.setEnd_time(endDateStr);
//                meterHisPvDataRequest.setStart_time(startDateStr);
//                meterHisPvDataRequest.setToken(login.getToken());
//
//
//                List<PvHisEnergyEntity> saveList = new java.util.ArrayList<>();
//                MeterHisPvDataResponse meterTodayPvDataResponse = apiService.getHisPvMeterData(meterHisPvDataRequest, login.getToken());
//
//
//                if (meterTodayPvDataResponse != null) {
//                    Map<String, Map<String, List<JSONObject>>> resultData = meterTodayPvDataResponse.getResult_data();
//                    if (resultData != null) {
//                        for (Map.Entry<String, Map<String, List<JSONObject>>> map1 : resultData.entrySet()) {
//                            for (Map.Entry<String, List<JSONObject>> map2 : map1.getValue().entrySet()) {
//                                for (JSONObject jsonObject : map2.getValue()) {
//                                    PvHisEnergyEntity pvHisEnergyEntity = new PvHisEnergyEntity();
//                                    pvHisEnergyEntity.setCreateTime(new Date());
//                                    pvHisEnergyEntity.setUpdateTime(new Date());
//                                    pvHisEnergyEntity.setPsKey(map1.getKey());
//                                    pvHisEnergyEntity.setType(map2.getKey());
//                                    pvHisEnergyEntity.setTimeStamp(jsonObject.getLong("time_stamp"));
//                                    pvHisEnergyEntity.setValue(jsonObject.getString("2") == null ? BigDecimal.ZERO : new BigDecimal(jsonObject.getString("2")));
//                                    saveList.add(pvHisEnergyEntity);
//                                }
//
//                            }
//                        }
//                    }
//                }
//
//                List<Long> deleteList = new ArrayList<>();
//                for (PvHisEnergyEntity pvHisEnergyEntity : saveList) {
//                    List<PvHisEnergyEntity> pvHisEnergyEntities = pvHisEnergyMapper.selectList(new LambdaQueryWrapperX<PvHisEnergyEntity>().eqIfPresent(PvHisEnergyEntity::getType, pvHisEnergyEntity.getType()).eqIfPresent(PvHisEnergyEntity::getPsKey, pvHisEnergyEntity.getPsKey()).eqIfPresent(PvHisEnergyEntity::getTimeStamp, pvHisEnergyEntity.getTimeStamp()));
//                    if (CollUtil.isNotEmpty(pvHisEnergyEntities)) {
//                        deleteList.add(pvHisEnergyEntities.get(0).getId());
//                    }
//                }
//
//                if (CollUtil.isNotEmpty(deleteList)) {
//                    pvHisEnergyMapper.deleteBatchIds(deleteList);
//                }
//                pvHisEnergyMapper.insertBatch(saveList);
//
//                logger.info("定时任务执行完成");
//            } else {
//                // 未获取到锁，说明其他实例正在执行此任务
//                logger.info("未获取到分布式锁，任务已被其他实例执行");
//            }
//        } catch (Exception e) {
//            logger.error("定时任务执行异常: {}", e.getMessage(), e);
//        } finally {
//            // 无论执行成功与否，都要释放锁
//            if (locked) {
//                boolean released = redisLock.releaseLock(lockKey, requestId);
//                if (released) {
//                    logger.info("分布式锁释放成功");
//                } else {
//                    logger.warn("分布式锁释放失败，可能已过期");
//                }
//            }
//        }
//    }
//        @Scheduled(cron = "0 0 15 * * ?")
//    public void executeTaskWithLockForDbStatisticsData() {
//        // 生成唯一的请求ID，用于标识当前实例
//        String requestId = UUID.randomUUID().toString();
//
//        String lockKey = "scheduled:task:example:db:statisitcs:data";
//
//        boolean locked = false;
//        try {
//            // 尝试获取分布式锁
//            locked = redisLock.acquireLock(lockKey, requestId, LOCK_EXPIRE_TIME);
//
//            if (locked) {
//                logger.info("获取分布式锁成功，开始执行定时任务");
//
//                DateTime startDate = DateUtil.beginOfMonth(new Date());
//                String startDateStr = startDate.toString("yyyy-MM-dd");
//
//                DateTime endDate = DateUtil.endOfMonth(new Date());
//                String endDateStr = endDate.toString("yyyy-MM-dd");
//                JSONObject aa = new JSONObject();
//                aa.put("queryTime",startDateStr);
//                aa.put("queryType",1);
//                aa.put("timeQueryEnum",0);
//
//                List<Long> parkId = new ArrayList<>();
//                //五楼
//                parkId.add(762012760133701L);
//                //六楼
//                parkId.add(762011877638213L);
//                //地下室
//                parkId.add(762012584124485L);
//
//
//                for (Long aLong : parkId){
//                    aa.put("id", aLong);
//                    List<ElectricChartDataResponse> dbMeterData = apiService.getDbMeterData(aa);
//
//                    if(CollUtil.isEmpty(dbMeterData)){
//                        return;
//                    }
//
//                    List<DbStatisticsDataEntity> saveList = new java.util.ArrayList<>();
//                    for (ElectricChartDataResponse dbMeterDatum : dbMeterData) {
//                        DbStatisticsDataEntity dbStatisticsDataEntity = new DbStatisticsDataEntity();
//                        dbStatisticsDataEntity.setStatisticsTime(dbMeterDatum.getChartTime());
//                        dbStatisticsDataEntity.setParkId(String.valueOf(aLong));
//                        dbStatisticsDataEntity.setData(dbMeterDatum.getData());
//                        saveList.add(dbStatisticsDataEntity);
//                    }
//
//
//                    List<Long> deleteList = new ArrayList<>();
//                    for (DbStatisticsDataEntity pvHisEnergyEntity : saveList) {
//                        List<DbStatisticsDataEntity> pvHisEnergyEntities = dbStatisticsDataMapper.selectList(new LambdaQueryWrapperX<DbStatisticsDataEntity>()
//                                .eqIfPresent(DbStatisticsDataEntity::getDeviceId, pvHisEnergyEntity.getDeviceId())
//                                .eqIfPresent(DbStatisticsDataEntity::getParkId, pvHisEnergyEntity.getParkId())
//                                .eqIfPresent(DbStatisticsDataEntity::getTenantId, pvHisEnergyEntity.getTenantId())
//                                .eqIfPresent(DbStatisticsDataEntity::getStatisticsTime, pvHisEnergyEntity.getStatisticsTime()));
//                        if (CollUtil.isNotEmpty(pvHisEnergyEntities)) {
//                            deleteList.add(pvHisEnergyEntities.get(0).getMyid());
//                        }
//                    }
//
//                    if (CollUtil.isNotEmpty(deleteList)) {
//                        dbStatisticsDataMapper.deleteBatchIds(deleteList);
//                    }
//                    dbStatisticsDataMapper.insertBatch(saveList);
//                }
//
//
//                logger.info("定时任务执行完成");
//            } else {
//                // 未获取到锁，说明其他实例正在执行此任务
//                logger.info("未获取到分布式锁，任务已被其他实例执行");
//            }
//        } catch (Exception e) {
//            logger.error("定时任务执行异常: {}", e.getMessage(), e);
//        } finally {
//            // 无论执行成功与否，都要释放锁
//            if (locked) {
//                boolean released = redisLock.releaseLock(lockKey, requestId);
//                if (released) {
//                    logger.info("分布式锁释放成功");
//                } else {
//                    logger.warn("分布式锁释放失败，可能已过期");
//                }
//            }
//        }
//    }
//
//
//}