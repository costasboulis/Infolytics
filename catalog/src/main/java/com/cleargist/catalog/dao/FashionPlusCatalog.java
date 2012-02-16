package com.cleargist.catalog.dao;

import java.math.BigDecimal;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cleargist.catalog.entity.jaxb.Catalog;

/*
 * Assumes that the URL is a UUID for products. 
 */
public class FashionPlusCatalog extends CatalogDAOImpl {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private static String USER_AGENT = "ClearGist_bot";
	private static final Locale locale = new Locale("el", "GR"); 
	
	private String removeSpecialChars(String in) {
		String out = in.replaceAll("\\d+\\.\\d+", "NUMBER");
		out = out.replaceAll("\\d+", "NUMBER");
		out = out.toLowerCase(locale);
		out = out.replaceAll("[\\.,\\(\\)\\?;!:\\[\\]\\{\\}\"%&\\*'\\+/>-]", "");
		out = out.replace('ά', 'α');
		out = out.replace('ό', 'ο');
		out = out.replace('ή', 'η');
		out = out.replace('ώ', 'ω');
		out = out.replace('ύ', 'υ');
		out = out.replace('έ', 'ε');
		out = out.replace('ί', 'ι');
		out = out.replaceAll("\\s+", " ");
		out = out.trim();
		
		return out;
	}
	
	public void addProduct(String url, String tenantID) throws Exception {
		// Check if the product already exists
		if (doesProductExist(url, "", tenantID)) {
			return;
		}
		
		Catalog.Products.Product product = new Catalog.Products.Product();
		product.setUid(url);
		Document doc = Jsoup.connect(url).userAgent(USER_AGENT).get();
		Element productDetails = doc.select("[id=divProductDetails]").get(0);
		
		String title = productDetails.getElementsByAttributeValue("id", "h3Title").get(0).text();
		product.setName(title);
		
		String price = productDetails.getElementsByAttributeValue("id", "h2Price").get(0).text().replaceAll("&euro; ", "").replaceAll("€ ", "");
		product.setPrice(BigDecimal.valueOf(Double.parseDouble(price)));
		
		String category = productDetails.getElementsByAttributeValue("id", "h4CompanyCategory").get(0).text();
		product.setCategory(category);
		
		String description = productDetails.getElementsByAttributeValue("id", "pDescription").get(0).text();
		product.setDescription(removeSpecialChars(description));
		
		Element imageDetails = doc.select("[class=product-image]").get(0);
		String image = imageDetails.attr("src");
		product.setImage(image);
		
		product.setInstock("Y");
		
		
		addProduct(product, "", tenantID);
	}
	
	
}
