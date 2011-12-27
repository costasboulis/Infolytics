//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.12.05 at 03:55:26 �� EET 
//


package gr.infolytics.catalog.entity.jaxb;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}created_at" minOccurs="0"/>
 *         &lt;element name="products">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="product" maxOccurs="unbounded">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;all>
 *                             &lt;element ref="{}uid"/>
 *                             &lt;element ref="{}name"/>
 *                             &lt;element ref="{}link"/>
 *                             &lt;element ref="{}image"/>
 *                             &lt;element ref="{}price"/>
 *                             &lt;element ref="{}category"/>
 *                             &lt;element ref="{}category_id" minOccurs="0"/>
 *                             &lt;element ref="{}description" minOccurs="0"/>
 *                             &lt;element ref="{}weight" minOccurs="0"/>
 *                             &lt;element ref="{}manufacturer" minOccurs="0"/>
 *                             &lt;element ref="{}mpn" minOccurs="0"/>
 *                             &lt;element ref="{}shipping" minOccurs="0"/>
 *                             &lt;element ref="{}availability" minOccurs="0"/>
 *                             &lt;element ref="{}instock" minOccurs="0"/>
 *                             &lt;element ref="{}isbn" minOccurs="0"/>
 *                           &lt;/all>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "createdAt",
    "products"
})
@XmlRootElement(name = "catalog")
public class Catalog {

    @XmlElement(name = "created_at")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar createdAt;
    @XmlElement(required = true)
    protected Catalog.Products products;

    /**
     * Gets the value of the createdAt property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the value of the createdAt property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCreatedAt(XMLGregorianCalendar value) {
        this.createdAt = value;
    }

    /**
     * Gets the value of the products property.
     * 
     * @return
     *     possible object is
     *     {@link Catalog.Products }
     *     
     */
    public Catalog.Products getProducts() {
        return products;
    }

