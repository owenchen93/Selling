package selling.sunshine.controller;

import com.alibaba.fastjson.JSONObject;
import com.pingplusplus.model.Charge;
import common.sunshine.model.selling.agent.Agent;
import common.sunshine.model.selling.bill.CustomerOrderBill;
import common.sunshine.model.selling.bill.DepositBill;
import common.sunshine.model.selling.bill.OrderBill;
import common.sunshine.model.selling.user.User;
import common.sunshine.utils.ResponseCode;
import common.sunshine.utils.ResultData;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import selling.sunshine.form.WithdrawForm;
import selling.sunshine.model.WithdrawRecord;
import selling.sunshine.service.*;
import selling.sunshine.utils.Prompt;
import selling.sunshine.utils.PromptCode;
import selling.sunshine.utils.WechatConfig;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
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
    private OrderService orderService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private WithdrawService withdrawService;

    /**
     * 代理商账户余额首页
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/info")
    public ModelAndView info() {
        ModelAndView view = new ModelAndView();
        //确保当前已有代理商登录
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        if (user == null) {
            view.setViewName("redirect:/agent/login");
            return view;
        }
        //获取当前登录代理商的详细信息
        Map<String, Object> condition = new HashMap<>();
        condition.put("agentId", user.getAgent().getAgentId());
        Agent target = ((List<Agent>) agentService.fetchAgent(condition).getData()).get(0);
        view.addObject("agent", target);
        WechatConfig.oauthWechat(view, "/account/info");
        view.setViewName("/agent/account/info");
        return view;
    }

    /**
     * 提现页面
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/withdraw")
    public ModelAndView withdraw() {
        ModelAndView view = new ModelAndView();
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        if (user == null) {
            view.setViewName("redirect:/agent/login");
            return view;
        }
        Map<String, Object> condition = new HashMap<>();
        condition.put("agentId", user.getAgent().getAgentId());
        ResultData fetchAgentResponse = agentService.fetchAgent(condition);
        if (fetchAgentResponse.getResponseCode() != ResponseCode.RESPONSE_OK) {
            Prompt prompt = new Prompt("失败", "代理商不存在", "/account/info");
            view.addObject("prompt", prompt);
            WechatConfig.oauthWechat(view, "/agent/prompt");
            view.setViewName("/agent/prompt");
            return view;
        }
        Agent agent = ((List<Agent>) fetchAgentResponse.getData()).get(0);

        view.addObject("agent", agent);
        WechatConfig.oauthWechat(view, "/agent/account/withdraw");
        view.setViewName("/agent/account/withdraw");
        return view;
    }

    /**
     * 提现表单
     *
     * @param form
     * @param result
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/withdraw")
    public ModelAndView withdraw(@Valid WithdrawForm form, BindingResult result) {
        ModelAndView view = new ModelAndView();
        if (result.hasErrors()) {
            view.setViewName("redirect:/account/withdraw");
            return view;
        }
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        if (user == null) {
            view.setViewName("redirect:/agent/login");
            return view;
        }
        Map<String, Object> condition = new HashMap<>();
        condition.put("agentId", user.getAgent().getAgentId());
        ResultData fetchAgentResponse = agentService.fetchAgent(condition);
        if (fetchAgentResponse.getResponseCode() != ResponseCode.RESPONSE_OK) {
            Prompt prompt = new Prompt("失败", "代理商不存在", "/account/info");
            view.addObject("prompt", prompt);
            WechatConfig.oauthWechat(view, "/agent/prompt");
            view.setViewName("/agent/prompt");
            return view;
        }
        Agent agent = ((List<Agent>) fetchAgentResponse.getData()).get(0);
        double money = form.getMoney();
        condition.clear();
        if (money > agent.getCoffer()) {
            Prompt prompt = new Prompt("提示", "您的的提现金额超过余额", "/account/info");
            view.addObject("prompt", prompt);
            WechatConfig.oauthWechat(view, "/agent/prompt");
            view.setViewName("/agent/prompt");
            return view;
        }
        WithdrawRecord record = new WithdrawRecord();
        record.setAgent(user.getAgent());
        record.setAmount(money);
        record.setOpenId(agent.getWechat());
        record.setWealth(agent.getCoffer());
        ResultData withdrawData = agentService.applyWithdraw(record);
        if (withdrawData.getResponseCode() != ResponseCode.RESPONSE_OK) {
            Prompt prompt = new Prompt("失败", "申请提现失败", "/account/info");
            view.addObject("prompt", prompt);
            WechatConfig.oauthWechat(view, "/agent/prompt");
            view.setViewName("/agent/prompt");
            return view;
        }
        ResultData consumeData = agentService.consume(agent, money);
        if (consumeData.getResponseCode() != ResponseCode.RESPONSE_OK) {
            Prompt prompt = new Prompt("失败", "余额不足", "/account/info");
            view.addObject("prompt", prompt);
            WechatConfig.oauthWechat(view, "/agent/prompt");
            view.setViewName("/agent/prompt");
            return view;
        }
        Prompt prompt = new Prompt("提示", "申请提现成功,提现金额当前已被冻结，预计2日内到账", "/account/info");
        view.addObject("prompt", prompt);
        WechatConfig.oauthWechat(view, "/agent/prompt");
        view.setViewName("/agent/prompt");
        return view;
    }

    /**
     * 充值页面
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/deposit")
    public ModelAndView deposit() {
        ModelAndView view = new ModelAndView();
        WechatConfig.oauthWechat(view, "/agent/account/recharge");
        view.setViewName("/agent/account/recharge");
        return view;
    }

    /**
     * 充值表单
     *
     * @param request
     * @return
     */
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
        String openId = params.getString("open_id");
        ResultData createResponse = billService.createDepositBill(bill, openId);
        if (createResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
            charge = (Charge) createResponse.getData();
        }
        return charge;
    }

    /**
     * 充值提示页面
     *
     * @param billId
     * @param result
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/charge/{billId}/prompt")
    public ModelAndView prompt(@PathVariable("billId") String billId, String result) {
        ModelAndView view = new ModelAndView();
        Prompt prompt;
        if (billId.startsWith("DPB")) {
            if (!StringUtils.isEmpty(result) && result.equals("success")) {
                prompt = new Prompt("提示", "恭喜您,充值成功!", "/account/info");
            } else {
                prompt = new Prompt(PromptCode.WARNING, "提示", "对不起,您的充值已取消.", "/account/info");
            }
            view.addObject("prompt", prompt);
            WechatConfig.oauthWechat(view, "/agent/prompt");
            view.setViewName("/agent/prompt");
        } else if (billId.startsWith("ODB")) {
            String orderId = "";
            Map<String, Object> condition = new HashMap<String, Object>();
            condition.put("billId", billId);
            ResultData billFetchData = billService.fetchOrderBill(condition);
            if (billFetchData.getResponseCode() == ResponseCode.RESPONSE_OK) {
                OrderBill orderBill = ((List<OrderBill>) billFetchData.getData()).get(0);
                orderId = orderBill.getOrder().getOrderId();
            }
            if (!StringUtils.isEmpty(result) && result.equals("success")) {
                prompt = new Prompt("付款成功", "订单号：" + orderId + "，请等待发货", "/agent/order/manage/2");
            } else {
                prompt = new Prompt(PromptCode.WARNING, "提示", "对不起,您的付款失败了，请联系工作人员.", "/agent/order/manage/2");
            }
            view.addObject("prompt", prompt);
            WechatConfig.oauthWechat(view, "/agent/prompt");
            view.setViewName("/agent/prompt");
        } else if (billId.startsWith("COB")) {
            String orderId = "";
            Map<String, Object> condition = new HashMap<String, Object>();
            condition.put("billId", billId);
            ResultData billFetchData = billService.fetchCustomerOrderBill(condition);
            if (billFetchData.getResponseCode() == ResponseCode.RESPONSE_OK) {
                CustomerOrderBill customerOrderBill = ((List<CustomerOrderBill>) billFetchData.getData()).get(0);
                orderId = customerOrderBill.getCustomerOrder().getOrderId();
            }
            if (!StringUtils.isEmpty(result) && result.equals("success")) {
                prompt = new Prompt("提示", "恭喜您,付款成功!", "/account/info");
            } else {
                prompt = new Prompt(PromptCode.WARNING, "提示", "对不起,您的付款已取消.", "/account/info");
            }
            view.addObject("prompt", prompt);
            view.addObject("orderId", orderId);
            WechatConfig.oauthWechat(view, "/customer/prompt");
            view.setViewName("/customer/prompt");
        }
        return view;
    }
}
