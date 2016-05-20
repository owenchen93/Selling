package selling.sunshine.controller;

import com.alibaba.fastjson.JSONObject;
import com.pingplusplus.model.Charge;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import selling.sunshine.model.Agent;
import selling.sunshine.model.DepositBill;
import selling.sunshine.model.User;
import selling.sunshine.service.AgentService;
import selling.sunshine.service.BillService;
import selling.sunshine.service.ToolService;
import selling.sunshine.utils.Prompt;
import selling.sunshine.utils.PromptCode;
import selling.sunshine.utils.ResponseCode;
import selling.sunshine.utils.ResultData;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sunshine on 4/14/16.
 */
@RequestMapping("/account")
@RestController
public class AccountController {

    private Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private ToolService toolService;

    @Autowired
    private BillService billService;

    @Autowired
    private AgentService agentService;

    @RequestMapping(method = RequestMethod.GET, value = "/info")
    public ModelAndView info() {
        ModelAndView view = new ModelAndView();
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        Agent agent = user.getAgent();
        Map<String, Object> condition = new HashMap<>();
        condition.put("agentId", agent.getAgentId());
        Agent target = ((List<Agent>) agentService.fetchAgent(condition)
                .getData()).get(0);
        view.addObject("agent", target);
        view.setViewName("/agent/account/info");
        return view;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/deposit")
    public ModelAndView deposit() {
        ModelAndView view = new ModelAndView();
        view.setViewName("/agent/account/recharge");
        return view;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/deposit")
    public Charge deposit(HttpServletRequest request) {
        Charge charge = new Charge();
        JSONObject params = toolService.getParams(request);
        Subject subject = SecurityUtils.getSubject();
        String clientIp = toolService.getIP(request);
        User user = (User) subject.getPrincipal();
        DepositBill bill = new DepositBill(Double.parseDouble(String
                .valueOf(params.get("amount"))), String.valueOf(params
                .get("channel")), clientIp, user.getAgent());
        ResultData createResponse = billService.createDepositBill(bill);
        if (createResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
            charge = (Charge) createResponse.getData();
        }
        return charge;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/deposit/{billId}/prompt")
    public ModelAndView prompt(@PathVariable("billId") String billId,
                               String result) {
        ModelAndView view = new ModelAndView();
        Prompt prompt = new Prompt();
        if (!StringUtils.isEmpty(result) && result.equals("success")) {
            prompt.setCode(PromptCode.SUCCESS);
            prompt.setTitle("提示");
            prompt.setMessage("恭喜您,充值成功!");
        } else {
            prompt.setCode(PromptCode.WARNING);
            prompt.setMessage("对不起,您的充值已取消.");
        }
        prompt.setConfirmURL("/account/info");
        view.addObject("prompt", prompt);
        view.setViewName("/agent/prompt");
        return view;
    }
}