    /**
     * Sets the value of the products property.
     * 
     * @param value
     *     allowed object is
     *     {@link Catalog.Products }
     *     
     */
    public void setProducts(Catalog.Products value) {
        this.products = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="product" maxOccurs="unbounded">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;all>
     *                   &lt;element ref="{}uid"/>
     *                   &lt;element ref="{}name"/>
     *                   &lt;element ref="{}link"/>
     *                   &lt;element ref="{}image"/>
     *                   &lt;element ref="{}price"/>
     *                   &lt;element ref="{}category"/>
     *                   &lt;element ref="{}category_id" minOccurs="0"/>
     *                   &lt;element ref="{}description" minOccurs="0"/>
     *                   &lt;element ref="{}weight" minOccurs="0"/>
     *                   &lt;element ref="{}manufacturer" minOccurs="0"/>
     *                   &lt;element ref="{}mpn" minOccurs="0"/>
     *                   &lt;element ref="{}shipping" minOccurs="0"/>
     *                   &lt;element ref="{}availability" minOccurs="0"/>
     *                   &lt;element ref="{}instock" minOccurs="0"/>
     *                   &lt;element ref="{}isbn" minOccurs="0"/>
     *                 &lt;/all>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "product"
    })
    public static class Products {

        @XmlElement(required = true)
        protected List<Catalog.Products.Product> product;

        /**
         * Gets the value of the product property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the product property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getProduct().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Catalog.Products.Product }
         * 
         * 
         */
        public List<Catalog.Products.Product> getProduct() {
            if (product == null) {
                product = new ArrayList<Catalog.Products.Product>();
            }
            return this.product;
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;all>
         *         &lt;element ref="{}uid"/>
         *         &lt;element ref="{}name"/>
         *         &lt;element ref="{}link"/>
         *         &lt;element ref="{}image"/>
         *         &lt;element ref="{}price"/>
         *         &lt;element ref="{}category"/>
         *         &lt;element ref="{}category_id" minOccurs="0"/>
         *         &lt;element ref="{}description" minOccurs="0"/>
         *         &lt;element ref="{}weight" minOccurs="0"/>
         *         &lt;element ref="{}manufacturer" minOccurs="0"/>
         *         &lt;element ref="{}mpn" minOccurs="0"/>
         *         &lt;element ref="{}shipping" minOccurs="0"/>
         *         &lt;element ref="{}availability" minOccurs="0"/>
         *         &lt;element ref="{}instock" minOccurs="0"/>
         *         &lt;element ref="{}isbn" minOccurs="0"/>
         *       &lt;/all>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {

        })
        public static class Product {

            @XmlElement(required = true)
            protected String uid;
            @XmlElement(required = true)
            protected String name;
            @XmlElement(required = true)
            protected String link;
            @XmlElement(required = true)
            protected String image;
            @XmlElement(required = true)
            protected BigDecimal price;
            @XmlElement(required = true)
            protected String category;
            @XmlElement(name = "category_id")
            @XmlSchemaType(name = "positiveInteger")
            protected BigInteger categoryId;
            protected String description;
            @XmlSchemaType(name = "positiveInteger")
            protected BigInteger weight;
            protected String manufacturer;
            protected String mpn;
            protected BigDecimal shipping;
            protected String availability;
            protected String instock;
            protected String isbn;

            /**
             * Gets the value of the uid property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getUid() {
                return uid;
            }

            /**
             * Sets the value of the uid property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setUid(String value) {
                this.uid = value;
            }

            /**
             * Gets the value of the name property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getName() {
                return name;
            }

            /**
             * Sets the value of the name property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setName(String value) {
                this.name = value;
            }

            /**
             * Gets the value of the link property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getLink() {
                return link;
            }

            /**
             * Sets the value of the link property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setLink(String value) {
                this.link = value;
            }

            /**
             * Gets the value of the image property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getImage() {
                return image;
            }

            /**
             * Sets the value of the image property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setImage(String value) {
                this.image = value;
            }

            /**
             * Gets the value of the price property.
             * 
             * @return
             *     possible object is
             *     {@link BigDecimal }
             *     
             */
            public BigDecimal getPrice() {
                return price;
            }

            /**
             * Sets the value of the price property.
             * 
             * @param value
             *     allowed object is
             *     {@link BigDecimal }
             *     
             */
            public void setPrice(BigDecimal value) {
                this.price = value;
            }

            /**
             * Gets the value of the category property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getCategory() {
                return category;
            }

            /**
             * Sets the value of the category property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setCategory(String value) {
                this.category = value;
            }

            /**
             * Gets the value of the categoryId property.
             * 
             * @return
             *     possible object is
             *     {@link BigInteger }
             *     
             */
            public BigInteger getCategoryId() {
                return categoryId;
            }

            /**
             * Sets the value of the categoryId property.
             * 
             * @param value
             *     allowed object is
             *     {@link BigInteger }
             *     
             */
            public void setCategoryId(BigInteger value) {
                this.categoryId = value;
            }

            /**
             * Gets the value of the description property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getDescription() {
                return description;
            }

            /**
             * Sets the value of the description property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setDescription(String value) {
                this.description = value;
            }

            /**
             * Gets the value of the weight property.
             * 
             * @return
             *     possible object is
             *     {@link BigInteger }
             *     
             */
            public BigInteger getWeight() {
                return weight;
            }

            /**
             * Sets the value of the weight property.
             * 
             * @param value
             *     allowed object is
             *     {@link BigInteger }
             *     
             */
            public void setWeight(BigInteger value) {
                this.weight = value;
            }

            /**
             * Gets the value of the manufacturer property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getManufacturer() {
                return manufacturer;
            }

            /**
             * Sets the value of the manufacturer property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setManufacturer(String value) {
                this.manufacturer = value;
            }

            /**
             * Gets the value of the mpn property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getMpn() {
                return mpn;
            }

            /**
             * Sets the value of the mpn property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setMpn(String value) {
                this.mpn = value;
            }

            /**
             * Gets the value of the shipping property.
             * 
             * @return
             *     possible object is
             *     {@link BigDecimal }
             *     
             */
            public BigDecimal getShipping() {
                return shipping;
            }

            /**
             * Sets the value of the shipping property.
             * 
             * @param value
             *     allowed object is
             *     {@link BigDecimal }
             *     
             */
            public void setShipping(BigDecimal value) {
                this.shipping = value;
            }

            /**
             * Gets the value of the availability property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getAvailability() {
                return availability;
            }

            /**
             * Sets the value of the availability property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setAvailability(String value) {
                this.availability = value;
            }

            /**
             * Gets the value of the instock property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getInstock() {
                return instock;
            }

            /**
             * Sets the value of the instock property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setInstock(String value) {
                this.instock = value;
            }

            /**
             * Gets the value of the isbn property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getIsbn() {
                return isbn;
            }

            /**
             * Sets the value of the isbn property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setIsbn(String value) {
                this.isbn = value;
            }

        }

    }

}
