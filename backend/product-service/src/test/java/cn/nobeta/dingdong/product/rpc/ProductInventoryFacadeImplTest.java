package cn.nobeta.dingdong.product.rpc;
import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.common.rpc.ProductInventoryFacade.LockItem;
import cn.nobeta.dingdong.product.domain.InventorySkuView;
import cn.nobeta.dingdong.product.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;import java.util.List;
import static org.junit.jupiter.api.Assertions.*;import static org.mockito.ArgumentMatchers.*;import static org.mockito.Mockito.*;
class ProductInventoryFacadeImplTest {
 @Test void locksStockAndReturnsSnapshot() {
  ProductMapper mapper=mock(ProductMapper.class);InventorySkuView sku=sku();when(mapper.findInventorySku(1L)).thenReturn(sku);when(mapper.countActiveLock("DD1",1L)).thenReturn(0);when(mapper.lockStock(1L,2)).thenReturn(1);
  var result=new ProductInventoryFacadeImpl(mapper).lockInventory("DD1",List.of(new LockItem(1L,2)));
  assertEquals(new BigDecimal("99.90"),result.getFirst().price());assertEquals(8,result.getFirst().availableStock());verify(mapper).insertInventoryLock("DD1",1L,2);
 }
 @Test void rejectsInsufficientStockWithoutCreatingLock() {
  ProductMapper mapper=mock(ProductMapper.class);when(mapper.findInventorySku(1L)).thenReturn(sku());when(mapper.countActiveLock("DD1",1L)).thenReturn(0);when(mapper.lockStock(1L,20)).thenReturn(0);
  BusinessException error=assertThrows(BusinessException.class,()->new ProductInventoryFacadeImpl(mapper).lockInventory("DD1",List.of(new LockItem(1L,20))));
  assertEquals("INVENTORY_INSUFFICIENT",error.getCode());verify(mapper,never()).insertInventoryLock(anyString(),anyLong(),anyInt());
 }
 private InventorySkuView sku(){InventorySkuView s=new InventorySkuView();s.setSkuId(1L);s.setSkuCode("DEMO-SKU");s.setTitle("演示商品");s.setSpecJson("{}");s.setPrice(new BigDecimal("99.90"));s.setAvailableStock(10);s.setStatus(1);return s;}
}
