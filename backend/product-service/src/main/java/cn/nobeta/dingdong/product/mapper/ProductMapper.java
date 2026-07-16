package cn.nobeta.dingdong.product.mapper;
import cn.nobeta.dingdong.product.domain.*;
import org.apache.ibatis.annotations.*;
import java.util.List;
@Mapper
public interface ProductMapper {
    @Select("select id,title,subtitle,description,main_image_url,category_id,brand_id,status,created_at from product_spu where id=#{id} and deleted=0") ProductSpu findSpuById(Long id);
    @Select("select id,title,subtitle,description,main_image_url,category_id,brand_id,status,created_at from product_spu where id=#{id} and deleted=0 and status=1") ProductSpu findPublishedSpuById(Long id);
    @Insert("insert into product_spu(title,subtitle,description,main_image_url,category_id,brand_id,status) values(#{title},#{subtitle},#{description},#{mainImageUrl},#{categoryId},#{brandId},#{status})") @Options(useGeneratedKeys=true,keyProperty="id") int insertSpu(ProductSpu spu);
    @Update("update product_spu set title=#{title},subtitle=#{subtitle},description=#{description},main_image_url=#{mainImageUrl},category_id=#{categoryId},brand_id=#{brandId},status=#{status} where id=#{id} and deleted=0") int updateSpu(ProductSpu spu);
    @Select("select id,spu_id,sku_code,spec_json,price,available_stock,locked_stock,sales,status,version from product_sku where spu_id=#{spuId} and deleted=0 order by id") List<ProductSku> findSkusBySpuId(Long spuId);
    @Select("select id,spu_id,sku_code,spec_json,price,available_stock,locked_stock,sales,status,version from product_sku where spu_id=#{spuId} and deleted=0 and status=1 order by id") List<ProductSku> findSaleableSkusBySpuId(Long spuId);
    @Select("select id,spu_id,sku_code,spec_json,price,available_stock,locked_stock,sales,status,version from product_sku where id=#{id} and spu_id=#{spuId} and deleted=0") ProductSku findSku(@Param("id") Long id,@Param("spuId") Long spuId);
    @Insert("insert into product_sku(spu_id,sku_code,spec_json,price,available_stock,locked_stock,sales,status,version) values(#{spuId},#{skuCode},#{specJson},#{price},#{availableStock},0,0,#{status},0)") @Options(useGeneratedKeys=true,keyProperty="id") int insertSku(ProductSku sku);
    @Update("update product_sku set sku_code=#{skuCode},spec_json=#{specJson},price=#{price},available_stock=#{availableStock},status=#{status} where id=#{id} and spu_id=#{spuId} and deleted=0") int updateSku(ProductSku sku);
    @Select("select k.id as sku_id,k.sku_code,s.title,s.main_image_url,k.spec_json,k.price,k.available_stock,k.status from product_sku k join product_spu s on s.id=k.spu_id where k.id=#{skuId} and k.deleted=0 and s.deleted=0") InventorySkuView findInventorySku(Long skuId);
    @Update("update product_sku set available_stock=available_stock-#{quantity}, locked_stock=locked_stock+#{quantity}, version=version+1 where id=#{skuId} and deleted=0 and status=1 and available_stock >= #{quantity}") int lockStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);
    @Update("update product_sku set available_stock=available_stock+#{quantity}, locked_stock=locked_stock-#{quantity}, version=version+1 where id=#{skuId} and locked_stock >= #{quantity}") int unlockStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);
    @Update("update product_sku set locked_stock=locked_stock-#{quantity}, sales=sales+#{quantity}, version=version+1 where id=#{skuId} and locked_stock >= #{quantity}") int confirmStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);
    @Insert("insert into inventory_lock(order_no,sku_id,quantity,status) values(#{orderNo},#{skuId},#{quantity},'LOCKED')") int insertInventoryLock(@Param("orderNo") String orderNo,@Param("skuId") Long skuId,@Param("quantity") Integer quantity);
    @Select("select count(1) from inventory_lock where order_no=#{orderNo} and sku_id=#{skuId} and status='LOCKED'") int countActiveLock(@Param("orderNo") String orderNo,@Param("skuId") Long skuId);
    @Select("select sku_id,quantity from inventory_lock where order_no=#{orderNo} and status='LOCKED'") List<InventoryLockRecord> findActiveLocks(String orderNo);
    @Update("update inventory_lock set status='RELEASED', released_at=current_timestamp where order_no=#{orderNo} and status='LOCKED'") int releaseLocks(String orderNo);
    @Update("update inventory_lock set status='CONFIRMED', released_at=current_timestamp where order_no=#{orderNo} and status='LOCKED'") int confirmLocks(String orderNo);
    @Select("""
      <script>select distinct s.id,s.title,s.subtitle,s.description,s.main_image_url,s.category_id,s.brand_id,s.status,s.created_at
      from product_spu s join product_sku k on k.spu_id=s.id and k.deleted=0 and k.status=1
      where s.deleted=0 and s.status=1
      <if test='keyword != null and !keyword.isBlank()'>and (s.title like concat('%',#{keyword},'%') or s.subtitle like concat('%',#{keyword},'%'))</if>
      <if test='categoryId != null'>and s.category_id=#{categoryId}</if><if test='brandId != null'>and s.brand_id=#{brandId}</if>
      <if test='minPrice != null'>and k.price &gt;= #{minPrice}</if><if test='maxPrice != null'>and k.price &lt;= #{maxPrice}</if>
      order by ${sort} limit #{size} offset #{offset}</script>
      """) List<ProductSpu> search(ProductQuery query);
    @Select("""
      <script>select count(distinct s.id) from product_spu s join product_sku k on k.spu_id=s.id and k.deleted=0 and k.status=1
      where s.deleted=0 and s.status=1
      <if test='keyword != null and !keyword.isBlank()'>and (s.title like concat('%',#{keyword},'%') or s.subtitle like concat('%',#{keyword},'%'))</if>
      <if test='categoryId != null'>and s.category_id=#{categoryId}</if><if test='brandId != null'>and s.brand_id=#{brandId}</if>
      <if test='minPrice != null'>and k.price &gt;= #{minPrice}</if><if test='maxPrice != null'>and k.price &lt;= #{maxPrice}</if></script>
      """) long countSearch(ProductQuery query);
}
