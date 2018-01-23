package com.babel.venus.dao.db;


import com.babel.forseti_order.model.UserOrderPO;
import com.babel.venus.vo.UserOrderVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.MapperMy;

import java.util.List;

/**
 * 会员注单
 */
@Repository
public interface UserOrderMapper extends MapperMy<UserOrderPO> {

    int insertByDate(@Param("param") UserOrderPO record);

    int batchInsert(@Param("list") List<UserOrderPO> orderPOs,@Param("pdate") String pdate);

    /**
     * 获取当期无效的追单数据
     * @param vo
     * @return
     */
    List<UserOrderPO> findOrders(@Param("param") UserOrderVo vo, @Param("pdate") String pdate);



}