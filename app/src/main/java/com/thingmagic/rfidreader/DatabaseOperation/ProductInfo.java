package com.thingmagic.rfidreader.DatabaseOperation;

import java.io.Serializable;

public class ProductInfo implements Serializable {
    private int productID;
    private String productLabel;
    private String productEPC;

    public ProductInfo(){
    }

    public ProductInfo(String label, String ePC){
        this.setProductLabel(label);
        this.setProductEPC(ePC);
    }

    public ProductInfo(int ID, String label, String ePC){
        this.setProductID(ID);
        this.setProductLabel(label);
        this.setProductEPC(ePC);
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public String getProductLabel() {
        return productLabel;
    }

    public void setProductLabel(String productLabel) {
        this.productLabel = productLabel;
    }

    public String getProductEPC() {
        return productEPC;
    }

    public void setProductEPC(String productEPC) {
        this.productEPC = productEPC;
    }
}
