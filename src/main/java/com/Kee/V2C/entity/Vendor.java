package com.Kee.V2C.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vendors")
public class Vendor {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "vendor_name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "address")
    private String address;

    @Column(name="rating")
    private float rating;

    @Column(name="image_url")
    private String imageUrl;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Credential credential;


    //when searching for a vendor we don't want the list of products unless stated
    @OneToMany(mappedBy = "vendor",fetch = FetchType.LAZY)
    private List<Product> products=new ArrayList<>();

    //
    @OneToOne(mappedBy = "vendor",fetch = FetchType.LAZY)
    private Shop shop;

    @OneToMany(mappedBy = "owner",fetch = FetchType.LAZY)
    private List<ProductModel> productModels=new ArrayList<>();//for products created by this vendor

    @OneToMany(mappedBy = "vendor")
    private List<ProductRequest> productRequests;

    @OneToMany(mappedBy = "vendor",fetch = FetchType.LAZY)
    private List<SubOrder> subOrders;

    public Vendor(){}

    public Vendor(String name,String description, String imageUrl, String address) {
        this.name = name;
        this.description=description;
        this.imageUrl=imageUrl;
        this.address=address;
        this.rating=2.5f;//hardcode this for every new vendor
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Credential getCredential() {
        return credential;
    }

    public void setCredential(Credential credential) {
        this.credential = credential;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }


    public Shop getShop() {return shop;}

    public List<ProductModel> getProductModels() {
        return productModels;
    }

    public void setProductModels(List<ProductModel> productModels) {
        this.productModels = productModels;
    }

    public void addProduct(Product product){
        products.add(product);
        product.setVendor(this);//the bi-directional link
    }

    public void addProductModel(ProductModel productModel){
        productModels.add(productModel);
        productModel.setVendor(this);
    }

        public void addSubOrder(SubOrder subOrder){
        subOrders.add(subOrder);
        subOrder.setVendor(this);
    }



    @Override
    public String toString() {
        return "Vendor{" +
                "id=" + id +
                ", vendor name='" + name + '\'' +
                ", rating=" + rating +
                '}';
    }
}
