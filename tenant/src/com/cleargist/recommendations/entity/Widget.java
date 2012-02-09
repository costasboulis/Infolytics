package com.cleargist.recommendations.entity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import jsx3.html.Text;

import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Entity
@Table(name = "WIDGET")
public class Widget {
	
	@Id
	@Column(name="ID")
	private String id;
	@ManyToOne
	@JoinColumn(name="token",nullable=false)
	private Tenant tenant;
	@Column(name="NAME")
	private String name;
	@Column(name="TYPE")
	private WidgetType type;
	@Column(name="DESCRIPTION")
	private String description;
	@Column(name="CODE", columnDefinition="LONGTEXT")
	private String code;
	@Column(name="PREVIEWCODE", columnDefinition="LONGTEXT")
	private String previewCode;
	@Column(name="LAYOUTTYPE", nullable=false, columnDefinition="VARCHAR(20) default 'horizontal'")
	private String layoutType;
	@Column(name="NOOFITEMS", nullable=false, columnDefinition="INT(4) default '4'")
	private int noOfItems;
	@Column(name="IMAGESIZEWIDTH", nullable=false, columnDefinition="VARCHAR(20) default 'auto'")
	private String imageSizeWidth;
	@Column(name="IMAGESIZEHEIGHT", nullable=false, columnDefinition="VARCHAR(20) default 'auto'")
	private String imageSizeHeight;
	@Column(name="TEXTAREAWIDTH", nullable=false, columnDefinition="INT(4) default '200'")
	private int textAreaWidth;
	@Column(name="LISTWIDTH", nullable=false, columnDefinition="INT(3) default '100'")
	private int listWidth;
	@Column(name="LISTSPACES", nullable=false, columnDefinition="INT(3) default '14'")
	private int listSpaces;
	@Column(name="LISTDISPLAY", nullable=false, columnDefinition="VARCHAR(20) default 'block'")
	private String listDisplay;
	@Column(name="LISTSPACES2", nullable=false, columnDefinition="INT(3) default '0'")
	private int listSpaces2;
	@Column(name="SHOWHEADER", nullable=false, columnDefinition="TINYINT(1) default '1'")
	private int showHeader;
	@Column(name="SHOWIMAGES", nullable=false, columnDefinition="TINYINT(1) default '1'")
	private int showImages;
	@Column(name="SHOWCLEARGISTLOGO", nullable=false, columnDefinition="TINYINT(1) default '1'")
	private int showClearGistLogo;
	@Column(name="BORDERCOLOR", nullable=false, columnDefinition="VARCHAR(10) default 'ffffff'")
	private String borderColor;
	@Column(name="BORDERWIDTH", nullable=false, columnDefinition="TINYINT(2) default '1'")
	private int borderWidth;
	@Column(name="HEADERBACK", nullable=false, columnDefinition="VARCHAR(10) default 'ffffff'")
	private String headerBack;
	@Column(name="HEADERBACKTRANS", nullable=false, columnDefinition="TINYINT(1) default '0'")
	private int headerBackTrans;
	@Column(name="MAINBACK", nullable=false, columnDefinition="VARCHAR(10) default 'ffffff'")
	private String mainBack;
	@Column(name="MAINBACKTRANS", nullable=false, columnDefinition="TINYINT(1) default '1'")
	private int mainBackTrans;
	@Column(name="FOOTERBACK", nullable=false, columnDefinition="VARCHAR(10) default 'ffffff'")
	private String footerBack;
	@Column(name="FOOTERBACKTRANS", nullable=false, columnDefinition="TINYINT(1) default '1'")
	private int footerBackTrans;
	@Column(name="FONTFAMILY", nullable=false, columnDefinition="VARCHAR(100) default 'Arial, Helvetica, sans-serif'")
	private String fontFamily;
	@Column(name="HEADERTEXTCOLOR", nullable=false, columnDefinition="VARCHAR(10) default '000000'")
	private String headerTextColor;
	@Column(name="HEADERTEXT", nullable=false, columnDefinition="VARCHAR(200) default 'People also bought...'")
	private String headerText;
	@Column(name="HEADERTEXTSIZE", nullable=false, columnDefinition="TINYINT(2) default '12'")
	private int headerTextSize;
	@Column(name="HEADERTEXTWEIGHT", nullable=false, columnDefinition="VARCHAR(10) default 'normal'")
	private String headerTextWeight;
	@Column(name="HEADERTEXTALIGN", nullable=false, columnDefinition="VARCHAR(10) default 'left'")
	private String headerTextAlign;
	@Column(name="NAMETEXTCOLOR", nullable=false, columnDefinition="VARCHAR(10) default '000000'")
	private String nameTextColor;
	@Column(name="NAMETEXTSIZE", nullable=false, columnDefinition="TINYINT(2) default '12'")
	private int nameTextSize;
	@Column(name="NAMETEXTWEIGHT", nullable=false, columnDefinition="VARCHAR(10) default 'normal'")
	private String nameTextWeight;
	@Column(name="NAMETEXTALIGN", nullable=false, columnDefinition="VARCHAR(10) default 'left'")
	private String nameTextAlign;
	@Column(name="PRICETEXTCOLOR", nullable=false, columnDefinition="VARCHAR(10) default '000000'")
	private String priceTextColor;
	@Column(name="PRICETEXTSIZE", nullable=false, columnDefinition="TINYINT(2) default '12'")
	private int priceTextSize;
	@Column(name="PRICETEXTWEIGHT", nullable=false, columnDefinition="VARCHAR(10) default 'normal'")
	private String priceTextWeight;
	@Column(name="PRICETEXTALIGN", nullable=false, columnDefinition="VARCHAR(10) default 'left'")
	private String priceTextAlign;
	@Column(name="CATEGORYTEXTCOLOR", nullable=false, columnDefinition="VARCHAR(10) default '000000'")
	private String categoryTextColor;
	@Column(name="CATEGORYTEXTSIZE", nullable=false, columnDefinition="TINYINT(2) default '12'")
	private int categoryTextSize;
	@Column(name="CATEGORYTEXTWEIGHT", nullable=false, columnDefinition="VARCHAR(10) default 'normal'")
	private String categoryTextWeight;
	@Column(name="CATEGORYTEXTALIGN", nullable=false, columnDefinition="VARCHAR(10) default 'left'")
	private String categoryTextAlign;
	@Column(name="STOCKTEXTCOLOR", nullable=false, columnDefinition="VARCHAR(10) default '000000'")
	private String stockTextColor;
	@Column(name="STOCKTEXTSIZE", nullable=false, columnDefinition="TINYINT(2) default '12'")
	private int stockTextSize;
	@Column(name="STOCKTEXTWEIGHT", nullable=false, columnDefinition="VARCHAR(10) default 'normal'")
	private String stockTextWeight;
	@Column(name="STOCKTEXTALIGN", nullable=false, columnDefinition="VARCHAR(10) default 'left'")
	private String stockTextAlign;
	@Column(name="SHOWNAME", nullable=false, columnDefinition="TINYINT(1) default '1'")
	private int showName;
	@Column(name="SHOWPRICE", nullable=false, columnDefinition="TINYINT(1) default '1'")
	private int showPrice;
	@Column(name="SHOWCATEGORY", nullable=false, columnDefinition="TINYINT(1) default '0'")
	private int showCategory;
	@Column(name="SHOWSTOCK", nullable=false, columnDefinition="TINYINT(1) default '0'")
	private int showStock;
	@Column(name="DATEUPDATED")
	@Temporal(TemporalType.DATE)
	private Date dateUpdated = Calendar.getInstance().getTime();
 
