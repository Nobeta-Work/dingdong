package cn.nobeta.dingdong.order.service;
import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.common.rpc.ProductInventoryFacade;
import cn.nobeta.dingdong.order.api.OrderRequests.*;
import cn.nobeta.dingdong.order.domain.CartItem;
import cn.nobeta.dingdong.order.repository.CartRepository;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车业务服务
 * <p>管理用户的购物车状态，支持添加商品、更新数量/选中状态、批量删除等操作。
 * 与产品服务（product-service）通过 Dubbo RPC 交互，确保商品有效性。</p>
 */
@Service
public class CartService {
    private final CartRepository cartRepository;
    @DubboReference(check=false) private ProductInventoryFacade productFacade;

    public CartService(CartRepository cartRepository){this.cartRepository=cartRepository;}

    /** 查询当前用户购物车列表 */
    public List<CartItem> list(Long userId){return cartRepository.findByUserId(userId);}

    /**
     * 添加商品到购物车（幂等合并）
     * <ol>
     * <li>通过 RPC 验证商品 SKU 存在并获取快照</li>
     * <li>查询该用户是否已存在该 SKU 的购物车项</li>
     * <li>若不存在则创建新项，否则累加数量（上限 99）</li>
     * <li>标记为选中状态</li>
     * </ol>
     */
    public CartItem add(Long userId,AddCartItemRequest r){
        // 1. 验证商品有效性（调用产品服务）
        productFacade.getSkuSnapshots(List.of(r.skuId()));
        // 2. 查询现有购物车项
        CartItem current=cartRepository.findBySku(userId,r.skuId());
        if(current==null){
            // 新建购物车项
            current=new CartItem();
            current.setUserId(userId);
            current.setSkuId(r.skuId());
            current.setId(r.skuId());
            current.setQuantity(r.quantity());
            current.setSelected(true);
            current.setCreatedAt(LocalDateTime.now());
        }else{
            // 合并数量（累加）
            current.setQuantity(Math.addExact(current.getQuantity(),r.quantity()));
            if(current.getQuantity()>99)throw new BusinessException("CART_QUANTITY_LIMIT","单个商品最多购买 99 件");
            current.setSelected(true);
        }
        return cartRepository.save(current);
    }

    /** 更新购物车项数量与选中状态 */
    public CartItem update(Long userId,Long id,UpdateCartItemRequest r){
        CartItem item=require(userId,id);
        item.setQuantity(r.quantity());
        item.setSelected(r.selected());
        return cartRepository.save(item);
    }

    /** 从 Redis 删除购物车项 */
    public void delete(Long userId,Long id){
        if(!cartRepository.deleteOwned(userId,id))throw new BusinessException("CART_ITEM_NOT_FOUND","购物车商品不存在");
    }

    /** 内部校验：确认指定 ID 的购物车项属于当前用户 */
    public CartItem require(Long userId,Long id){
        CartItem item=cartRepository.findOwned(userId,id);
        if(item==null)throw new BusinessException("CART_ITEM_NOT_FOUND","购物车商品不存在");
        return item;
    }

    /**
     * 获取待结算的购物车项 — 为订单创建服务
     * <ul>
     * <li>当 {@code ids} 为 {@code null} 或空列表时，返回所有当前用户已选中的商品</li>
     * <li>当 {@code ids} 不为空时，返回列表中包含的所有购物车项（需全部存在）</li>
     * </ul>
     * @param userId 当前用户 ID
     * @param ids 购物车项 ID 列表（可选）
     * @return 有效的购物车项列表
     * @throws BusinessException 当任何指定 ID 的商品不存在时抛出
     */
    public List<CartItem> itemsForOrder(Long userId,List<Long> ids){
        // 查询当前用户所有购物车项
        List<CartItem> source=cartRepository.findByUserId(userId);
        // 筛选符合条件的商品
        List<CartItem> result=(ids==null||ids.isEmpty())?
                // 无指定 ID → 取所有选中项
                source.stream().filter(i->Boolean.TRUE.equals(i.getSelected())).toList():
                // 有指定 ID → 取列表中包含的项（需全部存在）
                source.stream().filter(i->ids.contains(i.getId())).toList();
        // 校验结果有效性
        if(result.isEmpty()||(ids!=null&&!ids.isEmpty()&&result.size()!=ids.stream().distinct().count()))
            throw new BusinessException("CART_ITEM_NOT_FOUND","待结算购物车商品不存在");
        return result;
    }

    /**
     * 订单创建成功后从 Redis 批量删除购物车项
     * <p>调用者需确保 {@code ids} 与 {@link #itemsForOrder} 返回的项一一对应，
     * 避免误删用户未结算的商品。</p>
     */
    public void removeAfterOrder(Long userId,List<Long> ids){
        cartRepository.deleteBatch(userId,ids);
    }
}
