package com.cardioguard.controller;

import com.cardioguard.common.Result;
import com.cardioguard.entity.EcgMultiLeadRecord;
import com.cardioguard.service.EcgMultiLeadRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 多导联ECG记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ecg/multilead")
@Api(tags = "多导联ECG记录管理")
public class EcgMultiLeadRecordController {
    
    @Autowired
    private EcgMultiLeadRecordService multiLeadRecordService;
    
    @PostMapping
    @ApiOperation("创建多导联ECG记录")
    public Result<EcgMultiLeadRecord> createRecord(@RequestBody EcgMultiLeadRecord record) {
        try {
            EcgMultiLeadRecord result = multiLeadRecordService.createRecord(record);
            return Result.success(result);
        } catch (Exception e) {
            log.error("创建多导联ECG记录失败", e);
            return Result.error("创建多导联ECG记录失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/{recordId}/status")
    @ApiOperation("更新记录状态")
    public Result<EcgMultiLeadRecord> updateRecordStatus(
            @PathVariable Long recordId,
            @RequestParam String status) {
        try {
            EcgMultiLeadRecord result = multiLeadRecordService.updateRecordStatus(recordId, status);
            if (result != null) {
                return Result.success(result);
            } else {
                return Result.error("记录不存在");
            }
        } catch (Exception e) {
            log.error("更新记录状态失败", e);
            return Result.error("更新记录状态失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/user/{userId}")
    @ApiOperation("查询用户的多导联记录")
    public Result<List<EcgMultiLeadRecord>> getRecordsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            List<EcgMultiLeadRecord> records = multiLeadRecordService.getRecordsByUserId(userId, page, size);
            return Result.success(records);
        } catch (Exception e) {
            log.error("查询多导联记录失败", e);
            return Result.error("查询多导联记录失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/{recordId}")
    @ApiOperation("获取记录详情")
    public Result<EcgMultiLeadRecord> getRecordById(@PathVariable Long recordId) {
        try {
            EcgMultiLeadRecord record = multiLeadRecordService.getRecordById(recordId);
            if (record != null) {
                return Result.success(record);
            } else {
                return Result.error("记录不存在");
            }
        } catch (Exception e) {
            log.error("获取记录详情失败", e);
            return Result.error("获取记录详情失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{recordId}")
    @ApiOperation("删除记录")
    public Result<String> deleteRecord(
            @PathVariable Long recordId,
            @RequestParam Long userId) {
        try {
            multiLeadRecordService.deleteRecord(recordId, userId);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除记录失败", e);
            return Result.error("删除记录失败: " + e.getMessage());
        }
    }
}
