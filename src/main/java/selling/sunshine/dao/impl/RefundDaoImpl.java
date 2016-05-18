package selling.sunshine.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import selling.sunshine.dao.BaseDao;
import selling.sunshine.dao.RefundDao;
import selling.sunshine.model.RefundConfig;
import selling.sunshine.utils.IDGenerator;
import selling.sunshine.utils.ResponseCode;
import selling.sunshine.utils.ResultData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sunshine on 5/17/16.
 */
public class RefundDaoImpl extends BaseDao implements RefundDao {

    private Logger logger = LoggerFactory.getLogger(RefundDaoImpl.class);

    private Object lock = new Object();

    @Transactional
    @Override
    public ResultData insertRefundConfig(RefundConfig config) {
        ResultData result = new ResultData();
        config.setRefundConfigId(IDGenerator.generate("RCG"));
        synchronized (lock) {
            try {
                Map<String, Object> condition = new HashMap<>();
                condition.put("goodsId", config.getGoods().getGoodsId());
                condition.put("blockFlag", false);
                RefundConfig target = sqlSession.selectOne("selling.refund.config.query", condition);
                if (target != null) {
                    target.setBlockFlag(true);
                    sqlSession.update("selling.refund.config.block", target);
                }
                sqlSession.insert("selling.refund.config.insert", config);
                result.setData(config);
            } catch (Exception e) {
                logger.error(e.getMessage());
                result.setResponseCode(ResponseCode.RESPONSE_ERROR);
                result.setDescription(e.getMessage());
            } finally {
                return result;
            }
        }
    }

    @Override
    public ResultData queryRefundConfig(Map<String, Object> condition) {
        ResultData result = new ResultData();
        condition = handle(condition);
        try {
            List<RefundConfig> list = sqlSession.selectList("selling.refund.config.query", condition);
            result.setData(list);
        } catch (Exception e) {
            logger.error(e.getMessage());
            result.setResponseCode(ResponseCode.RESPONSE_ERROR);
            result.setDescription(e.getMessage());
        }
        return result;
    }
}