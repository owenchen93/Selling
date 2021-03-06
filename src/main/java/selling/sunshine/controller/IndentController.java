package selling.sunshine.controller;

import com.alibaba.fastjson.JSONObject;
import common.sunshine.model.selling.order.CustomerOrder;
import common.sunshine.model.selling.order.EventOrder;
import common.sunshine.model.selling.order.OrderItem;
import common.sunshine.model.selling.order.support.OrderItemStatus;
import common.sunshine.pagination.DataTablePage;
import common.sunshine.pagination.DataTableParam;
import common.sunshine.utils.IDGenerator;
import common.sunshine.utils.ResponseCode;
import common.sunshine.utils.ResultData;
import common.sunshine.utils.SortRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import selling.sunshine.form.TimeRangeForm;
import selling.sunshine.service.EventService;
import selling.sunshine.service.IndentService;
import selling.sunshine.service.OrderService;
import selling.sunshine.utils.DateUtils;
import selling.sunshine.utils.ZipCompressor;
import selling.sunshine.vo.order.OrderItemSum;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 订货单报表接口
 * Created by sunshine on 7/7/16.
 */
@RestController
@RequestMapping("/indent")
public class IndentController {
    private Logger logger = LoggerFactory.getLogger(IndentController.class);

    @Autowired
    private IndentService indentService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private EventService eventService;

