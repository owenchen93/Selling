package selling.sunshine.model;

/**
 * Created by sunshine on 4/8/16.
 */
public class Goods extends Entity {
    private String goodsId;
    private String name;
    private double price;
    private String description;

    public Goods() {
        super();
    }

    public Goods(String name, double price) {
        this();
        this.name = name;
        this.price = price;
    }

    public Goods(String name, double price, String description) {
        this(name, price);
        this.description = description;
    }

    public Goods(String name, double price, String description, boolean block) {
        this(name, price, description);
        this.setBlockFlag(block);
    }

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
