package selling.sunshine.model.sum;

public class OrderSeries {

	private String name;
	private int[] data;

	public OrderSeries(String name, int[] data) {
		super();
		this.name = name;
		this.data = data;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int[] getData() {
		return data;
	}

	public void setData(int[] data) {
		this.data = data;
	}

}
