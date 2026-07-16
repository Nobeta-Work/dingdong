package cn.nobeta.dingdong.order.service;
import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.common.rpc.ProductInventoryFacade;
import cn.nobeta.dingdong.order.api.OrderRequests.*;
import cn.nobeta.dingdong.order.domain.CartItem;
import cn.nobeta.dingdong.order.mapper.CartMapper;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
public class CartService {
 private final CartMapper cartMapper;
 @DubboReference(check=false) private ProductInventoryFacade productFacade;
 public CartService(CartMapper cartMapper){this.cartMapper=cartMapper;}
 public List<CartItem> list(Long userId){return cartMapper.findByUserId(userId);}
 @Transactional public CartItem add(Long userId,AddCartItemRequest r){
  productFacade.getSkuSnapshots(List.of(r.skuId()));
  CartItem current=cartMapper.findBySku(userId,r.skuId());
  if(current==null){current=new CartItem();current.setUserId(userId);current.setSkuId(r.skuId());current.setQuantity(r.quantity());current.setSelected(true);cartMapper.insert(current);}else{current.setQuantity(Math.addExact(current.getQuantity(),r.quantity()));if(current.getQuantity()>99)throw new BusinessException("CART_QUANTITY_LIMIT","单个商品最多购买 99 件");cartMapper.update(current);} return require(userId,current.getId());
 }
 @Transactional public CartItem update(Long userId,Long id,UpdateCartItemRequest r){CartItem item=require(userId,id);item.setQuantity(r.quantity());item.setSelected(r.selected());cartMapper.update(item);return require(userId,id);}
 @Transactional public void delete(Long userId,Long id){if(cartMapper.deleteOwned(id,userId)==0)throw new BusinessException("CART_ITEM_NOT_FOUND","购物车商品不存在");}
 public CartItem require(Long userId,Long id){CartItem item=cartMapper.findOwned(id,userId);if(item==null)throw new BusinessException("CART_ITEM_NOT_FOUND","购物车商品不存在");return item;}
 public List<CartItem> itemsForOrder(Long userId,List<Long> ids){List<CartItem> source=cartMapper.findByUserId(userId);List<CartItem> result=(ids==null||ids.isEmpty())?source.stream().filter(i->Boolean.TRUE.equals(i.getSelected())).toList():source.stream().filter(i->ids.contains(i.getId())).toList();if(result.isEmpty()||(ids!=null&&!ids.isEmpty()&&result.size()!=ids.stream().distinct().count()))throw new BusinessException("CART_ITEM_NOT_FOUND","待结算购物车商品不存在");return result;}
 @Transactional public void removeAfterOrder(Long userId,List<Long> ids){if(!ids.isEmpty())cartMapper.deleteBatch(userId,ids);}
}
