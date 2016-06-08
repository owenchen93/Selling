package selling.sunshine.service;

import selling.sunshine.model.DepositBill;
import selling.sunshine.model.OrderBill;
import selling.sunshine.utils.ResultData;

import java.util.Map;

/**
 * Created by sunshine on 5/10/16.
 */
public interface BillService {
    ResultData createDepositBill(DepositBill bill);

    ResultData fetchDepositBill(Map<String, Object> condition);

    ResultData updateDepositBill(DepositBill bill);

    ResultData createOrderBill(OrderBill bill);

    ResultData fetchOrderBill(Map<String, Object> condition);

    ResultData updateOrderBill(OrderBill bill);
}