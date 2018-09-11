package com.jaagro.tms.biz.service.impl;

import com.jaagro.tms.api.dto.base.ListTruckTypeDto;
import com.jaagro.tms.api.dto.customer.ShowSiteDto;
import com.jaagro.tms.api.dto.truck.TruckDto;
import com.jaagro.tms.api.dto.waybill.*;
import com.jaagro.tms.api.service.OrderService;
import com.jaagro.tms.api.service.WaybillPlanService;
import com.jaagro.tms.biz.entity.OrderGoods;
import com.jaagro.tms.biz.entity.OrderItems;
import com.jaagro.tms.biz.entity.Orders;
import com.jaagro.tms.biz.mapper.*;
import com.jaagro.tms.biz.service.CustomerClientService;
import com.jaagro.tms.biz.service.DriverClientService;
import com.jaagro.tms.biz.service.TruckClientService;
import com.jaagro.tms.biz.service.TruckTypeClientService;
import com.jaagro.tms.biz.vo.MiddleObjectVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tony
 */
@Service
public class WaybillPlanServiceImpl implements WaybillPlanService {

    private static final Logger log = LoggerFactory.getLogger(WaybillPlanServiceImpl.class);

    @Autowired
    private CurrentUserService currentUserService;
    @Autowired
    private TruckTypeClientService truckTypeClientService;
    @Autowired
    private CustomerClientService customerClientService;
    @Autowired
    private WaybillMapper waybillMapper;
    @Autowired
    private WaybillItemsMapper waybillItemsMapper;
    @Autowired
    private WaybillGoodsMapper waybillGoodsMapper;
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderItemsMapper orderItemsMapper;
    @Autowired
    private OrderGoodsMapper orderGoodsMapper;
    @Autowired
    private WaybillTrackingImagesMapper waybillTrackingImagesMapper;
    @Autowired
    private WaybillTrackingMapper waybillTrackingMapper;
    @Autowired
    private OrderGoodsMarginMapper orderGoodsMarginMapper;
    @Autowired
    private OrderService orderService;
    @Autowired
    private DriverClientService driverClientService;
    @Autowired
    private TruckClientService truckClientService;

    @Override
    public List<ListWaybillPlanDto> createWaybillPlan(CreateWaybillPlanDto waybillDto){
        Integer orderId = waybillDto.getOrderId();
        List<CreateWaybillItemsPlanDto>  waybillItemsDtos = waybillDto.getWaybillItems();
        List<TruckDto> truckDtos = waybillDto.getTrucks();
        List<MiddleObjectVo> middleObjects = new ArrayList<>();
        for(CreateWaybillItemsPlanDto waybillItemsDto:waybillItemsDtos) {
            Integer orderItemId = waybillItemsDto.getOrderItemId();
            List<CreateWaybillGoodsPlanDto> goods =  waybillItemsDto.getGoods();
            for(CreateWaybillGoodsPlanDto waybillGoodsDto:goods){
                MiddleObjectVo  middleObject = new MiddleObjectVo();
                middleObject.setOrderId(orderId);
                middleObject.setOrderItemId(orderItemId);
                middleObject.setOrderGoodsId(waybillGoodsDto.getGoodsId());
                middleObject.setProportioning(waybillGoodsDto.getProportioning());
                middleObjects.add(middleObject);
            }
        }
        List<ListWaybillPlanDto> waybillDtos = null;
        if(!CollectionUtils.isEmpty(middleObjects)){
            try {
                waybillDtos=wayBillAlgorithm(middleObjects,truckDtos);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("计算配运单失败，货物内容={}", middleObjects);
                throw new RuntimeException("计算配运单失败");
            }
        }
        return waybillDtos;
    }

    /**
     * 根据orderId获取订单计划
     *
     * @param orderId
     * @return
     */
    @Override
    public Map<String, Object> getWaybillPlanByOrderId(Integer orderId) {
        return null;
    }

    /**
     * 从配载计划中移除运单【逻辑删除】
     *
     * @param waybillId
     * @return 结果集
     * @author tony
     */
    @Override
    public Map<String, Object> removeWaybillFromPlan(Integer waybillId) {
        //判断运单状态是否满足条件
        return null;
    }

