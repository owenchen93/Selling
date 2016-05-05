package selling.sunshine.model;

/**
 * Created by sunshine on 4/8/16.
 */
public class OrderItem {
	private String orderItemId;
	private String customerId;
	private String goodsId;
	private int goodsQuantity;
	private double orderItemPrice;
	private Order order;
	
	public OrderItem(String orderItemId, String customerId, String goodsId, int goodsQuantity) {
		this.orderItemId = orderItemId;
		this.customerId = customerId;
		this.goodsId = goodsId;
		this.goodsQuantity = goodsQuantity;
	}

	public String getOrderItemId() {
		return orderItemId;
	}

	public void setOrderItemId(String orderItemId) {
		this.orderItemId = orderItemId;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(String goodsId) {
		this.goodsId = goodsId;
	}

	public int getGoodsQuantity() {
		return goodsQuantity;
	}

	public void setGoodsQuantity(int goodsQuantity) {
		this.goodsQuantity = goodsQuantity;
	}

	public double getOrderItemPrice() {
		return orderItemPrice;
	}

	public void setOrderItemPrice(double orderItemPrice) {
		this.orderItemPrice = orderItemPrice;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

}
