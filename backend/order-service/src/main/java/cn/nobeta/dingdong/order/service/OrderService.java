package cn.nobeta.dingdong.order.service;
import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.common.rpc.ProductInventoryFacade;
import cn.nobeta.dingdong.common.rpc.UserAddressFacade;
import cn.nobeta.dingdong.order.api.OrderRequests.CreateOrderRequest;
import cn.nobeta.dingdong.order.domain.*;
import cn.nobeta.dingdong.order.mapper.OrderMapper;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;import java.time.LocalDateTime;import java.time.format.DateTimeFormatter;import java.util.*;
@Service
public class OrderService {
 private final OrderMapper orderMapper;private final CartService cartService;
 @DubboReference(check=false) private ProductInventoryFacade productFacade;
 @DubboReference(check=false) private UserAddressFacade addressFacade;
 public OrderService(OrderMapper orderMapper,CartService cartService){this.orderMapper=orderMapper;this.cartService=cartService;}
 @Transactional public MallOrder create(Long userId,CreateOrderRequest request){
  List<CartItem> carts=cartService.itemsForOrder(userId,request.cartItemIds());
  var address=addressFacade.getAddressSnapshot(userId,request.addressId());String orderNo=nextOrderNo();boolean locked=false;
  try{
   List<ProductInventoryFacade.LockItem> locks=carts.stream().map(i->new ProductInventoryFacade.LockItem(i.getSkuId(),i.getQuantity())).toList();
   List<ProductInventoryFacade.SkuSnapshot> snapshots=productFacade.lockInventory(orderNo,locks);locked=true;
   Map<Long,ProductInventoryFacade.SkuSnapshot> snapshotMap=new HashMap<>();snapshots.forEach(s->snapshotMap.put(s.skuId(),s));
   BigDecimal total=BigDecimal.ZERO;for(CartItem cart:carts){var s=snapshotMap.get(cart.getSkuId());if(s==null)throw new BusinessException("PRODUCT_SKU_NOT_FOUND","商品不可售");total=total.add(s.price().multiply(BigDecimal.valueOf(cart.getQuantity())));}
   MallOrder order=new MallOrder();order.setOrderNo(orderNo);order.setUserId(userId);order.setReceiverName(address.receiverName());order.setReceiverPhone(address.receiverPhone());order.setReceiverAddress(String.join(" ",address.province(),address.city(),address.district(),address.detailAddress()));order.setTotalAmount(total);order.setStatus("PENDING_PAYMENT");orderMapper.insertOrder(order);
   for(CartItem cart:carts){var s=snapshotMap.get(cart.getSkuId());OrderItem item=new OrderItem();item.setOrderId(order.getId());item.setSkuId(s.skuId());item.setSkuCode(s.skuCode());item.setProductTitle(s.title());item.setProductImageUrl(s.mainImageUrl());item.setSpecJson(s.specJson());item.setUnitPrice(s.price());item.setQuantity(cart.getQuantity());item.setTotalAmount(s.price().multiply(BigDecimal.valueOf(cart.getQuantity())));orderMapper.insertItem(item);}
   cartService.removeAfterOrder(userId,carts.stream().map(CartItem::getId).toList());return orderMapper.findOwned(orderNo,userId);
  }catch(RuntimeException exception){if(locked)productFacade.unlockInventory(orderNo);throw exception;}
 }
 public MallOrder get(Long userId,String orderNo){MallOrder o=orderMapper.findOwned(orderNo,userId);if(o==null)throw new BusinessException("ORDER_NOT_FOUND","订单不存在");return o;}
 public List<OrderItem> items(Long orderId){return orderMapper.findItems(orderId);}
 public List<MallOrder> page(Long userId,int page,int size){return orderMapper.findPage(userId,size,(page-1)*size);}
 public long count(Long userId){return orderMapper.countByUserId(userId);}
 private String nextOrderNo(){return "DD"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))+String.format("%03d",new Random().nextInt(1000));}
}
