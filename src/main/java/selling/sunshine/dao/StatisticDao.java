package selling.sunshine.dao;

import selling.sunshine.pagination.DataTableParam;
import selling.sunshine.utils.ResultData;

/**
 * Created by sunshine on 6/24/16.
 */
public interface StatisticDao {
    ResultData queryOrderSum();
    
    ResultData orderStatistics();
    
    ResultData orderStatisticsByPage(DataTableParam param);
    
    ResultData agentGoodsMonthByPage(DataTableParam param);
    
    ResultData agentGoodsMonth();
    
    ResultData agentGoodsByPage(DataTableParam param);
    
    ResultData agentGoods();
    
    ResultData orderMonth();
    
    ResultData orderByYear();
    
    ResultData topThreeAgent();
}
