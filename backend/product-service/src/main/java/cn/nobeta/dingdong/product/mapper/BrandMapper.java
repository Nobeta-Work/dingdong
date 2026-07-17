package cn.nobeta.dingdong.product.mapper;

import cn.nobeta.dingdong.product.domain.Brand;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 商品品牌Mapper接口
 * 负责product_brand品牌数据表的CRUD数据库操作
 * 逻辑删除标识：deleted=0 代表未删除有效数据
 */
@Mapper
public interface BrandMapper {

    /**
     * 查询所有未被逻辑删除的品牌列表
     * 按排序字段sort_order升序，同排序则按主键id升序排列
     * @return 全部有效品牌集合
     */
    @Select("select id,name,logo_url,sort_order,status from product_brand where deleted=0 order by sort_order,id")
    List<Brand> findAll();

    /**
     * 查询已启用状态、未逻辑删除的品牌
     * 筛选条件：deleted=0 且 启用状态status=1
     * 排序规则：先按sort_order，再按id
     * @return 可用启用品牌列表
     */
    @Select("select id,name,logo_url,sort_order,status from product_brand where deleted=0 and status=1 order by sort_order,id")
    List<Brand> findEnabled();

    /**
     * 根据主键ID查询单条有效品牌信息
     * @param id 品牌主键id
     * @return 匹配id且未删除的品牌对象，无数据返回null
     */
    @Select("select id,name,logo_url,sort_order,status from product_brand where id=#{id} and deleted=0")
    Brand findById(Long id);

    /**
     * 统计指定品牌名称在有效数据中的条数
     * 用于新增/编辑时校验品牌名称是否重复
     * @param name 待校验的品牌名称
     * @return 重名数据数量
     */
    @Select("select count(1) from product_brand where name=#{name} and deleted=0")
    int countByName(String name);

    /**
     * 新增品牌数据
     * 自动回填数据库自增主键至实体类id属性
     * @param brand 品牌实体参数
     * @return 数据库受影响行数
     */
    @Insert("insert into product_brand(name,logo_url,sort_order,status) values(#{name},#{logoUrl},#{sortOrder},#{status})")
    @Options(useGeneratedKeys=true,keyProperty="id")
    int insert(Brand brand);

    /**
     * 根据主键id更新品牌基础信息
     * 仅更新未被逻辑删除(deleted=0)的数据
     * @param brand 携带id及待修改字段的品牌实体
     * @return 数据库受影响行数
     */
    @Update("update product_brand set name=#{name},logo_url=#{logoUrl},sort_order=#{sortOrder},status=#{status} where id=#{id} and deleted=0")
    int update(Brand brand);
}