	public Widget() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public Tenant getTenant() {
		return tenant;
	}

	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public WidgetType getType() {
		return type;
	}

	public void setType(WidgetType type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	public String getPreviewCode() {
		return previewCode;
	}

	public void setPreviewCode(String previewCode) {
		this.previewCode = previewCode;
	}

	public String getLayoutType() {
		return layoutType;
	}

	public void setLayoutType(String layoutType) {
		this.layoutType = layoutType;
	}

	public int getNoOfItems() {
		return noOfItems;
	}

	public void setNoOfItems(int noOfItems) {
		this.noOfItems = noOfItems;
	}

	public String getImageSizeWidth() {
		return imageSizeWidth;
	}

	public void setImageSizeWidth(String imageSizeWidth) {
		this.imageSizeWidth = imageSizeWidth;
	}

	public String getImageSizeHeight() {
		return imageSizeHeight;
	}

	public void setImageSizeHeight(String imageSizeHeight) {
		this.imageSizeHeight = imageSizeHeight;
	}

	public int getTextAreaWidth() {
		return textAreaWidth;
	}
	
	public int getListWidth() {
		return listWidth;
	}

	public void setListWidth(int listWidth) {
		this.listWidth = listWidth;
	}
	
	public int getListSpaces() {
		return listSpaces;
	}

	public void setListSpaces(int listSpaces) {
		this.listSpaces = listSpaces;
	}
	
	public int getListSpaces2() {
		return listSpaces2;
	}
	
	public void setListSpaces2(int listSpaces2) {
		this.listSpaces2 = listSpaces2;
	}
	
	public String getListDisplay() {
		return listDisplay;
	}

	public void setListDisplay(String listDisplay) {
		this.listDisplay = listDisplay;
	}

	public void setTextAreaWidth(int textAreaWidth) {
		this.textAreaWidth = textAreaWidth;
	}

	public int getShowHeader() {
		return showHeader;
	}

	public void setShowHeader(int showHeader) {
		this.showHeader = showHeader;
	}

	public int getShowImages() {
		return showImages;
	}

	public void setShowImages(int showImages) {
		this.showImages = showImages;
	}

	public int getShowClearGistLogo() {
		return showClearGistLogo;
	}

	public void setShowClearGistLogo(int showClearGistLogo) {
		this.showClearGistLogo = showClearGistLogo;
	}

	public String getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(String borderColor) {
		this.borderColor = borderColor;
	}

	public int getBorderWidth() {
		return borderWidth;
	}

	public void setBorderWidth(int borderWidth) {
		this.borderWidth = borderWidth;
	}

	public String getHeaderBack() {
		return headerBack;
	}

	public void setHeaderBack(String headerBack) {
		this.headerBack = headerBack;
	}

	public int getHeaderBackTrans() {
		return headerBackTrans;
	}

	public void setHeaderBackTrans(int headerBackTrans) {
		this.headerBackTrans = headerBackTrans;
	}

	public String getMainBack() {
		return mainBack;
	}

	public void setMainBack(String mainBack) {
		this.mainBack = mainBack;
	}

	public int getMainBackTrans() {
		return mainBackTrans;
	}

	public void setMainBackTrans(int mainBackTrans) {
		this.mainBackTrans = mainBackTrans;
	}

	public String getFooterBack() {
		return footerBack;
	}

	public void setFooterBack(String footerBack) {
		this.footerBack = footerBack;
	}

	public int getFooterBackTrans() {
		return footerBackTrans;
	}

	public void setFooterBackTrans(int footerBackTrans) {
		this.footerBackTrans = footerBackTrans;
	}

	public String getFontFamily() {
		return fontFamily;
	}

	public void setFontFamily(String fontFamily) {
		this.fontFamily = fontFamily;
	}

	public String getHeaderTextColor() {
		return headerTextColor;
	}

	public void setHeaderTextColor(String headerTextColor) {
		this.headerTextColor = headerTextColor;
	}

	public String getHeaderText() {
		return headerText;
	}

	public void setHeaderText(String headerText) {
		this.headerText = headerText;
	}

	public int getHeaderTextSize() {
		return headerTextSize;
	}

	public void setHeaderTextSize(int headerTextSize) {
		this.headerTextSize = headerTextSize;
	}

	public String getHeaderTextWeight() {
		return headerTextWeight;
	}

	public void setHeaderTextWeight(String headerTextWeight) {
		this.headerTextWeight = headerTextWeight;
	}

	public String getHeaderTextAlign() {
		return headerTextAlign;
	}

	public void setHeaderTextAlign(String headerTextAlign) {
		this.headerTextAlign = headerTextAlign;
	}

	public String getNameTextColor() {
		return nameTextColor;
	}

	public void setNameTextColor(String nameTextColor) {
		this.nameTextColor = nameTextColor;
	}

	public int getNameTextSize() {
		return nameTextSize;
	}

	public void setNameTextSize(int nameTextSize) {
		this.nameTextSize = nameTextSize;
	}

	public String getNameTextWeight() {
		return nameTextWeight;
	}

	public void setNameTextWeight(String nameTextWeight) {
		this.nameTextWeight = nameTextWeight;
	}

	public String getNameTextAlign() {
		return nameTextAlign;
	}

	public void setNameTextAlign(String nameTextAlign) {
		this.nameTextAlign = nameTextAlign;
	}

	public String getPriceTextColor() {
		return priceTextColor;
	}

	public void setPriceTextColor(String priceTextColor) {
		this.priceTextColor = priceTextColor;
	}

	public int getPriceTextSize() {
		return priceTextSize;
	}

	public void setPriceTextSize(int priceTextSize) {
		this.priceTextSize = priceTextSize;
	}

	public String getPriceTextWeight() {
		return priceTextWeight;
	}

	public void setPriceTextWeight(String priceTextWeight) {
		this.priceTextWeight = priceTextWeight;
	}

	public String getPriceTextAlign() {
		return priceTextAlign;
	}

	public void setPriceTextAlign(String priceTextAlign) {
		this.priceTextAlign = priceTextAlign;
	}

	public String getCategoryTextColor() {
		return categoryTextColor;
	}

	public void setCategoryTextColor(String categoryTextColor) {
		this.categoryTextColor = categoryTextColor;
	}

	public int getCategoryTextSize() {
		return categoryTextSize;
	}

	public void setCategoryTextSize(int categoryTextSize) {
		this.categoryTextSize = categoryTextSize;
	}

	public String getCategoryTextWeight() {
		return categoryTextWeight;
	}

	public void setCategoryTextWeight(String categoryTextWeight) {
		this.categoryTextWeight = categoryTextWeight;
	}

	public String getCategoryTextAlign() {
		return categoryTextAlign;
	}

	public void setCategoryTextAlign(String categoryTextAlign) {
		this.categoryTextAlign = categoryTextAlign;
	}

	public String getStockTextColor() {
		return stockTextColor;
	}

	public void setStockTextColor(String stockTextColor) {
		this.stockTextColor = stockTextColor;
	}

	public int getStockTextSize() {
		return stockTextSize;
	}

	public void setStockTextSize(int stockTextSize) {
		this.stockTextSize = stockTextSize;
	}

	public String getStockTextWeight() {
		return stockTextWeight;
	}

	public void setStockTextWeight(String stockTextWeight) {
		this.stockTextWeight = stockTextWeight;
	}

	public String getStockTextAlign() {
		return stockTextAlign;
	}

	public void setStockTextAlign(String stockTextAlign) {
		this.stockTextAlign = stockTextAlign;
	}

	public int getShowName() {
		return showName;
	}

	public void setShowName(int showName) {
		this.showName = showName;
	}

	public int getShowPrice() {
		return showPrice;
	}

	public void setShowPrice(int showPrice) {
		this.showPrice = showPrice;
	}

	public int getShowCategory() {
		return showCategory;
	}

	public void setShowCategory(int showCategory) {
		this.showCategory = showCategory;
	}

	public int getShowStock() {
		return showStock;
	}

	public void setShowStock(int showStock) {
		this.showStock = showStock;
	}

	public Date getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(Date dateUpdated) {
		this.dateUpdated = dateUpdated;
	}
	
	public Element getXmlNodeRepresentation(Document doc){
		Element root = doc.createElement("row");
		root.setAttribute("id", this.getId());
		
		Class c =  this.getClass();
		String fields[] = new String[]{
			"name"
		};
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

}
