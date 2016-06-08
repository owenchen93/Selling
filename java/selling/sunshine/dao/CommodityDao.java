package selling.sunshine.dao;

import selling.sunshine.model.Goods;
import selling.sunshine.pagination.DataTableParam;
import selling.sunshine.utils.ResultData;

import java.util.Map;

/**
 * Created by sunshine on 4/8/16.
 */
public interface CommodityDao {
    ResultData insertCommodity(Goods goods);

    ResultData queryCommodity(Map<String, Object> condition);

    ResultData queryCommodityByPage(Map<String, Object> condition, DataTableParam param);
    
    ResultData updateCommodity(Goods goods);

}