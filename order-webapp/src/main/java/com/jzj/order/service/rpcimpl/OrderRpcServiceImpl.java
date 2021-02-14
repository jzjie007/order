package com.jzj.order.service.rpcimpl;

import com.jzj.order.client.IOrderRpcService;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@DubboService(interfaceClass = IOrderRpcService.class)
public class OrderRpcServiceImpl implements IOrderRpcService {

    private static final Logger log = LogManager.getLogger(OrderRpcServiceImpl.class);

    @Override
    public String getOrderInfo(String orderId) {
        log.info("调用 order 的rpc接口 orderId:{}", orderId);
        return "订单编号--->" + orderId;
    }
}
