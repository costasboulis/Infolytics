//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.04.07 at 12:14:02 μμ EEST 
//


package com.cleargist.data.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for actionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="actionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="name" type="{}mainActionType"/>
 *         &lt;element name="ratingAction" type="{}ratingType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "actionType", propOrder = {
    "name",
    "ratingAction"
})
public class ActionType {

    protected MainActionType name;
    protected RatingType ratingAction;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link MainActionType }
     *     
     */
    public MainActionType getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link MainActionType }
     *     
     */
    public void setName(MainActionType value) {
        this.name = value;
    }

    /**
     * Gets the value of the ratingAction property.
     * 
     * @return
     *     possible object is
     *     {@link RatingType }
     *     
     */
    public RatingType getRatingAction() {
        return ratingAction;
    }

    /**
     * Sets the value of the ratingAction property.
     * 
     * @param value
     *     allowed object is
     *     {@link RatingType }
     *     
     */
    public void setRatingAction(RatingType value) {
        this.ratingAction = value;
    }

}
