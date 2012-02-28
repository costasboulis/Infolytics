package com.cleargist.recommendations.entity;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Catalog {
	
	private String id;
	private String custId;
	private String item;
	private String category;
	private String url;
	private String image;
	private String description;
	private double price;
	private int stock;
	private Date dateInserted = Calendar.getInstance().getTime();
	private BigInteger weight;
	private String manufacturer;
	private String mpn;
	private BigDecimal shipping;
	private String availability;
	private String instock;
	private String isbn;
    
	
	public Catalog() {
		super();
	}
	

	public Catalog(String id, String custId, String item, String category,
			String url, String image, String description, double price,
			int stock) {
		super();
		this.id = id;
		this.custId = custId;
		this.item = item;
		this.category = category;
		this.url = url;
		this.image = image;
		this.description = description;
		this.price = price;
		this.stock = stock;
	}

	
	public Catalog(String id, String custId, String item, String category,
			String url, String image, String description, double price,
			int stock, Date dateInserted, BigInteger weight,
			String manufacturer, String mpn, BigDecimal shipping,
			String availability, String instock, String isbn) {
		super();
		this.id = id;
		this.custId = custId;
		this.item = item;
		this.category = category;
		this.url = url;
		this.image = image;
		this.description = description;
		this.price = price;
		this.stock = stock;
		this.dateInserted = dateInserted;
		this.weight = weight;
		this.manufacturer = manufacturer;
		this.mpn = mpn;
		this.shipping = shipping;
		this.availability = availability;
		this.instock = instock;
		this.isbn = isbn;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCustId() {
		return custId;
	}

	public void setCustId(String custId) {
		this.custId = custId;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getStock() {
		return stock;
	}

	public void setStock(int stock) {
		this.stock = stock;
	}
	
	
	public String getCategory() {
		return category;
	}


	public void setCategory(String category) {
		this.category = category;
	}
	
	

	public Date getDateInserted() {
		return dateInserted;
	}



	public void setDateInserted(Date dateInserted) {
		this.dateInserted = dateInserted;
	}


	
	public BigInteger getWeight() {
		return weight;
	}


	public void setWeight(BigInteger weight) {
		this.weight = weight;
	}


	public String getManufacturer() {
		return manufacturer;
	}


	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}


	public String getMpn() {
		return mpn;
	}


	public void setMpn(String mpn) {
		this.mpn = mpn;
	}


	public BigDecimal getShipping() {
		return shipping;
	}


	public void setShipping(BigDecimal shipping) {
		this.shipping = shipping;
	}


	public String getAvailability() {
		return availability;
	}


	public void setAvailability(String availability) {
		this.availability = availability;
	}


	public String getInstock() {
		return instock;
	}


	public void setInstock(String instock) {
		this.instock = instock;
	}


	public String getIsbn() {
		return isbn;
	}


	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}


	public Element getXmlNodeRepresentation(Document doc){
		Element root = doc.createElement("row");
		root.setAttribute("id", this.getCustId().toString());
		
		Class c =  this.getClass();
		String fields[] = new String[]{
			"custId"
		};
		//,"item","category","price","description","stock"
		for(String cfield : fields){
			try {
				Field field = c.getDeclaredField(cfield);
				field.setAccessible(true);
				Element curNode = doc.createElement("cell");
				Class type = field.getType();
				String className = type.getName();
				Object value = field.get(this);
				String output = "";
				if(value!=null){
					output = value.toString();
				}
				/*if(className.toLowerCase().equals("java.lang.string")){*/
					CDATASection section = doc.createCDATASection(output);
					curNode.appendChild(section);
				/*}*/
				root.appendChild(curNode);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return root;
	}
	
	public static List<String> getFields(){
		List<String> fieldNames = new ArrayList<String>();
		Class self = new Catalog().getClass();
		Field[] fields = self.getDeclaredFields();
		for(Field f : fields){
			fieldNames.add(f.getName().toUpperCase());
		}
		return fieldNames;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((custId == null) ? 0 : custId.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((image == null) ? 0 : image.hashCode());
		result = prime * result + stock;
		result = prime * result + ((item == null) ? 0 : item.hashCode());
		long temp;
		temp = Double.doubleToLongBits(price);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Catalog other = (Catalog) obj;
		if (custId == null) {
			if (other.custId != null)
				return false;
		} else if (!custId.equals(other.custId))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (image == null) {
			if (other.image != null)
				return false;
		} else if (!image.equals(other.image))
			return false;
		if (stock != other.stock)
			return false;
		if (item == null) {
			if (other.item != null)
				return false;
		} else if (!item.equals(other.item))
			return false;
		if (Double.doubleToLongBits(price) != Double
				.doubleToLongBits(other.price))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Catalog [id=" + id + ", custId=" + custId + ", item=" + item
				+ ", url=" + url + ", image=" + image + ", description="
				+ description + ", price=" + price + ", stock=" + stock
				+ "]";
	}
	
	
}
