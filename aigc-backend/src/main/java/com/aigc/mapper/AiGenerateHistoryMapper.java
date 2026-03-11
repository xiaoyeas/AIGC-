package com.aigc.mapper;

import com.aigc.entity.AiGenerateHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI图片生成历史记录Mapper接口
 * 
 * <p>继承MyBatis-Plus的BaseMapper接口，自动获得基础的CRUD能力。
 * 无需编写XML映射文件即可完成大部分数据库操作。</p>
 * 
 * <p>继承的方法：</p>
 * <ul>
 *   <li>insert(T entity): 插入一条记录</li>
 *   <li>deleteById(Serializable id): 根据ID删除</li>
 *   <li>updateById(T entity): 根据ID更新</li>
 *   <li>selectById(Serializable id): 根据ID查询</li>
 *   <li>selectList(Wrapper&lt;T&gt; queryWrapper): 条件查询列表</li>
 *   <li>selectPage(Page&lt;T&gt; page, Wrapper&lt;T&gt; queryWrapper): 分页查询</li>
 * </ul>
 * 
 * <p>设计思路：</p>
 * <ul>
 *   <li>利用MyBatis-Plus简化开发，减少重复代码</li>
 *   <li>复杂查询可在Service层使用LambdaQueryWrapper构建</li>
 *   <li>如需自定义SQL，可在此接口添加方法并编写XML</li>
 * </ul>
 * 
 * @author AIGC Platform
 * @version 1.0.0
 */
@Mapper
public interface AiGenerateHistoryMapper extends BaseMapper<AiGenerateHistory> {
}
