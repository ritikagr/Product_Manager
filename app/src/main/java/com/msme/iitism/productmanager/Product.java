package com.msme.iitism.productmanager;

/**
 * Created by Ritik on 10-10-2016.
 */
public class Product {

    private String product_id;
    private int count;

    public Product()
    {

    }

    public Product(String product_id,int count)
    {
        this.product_id = product_id;
        this.count = count;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
