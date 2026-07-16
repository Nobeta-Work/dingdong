package cn.nobeta.dingdong.product.mapper;
import cn.nobeta.dingdong.product.domain.Category;
import org.apache.ibatis.annotations.*;
import java.util.List;
@Mapper
public interface CategoryMapper {
    @Select("select id,name,parent_id,sort_order,status from product_category where deleted=0 order by sort_order,id") List<Category> findAll();
    @Select("select id,name,parent_id,sort_order,status from product_category where deleted=0 and status=1 order by sort_order,id") List<Category> findEnabled();
    @Select("select id,name,parent_id,sort_order,status from product_category where id=#{id} and deleted=0") Category findById(Long id);
    @Select("select count(1) from product_category where name=#{name} and parent_id=#{parentId} and deleted=0") int countByName(@Param("name") String name, @Param("parentId") Long parentId);
    @Insert("insert into product_category(name,parent_id,sort_order,status) values(#{name},#{parentId},#{sortOrder},#{status})") @Options(useGeneratedKeys=true,keyProperty="id") int insert(Category category);
    @Update("update product_category set name=#{name},parent_id=#{parentId},sort_order=#{sortOrder},status=#{status} where id=#{id} and deleted=0") int update(Category category);
}
