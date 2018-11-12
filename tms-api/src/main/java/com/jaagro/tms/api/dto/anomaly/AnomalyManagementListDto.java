package com.jaagro.tms.api.dto.anomaly;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author @Gao.
 * 异常管理列表
 */
@Data
@Accessors(chain = true)
public class AnomalyManagementListDto implements Serializable {

    /**
     * 异常id
     */
    private Integer id;

    /**
     * 异常类型
     */
    private String typeName;

    /**
     * 处理状态
     */
    private String processingStatus;

    /**
     * 扣款金额
     */
    private String deductMoney;

    /**
     * 赔款金额
     */
    private String compensateMoney;

    /**
     * 运单编号
     */
    private Integer waybillId;

    /**
     * 登记时间
     */
    private Date createTime;

    /**
     * 登记人
     */
    private String creatorName;

    /**
     * 处理人
     */
    private String processorName;

    /**
     * 审核人
     */
    private String auditName;

    /**
     * 审核人处理描述
     */
    private String auditDesc;

    /**
     * 审核状态
     */
    private String auditStatus;
    /**
     * 审核时间
     */
    private Date auditTime;
    /**
     * 是否涉及费用调整  0-否 1-是
     */
    private Boolean adjustStatus;

}