    /**
     * 查询并生成订货单信息
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/overview")
    public ResultData indent() {
        ResultData result = new ResultData();
        Map<String, Object> condition = new HashMap<>();
        List<Integer> status = new ArrayList<>(Arrays.asList(OrderItemStatus.PAYED.getCode(), OrderItemStatus.SHIPPED.getCode(), OrderItemStatus.RECEIVED.getCode(), 4));
        condition.put("statusList", status);
        condition.put("blockFlag", false);
        List<SortRule> rule = new ArrayList<>();
        rule.add(new SortRule("create_time", "asc"));
        condition.put("sort", rule);
        ResultData queryResponse = orderService.fetchOrderItem(condition);
        Timestamp createAt = null;
        if (queryResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
            OrderItem item = ((List<OrderItem>) queryResponse.getData()).get(0);
            createAt = item.getCreateAt();
        }
        condition.remove("statusList");
        condition.put("status", status);
        queryResponse = orderService.fetchCustomerOrder(condition);
        if (queryResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
            CustomerOrder item = ((List<CustomerOrder>) queryResponse.getData()).get(0);
            if (createAt == null || createAt.after(item.getCreateAt())) {
                createAt = item.getCreateAt();
            }
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("start", format.format(createAt));
        result.setData(jsonObject);
        return result;
    }

    /**
     * 在服务器上生成相关的订货单数据压缩包
     *
     * @param form
     * @param result
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/overview")
    public ResultData indent(@Valid TimeRangeForm form, BindingResult result) {
        ResultData data = new ResultData();
        boolean empty = true;
        if (result.hasErrors()) {
            data.setResponseCode(ResponseCode.RESPONSE_ERROR);
            return data;
        }
        Map<String, Object> condition = new HashMap<>();
        condition.put("start", form.getStart());
        condition.put("end", form.getEnd());
        condition.put("blockFlag", false);
        List<Integer> status = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        condition.put("statusList", status);
        ResultData queryResponse = orderService.fetchOrderItem(condition);
        List total = new ArrayList<>();
        if (queryResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
            empty = false;
            List<OrderItem> list = (List<OrderItem>) queryResponse.getData();
            total.addAll(list);
            indentService.produce(list);
        }
        condition.remove("statusList");
        condition.put("status", status);
        queryResponse = orderService.fetchCustomerOrder(condition);
        if (queryResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
            empty = false;
            List<CustomerOrder> list = (List<CustomerOrder>) queryResponse.getData();
            total.addAll(list);
            indentService.produce(list);
        }
        queryResponse = eventService.fetchEventOrder(condition);
        if (queryResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
            empty = false;
            List<EventOrder> list = (List<EventOrder>) queryResponse.getData();
            total.addAll(list);
            indentService.produce(list);
        }
        if (empty) {
            data.setResponseCode(ResponseCode.RESPONSE_NULL);
            return data;
        }
        //indent generate summary indent xlsx

        ResultData resultData = indentService.produceSummary(total);
        String summaryPath = resultData.getData().toString();

        String path = IndentController.class.getResource("/").getPath();
        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("windows") >= 0) {
            path = path.substring(1);
        }
        int index = path.lastIndexOf("/WEB-INF/classes/");
        String parent = path.substring(0, index);
        String directory = "/material/journal/indent";
        DateUtils dateUtils = new DateUtils();
        dateUtils.process(form.getStart(), form.getEnd());
        List<String> dateList = dateUtils.getDateList();
        List<String> pathList = new ArrayList<String>();
        dateList.forEach((date) -> {
            StringBuffer sb = new StringBuffer(parent).append(directory).append("/").append(date.replaceAll("-", ""));
            pathList.add(sb.toString());
        });
        pathList.add((new StringBuffer(parent).append(summaryPath)).toString());
        String zipName = IDGenerator.generate("Indent");
        StringBuffer sb = new StringBuffer(parent).append(directory).append("/").append(zipName + ".zip");
        ZipCompressor zipCompressor = new ZipCompressor(sb.toString());
        zipCompressor.compress(pathList);
        File file = new File((new StringBuffer(parent).append(summaryPath)).toString());
        file.delete();
        data.setData(zipName);
        return data;
    }

    /**
     * 从服务器上下载之前生成的订货单压缩包
     *
     * @param fileName
     * @param tempFileName
     * @param request
     * @param response
     * @return
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(method = RequestMethod.GET, value = "/download/{fileName}/{tempFileName}")
    public String download(@PathVariable("fileName") String fileName, @PathVariable("tempFileName") String tempFileName, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        // 1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
        response.setContentType("multipart/form-data");
        // 2.设置文件头：最后一个参数是设置下载文件名
        response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("订货单报表_" + fileName + ".zip", "utf-8"));
        OutputStream out;
        // 通过文件路径获得File对象
        String path = IndentController.class.getResource("/").getPath();
        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("windows") >= 0) {
            path = path.substring(1);
        }
        int index = path.lastIndexOf("/WEB-INF/classes/");
        String parent = path.substring(0, index);
        String directory = "/material/journal/indent";
        StringBuffer sb = new StringBuffer(parent).append(directory).append("/").append(tempFileName + ".zip");
        File file = new File(sb.toString());
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream buff = new BufferedInputStream(fis);
            byte[] b = new byte[1024];// 相当于我们的缓存
            long k = 0;// 该值用于计算当前实际下载了多少字节

            // 3.通过response获取OutputStream对象(out)
            out = response.getOutputStream();
            // 开始循环下载
            while (k < file.length()) {
                int j = buff.read(b, 0, 1024);
                k += j;
                out.write(b, 0, j);
            }
            buff.close();
            fis.close();
            out.close();
            out.flush();
            file.delete();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 报表专区中订货单界面上显示的订货单列表（没有部署到服务器上）
     *
     * @param param
     * @return
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/list")
    public DataTablePage<OrderItemSum> list(DataTableParam param) {
        DataTablePage<OrderItemSum> result = new DataTablePage<>(param);
        if (StringUtils.isEmpty(param)) {
            return result;
        }
        Map<String, Object> condition = new HashMap<>();
        condition.put("blockFlag", false);
        List<Integer> status = new ArrayList<>(Arrays.asList(OrderItemStatus.PAYED.getCode(), OrderItemStatus.SHIPPED.getCode(), OrderItemStatus.RECEIVED.getCode(), OrderItemStatus.EXCHANGED.getCode()));
        condition.put("statusList", status);
        ResultData queryResponse = orderService.fetchOrderItemSum(condition, param);
        if (queryResponse.getResponseCode() == ResponseCode.RESPONSE_OK) {
            result = (DataTablePage<OrderItemSum>) queryResponse.getData();
        }
        return result;
    }


}
