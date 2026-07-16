package cn.nobeta.dingdong.product.mapper;
import cn.nobeta.dingdong.product.domain.Brand;
import org.apache.ibatis.annotations.*;
import java.util.List;
@Mapper
public interface BrandMapper {
    @Select("select id,name,logo_url,sort_order,status from product_brand where deleted=0 order by sort_order,id") List<Brand> findAll();
    @Select("select id,name,logo_url,sort_order,status from product_brand where deleted=0 and status=1 order by sort_order,id") List<Brand> findEnabled();
    @Select("select id,name,logo_url,sort_order,status from product_brand where id=#{id} and deleted=0") Brand findById(Long id);
    @Select("select count(1) from product_brand where name=#{name} and deleted=0") int countByName(String name);
    @Insert("insert into product_brand(name,logo_url,sort_order,status) values(#{name},#{logoUrl},#{sortOrder},#{status})") @Options(useGeneratedKeys=true,keyProperty="id") int insert(Brand brand);
    @Update("update product_brand set name=#{name},logo_url=#{logoUrl},sort_order=#{sortOrder},status=#{status} where id=#{id} and deleted=0") int update(Brand brand);
}
