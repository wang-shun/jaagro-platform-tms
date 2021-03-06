package com.jaagro.tms.web.vo.pc;

import com.jaagro.tms.web.vo.chat.SiteVo;
import com.jaagro.tms.web.vo.chat.UserVo;
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
public class GetOrderItemsVo implements Serializable {

    /**
     *
     */
    private Integer id;

    /**
     * 订单id
     */
    private Integer orderId;

    /**
     * 卸货地id
     */
    private SiteVo unload;

    /**
     * 要求卸货时间
     */
    private Date unloadTime;

    /**
     * 是否有效
     */
    private Boolean enabled;

    /**
     * 修改时间
     */
    private Date modifyTime;

    /**
     * 修改人id
     */
    private UserVo modifyUserId;

    /**
     * 货物列表
     */
    private List<GetOrderGoodsVo> goodsVoList;
}