    /**
     * 货物配运单核心算法
     * @param middleObjects
     * @param truckDtos
     */
    private List<ListWaybillPlanDto> wayBillAlgorithm(List<MiddleObjectVo> middleObjects,List<TruckDto> truckDtos){
        List<MiddleObjectVo> middleObjects_assigned = new ArrayList<>();
        List<ListWaybillPlanDto> waybillDtos = new ArrayList<>();
        //按配送地排序
        List<MiddleObjectVo> newMiddleObjectsList = middleObjects.stream().sorted((e1,e2)->Integer.compare(e1.getOrderItemId(),e2.getOrderItemId())).collect(Collectors.toList());
        Iterator<TruckDto> truckIterator = truckDtos.iterator();
        while(truckIterator.hasNext()) {
            TruckDto truckDto = truckIterator.next();
            Integer capacity = truckDto.getCapacity();
            //使用按照送货地排序后货物分配到车辆生成临时送货单
            List<MiddleObjectVo> assignList = new ArrayList<>();
            for (int k = 0;k<truckDto.getNumber(); k++){
                Iterator<MiddleObjectVo> goodsIterator = newMiddleObjectsList.iterator();
                Integer proposioningCount = 0;
                while (goodsIterator.hasNext()) {
                    MiddleObjectVo middleObject = goodsIterator.next();
                    proposioningCount += middleObject.getProportioning();
                    Integer planAmount  = (middleObject.getProportioning() + capacity - proposioningCount);
                    MiddleObjectVo assignedObj = new MiddleObjectVo();
                    BeanUtils.copyProperties(middleObject,assignedObj);
                    assignList.add(assignedObj);
                    if (proposioningCount > capacity) {
                        List<MiddleObjectVo> assignedDtos = assignList.stream().filter(c -> c.getOrderGoodsId().equals(middleObject.getOrderGoodsId())).collect(Collectors.toList());
                        assignedDtos.get(0).setTruckId(truckDto.getTruckId());
                        assignedDtos.get(0).setProportioning(planAmount);
                        assignedDtos.get(0).setPlanAmount(planAmount);
                        assignedDtos.get(0).setUnPlanAmount(0);
                        middleObject.setProportioning(proposioningCount - capacity);
                    } else {
                        List<MiddleObjectVo> assignedDtos = assignList.stream().filter(c -> c.getOrderGoodsId().equals(middleObject.getOrderGoodsId())).collect(Collectors.toList());
                        assignedDtos.get(0).setTruckId(truckDto.getTruckId());
                        assignedDtos.get(0).setPlanAmount(middleObject.getProportioning());
                        assignedDtos.get(0).setUnPlanAmount(0);
                        goodsIterator.remove();
                    }

                    Integer assignTotal = assignList.stream().mapToInt(c -> c.getPlanAmount()).sum();
                    boolean flag = (assignTotal.equals(capacity) || (assignTotal<capacity && newMiddleObjectsList.size()==0));
                    if (flag) {
                        middleObjects_assigned.addAll(assignList);
                        ListWaybillPlanDto waybillDto = this.makeOneWaybillPlan(assignList);
                        waybillDtos.add(waybillDto);
                        assignList.clear();
                        break;
                    }
                }
            }
        }
        System.out.println("margin==="+newMiddleObjectsList.toString());
        System.out.println("assigned==="+middleObjects_assigned.toString());
        log.error("货物配运时剩余货物，margin={}", newMiddleObjectsList);
        log.error("货物配运时已经分配的货物，assigned={}", middleObjects_assigned);

        return waybillDtos;
    }

    /**
     * 生成一个运单计划
     * @param assignList
     * @return
     */
    private  ListWaybillPlanDto makeOneWaybillPlan(List<MiddleObjectVo> assignList){
        ListWaybillPlanDto waybillPlanDto = new ListWaybillPlanDto();
        Orders ordersData = null;
        ListTruckTypeDto truckType = null;
        Integer orderId = 0;
        Integer truckTypeId = 0;
        for (MiddleObjectVo obj:assignList) {
            orderId = obj.getOrderId();
            ordersData = ordersMapper.selectByPrimaryKey(orderId);
            if(ordersData == null){
                throw new NullPointerException("订单号为：" + orderId + " 不存在");
            }
            truckTypeId= obj.getTruckId();
            truckType = truckTypeClientService.getTruckTypeById(truckTypeId);
            break;
        }
        waybillPlanDto.setOrderId(orderId);
        waybillPlanDto.setNeedTruckTypeId(truckTypeId);
        waybillPlanDto.setNeedTruckType(truckType);
        waybillPlanDto.setLoadSiteId(ordersData.getLoadSiteId());
        waybillPlanDto.setTruckTeamContractId(ordersData.getCustomerContractId());
        waybillPlanDto.setLoadTime(ordersData.getLoadTime());
        waybillPlanDto.setWaybillPlanTime(new Date());
        waybillPlanDto.setGoodType(ordersData.getGoodsType());
        List<ListWaybillItemsPlanDto> itemsList = new ArrayList<>();
        for (MiddleObjectVo obj:assignList) {
            OrderGoods orderGoods = orderGoodsMapper.selectByPrimaryKey(obj.getOrderGoodsId());
            Integer goodsUnit = orderGoods.getGoodsUnit();
            if(orderGoods == null){
                throw new NullPointerException("goodsId为：" + obj.getOrderGoodsId()+ " 的货物不存在");
            }
            OrderItems orderItems = orderItemsMapper.selectByPrimaryKey(obj.getOrderItemId());
            ListWaybillItemsPlanDto waybillItemsPlanDto = new ListWaybillItemsPlanDto();
            ShowSiteDto showSiteDto = customerClientService.getShowSiteById(orderItems.getUnloadId());
            waybillItemsPlanDto.setUnloadSiteId(orderItems.getUnloadId());
            waybillItemsPlanDto.setOrderItemId(orderItems.getId());
            waybillItemsPlanDto.setShowSiteDto(showSiteDto);
            waybillItemsPlanDto.setRequiredTime(orderItems.getUnloadTime());
            List<ListWaybillGoodsPlanDto> goodsList = new LinkedList<>();
            ListWaybillGoodsPlanDto waybillGoodsDto = new ListWaybillGoodsPlanDto();
            waybillGoodsDto
                    .setOrderGoodsId(orderGoods.getId())
                    .setGoodsName(orderGoods.getGoodsName())
                    .setGoodsQuantity(orderGoods.getGoodsQuantity())
                    .setGoodsUnit(orderGoods.getGoodsUnit())
                    .setJoinDrug(orderGoods.getJoinDrug());
            if(goodsUnit==3){
                waybillGoodsDto.setGoodsWeight(new BigDecimal(obj.getPlanAmount()));
            }else{
                waybillGoodsDto.setGoodsQuantity(obj.getPlanAmount());
            }
            goodsList.add(waybillGoodsDto);
            waybillItemsPlanDto.setGoods(goodsList);
            itemsList.add(waybillItemsPlanDto);
        }
        waybillPlanDto.setWaybillItems(itemsList);
        return waybillPlanDto;
    }

}
