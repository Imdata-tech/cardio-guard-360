package com.cardioguard.service;

import com.cardioguard.entity.EcgMultiLeadRecord;

import java.util.List;

/**
 * 多导联ECG记录服务接口
 */
public interface EcgMultiLeadRecordService {
    
    /**
     * 创建多导联ECG记录
     * 
     * @param record 记录对象
     * @return 创建的记录
     */
    EcgMultiLeadRecord createRecord(EcgMultiLeadRecord record);
    
    /**
     * 更新记录状态
     * 
     * @param recordId 记录ID
     * @param status 状态
     * @return 更新后的记录
     */
    EcgMultiLeadRecord updateRecordStatus(Long recordId, String status);
    
    /**
     * 根据用户ID查询记录列表
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 记录列表
     */
    List<EcgMultiLeadRecord> getRecordsByUserId(Long userId, Integer page, Integer size);
    
    /**
     * 根据记录ID获取详情
     * 
     * @param recordId 记录ID
     * @return 记录详情
     */
    EcgMultiLeadRecord getRecordById(Long recordId);
    
    /**
     * 删除记录
     * 
     * @param recordId 记录ID
     * @param userId 用户ID(权限验证)
     */
    void deleteRecord(Long recordId, Long userId);
}
