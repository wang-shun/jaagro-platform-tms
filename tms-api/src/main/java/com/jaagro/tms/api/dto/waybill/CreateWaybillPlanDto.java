package com.jaagro.tms.api.dto.waybill;

import com.jaagro.tms.api.dto.truck.TruckDto;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author gavin
 */
@Data
@Accessors(chain = true)
public class CreateWaybillPlanDto implements Serializable {

    /**
     *订单id
     */
    private Integer orderId;

    /**
     * 需求车型列表
     */
    private List<TruckDto> trucks;

    /**
     * 发货地和对应的货物明细列表 运单明细列表
     */
    private List<CreateWaybillItemsPlanDto> waybillItems;
}