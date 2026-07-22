package cn.nobeta.dingdong.product.mapper;

import cn.nobeta.dingdong.product.domain.*;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 商品模块MyBatis Mapper接口
 * 负责SPU商品、SKU库存、库存锁定记录的数据CRUD与库存扣减/解锁/确认库存操作
 */
@Mapper
public interface ProductMapper {

    /**
     * 根据主键ID查询单条SPU完整信息（逻辑未删除）
     * @param id SPU主键id
     * @return 商品SPU实体
     */
    @Select("select id,title,subtitle,description,main_image_url,category_id,brand_id,status,created_at from product_spu where id=#{id} and deleted=0")
    ProductSpu findSpuById(Long id);

    /**
     * 根据ID查询已上架发布状态的SPU（对外展示、前端可浏览商品）
     * @param id SPU主键id
     * @return 已上架SPU实体
     */
    @Select("select id,title,subtitle,description,main_image_url,category_id,brand_id,status,created_at from product_spu where id=#{id} and deleted=0 and status=1")
    ProductSpu findPublishedSpuById(Long id);

    /**
     * 新增商品SPU基础信息，主键自增回填至入参spu对象id字段
     * @param spu SPU新增参数实体
     * @return 数据库受影响行数
     */
    @Insert("insert into product_spu(title,subtitle,description,main_image_url,category_id,brand_id,status) values(#{title},#{subtitle},#{description},#{mainImageUrl},#{categoryId},#{brandId},#{status})")
    @Options(useGeneratedKeys=true,keyProperty="id")
    int insertSpu(ProductSpu spu);

    /**
     * 更新SPU商品基础信息，仅更新逻辑未删除数据
     * @param spu 待更新SPU实体（必须携带主键id）
     * @return 数据库受影响行数
     */
    @Update("update product_spu set title=#{title},subtitle=#{subtitle},description=#{description},main_image_url=#{mainImageUrl},category_id=#{categoryId},brand_id=#{brandId},status=#{status} where id=#{id} and deleted=0")
    int updateSpu(ProductSpu spu);

    /**
     * 根据SPU主键查询该商品下所有SKU规格列表（包含下架SKU，逻辑未删），按ID升序排列
     * @param spuId 商品SPU主键ID
     * @return 对应SKU集合
     */
    @Select("select id,spu_id,sku_code,spec_json,price,available_stock,locked_stock,sales,status,version from product_sku where spu_id=#{spuId} and deleted=0 order by id")
    List<ProductSku> findSkusBySpuId(Long spuId);

    /**
     * 根据SPU ID查询可售卖上架状态SKU列表，仅查询status=1有效在售规格
     * @param spuId 父级SPU编号
     * @return 在售SKU列表
     */
    @Select("select id,spu_id,sku_code,spec_json,price,available_stock,locked_stock,sales,status,version from product_sku where spu_id=#{spuId} and deleted=0 and status=1 order by id")
    List<ProductSku> findSaleableSkusBySpuId(Long spuId);

    /**
     * 根据SKU主键+所属SPU双条件精准查询单条SKU数据
     * @param id SKU自身主键
     * @param spuId 所属SPU编号
     * @return 单个SKU实体
     */
    @Select("select id,spu_id,sku_code,spec_json,price,available_stock,locked_stock,sales,status,version from product_sku where id=#{id} and spu_id=#{spuId} and deleted=0")
    ProductSku findSku(@Param("id") Long id,@Param("spuId") Long spuId);

    /**
     * 新增SKU规格记录
     * 初始化锁定库存locked_stock=0、销量sales=0、版本号version=0，主键自动回写
     * @param sku SKU新增实体参数
     * @return 数据库受影响行数
     */
    @Insert("insert into product_sku(spu_id,sku_code,spec_json,price,available_stock,locked_stock,sales,status,version) values(#{spuId},#{skuCode},#{specJson},#{price},#{availableStock},0,0,#{status},0)")
    @Options(useGeneratedKeys=true,keyProperty="id")
    int insertSku(ProductSku sku);

    /**
     * 修改SKU基础信息（编码、规格、售价、可用库存、上下架状态）
     * 限定同SPU下、未逻辑删除才可更新
     * @param sku 待修改SKU实体（必须携带id、spuId）
     * @return 受影响行数
     */
    @Update("update product_sku set sku_code=#{skuCode},spec_json=#{specJson},price=#{price},available_stock=#{availableStock},status=#{status} where id=#{id} and spu_id=#{spuId} and deleted=0")
    int updateSku(ProductSku sku);

    /**
     * 关联查询SKU+所属SPU商品信息，用于库存业务视图展示
     * 联表product_sku与product_spu，两者均未逻辑删除
     * @param skuId SKU主键ID
     * @return 库存业务聚合视图对象
     */
    @Select("select k.id as sku_id,k.sku_code,s.title,s.main_image_url,k.spec_json,k.price,k.available_stock,k.locked_stock,k.sales,k.status from product_sku k join product_spu s on s.id=k.spu_id where k.id=#{skuId} and k.deleted=0 and s.deleted=0")
    InventorySkuView findInventorySku(Long skuId);

