package selling.sunshine.service;

import selling.sunshine.pagination.DataTableParam;
import selling.sunshine.utils.ResultData;

/**
 * Created by sunshine on 6/24/16.
 */
public interface StatisticService {
    ResultData query4OrderSum();
    
    ResultData orderStatisticsByPage(DataTableParam param);
    
    ResultData agentGoodsMonthByPage(DataTableParam param);
    
    ResultData agentGoodsByPage(DataTableParam param);
    
    ResultData orderMonth();
    
    ResultData orderByYear();
    
    ResultData topThreeAgent();
}
