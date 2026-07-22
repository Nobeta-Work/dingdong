package cn.nobeta.dingdong.product.rpc;
import cn.nobeta.dingdong.common.exception.BusinessException;
import cn.nobeta.dingdong.common.rpc.ProductInventoryFacade.LockItem;
import cn.nobeta.dingdong.product.domain.InventorySkuView;
import cn.nobeta.dingdong.product.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;import java.util.List;
import static org.junit.jupiter.api.Assertions.*;import static org.mockito.ArgumentMatchers.*;import static org.mockito.Mockito.*;

/**
 * ProductInventoryFacadeImpl#lockInventory 库存锁定流程测试
 * 价格校验链路测试：验证库存锁定返回的 SkuSnapshot 中包含正确的数据库价格，
 * 以及库存不足时正确拒绝。
 * - 正常锁定：返回快照中包含正确的价格（99.90）和可用库存
 * - 库存不足：抛出 BusinessException 且不插入锁定记录
 */
class ProductInventoryFacadeImplTest {
	/** 测试正常锁定：返回价格快照与剩余可用库存 */
	@Test void locksStockAndReturnsSnapshot() {
		ProductMapper mapper=mock(ProductMapper.class);InventorySkuView sku=sku();when(mapper.findInventorySkuForUpdate(1L)).thenReturn(sku);when(mapper.countActiveLock("DD1",1L)).thenReturn(0);when(mapper.lockStock(1L,2)).thenReturn(1);
		var result=new ProductInventoryFacadeImpl(mapper).lockInventory("DD1",List.of(new LockItem(1L,2)));
		// 验证：返回的快照价格与数据库一致（99.90）
		assertEquals(new BigDecimal("99.90"),result.getFirst().price());
		// 验证：剩余可用库存 = 10 - 2 = 8
		assertEquals(8,result.getFirst().availableStock());verify(mapper).insertInventoryLock("DD1",1L,2);
	}
	/** 测试库存不足：抛出异常且不插入锁定记录 */
	@Test void rejectsInsufficientStockWithoutCreatingLock() {
		ProductMapper mapper=mock(ProductMapper.class);when(mapper.findInventorySkuForUpdate(1L)).thenReturn(sku());when(mapper.countActiveLock("DD1",1L)).thenReturn(0);when(mapper.lockStock(1L,20)).thenReturn(0);
		BusinessException error=assertThrows(BusinessException.class,()->new ProductInventoryFacadeImpl(mapper).lockInventory("DD1",List.of(new LockItem(1L,20))));
		// 验证：库存不足异常码
		assertEquals("INVENTORY_INSUFFICIENT",error.getCode());
		// 验证：未插入锁定记录
		verify(mapper,never()).insertInventoryLock(anyString(),anyLong(),anyInt());
	}
	/** 构造测试用的在售 SKU 视图（价格 99.90，可用库存 10） */
	private InventorySkuView sku(){InventorySkuView s=new InventorySkuView();s.setSkuId(1L);s.setSkuCode("DEMO-SKU");s.setTitle("演示商品");s.setSpecJson("{}");s.setPrice(new BigDecimal("99.90"));s.setAvailableStock(10);s.setLockedStock(0);s.setSales(0);s.setStatus(1);return s;}
}
