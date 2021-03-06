package com.jaagro.tms.api.dto.order;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author tony
 */
@Data
@Accessors(chain = true)
public class CreateOrderDto implements Serializable {

    /**
     * 客户合同id
     */
    private Integer customerContractId;

    /**
     * 货物类型
     */
    private Integer goodsType;

    /**
     * 客户id
     */
    private Integer customerId;

    /**
     * 装货地id
     */
    private Integer loadSiteId;

    /**
     * 要求装货时间
     */
    private Date loadTime;

    /**
     * 是否需要纸质回单
     */
    private Boolean paperReceipt;

    /**
     * 备注
     */
    private String notes;

    /**
     * 订单明细列表
     */
    private List<CreateOrderItemsDto> orderItems;
}
