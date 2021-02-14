package com.jzj.order.client;

/**
 * 当前描述：
 *
 * @author: jiazijie
 * @since: 2021/2/10 下午10:56
 */
public interface IOrderRpcService {
    /**
     * 获取order的RPC接口
     *
     * @param orderId
     * @return
     */
    String getOrderInfo(String orderId);
}