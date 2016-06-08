package selling.sunshine.controller;

import com.alibaba.fastjson.JSONObject;

import com.pingplusplus.model.Charge;
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

import selling.sunshine.form.BankCardForm;
import selling.sunshine.form.WithdrawForm;
import selling.sunshine.model.Agent;
import selling.sunshine.model.BankCard;
import selling.sunshine.model.DepositBill;
import selling.sunshine.model.Order;
import selling.sunshine.model.OrderBill;
import selling.sunshine.model.User;
import selling.sunshine.model.WithdrawRecord;
import selling.sunshine.service.AgentService;
import selling.sunshine.service.BillService;
import selling.sunshine.service.ToolService;
import selling.sunshine.utils.Prompt;
import selling.sunshine.utils.PromptCode;
import selling.sunshine.utils.ResponseCode;
import selling.sunshine.utils.ResultData;

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
    private AgentService agentService;

    @RequestMapping(method = RequestMethod.GET, value = "/info")
    public ModelAndView info() {
        ModelAndView view = new ModelAndView();
        Subject subject = SecurityUtils.getSubject();
        User user = (User) subject.getPrincipal();
        if (user == null) {
            view.setViewName("/agent/login");
            return view;
        }
        //获取agent的详细信息
        Map<String, Object> condition = new HashMap<>();
        condition.put("agentId", user.getAgent().getAgentId());
        Agent target = ((List<Agent>) agentService.fetchAgent(condition)
                .getData()).get(0);
        view.addObject("agent", target);
        view.setViewName("/agent/account/info");
        return view;
    }
     
    @RequestMapping(method = RequestMethod.GET, value = "/withdraw")
    public ModelAndView withdraw(){
    	ModelAndView view = new ModelAndView();
    	Subject subject = SecurityUtils.getSubject();
    	User user = (User) subject.getPrincipal();
    	if (user == null) {
            view.setViewName("/agent/login");
            return view;
        }
    	Map<String, Object> condition = new HashMap<String, Object>();
    	condition.put("agentId", user.getAgent().getAgentId());
    	ResultData fetchAgentResponse = agentService.fetchAgent(condition);
    	if(fetchAgentResponse.getResponseCode() != ResponseCode.RESPONSE_OK){
    		Prompt prompt = new Prompt("失败", "代理商不存在", "/account/info");
            view.addObject("prompt", prompt);
            view.setViewName("/agent/prompt");
    		return view;
    	}
    	Agent agent = ((List<Agent>)fetchAgentResponse.getData()).get(0);
    	
    	ResultData fetchBankCardResponse = agentService.fetchBankCard(condition);
    	if(fetchBankCardResponse.getResponseCode() == ResponseCode.RESPONSE_ERROR){
    		Prompt prompt = new Prompt("失败", "银行卡错误", "/account/info");
            view.addObject("prompt", prompt);
            view.setViewName("/agent/prompt");
            return view;
    	}
    	if(fetchBankCardResponse.getResponseCode() == ResponseCode.RESPONSE_NULL){
    		view.addObject("bankCard","empty");
    	}else{
    		List<BankCard> bankCardList = (List<BankCard>) fetchBankCardResponse.getData();
    		view.addObject("bankCard", bankCardList.get(0));
    	}
    	view.addObject("agent", agent);
    	view.setViewName("/agent/account/withdraw");
    	return view;
    }
    
    @RequestMapping(method = RequestMethod.POST, value="/bankCardModify")
    public ResultData bankCardModify(@Valid BankCardForm form){
    	ResultData result = new ResultData();
    	Subject subject = SecurityUtils.getSubject();
    	User user = (User) subject.getPrincipal();
    	if (user == null) {
            result.setResponseCode(ResponseCode.RESPONSE_ERROR);
            result.setDescription("请重新登录");
            return result;
        }
    	BankCard bankCard = new BankCard(form.getBankCardNo(),user.getAgent());
    	ResultData modifyData = agentService.modifyBankCard(bankCard);
    	return modifyData;
    }
    
    @RequestMapping(method = RequestMethod.POST, value="/withdraw")
    public ModelAndView withdraw(@Valid WithdrawForm form, BindingResult result){
    	ModelAndView view = new ModelAndView();
    	Subject subject = SecurityUtils.getSubject();
    	User user = (User) subject.getPrincipal();
    	if (user == null) {
            view.setViewName("/agent/login");
            return view;
        }
    	Map<String, Object> condition = new HashMap<String, Object>();
    	condition.put("agentId", user.getAgent().getAgentId());
    	ResultData fetchAgentResponse = agentService.fetchAgent(condition);
    	if(fetchAgentResponse.getResponseCode() != ResponseCode.RESPONSE_OK){
    		Prompt prompt = new Prompt("失败", "代理商不存在", "/account/info");
            view.addObject("prompt", prompt);
            view.setViewName("/agent/prompt");
    		return view;
    	}
    	Agent agent = ((List<Agent>)fetchAgentResponse.getData()).get(0);

    	String bankCardNo = form.getBankCardNo();
    	double money = form.getMoney();
    	condition.clear();
    	condition.put("bankCardNo", bankCardNo);
    	ResultData bankCardData = agentService.fetchBankCard(condition);
    	if(bankCardData.getResponseCode() != ResponseCode.RESPONSE_OK){
    		Prompt prompt = new Prompt("失败", "银行卡号不存在", "/account/info");
            view.addObject("prompt", prompt);
            view.setViewName("/agent/prompt");
            return view;
    	}
    	
    	if(money > agent.getCoffer()){
    		Prompt prompt = new Prompt("提示", "您的的提现金额超过余额", "/account/info");
            view.addObject("prompt", prompt);
            view.setViewName("/agent/prompt");
            return view;
    	}
    	
    	WithdrawRecord record = new WithdrawRecord();
    	record.setAgent(user.getAgent());
    	record.setAmount(money);
    	record.setBankCardNo(bankCardNo);
    	record.setWealth(agent.getCoffer());
    	ResultData withdrawData =  agentService.applyWithdraw(record);
    	if(withdrawData.getResponseCode() != ResponseCode.RESPONSE_OK){
    		Prompt prompt = new Prompt("失败", "申请提现失败", "/account/info");
            view.addObject("prompt", prompt);
            view.setViewName("/agent/prompt");
    		return view;
    	}
    	
    	ResultData consumeData = agentService.consume(agent, money);
    	if(consumeData.getResponseCode() != ResponseCode.RESPONSE_OK){
    		Prompt prompt = new Prompt("失败", "余额不足", "/account/info");
            view.addObject("prompt", prompt);
            view.setViewName("/agent/prompt");
    		return view;
    	}
    	
    	Prompt prompt = new Prompt("提示", "申请提现成功，预计2日内到账", "/account/info");
        view.addObject("prompt", prompt);
        view.setViewName("/agent/prompt");
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
    
    @RequestMapping(method = RequestMethod.GET, value = "/charge/{billId}/prompt")
    public ModelAndView prompt(@PathVariable("billId") String billId,
                               String result) {
        ModelAndView view = new ModelAndView();
        Prompt prompt;
	    if(billId.startsWith("DPB")){
	        if (!StringUtils.isEmpty(result) && result.equals("success")) {
	            prompt = new Prompt("提示", "恭喜您,充值成功!", "/account/info");
	        } else {
	            prompt = new Prompt(PromptCode.WARNING, "提示", "对不起,您的充值已取消.", "/account/info");
	        }
	        view.addObject("prompt", prompt);
	        view.setViewName("/agent/prompt");
        } else if(billId.startsWith("ODB")){
        	String orderId = "";
        	Map<String, Object> condition = new HashMap<String, Object>();
        	condition.put("billId", billId);
        	ResultData billFetchData = billService.fetchOrderBill(condition);
        	if(billFetchData.getResponseCode() == ResponseCode.RESPONSE_OK){
        		OrderBill orderBill = ((List<OrderBill>)billFetchData.getData()).get(0);
        		orderId = orderBill.getOrder().getOrderId();
        	}
        	if (!StringUtils.isEmpty(result) && result.equals("success")) {
	            prompt = new Prompt("付款成功", "订单号：" + orderId + "，请等待发货", "/agent/order/manage/2");
	        } else {
	            prompt = new Prompt(PromptCode.WARNING, "提示", "对不起,您的付款失败了，请联系工作人员.", "/agent/order/manage/2");
	        }
	        view.addObject("prompt", prompt);
	        view.setViewName("/agent/prompt");
        }
        return view;
    }
}