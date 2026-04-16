package com.cardioguard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cardioguard.entity.EcgMultiLeadRecord;
import com.cardioguard.mapper.EcgMultiLeadRecordMapper;
import com.cardioguard.service.EcgMultiLeadRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 多导联ECG记录服务实现类
 */
@Slf4j
@Service
public class EcgMultiLeadRecordServiceImpl implements EcgMultiLeadRecordService {
    
    @Autowired
    private EcgMultiLeadRecordMapper multiLeadRecordMapper;
    
    @Override
    public EcgMultiLeadRecord createRecord(EcgMultiLeadRecord record) {
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        record.setStatus("RECORDING");
        multiLeadRecordMapper.insert(record);
        return record;
    }
    
    @Override
    public EcgMultiLeadRecord updateRecordStatus(Long recordId, String status) {
        EcgMultiLeadRecord record = multiLeadRecordMapper.selectById(recordId);
        if (record != null) {
            record.setStatus(status);
            record.setUpdatedAt(LocalDateTime.now());
            multiLeadRecordMapper.updateById(record);
        }
        return record;
    }
    
    @Override
    public List<EcgMultiLeadRecord> getRecordsByUserId(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<EcgMultiLeadRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcgMultiLeadRecord::getUserId, userId)
               .orderByDesc(EcgMultiLeadRecord::getCreatedAt);
        
        Page<EcgMultiLeadRecord> pageInfo = new Page<>(page, size);
        Page<EcgMultiLeadRecord> result = multiLeadRecordMapper.selectPage(pageInfo, wrapper);
        return result.getRecords();
    }
    
    @Override
    public EcgMultiLeadRecord getRecordById(Long recordId) {
        return multiLeadRecordMapper.selectById(recordId);
    }
    
    @Override
    public void deleteRecord(Long recordId, Long userId) {
        EcgMultiLeadRecord record = multiLeadRecordMapper.selectById(recordId);
        if (record != null && record.getUserId().equals(userId)) {
            multiLeadRecordMapper.deleteById(recordId);
        } else {
            throw new RuntimeException("记录不存在或无权限删除");
        }
    }
}