    @Select("select k.id as sku_id,k.sku_code,s.title,s.main_image_url,k.spec_json,k.price,k.available_stock,k.locked_stock,k.sales,k.status from product_sku k join product_spu s on s.id=k.spu_id where k.id=#{skuId} and k.deleted=0 and s.deleted=0 for update")
    InventorySkuView findInventorySkuForUpdate(Long skuId);

    /**
     * 订单下单锁定库存（乐观锁机制，version版本号自增）
     * 可用库存减少指定数量，锁定库存增加对应数量；
     * 前置校验：商品未删、在售状态、可用库存充足，否则更新行数为0代表锁库存失败
     * @param skuId 锁定库存的SKU编号
     * @param quantity 锁定库存数量
     * @return 受影响行数，0=锁库存失败
     */
    @Update("update product_sku set available_stock=available_stock-#{quantity}, locked_stock=locked_stock+#{quantity}, version=version+1 where id=#{skuId} and deleted=0 and status=1 and available_stock >= #{quantity}")
    int lockStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    /**
     * 订单取消/超时关闭，释放已锁定库存
     * 可用库存回加，锁定库存扣减；校验锁定库存足够扣减，版本号+1
     * @param skuId 目标SKU
     * @param quantity 释放数量
     * @return 受影响行数
     */
    @Update("update product_sku set available_stock=available_stock+#{quantity}, locked_stock=locked_stock-#{quantity}, version=version+1 where id=#{skuId} and locked_stock >= #{quantity}")
    int unlockStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    /**
     * 订单确认收货/支付完成，正式扣减锁定库存转为真实销量
     * 锁定库存减少，商品销量sales累加，版本号自增
     * @param skuId 对应SKU
     * @param quantity 确认扣减数量
     * @return 受影响行数
     */
    @Update("update product_sku set locked_stock=locked_stock-#{quantity}, sales=sales+#{quantity}, version=version+1 where id=#{skuId} and locked_stock >= #{quantity}")
    int confirmStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    /**
     * 插入库存锁定流水记录，记录订单维度锁单明细，初始状态LOCKED已锁定
     * @param orderNo 订单编号
     * @param skuId SKU主键
     * @param quantity 锁定件数
     * @return 插入行数
     */
    @Insert("insert into inventory_lock(order_no,sku_id,quantity,status) values(#{orderNo},#{skuId},#{quantity},'LOCKED')")
    int insertInventoryLock(@Param("orderNo") String orderNo,@Param("skuId") Long skuId,@Param("quantity") Integer quantity);

    /**
     * 统计指定订单+SKU下有效未释放锁定记录条数，用于幂等防重复锁库存
     * @param orderNo 订单号
     * @param skuId SKU ID
     * @return 符合条件记录总数
     */
    @Select("select count(1) from inventory_lock where order_no=#{orderNo} and sku_id=#{skuId} and status='LOCKED'")
    int countActiveLock(@Param("orderNo") String orderNo,@Param("skuId") Long skuId);

    /**
     * 根据订单号查询该订单下所有处于LOCKED锁定状态的库存明细列表
     * @param orderNo 订单编号
     * @return 订单关联库存锁定记录集合
     */
    @Select("select sku_id,quantity from inventory_lock where order_no=#{orderNo} and status='LOCKED'")
    List<InventoryLockRecord> findActiveLocks(String orderNo);

    /**
     * 整单取消：将该订单下所有锁定库存记录状态改为RELEASED已释放，并写入释放时间戳
     * @param orderNo 订单编号
     * @return 更新条目数量
     */
    @Update("update inventory_lock set status='RELEASED', released_at=current_timestamp where order_no=#{orderNo} and status='LOCKED'")
    int releaseLocks(String orderNo);

    /**
     * 订单履约确认：批量修改订单下锁定记录为CONFIRMED已确认核销，记录释放时间
     * @param orderNo 订单号
     * @return 更新行数
     */
    @Update("update inventory_lock set status='CONFIRMED', released_at=current_timestamp where order_no=#{orderNo} and status='LOCKED'")
    int confirmLocks(String orderNo);

    @Select("select count(1) from inventory_change_log where business_key=#{businessKey}")
    int countInventoryChange(String businessKey);

    @Insert("""
      insert into inventory_change_log(sku_id,business_key,business_type,reference_no,change_available,change_locked,change_sales,
      before_available,after_available,before_locked,after_locked,before_sales,after_sales,remark)
      values(#{skuId},#{businessKey},#{businessType},#{referenceNo},#{changeAvailable},#{changeLocked},#{changeSales},
      #{beforeAvailable},#{afterAvailable},#{beforeLocked},#{afterLocked},#{beforeSales},#{afterSales},#{remark})
      """)
    int insertInventoryChange(InventoryChangeLog log);

    @Select("""
      <script>select id,sku_id,business_key,business_type,reference_no,change_available,change_locked,change_sales,
      before_available,after_available,before_locked,after_locked,before_sales,after_sales,remark,created_at
      from inventory_change_log where 1=1
      <if test='skuId != null'>and sku_id=#{skuId}</if><if test='businessType != null and !businessType.isBlank()'>and business_type=#{businessType}</if>
      order by id desc limit #{size} offset #{offset}</script>
      """)
    List<InventoryChangeLog> findInventoryChanges(@Param("skuId") Long skuId, @Param("businessType") String businessType,
                                                   @Param("offset") int offset, @Param("size") int size);

