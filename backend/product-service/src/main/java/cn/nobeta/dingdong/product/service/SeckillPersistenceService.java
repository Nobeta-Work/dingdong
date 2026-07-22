package cn.nobeta.dingdong.product.service;

import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.product.domain.InventoryChangeLog;
import cn.nobeta.dingdong.product.domain.InventorySkuView;
import cn.nobeta.dingdong.product.domain.SeckillActivity;
import cn.nobeta.dingdong.product.domain.SeckillOrder;
import cn.nobeta.dingdong.product.event.SeckillOrderEvent;
import cn.nobeta.dingdong.product.mapper.ProductMapper;
import cn.nobeta.dingdong.product.mapper.SeckillMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeckillPersistenceService {
    private final SeckillMapper mapper; private final ProductMapper productMapper;
    public SeckillPersistenceService(SeckillMapper mapper,ProductMapper productMapper){this.mapper=mapper;this.productMapper=productMapper;}

    @Transactional
    public void persist(SeckillOrderEvent event){
        if(mapper.findOrderByRequest(event.requestId())!=null)return;
        SeckillActivity activity=mapper.findActivity(event.activityId());
        if(activity==null)throw new BusinessException("SECKILL_ACTIVITY_NOT_FOUND","秒杀活动不存在");
        InventorySkuView before=productMapper.findInventorySkuForUpdate(event.skuId());
        if(before==null||before.getAvailableStock()<=0)throw new BusinessException("SECKILL_DB_STOCK_INSUFFICIENT","数据库商品库存不足");
        if(mapper.decreaseDbStock(event.activityId())==0)throw new BusinessException("SECKILL_ACTIVITY_SOLD_OUT","数据库活动库存不足");
        if(productMapper.confirmSeckillStock(event.skuId())==0)throw new BusinessException("SECKILL_DB_STOCK_INSUFFICIENT","数据库商品库存不足");
        SeckillOrder order=new SeckillOrder();order.setRequestId(event.requestId());order.setActivityId(event.activityId());order.setUserId(event.userId());order.setSkuId(event.skuId());order.setQuantity(1);order.setSeckillPrice(activity.getSeckillPrice());
        mapper.insertOrder(order);
        InventoryChangeLog log=new InventoryChangeLog();log.setSkuId(event.skuId());log.setBusinessKey("SECKILL_CONFIRM:"+event.requestId());log.setBusinessType("SECKILL_CONFIRM");log.setReferenceNo(event.requestId());
        log.setChangeAvailable(-1);log.setChangeLocked(0);log.setChangeSales(1);log.setBeforeAvailable(before.getAvailableStock());log.setAfterAvailable(before.getAvailableStock()-1);
        log.setBeforeLocked(before.getLockedStock());log.setAfterLocked(before.getLockedStock());log.setBeforeSales(before.getSales());log.setAfterSales(before.getSales()+1);log.setRemark("秒杀消息异步落库");
        productMapper.insertInventoryChange(log);
    }
}
