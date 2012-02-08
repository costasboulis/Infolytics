package com.cleargist.catalog.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cleargist.catalog.entity.jaxb.Catalog;

public class SintagesPareasCatalog extends CatalogDAOImpl {
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
		
		// Now extract the metatags and the ingredients, create the new product and insert in the catalog
		Catalog.Products.Product product = new Catalog.Products.Product();
		product.setUid(url);
		Document doc = Jsoup.connect(url).userAgent(USER_AGENT).get();
		Elements openGraphTags = doc.getElementsByTag("meta");
		for (int i = 0; i < openGraphTags.size(); i ++) {
			List<Attribute> attributes = openGraphTags.get(i).attributes().asList();
			for (int att = 0; att < attributes.size(); att ++) {
				Attribute attribute = attributes.get(att);
				if (attribute.getKey().equals("property")) {
					if (attribute.getValue().equals("og:url")) {
						Attribute nextAttribute = attributes.get(att + 1);
						if (nextAttribute.equals("content")) {
							product.setLink(nextAttribute.getValue());
						}
					}
					else if (attribute.getValue().equals("og:image")) {
						Attribute nextAttribute = attributes.get(att + 1);
						if (nextAttribute.equals("content")) {
							product.setImage(nextAttribute.getValue());
						}
					}
					else if (attribute.getValue().equals("og:title")) {
						Attribute nextAttribute = attributes.get(att + 1);
						if (nextAttribute.equals("content")) {
							product.setName(nextAttribute.getValue());
						}
					}
					else if (attribute.getValue().equals("og:description")) {  
						Attribute nextAttribute = attributes.get(att + 1);
						if (nextAttribute.equals("content")) {
							product.setDescription(nextAttribute.getValue());
						}
					}
				}
			}
			
		}
		
		
		Elements tmpElements = doc.getElementsByClass("rr_ingredients");
		if (tmpElements == null || tmpElements.size() == 0) {
			logger.info("No ingredients found for " + url);
		}
		else {
			Element element = tmpElements.get(0);
			StringBuffer sb = new StringBuffer();
			for (Element e : element.getElementsByTag("li")) {
				sb.append(" "); sb.append(e.text()); 
			}
			String processedText = removeSpecialChars(sb.toString());
			product.setDescription(processedText);
		}
		
		product.setCategory("FOOD");
		product.setPrice(BigDecimal.valueOf(0.0));
		product.setInstock("Y");
		
		addProduct(product, "", tenantID);
	}
}
