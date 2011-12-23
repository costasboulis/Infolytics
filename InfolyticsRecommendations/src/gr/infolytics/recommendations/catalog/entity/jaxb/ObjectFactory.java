//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.12.05 at 03:55:26 �� EET 
//


package gr.infolytics.recommendations.catalog.entity.jaxb;

import java.math.BigDecimal;
import java.math.BigInteger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the gr.infolytics.recommendations.catalog.entity.jaxb package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Uid_QNAME = new QName("", "uid");
    private final static QName _Weight_QNAME = new QName("", "weight");
    private final static QName _Link_QNAME = new QName("", "link");
    private final static QName _Image_QNAME = new QName("", "image");
    private final static QName _CategoryId_QNAME = new QName("", "category_id");
    private final static QName _Mpn_QNAME = new QName("", "mpn");
    private final static QName _Shipping_QNAME = new QName("", "shipping");
    private final static QName _Category_QNAME = new QName("", "category");
    private final static QName _Price_QNAME = new QName("", "price");
    private final static QName _Manufacturer_QNAME = new QName("", "manufacturer");
    private final static QName _Description_QNAME = new QName("", "description");
    private final static QName _Isbn_QNAME = new QName("", "isbn");
    private final static QName _Name_QNAME = new QName("", "name");
    private final static QName _CreatedAt_QNAME = new QName("", "created_at");
    private final static QName _Instock_QNAME = new QName("", "instock");
    private final static QName _Availability_QNAME = new QName("", "availability");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: gr.infolytics.recommendations.catalog.entity.jaxb
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Catalog.Products }
     * 
     */
    public Catalog.Products createCatalogProducts() {
        return new Catalog.Products();
    }

    /**
     * Create an instance of {@link Catalog.Products.Product }
     * 
     */
    public Catalog.Products.Product createCatalogProductsProduct() {
        return new Catalog.Products.Product();
    }

    /**
     * Create an instance of {@link Catalog }
     * 
     */
    public Catalog createCatalog() {
        return new Catalog();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "uid")
    public JAXBElement<String> createUid(String value) {
        return new JAXBElement<String>(_Uid_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "weight")
    public JAXBElement<BigInteger> createWeight(BigInteger value) {
        return new JAXBElement<BigInteger>(_Weight_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "link")
    public JAXBElement<String> createLink(String value) {
        return new JAXBElement<String>(_Link_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "image")
    public JAXBElement<String> createImage(String value) {
        return new JAXBElement<String>(_Image_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "category_id")
    public JAXBElement<BigInteger> createCategoryId(BigInteger value) {
        return new JAXBElement<BigInteger>(_CategoryId_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "mpn")
    public JAXBElement<String> createMpn(String value) {
        return new JAXBElement<String>(_Mpn_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "shipping")
    public JAXBElement<BigDecimal> createShipping(BigDecimal value) {
        return new JAXBElement<BigDecimal>(_Shipping_QNAME, BigDecimal.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "category")
    public JAXBElement<String> createCategory(String value) {
        return new JAXBElement<String>(_Category_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "price")
    public JAXBElement<BigDecimal> createPrice(BigDecimal value) {
        return new JAXBElement<BigDecimal>(_Price_QNAME, BigDecimal.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "manufacturer")
    public JAXBElement<String> createManufacturer(String value) {
        return new JAXBElement<String>(_Manufacturer_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "description")
    public JAXBElement<String> createDescription(String value) {
        return new JAXBElement<String>(_Description_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "isbn")
    public JAXBElement<String> createIsbn(String value) {
        return new JAXBElement<String>(_Isbn_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "name")
    public JAXBElement<String> createName(String value) {
        return new JAXBElement<String>(_Name_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "created_at")
    public JAXBElement<XMLGregorianCalendar> createCreatedAt(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_CreatedAt_QNAME, XMLGregorianCalendar.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "instock")
    public JAXBElement<String> createInstock(String value) {
        return new JAXBElement<String>(_Instock_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "availability")
    public JAXBElement<String> createAvailability(String value) {
        return new JAXBElement<String>(_Availability_QNAME, String.class, null, value);
    }

}
