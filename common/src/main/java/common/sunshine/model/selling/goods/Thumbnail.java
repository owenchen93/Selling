package common.sunshine.model.selling.goods;

import common.sunshine.model.Entity;

/**
 * 商品照片图类
 * Created by sunshine on 6/16/16.
 */
public class Thumbnail extends Entity {
    private String thumbnailId;

    /* 商品t图片路径 */
    private String path;

    /* 关联的商品 */
    private Goods4Customer goods;

    /* 缩略图的类型, cover 和 slide*/
    private String type;

    public Thumbnail() {
        super();
    }

    public Thumbnail(String path) {
        this();
        this.path = path;
    }

    public Thumbnail(String path, Goods4Customer goods) {
        this(path);
        this.goods = goods;
    }

    public Thumbnail(String path, String type) {
        this(path);
        this.type = type;
    }

    public String getThumbnailId() {
        return thumbnailId;
    }

    public void setThumbnailId(String thumbnailId) {
        this.thumbnailId = thumbnailId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Goods4Customer getGoods() {
        return goods;
    }

    public void setGoods(Goods4Customer goods) {
        this.goods = goods;
    }
}
