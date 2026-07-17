package cn.nobeta.dingdong.product.mapper;
import cn.nobeta.dingdong.product.domain.Category;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 商品分类Mapper
 * 操作product_category分类表，deleted=0为未删除有效数据
 */
@Mapper
public interface CategoryMapper {
    /**
     * 查询所有未删除分类，按排序号、主键升序排列
     */
    @Select("select id,name,parent_id,sort_order,status from product_category where deleted=0 order by sort_order,id") 
    List<Category> findAll();
    /**
     * 查询已启用且未删除的分类数据
     */
    @Select("select id,name,parent_id,sort_order,status from product_category where deleted=0 and status=1 order by sort_order,id")
     List<Category> findEnabled();
    /**
     * 根据id查询单条未删除分类
     */
    @Select("select id,name,parent_id,sort_order,status from product_category where id=#{id} and deleted=0") 
    Category findById(Long id);
    /**
     * 统计同一父分类下同名分类数量，用于重名校验
     */
    @Select("select count(1) from product_category where name=#{name} and parent_id=#{parentId} and deleted=0") 
    int countByName(@Param("name") String name, @Param("parentId") Long parentId);
    /**
     * 新增分类，自动回填自增主键id
     */
    @Insert("insert into product_category(name,parent_id,sort_order,status) values(#{name},#{parentId},#{sortOrder},#{status})") @Options(useGeneratedKeys=true,keyProperty="id") 
    int insert(Category category);
    /**
     * 根据id更新未删除的分类信息
     */
    @Update("update product_category set name=#{name},parent_id=#{parentId},sort_order=#{sortOrder},status=#{status} where id=#{id} and deleted=0")
     int update(Category category);
}
