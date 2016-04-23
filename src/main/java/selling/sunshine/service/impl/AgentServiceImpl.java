package selling.sunshine.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import selling.sunshine.dao.AgentDao;
import selling.sunshine.model.Agent;
import selling.sunshine.pagination.DataTableParam;
import selling.sunshine.service.AgentService;
import selling.sunshine.utils.Encryption;
import selling.sunshine.utils.ResponseCode;
import selling.sunshine.utils.ResultData;

import java.util.Map;

/**
 * Created by sunshine on 4/8/16.
 */
@Service
public class AgentServiceImpl implements AgentService {
    private Logger logger = LoggerFactory.getLogger(AgentServiceImpl.class);

    @Autowired
    private AgentDao agentDao;

    @Override
    public ResultData login(Map<String, Object> condition) {
        ResultData result = new ResultData();

        return result;
    }

    @Override
    public ResultData createAgent(Agent agent) {
        ResultData result = new ResultData();
        agent.setPassword(Encryption.desEncode(agent.getPassword(), agent.getPhone()));
        ResultData insertResponse = agentDao.insertAgent(agent);
        result.setResponseCode(insertResponse.getResponseCode());
        if (insertResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
            result.setData(insertResponse.getData());
        } else {
            result.setDescription(insertResponse.getDescription());
        }
        return result;
    }

    @Override
    public ResultData fetchAgent(Map<String, Object> condition, DataTableParam param) {
        ResultData result = new ResultData();
        ResultData queryResponse = agentDao.queryAgentByPage(condition, param);
        result.setResponseCode(queryResponse.getResponseCode());
        if (queryResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
            result.setData(queryResponse.getData());
        } else if (queryResponse.getResponseCode() == ResponseCode.RESPONSE_ERROR) {
            result.setDescription(queryResponse.getDescription());
        }
        return result;
    }
}