    @Select("<script>select count(1) from inventory_change_log where 1=1 <if test='skuId != null'>and sku_id=#{skuId}</if><if test='businessType != null and !businessType.isBlank()'>and business_type=#{businessType}</if></script>")
    long countInventoryChanges(@Param("skuId") Long skuId, @Param("businessType") String businessType);

    @Update("update product_sku set available_stock=available_stock+#{delta},version=version+1 where id=#{skuId} and deleted=0 and available_stock+#{delta} >= 0")
    int adjustAvailableStock(@Param("skuId") Long skuId, @Param("delta") Integer delta);

    @Update("update product_sku set available_stock=available_stock-1,sales=sales+1,version=version+1 where id=#{skuId} and deleted=0 and status=1 and available_stock&gt;0")
    int confirmSeckillStock(Long skuId);

    /**
     * 前台商品多条件分页检索查询
     * 联表SPU+在售SKU，去重SPU数据；支持关键词、分类、品牌、价格区间筛选
     * 动态SQL拼接查询条件，${sort}为前端传入排序字段防硬编码需自行校验防SQL注入
     * limit+offset实现分页
     * @param query 分页查询条件封装对象
     * @return 分页商品SPU列表
     */
    @Select("""
      <script>select s.id,s.title,s.subtitle,s.description,s.main_image_url,s.category_id,s.brand_id,s.status,s.created_at,
      (select min(k.price) from product_sku k where k.spu_id=s.id and k.deleted=0 and k.status=1) as min_price,
      (select coalesce(sum(k.sales),0) from product_sku k where k.spu_id=s.id and k.deleted=0 and k.status=1) as sales
      from product_spu s where s.deleted=0 and s.status=1
      and exists (select 1 from product_sku k where k.spu_id=s.id and k.deleted=0 and k.status=1
      <if test='minPrice != null'>and k.price &gt;= #{minPrice}</if><if test='maxPrice != null'>and k.price &lt;= #{maxPrice}</if>)
      <if test='keyword != null and !keyword.isBlank()'>and (s.title like concat('%',#{keyword},'%') or s.subtitle like concat('%',#{keyword},'%'))</if>
      <if test='categoryId != null'>and s.category_id=#{categoryId}</if><if test='brandId != null'>and s.brand_id=#{brandId}</if>
      order by ${sort} limit #{size} offset #{offset}</script>
      """)
    List<ProductSpu> search(ProductQuery query);

    /**
     * 统计商品检索总条数，用于分页计算总页数
     * 与search查询SQL筛选条件完全一致，仅替换为count(distinct s.id)统计去重SPU总数
     * @param query 查询条件参数
     * @return 符合条件商品总数量
     */
    @Select("""
      <script>select count(distinct s.id) from product_spu s join product_sku k on k.spu_id=s.id and k.deleted=0 and k.status=1
      where s.deleted=0 and s.status=1
      <if test='keyword != null and !keyword.isBlank()'>and (s.title like concat('%',#{keyword},'%') or s.subtitle like concat('%',#{keyword},'%'))</if>
      <if test='categoryId != null'>and s.category_id=#{categoryId}</if><if test='brandId != null'>and s.brand_id=#{brandId}</if>
      <if test='minPrice != null'>and k.price &gt;= #{minPrice}</if><if test='maxPrice != null'>and k.price &lt;= #{maxPrice}</if></script>
      """)
    long countSearch(ProductQuery query);

    @Select("""
      <script>select s.id,s.title,s.subtitle,s.description,s.main_image_url,s.category_id,s.brand_id,s.status,s.created_at,
      (select min(k.price) from product_sku k where k.spu_id=s.id and k.deleted=0) as min_price,
      (select coalesce(sum(k.sales),0) from product_sku k where k.spu_id=s.id and k.deleted=0) as sales
      from product_spu s where s.deleted=0
      <if test='keyword != null and !keyword.isBlank()'>and (s.title like concat('%',#{keyword},'%') or s.subtitle like concat('%',#{keyword},'%'))</if>
      <if test='categoryId != null'>and s.category_id=#{categoryId}</if><if test='brandId != null'>and s.brand_id=#{brandId}</if>
      <if test='status != null'>and s.status=#{status}</if>
      order by s.id desc limit #{size} offset #{offset}</script>
      """)
    List<ProductSpu> searchAdmin(AdminProductQuery query);

    @Select("""
      <script>select count(1) from product_spu s where s.deleted=0
      <if test='keyword != null and !keyword.isBlank()'>and (s.title like concat('%',#{keyword},'%') or s.subtitle like concat('%',#{keyword},'%'))</if>
      <if test='categoryId != null'>and s.category_id=#{categoryId}</if><if test='brandId != null'>and s.brand_id=#{brandId}</if>
      <if test='status != null'>and s.status=#{status}</if></script>
      """)
    long countAdmin(AdminProductQuery query);
}
