package gr.infolytics.recommendations.web;

import gr.infolytics.recommendations.dao.RecommendationsDAO;
import gr.infolytics.recommendations.entity.ActivityEvent;
import gr.infolytics.recommendations.entity.Catalog;
import gr.infolytics.recommendations.entity.CatalogStatus;
import gr.infolytics.recommendations.entity.ErrorType;
import gr.infolytics.recommendations.entity.Tenant;
import gr.infolytics.recommendations.entity.Widget;
import gr.infolytics.recommendations.entity.WidgetType;
import gr.infolytics.recommendations.validators.TenantValidator;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;


/**
 * This is the core of the Recommendations functionality.  It's a Spring controller implemented
 * using annotations.  Most methods for loading and storing journals, entries, comments and photos
 * are initiated in this class.
 */
@Controller
public class RecommendationsController {

	private RecommendationsDAO dao;
	private NotificationManager notificationManager;
	private static final Logger logger=Logger.getLogger(RecommendationsController.class.getName());
	//private String TENANT_TOKEN = "NONE";
	//initialize data
	private Date curDateTime = Calendar.getInstance().getTime();
	private Integer tenant = 0;
	//initialize failure data
	private List<String> failures = new ArrayList<String>();
	//initialize list of simple db attributes
	private List<ReplaceableItem> simpleDBItems = new ArrayList<ReplaceableItem>();
	//validators
	private TenantValidator tenantValidator = new TenantValidator();

	/**
	 * AWS Elastic Beanstalk checks your application's health by periodically
	 * sending an HTTP HEAD request to a resource in your application. By
	 * default, this is the root or default resource in your application,
	 * but can be configured for each environment.
	 *
	 * Here, we report success as long as the app server is up, but skip
	 * generating the whole page since this is a HEAD request only. You
	 * can employ more sophisticated health checks in your application.
	 *
	 * @param model the spring model for the request
	 */
	@RequestMapping(value="/home.do", method=RequestMethod.HEAD)
	public void doHealthCheck(HttpServletResponse response) {
		response.setContentLength(0);
		response.setStatus(HttpServletResponse.SC_OK);
	}

	/**
	 * The sign up controller, which records the company's date
	 * @param attributes that hold the registration data 
	 */
	@RequestMapping(value="/signup.do", method={RequestMethod.GET})
	public Tenant setUpForm() {
		return new Tenant();
	}

	@RequestMapping(value="/signup.do", method={RequestMethod.POST})
	public String signUp (Tenant tenant, BindingResult result) {

		tenantValidator.validate(tenant, result);

		if (result.hasErrors()) {
			return "redirect:servererror.do?error=Post vars are missing";
		} else {
			//generate custom id
			UUID uuId = UUID.randomUUID();
			tenant.setId(uuId.toString());
			//get next token
			tenant.setToken(dao.getNextTenant().getToken()+1);
			tenant.setCatalogStatus(CatalogStatus.WAITING);

			//save tenant
			dao.saveTenant(tenant);
			//notify upon registration and send confirmation link
			notificationManager.sendConfirmationEmail(tenant);
			return "redirect:signupresults.do";
		}

	}

	@RequestMapping(value="/check_username.do", method={RequestMethod.POST})
	public String checkUsername (@RequestParam("username") String username, Model model) {

		JSONObject json = new JSONObject();
		Tenant tenantUsernameExists = dao.getTenantByUsername(username);
		if (tenantUsernameExists == null) {
			try {
				json.put("answer", "false");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			try {
				json.put("answer", "true");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		model.addAttribute("jsonResponse",json.toString());

		return null;

	}

	@RequestMapping(value="/signupresults.do", method={RequestMethod.GET})
	public void signUpResults ()  {

	}

	@RequestMapping(value="/activate.do", method={RequestMethod.GET})
	public String activateTenant (@RequestParam(value = "user") String tenantId) {

		if (tenantId=="") {
			return "redirect:servererror.do?error=Parameters are missing";
		}  else {

			//get tenant
			Tenant tenant = dao.getTenantById(tenantId);

			if (tenant==null || tenant.getActive()==1) {
				return "redirect:servererror.do?error=User is not valid or already activated";
			} else {
				
				Widget widget = new Widget();
				
				try {
					//generate tenant domains & initialize widget
					/*UUID uuId = UUID.randomUUID();
					simpleDBItems.add(new ReplaceableItem().withName("wid"+uuId).withAttributes(
							new ReplaceableAttribute().withName("ID").withValue(uuId.toString()),
							new ReplaceableAttribute().withName("NAME").withValue("Default Widget"),
							new ReplaceableAttribute().withName("DESCRIPTION").withValue("This is the default widget, generated upon your activation"),
							new ReplaceableAttribute().withName("LAYOUTTYPE").withValue("horizontal"),
							new ReplaceableAttribute().withName("NOOFITEMS").withValue("4"),
							new ReplaceableAttribute().withName("IMAGESIZEWIDTH").withValue("auto"),
							new ReplaceableAttribute().withName("IMAGESIZEHEIGHT").withValue("auto"),
							new ReplaceableAttribute().withName("TEXTAREAWIDTH").withValue("400"),
							new ReplaceableAttribute().withName("SHOWHEADER").withValue("1"),
							new ReplaceableAttribute().withName("SHOWIMAGES").withValue("1"),
							new ReplaceableAttribute().withName("SHOWCLEARGISTLOGO").withValue("1"),
							new ReplaceableAttribute().withName("BORDERCOLOR").withValue("ffffff"),
							new ReplaceableAttribute().withName("BORDERWIDTH").withValue("1"),
							new ReplaceableAttribute().withName("HEADERBACK").withValue("ffffff"),
							new ReplaceableAttribute().withName("HEADERBACKTRANS").withValue("0"),
							new ReplaceableAttribute().withName("MAINBACK").withValue("ffffff"),
							new ReplaceableAttribute().withName("MAINBACKTRANS").withValue("0"),
							new ReplaceableAttribute().withName("FOOTERBACK").withValue("ffffff"),
							new ReplaceableAttribute().withName("FOOTERBACKTRANS").withValue("0"),
							new ReplaceableAttribute().withName("FONTFAMILY").withValue("Arial"),
							new ReplaceableAttribute().withName("HEADERTEXTCOLOR").withValue("000000"),
							new ReplaceableAttribute().withName("HEADERTEXT").withValue("People also bought..."),
							new ReplaceableAttribute().withName("HEADERTEXTSIZE").withValue("12"),
							new ReplaceableAttribute().withName("HEADERTEXTWEIGHT").withValue("normal"),
							new ReplaceableAttribute().withName("HEADERTEXTALIGN").withValue("center"),
							new ReplaceableAttribute().withName("NAMETEXTCOLOR").withValue("000000"),
							new ReplaceableAttribute().withName("NAMETEXTSIZE").withValue("12"),
							new ReplaceableAttribute().withName("NAMETEXTWEIGHT").withValue("normal"),
							new ReplaceableAttribute().withName("NAMETEXTALIGN").withValue("center"),
							new ReplaceableAttribute().withName("PRICETEXTCOLOR").withValue("000000"),
							new ReplaceableAttribute().withName("PRICETEXTSIZE").withValue("12"),
							new ReplaceableAttribute().withName("PRICETEXTWEIGHT").withValue("normal"),
							new ReplaceableAttribute().withName("PRICETEXTALIGN").withValue("center"),
							new ReplaceableAttribute().withName("CATEGORYTEXTCOLOR").withValue("000000"),
							new ReplaceableAttribute().withName("CATEGORYTEXTSIZE").withValue("12"),
							new ReplaceableAttribute().withName("CATEGORYTEXTWEIGHT").withValue("normal"),
							new ReplaceableAttribute().withName("CATEGORYTEXTALIGN").withValue("center"),
							new ReplaceableAttribute().withName("STOCKTEXTCOLOR").withValue("000000"),
							new ReplaceableAttribute().withName("STOCKTEXTSIZE").withValue("12"),
							new ReplaceableAttribute().withName("STOCKTEXTWEIGHT").withValue("normal"),
							new ReplaceableAttribute().withName("STOCKTEXTALIGN").withValue("center"),
							new ReplaceableAttribute().withName("SHOWNAME").withValue("1"),
							new ReplaceableAttribute().withName("SHOWPRICE").withValue("1"),
							new ReplaceableAttribute().withName("SHOWCATEGORY").withValue("0"),
							new ReplaceableAttribute().withName("SHOWSTOCK").withValue("0"),
							new ReplaceableAttribute().withName("DATEUPDATED").withValue(curDateTime.toString()))
							);*/
					
					UUID uuId = UUID.randomUUID();
					widget.setId(uuId.toString());
					widget.setName("Default Widget");
					widget.setType(WidgetType.Most_popular_overall);
					widget.setDescription("This is the default widget");
					widget.setTenant(tenant);
					widget.setLayoutType("horizontal");
					widget.setNoOfItems(4);
					widget.setImageSizeHeight("auto");
					widget.setImageSizeWidth("100");
					widget.setTextAreaWidth(800);
					widget.setListWidth(65);
					widget.setListSpaces(14);
					widget.setListSpaces2(0);
					widget.setListDisplay("block");
					widget.setShowHeader(1);
					widget.setShowImages(1);
					widget.setShowClearGistLogo(1);
					widget.setBorderColor("ffffff");
					widget.setBorderWidth(1);
					widget.setHeaderBack("ffffff");
					widget.setHeaderBackTrans(0);
					widget.setMainBack("ffffff");
					widget.setMainBackTrans(0);
					widget.setFooterBack("ffffff");
					widget.setFooterBackTrans(0);
					widget.setFontFamily("Arial, Helvetica, sans-serif");
					widget.setHeaderTextColor("000000");
					widget.setHeaderText("People also bought...");
					widget.setHeaderTextSize(12);
					widget.setHeaderTextWeight("normal");
					widget.setHeaderTextAlign("center");
					widget.setNameTextColor("000000");
					widget.setNameTextSize(12);
					widget.setNameTextWeight("normal");
					widget.setNameTextAlign("center");
					widget.setPriceTextColor("000000");
					widget.setPriceTextSize(12);
					widget.setPriceTextWeight("normal");
					widget.setPriceTextAlign("center");
					widget.setCategoryTextColor("000000");
					widget.setCategoryTextSize(12);
					widget.setCategoryTextWeight("normal");
					widget.setCategoryTextAlign("center");
					widget.setStockTextColor("000000");
					widget.setStockTextSize(12);
					widget.setStockTextWeight("normal");
					widget.setStockTextAlign("center");
					widget.setShowName(1);
					widget.setShowPrice(1);
					widget.setShowCategory(0);
					widget.setShowStock(0);
					widget.setDateUpdated(curDateTime);
					
					dao.generateTenant(tenant);
					dao.addWidget(widget);
				} catch(Exception e) {
					e.printStackTrace();
				}

				return null;
			}
		}
	}

	/**
	 * The administration controllers
	 * 
	 * 
	 */
	
	@RequestMapping(value="admin/check_catalog_status.do", method={RequestMethod.GET})
	public String checkCatalogueStatus (@RequestParam("tenantId") String id, Model model) {

		JSONObject json = new JSONObject();
		Tenant tenant = dao.getTenantById(id);
		if (tenant == null) {
			try {
				json.put("answer", "false");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			try {
				json.put("answer", "true");
				json.put("status", tenant.getCatalogStatus());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		model.addAttribute("jsonResponse",json.toString());

		return null;

	}
	
	@RequestMapping(value="/admin/resetPass.do", method={RequestMethod.POST})
	public String ResetPass (@RequestParam("username") String username, @RequestParam("email") String email, Model model) {

		JSONObject json = new JSONObject();
		Tenant tenant = dao.getTenantByUsernameAndEmail(username, email);
		if (tenant == null) {
			try {
				json.put("answer", "false");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			try {
				json.put("answer", "true");
			} catch (JSONException e) {
				e.printStackTrace();
			}

			//reset pass
			tenant.setPassword(getRandomPassword());
			dao.resetPass(tenant);
			//notify upon registration and send confirmation link
			notificationManager.sendResetPassEmail(tenant);
		}

		model.addAttribute("jsonResponse",json.toString());

		return null;

	}
	
	@RequestMapping(value="admin/uploadCatalog.do", method = RequestMethod.POST)
	public String upload(HttpServletRequest request, HttpServletResponse response, @RequestParam("file") MultipartFile f,
			@RequestParam(value = "tenantToken") String token, @RequestParam(value = "tenantId") String id) {
		Tenant tenant = dao.getTenantById(id);
		
		try {
			dao.uploadCatalog(f, token);
			tenant.setCatalogStatus(CatalogStatus.SYNCING);
		} catch (Exception e) {
			tenant.setCatalogStatus(CatalogStatus.FAILED);
			e.printStackTrace();
		}
		
		dao.updateTenant(tenant);

		return "redirect:catalogue.do";
	}
	 

	@RequestMapping(value="/admin/home.do", method = RequestMethod.GET)
	public String doAdminHome(ModelMap model) {

		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Tenant tenant = dao.getTenantByUsername(user.getUsername());

		model.addAttribute("tenant", tenant);
		return "admin/home";

	}

	@RequestMapping(value="/admin/catalogue.do", method = RequestMethod.GET)
	public String doAdminCatalogue(ModelMap model) {

		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Tenant tenant = dao.getTenantByUsername(user.getUsername());

		model.addAttribute("tenant", tenant);
		return "admin/catalogue";

	}

	@RequestMapping(value="/admin/billing.do", method = RequestMethod.GET)
	public String doAdminBilling(ModelMap model) {

		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Tenant tenant = dao.getTenantByUsername(user.getUsername());

		model.addAttribute("tenant", tenant);
		return "admin/billing";

	}
	
	
	@RequestMapping(value="/admin/edit_widget.do", method = RequestMethod.GET)
	public String editWidgets(@RequestParam(value = "id") String id, ModelMap model) {

		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Tenant tenant = dao.getTenantByUsername(user.getUsername());
	
		Widget widget = dao.getWidget(tenant, id);
		//List<Catalog> items = dao.getSampleItems(widget.getNoOfItems(), Integer.toString(tenant.getToken()));
				
		//model.addAttribute("items", items);
		model.addAttribute("widget", widget);
		model.addAttribute("tenant", tenant);
		
		return "admin/edit_widget";

	}
	
	@RequestMapping(value="admin/get_widget_code.do", method={RequestMethod.GET})
	public String getWidgetCode (@RequestParam("id") String id, @RequestParam("tenantId") String tenantId, Model model) {

		JSONObject json = new JSONObject();
		Tenant tenant = dao.getTenantById(tenantId);
		Widget widget = dao.getWidget(tenant, id);
		if (widget == null) {
			try {
				json.put("answer", "false");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			try {
				json.put("answer", "true");
				json.put("code", widget.getCode());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		model.addAttribute("jsonResponse",json.toString());

		return null;

	}
	
	@RequestMapping(value="admin/preview_widget.do", method={RequestMethod.GET})
	public String previewWidget (@RequestParam("id") String id, @RequestParam("tenantId") String tenantId, Model model) {

		JSONObject json = new JSONObject();
		Tenant tenant = dao.getTenantById(tenantId);
		Widget widget = dao.getWidget(tenant, id);
		if (widget == null) {
			try {
				json.put("answer", "false");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			try {
				json.put("answer", "true");
				json.put("preview", widget.getPreviewCode());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		model.addAttribute("jsonResponse",json.toString());

		return null;

	}
	
	@RequestMapping(value="/admin/get_widget_items.do", method={RequestMethod.POST})
	public String getWidgetItems (@RequestParam("limit") int limit, @RequestParam("token") String token, Model model) {
		
		String lis="";
		JSONObject json = new JSONObject();
		List<Catalog> items = dao.getSampleItems(limit, token);
		
		for(Catalog item : items){
			lis += "<li style='overflow:hidden;position:relative;'>" +
						"<a href='"+item.getUrl()+"'>" +
						"<span class='clearGistWidgetImgSpan'><img src='"+item.getImage()+"' border='0' width='100' height='auto'/></span>" +
						"<span class='clearGistWidgetNameSpan'>&nbsp;"+item.getItem()+"</span>" +
						"<span class='clearGistWidgetCategorySpan'>&nbsp;"+item.getCategory()+"</span>" +
						"<span class='clearGistWidgetPriceSpan'>&nbsp;&euro;"+item.getPrice()+"</span>" +
						/*"<span class='clearGistWidgetStockSpan'>&nbsp;only "+item.getStock()+" left!</span>" +*/
					"</li>";
		}
		
		try {
			json.put("answer", "true");
			json.put("lis", lis);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		model.addAttribute("jsonResponse",json.toString());

		return null;

	}
	
	@RequestMapping(value="/admin/edit_widget_async.do", method={RequestMethod.POST})
	public String editWidgetAsync (@RequestParam("tenantId") String tenantId, @RequestParam("id") String id, @RequestParam("name") String name, @RequestParam("description") String description,
			@RequestParam("selLayoutType") String selLayoutType, @RequestParam("selNoOfItems") int selNoOfItems, @RequestParam("selImageSize") String selImageSize, @RequestParam("selImageSize2") String selImageSize2, 
			@RequestParam("selTextAreaWidth") int selTextAreaWidth, @RequestParam("selListWidth") int selListWidth, @RequestParam("selListSpaces") int selListSpaces, @RequestParam("selListSpaces2") int selListSpaces2, 
			@RequestParam("selListDisplay") String selListDisplay, @RequestParam("sHead") int sHead, @RequestParam("sImag") int sImag, @RequestParam("sLogo") int sLogo, 
			@RequestParam("borderColor") String borderColor, @RequestParam("selBorderWidth") int selBorderWidth, @RequestParam("headerBackColor") String headerBackColor, @RequestParam("hTrans") int hTrans, 
			@RequestParam("mainColor") String mainColor, @RequestParam("mTrans") int mTrans, @RequestParam("footerColor") String footerColor, @RequestParam("fTrans") int fTrans, 
			@RequestParam("selMainFontType") String selMainFontType, @RequestParam("headerColor") String headerColor, @RequestParam("headerText") String headerText, @RequestParam("selHeaderFontSize") int selHeaderFontSize, 
			@RequestParam("selHeaderFontWeight") String selHeaderFontWeight, @RequestParam("selHeaderFontAlign") String selHeaderFontAlign, @RequestParam("nameColor") String nameColor, @RequestParam("selNameFontSize") int selNameFontSize, 
			@RequestParam("selNameFontWeight") String selNameFontWeight, @RequestParam("selNameFontAlign") String selNameFontAlign, @RequestParam("priceColor") String priceColor, @RequestParam("selPriceFontSize") int selPriceFontSize, 
			@RequestParam("selPriceFontWeight") String selPriceFontWeight, @RequestParam("selPriceFontAlign") String selPriceFontAlign, @RequestParam("categoryColor") String categoryColor, @RequestParam("selCategoryFontSize") int selCategoryFontSize, 
			@RequestParam("selCategoryFontWeight") String selCategoryFontWeight, @RequestParam("selCategoryFontAlign") String selCategoryFontAlign, @RequestParam("stockColor") String stockColor, @RequestParam("selStockFontSize") int selStockFontSize, 
			@RequestParam("selStockFontWeight") String selStockFontWeight, @RequestParam("selStockFontAlign") String selStockFontAlign, @RequestParam("sName") int sName, @RequestParam("sPrice") int sPrice, 
			@RequestParam("sCat") int sCat, @RequestParam("sStoc") int sStoc, @RequestParam("widgetCode") String widgetCode, @RequestParam("previewCode") String previewCode, @RequestParam("selWidgetType") String widgetType, Model model) {

		JSONObject json = new JSONObject();
		Tenant tenant = dao.getTenantById(tenantId);
		Widget widget = dao.getWidget(tenant, id);
		
		widget.setName(name);
		widget.setType(WidgetType.valueOf(widgetType));
		widget.setDescription(description);
		widget.setCode(widgetCode);
		widget.setPreviewCode(previewCode);
		widget.setLayoutType(selLayoutType);
		widget.setNoOfItems(selNoOfItems);
		widget.setImageSizeWidth(selImageSize);
		widget.setImageSizeHeight(selImageSize2);
		widget.setTextAreaWidth(selTextAreaWidth);
		widget.setListWidth(selListWidth);
		widget.setListSpaces(selListSpaces);
		widget.setListSpaces2(selListSpaces2);
		widget.setListDisplay(selListDisplay);
		widget.setShowHeader(sHead);
		widget.setShowImages(sImag);
		widget.setShowClearGistLogo(sLogo);
		widget.setBorderColor(borderColor);
		widget.setBorderWidth(selBorderWidth);
		widget.setHeaderBack(headerBackColor);
		widget.setHeaderBackTrans(hTrans);
		widget.setMainBack(mainColor);
		widget.setMainBackTrans(mTrans);
		widget.setFooterBack(footerColor);
		widget.setFooterBackTrans(fTrans);
		widget.setFontFamily(selMainFontType);
		widget.setHeaderTextColor(headerColor);
		widget.setHeaderText(headerText);
		widget.setHeaderTextSize(selHeaderFontSize);
		widget.setHeaderTextWeight(selHeaderFontWeight);
		widget.setHeaderTextAlign(selHeaderFontAlign);
		widget.setNameTextColor(nameColor);
		widget.setNameTextSize(selNameFontSize);
		widget.setNameTextWeight(selNameFontWeight);
		widget.setNameTextAlign(selNameFontAlign);
		widget.setPriceTextColor(priceColor);
		widget.setPriceTextSize(selPriceFontSize);
		widget.setPriceTextWeight(selPriceFontWeight);
		widget.setPriceTextAlign(selPriceFontAlign);
		widget.setCategoryTextColor(categoryColor);
		widget.setCategoryTextSize(selCategoryFontSize);
		widget.setCategoryTextWeight(selCategoryFontWeight);
		widget.setCategoryTextAlign(selCategoryFontAlign);
		widget.setStockTextColor(stockColor);
		widget.setStockTextSize(selStockFontSize);
		widget.setStockTextWeight(selStockFontWeight);
		widget.setStockTextAlign(selStockFontAlign);
		widget.setShowName(sName);
		widget.setShowPrice(sPrice);
		widget.setShowCategory(sCat);
		widget.setShowStock(sStoc);
		widget.setDateUpdated(curDateTime);
		
		dao.updateWidget(widget);
		
		try {
			json.put("answer", "true");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	
		model.addAttribute("jsonResponse",json.toString());

		return null;

	}
	
	@RequestMapping(value="/admin/widgets.do", method = RequestMethod.GET)
	public String doAdminWidgets(ModelMap model) {

		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Tenant tenant = dao.getTenantByUsername(user.getUsername());
	
		//Widget widget = dao.getWidget(Integer.toString(tenant.getToken()));
		//List<Catalog> items = dao.getSampleItems(widget.getNoOfItems(), Integer.toString(tenant.getToken()));
				
		//model.addAttribute("items", items);
		//model.addAttribute("widget", widget);
		model.addAttribute("tenant", tenant);
		return "admin/widgets";

	}
	
	@RequestMapping(value="/admin/add_widget.do", method={RequestMethod.POST})
	public String addWidget (@RequestParam("id") String id, @RequestParam("name") String name, @RequestParam("description") String description, Model model) {

		JSONObject json = new JSONObject();
		Widget widget = new Widget();
		Tenant tenant = dao.getTenantById(id);
		try {
			/*//generate tenant domains & initialize widget
			UUID uuId = UUID.randomUUID();
			simpleDBItems.add(new ReplaceableItem().withName("wid"+uuId).withAttributes(
					new ReplaceableAttribute().withName("ID").withValue(uuId.toString()),
					new ReplaceableAttribute().withName("NAME").withValue(name),
					new ReplaceableAttribute().withName("DESCRIPTION").withValue(description),
					new ReplaceableAttribute().withName("LAYOUTTYPE").withValue("horizontal"),
					new ReplaceableAttribute().withName("NOOFITEMS").withValue("4"),
					new ReplaceableAttribute().withName("IMAGESIZEWIDTH").withValue("auto"),
					new ReplaceableAttribute().withName("IMAGESIZEHEIGHT").withValue("auto"),
					new ReplaceableAttribute().withName("TEXTAREAWIDTH").withValue("400"),
					new ReplaceableAttribute().withName("SHOWHEADER").withValue("1"),
					new ReplaceableAttribute().withName("SHOWIMAGES").withValue("1"),
					new ReplaceableAttribute().withName("SHOWCLEARGISTLOGO").withValue("1"),
					new ReplaceableAttribute().withName("BORDERCOLOR").withValue("ffffff"),
					new ReplaceableAttribute().withName("BORDERWIDTH").withValue("1"),
					new ReplaceableAttribute().withName("HEADERBACK").withValue("ffffff"),
					new ReplaceableAttribute().withName("HEADERBACKTRANS").withValue("0"),
					new ReplaceableAttribute().withName("MAINBACK").withValue("ffffff"),
					new ReplaceableAttribute().withName("MAINBACKTRANS").withValue("0"),
					new ReplaceableAttribute().withName("FOOTERBACK").withValue("ffffff"),
					new ReplaceableAttribute().withName("FOOTERBACKTRANS").withValue("0"),
					new ReplaceableAttribute().withName("FONTFAMILY").withValue("Arial"),
					new ReplaceableAttribute().withName("HEADERTEXTCOLOR").withValue("000000"),
					new ReplaceableAttribute().withName("HEADERTEXT").withValue("People also bought..."),
					new ReplaceableAttribute().withName("HEADERTEXTSIZE").withValue("12"),
					new ReplaceableAttribute().withName("HEADERTEXTWEIGHT").withValue("normal"),
					new ReplaceableAttribute().withName("HEADERTEXTALIGN").withValue("center"),
					new ReplaceableAttribute().withName("NAMETEXTCOLOR").withValue("000000"),
					new ReplaceableAttribute().withName("NAMETEXTSIZE").withValue("12"),
					new ReplaceableAttribute().withName("NAMETEXTWEIGHT").withValue("normal"),
					new ReplaceableAttribute().withName("NAMETEXTALIGN").withValue("center"),
					new ReplaceableAttribute().withName("PRICETEXTCOLOR").withValue("000000"),
					new ReplaceableAttribute().withName("PRICETEXTSIZE").withValue("12"),
					new ReplaceableAttribute().withName("PRICETEXTWEIGHT").withValue("normal"),
					new ReplaceableAttribute().withName("PRICETEXTALIGN").withValue("center"),
					new ReplaceableAttribute().withName("CATEGORYTEXTCOLOR").withValue("000000"),
					new ReplaceableAttribute().withName("CATEGORYTEXTSIZE").withValue("12"),
					new ReplaceableAttribute().withName("CATEGORYTEXTWEIGHT").withValue("normal"),
					new ReplaceableAttribute().withName("CATEGORYTEXTALIGN").withValue("center"),
					new ReplaceableAttribute().withName("STOCKTEXTCOLOR").withValue("000000"),
					new ReplaceableAttribute().withName("STOCKTEXTSIZE").withValue("12"),
					new ReplaceableAttribute().withName("STOCKTEXTWEIGHT").withValue("normal"),
					new ReplaceableAttribute().withName("STOCKTEXTALIGN").withValue("center"),
					new ReplaceableAttribute().withName("SHOWNAME").withValue("1"),
					new ReplaceableAttribute().withName("SHOWPRICE").withValue("1"),
					new ReplaceableAttribute().withName("SHOWCATEGORY").withValue("0"),
					new ReplaceableAttribute().withName("SHOWSTOCK").withValue("0"),
					new ReplaceableAttribute().withName("DATEUPDATED").withValue(curDateTime.toString()))
					);*/
			
			UUID uuId = UUID.randomUUID();
			widget.setId(uuId.toString());
			widget.setName(name);
			widget.setType(WidgetType.Most_popular_overall);
			widget.setDescription(description);
			widget.setTenant(tenant);
			widget.setLayoutType("horizontal");
			widget.setNoOfItems(4);
			widget.setImageSizeHeight("auto");
			widget.setImageSizeWidth("100");
			widget.setListWidth(65);
			widget.setListSpaces(14);
			widget.setListSpaces2(0);
			widget.setListDisplay("block");
			widget.setTextAreaWidth(800);
			widget.setShowHeader(1);
			widget.setShowImages(1);
			widget.setShowClearGistLogo(1);
			widget.setBorderColor("ffffff");
			widget.setBorderWidth(1);
			widget.setHeaderBack("ffffff");
			widget.setHeaderBackTrans(0);
			widget.setMainBack("ffffff");
			widget.setMainBackTrans(0);
			widget.setFooterBack("ffffff");
			widget.setFooterBackTrans(0);
			widget.setFontFamily("Arial, Helvetica, sans-serif");
			widget.setHeaderTextColor("000000");
			widget.setHeaderText("People also bought...");
			widget.setHeaderTextSize(12);
			widget.setHeaderTextWeight("normal");
			widget.setHeaderTextAlign("center");
			widget.setNameTextColor("000000");
			widget.setNameTextSize(12);
			widget.setNameTextWeight("normal");
			widget.setNameTextAlign("center");
			widget.setPriceTextColor("000000");
			widget.setPriceTextSize(12);
			widget.setPriceTextWeight("normal");
			widget.setPriceTextAlign("center");
			widget.setCategoryTextColor("000000");
			widget.setCategoryTextSize(12);
			widget.setCategoryTextWeight("normal");
			widget.setCategoryTextAlign("center");
			widget.setStockTextColor("000000");
			widget.setStockTextSize(12);
			widget.setStockTextWeight("normal");
			widget.setStockTextAlign("center");
			widget.setShowName(1);
			widget.setShowPrice(1);
			widget.setShowCategory(0);
			widget.setShowStock(0);
			widget.setDateUpdated(curDateTime);
			
			dao.addWidget(widget);
			
			json.put("answer", "true");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		model.addAttribute("jsonResponse",json.toString());

		return null;

	}
	
	@RequestMapping(value="/admin/delete_widget.do", method={RequestMethod.POST})
	public String deleteWidget (@RequestParam("tenantId") String tenantId, @RequestParam("id") String id, Model model) {

		JSONObject json = new JSONObject();
		
		try {
			Tenant tenant = dao.getTenantById(tenantId);
			dao.deleteWidget(tenant, id);
			json.put("answer", "true");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		model.addAttribute("jsonResponse",json.toString());

		return null;

	}
	

	@RequestMapping(value="/admin/get_widgets.do",method=RequestMethod.GET)
	public String getWidgetsXml(HttpServletRequest request, Model model){
		List<String> fields = Widget.getFields();
		/*String tenantId = request.getParameter("id");*/
		Tenant tenant = dao.getTenantById(request.getParameter("id"));
		/*long total = (Long) dao.countWidgets(tenant);*/

		List<Widget> widgets = dao.getWidgets(tenant);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			Element rows = document.createElement("rows");
			document.appendChild(rows);

			for(Widget w : widgets){
				Element curW = w.getXmlNodeRepresentation(document);
				
				String type = w.getType().toString().replace('_',' ');
				System.out.println(type);
				
				Element typeNode = document.createElement("cell");
				CDATASection editSection00 = 
						document.createCDATASection(
								type
								);
				typeNode.appendChild(editSection00);
				
				Element descNode = document.createElement("cell");
				CDATASection editSection0 = 
						document.createCDATASection(
								w.getDescription()
								);
				descNode.appendChild(editSection0);
				
				Element editNode = document.createElement("cell");
				CDATASection editSection = 
						document.createCDATASection(
								"<a href='edit_widget.do?id="+w.getId()+"'><img src='../images/pencil2.png' alt='edit' border='0' /></a>"
								);
				editNode.appendChild(editSection);
				
				Element previewNode = document.createElement("cell");
				CDATASection previewSection = 
						document.createCDATASection(
								"<a href='#' onclick='dialogPreview(\""+w.getId()+"\");'><img src='../images/preview.png' alt='preview' border='0' /></a>"
								);
				previewNode.appendChild(previewSection);
				
				Element codeNode = document.createElement("cell");
				CDATASection codeSection = 
						document.createCDATASection(
								"<a href='#' onclick='dialogCode(\""+w.getId()+"\");'><img src='../images/code.png' alt='code' border='0' /></a>"
								);
				codeNode.appendChild(codeSection);
				
				Element deleteNode = document.createElement("cell");
				CDATASection deleteSection = 
						document.createCDATASection(
								"<a href='#' onclick='deleteWidget(\""+w.getId()+"\", \""+w.getName()+"\");'><img src='../images/delete2.png' alt='delete' border='0' /></a>"
								);
				deleteNode.appendChild(deleteSection);
				
				rows.appendChild(curW);
				curW.appendChild(typeNode);
				curW.appendChild(descNode);
				curW.appendChild(editNode);
				curW.appendChild(previewNode);
				curW.appendChild(codeNode);
				curW.appendChild(deleteNode);
			}

			DOMSource domSource = new DOMSource(document);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer trans = tf.newTransformer();
			trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			trans.setOutputProperty(OutputKeys.METHOD, "xml");

			StringWriter writer = new StringWriter();
			StreamResult sr = new StreamResult(writer);
			trans.transform(domSource, sr);
			model.addAttribute("catalogXml",writer.toString());
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "admin/get_catalog_items";
	}

	@RequestMapping(value="/admin/settings.do", method = RequestMethod.GET)
	public String doAdminSettings(ModelMap model) {

		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Tenant tenant = dao.getTenantByUsername(user.getUsername());

		model.addAttribute("tenant", tenant);
		return "admin/settings";

	}

	@RequestMapping(value="/admin/help.do", method = RequestMethod.GET)
	public String doAdminHelp(ModelMap model) {

		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Tenant tenant = dao.getTenantByUsername(user.getUsername());

		model.addAttribute("tenant", tenant);
		return "admin/help";

	}
	
	@RequestMapping(value="/admin/reports.do", method = RequestMethod.GET)
	public String doAdminReports(ModelMap model) {

		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Tenant tenant = dao.getTenantByUsername(user.getUsername());

		model.addAttribute("tenant", tenant);
		return "admin/reports";

	}


	/*@RequestMapping(value="/admin")
	public String redirectTologin(ModelMap model) {
		return "redirect:admin/login.do";
	}*/

	@RequestMapping(value="/admin/login.do", method={RequestMethod.GET})
	public String login(ModelMap model) {
		return "admin/login";
	}

	@RequestMapping(value="/admin/forgot_pass.do", method={RequestMethod.GET})
	public String forgotPass(ModelMap model) {
		return "admin/forgot_pass";
	}


	@RequestMapping(value="/admin/loginfailed.do", method={RequestMethod.GET, RequestMethod.POST})
	public String loginerror(ModelMap model) {

		model.addAttribute("error", "true");
		return "admin/login";

	}

	@RequestMapping(value="/admin/logout.do", method = RequestMethod.GET)
	public String logout(ModelMap model) {

		return "admin/login";

	}


	/*@RequestMapping(value="/admin/login.do", method={RequestMethod.POST})
	public String adminLogin (Tenant tenant, BindingResult result) {

		return result.hasErrors()?"redirect:servererror.do?error=Post vars are missing":"redirect:signupresults.do";

	}*/

	@RequestMapping(value = "/admin/denied.do", method = RequestMethod.GET)   
	public void getDeniedPage() {
		/*return "redirect:/admin/denied.do";*/
	}

	@RequestMapping(value="/admin/verifyUser.do", method={RequestMethod.POST})
	public String verifyUser (@RequestParam("username") String username, @RequestParam("password") String password, Model model) {

		JSONObject json = new JSONObject();
		Tenant tenant = dao.verifyTenant(username,password);
		if (tenant == null) {
			try {
				json.put("answer", "false");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			try {
				json.put("answer", "true");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		model.addAttribute("jsonResponse",json.toString());

		return null;

	}

	@RequestMapping(value="/admin/update_per_settings.do", method={RequestMethod.POST})
	public String updatePerSettings (@RequestParam("id") String id, @RequestParam("firstName") String firstName, @RequestParam("lastName") String lastName, @RequestParam("email") String email,
			@RequestParam("company") String company, @RequestParam("url") String url, Model model) {

		JSONObject json = new JSONObject();
		Tenant tenant = dao.getTenantById(id);
		tenant.setFirstName(firstName);
		tenant.setLastName(lastName);
		tenant.setEmail(email);
		tenant.setCompany(company);
		tenant.setUrl(url);
		dao.updateTenant(tenant);
		try {
			json.put("answer", "true");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		model.addAttribute("jsonResponse",json.toString());

		return null;

	}

	@RequestMapping(value="/admin/update_log_settings.do", method={RequestMethod.POST})
	public String updateLogSettings (@RequestParam("id") String id, @RequestParam("password1") String password1, Model model) {

		JSONObject json = new JSONObject();
		Tenant tenant = dao.getTenantById(id);
		tenant.setPassword(password1);
		dao.updateTenantPassword(tenant);
		try {
			json.put("answer", "true");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		model.addAttribute("jsonResponse",json.toString());

		return null;

	}

	@RequestMapping(value="/admin/get_catalog_items.do",method=RequestMethod.GET)
	public String getCatalogXml(HttpServletRequest request, Model model){
		List<String> fields = Catalog.getFields();
		String tenantId = request.getParameter("id");
		
		Map<String,String> conditions = new HashMap<String,String>();

		if(request.getParameter("selKey")!=null
				&& request.getParameter("selKey").equals("item")){
			conditions.put("ITEM", request.getParameter("selValue"));
		}
		if(request.getParameter("selKey")!=null
				&& request.getParameter("selKey").equals("category")){
			conditions.put("CATEGORY", request.getParameter("selValue"));
		}
		if(request.getParameter("description")!=null
				&& request.getParameter("selKey").equals("item")){
			conditions.put("DESCRIPTION", request.getParameter("selValue"));
		}
		
		long total = conditions.size()>0 ? dao.countItems(conditions,tenantId) : dao.countItems(tenantId);
		//long total = dao.countItems(tenantId);
		int startPage = 1;
		int start = 0;
		int totalResults = 20;

		String sortBy = "ITEM";
		String orderBy = "ASC";

		boolean hasSorting = false;
		boolean hasOrdering = false;

		if(request.getParameter("sidx")!=null
				&& !request.getParameter("sidx").equals("")){
			String sidx = request.getParameter("sidx");
			if(fields.contains(sidx)){
				sortBy = sidx;
				hasSorting = true;
			}
		}
		if(request.getParameter("sord")!=null
				&& (request.getParameter("sord").equals("asc") || request.getParameter("sord").equals("desc")) ){
			orderBy = request.getParameter("sord");
			hasOrdering = true;
		}

		if(request.getParameter("rows")!=null){
			totalResults = Integer.valueOf(request.getParameter("rows"));
		}

		if(request.getParameter("page")!=null ){
			startPage = Integer.valueOf(request.getParameter("page"));
			start = startPage*totalResults-totalResults;
		}

		List<Catalog> catalog = null;

		if(conditions.size()>0 && hasSorting && hasOrdering){
			catalog = dao.getCatalog(conditions,sortBy,orderBy,start,totalResults,tenantId);
		}else if(hasSorting && hasOrdering){
			catalog = dao.getCatalog(sortBy,orderBy,start,totalResults,tenantId);
		}else{
			catalog = dao.getCatalog(start,totalResults,tenantId);
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			Element rows = document.createElement("rows");
			document.appendChild(rows);

			Element page = document.createElement("page");
			CDATASection editSectionPage = document.createCDATASection(Long.toString(startPage));
			page.appendChild(editSectionPage);

			Element records = document.createElement("records");
			CDATASection editSectionTotal = document.createCDATASection(Long.toString(total));
			records.appendChild(editSectionTotal);

			Element totalEl = document.createElement("total");
			int totalPages = 1;
			totalPages = (int) Math.ceil(((double)total)/((double)totalResults));
			CDATASection editSectionTotalEl = document.createCDATASection(Long.toString(totalPages));
			totalEl.appendChild(editSectionTotalEl);

			rows.appendChild(page);
			rows.appendChild(records);
			rows.appendChild(totalEl);

			for(Catalog c : catalog){
				Element curP = c.getXmlNodeRepresentation(document);

				Element imgNode = document.createElement("cell");
				CDATASection editSection = 
						document.createCDATASection(
								"<img src='"+c.getImage()+"' alt='item' border='0' style='cursor:pointer' width='50' onclick='imageDisplay(\""+c.getImage()+"\");' />"
								);
				imgNode.appendChild(editSection);
				
				Element itemNode = document.createElement("cell");
				CDATASection editSection2 = 
						document.createCDATASection(
								"<a href='"+c.getUrl()+"' target='_blank'>"+c.getItem()+"</a>"
								);
				itemNode.appendChild(editSection2);
				
				Element catNode = document.createElement("cell");
				CDATASection editSection3 = 
						document.createCDATASection(
								c.getCategory()
								);
				catNode.appendChild(editSection3);
				
				Element priceNode = document.createElement("cell");
				CDATASection editSection4 = 
						document.createCDATASection(
								"&euro;"+Double.toString(c.getPrice())
								);
				priceNode.appendChild(editSection4);
				
				Element descNode = document.createElement("cell");
				CDATASection editSection5 = 
						document.createCDATASection(
								c.getDescription()
								);
				descNode.appendChild(editSection5);
				
				/*Element urlNode = document.createElement("cell");
				CDATASection editSection2 = 
						document.createCDATASection(
								"<a href='"+c.getUrl()+"' target='_blank'><img src='../images/globe.png' alt='item' border='0' /></a>"
								);
				urlNode.appendChild(editSection2);*/
				
				curP.appendChild(imgNode);
				curP.appendChild(itemNode);
				curP.appendChild(catNode);
				curP.appendChild(priceNode);
				curP.appendChild(descNode);
				/*curP.appendChild(urlNode);*/
				rows.appendChild(curP);
			}

			DOMSource domSource = new DOMSource(document);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer trans = tf.newTransformer();
			trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			trans.setOutputProperty(OutputKeys.METHOD, "xml");

			StringWriter writer = new StringWriter();
			StreamResult sr = new StreamResult(writer);
			trans.transform(domSource, sr);
			model.addAttribute("catalogXml",writer.toString());
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		return "admin/get_catalog_items";
	}

	/**
	 * The activity controller, which records the user's activities
	 * @param attributes that hold the activity data 
	 */
	@RequestMapping(value="/activity.do", method={RequestMethod.GET})
	public @ResponseBody String recordActivity (@RequestParam(value = "userId") String userId, @RequestParam(value="event") String event,
			@RequestParam(value = "session") String session, @RequestParam(value = "itemId") String itemId, @RequestParam(value = "customer") String customer) {

		//initialize activity data
		String activityEvent = ActivityEvent.ITEM_PAGE.toString();

		//validate requests	- tenant
		try {
			tenant = Integer.parseInt(customer);
		} catch(NumberFormatException e) {
			failures.add("Tenant posted is not an int.");
		}

		//validate requests	- item
		if(itemId==null || itemId.equals(""))
			failures.add("Item not posted.");

		//validate requests	- event

		if(event.equals("PURCHASE")){
			activityEvent = ActivityEvent.PURCHASE.toString();
		}else if(event.equals("ITEM_PAGE")){
			activityEvent = ActivityEvent.ITEM_PAGE.toString();
		}else if(event.equals("ADD_TO_CART")){
			activityEvent = ActivityEvent.ADD_TO_CART.toString();
		}else if(event.equals("REC_SERVED")){
			activityEvent = ActivityEvent.REC_SERVED.toString();
		}else if(event.equals("REC_SEEN")){
			activityEvent = ActivityEvent.REC_SEEN.toString();
		}else if(event.equals("REC_CLICK")){
			activityEvent = ActivityEvent.REC_CLICK.toString();
		}else if(event.equals("HOME_PAGE_VIEW")){
			activityEvent = ActivityEvent.HOME_PAGE_VIEW.toString();
		}else if(event.equals("CATEGORY_PAGE_VIEW")){
			activityEvent = ActivityEvent.CATEGORY_PAGE_VIEW.toString();
		}else if(event.equals("SEARCH")){
			activityEvent = ActivityEvent.SEARCH.toString();
		}else if(event.equals("RATING")){
			activityEvent = ActivityEvent.ITEM_PAGE.toString();
		}else{
			failures.add("Event not posted");
		}

		//record activity or failures
		if(failures.size()==0){
			//initialize uuid and add to list of simple db activity attributes	
			UUID uuId = UUID.randomUUID();
			simpleDBItems.add(new ReplaceableItem().withName("act"+uuId).withAttributes(
					new ReplaceableAttribute().withName("ID").withValue(uuId.toString()),
					new ReplaceableAttribute().withName("EVENT").withValue(activityEvent),
					new ReplaceableAttribute().withName("ITEM").withValue(itemId),
					new ReplaceableAttribute().withName("USER").withValue(userId),
					new ReplaceableAttribute().withName("SESSION").withValue(session),
					new ReplaceableAttribute().withName("ACTDATE").withValue(curDateTime.toString()))
					);
			dao.saveActivity(simpleDBItems, tenant.toString());
			//logger.log(Level.INFO,"--------------Activity Saved----------------");
		}else{
			//add to list of list of simple db error attributes
			for(String mes:failures){
				//initialize uuid and add to list of simple db error attributes
				UUID uuId = UUID.randomUUID();
				simpleDBItems.add(new ReplaceableItem().withName("error"+uuId).withAttributes(
						new ReplaceableAttribute().withName("ERRDATE").withValue(curDateTime.toString()),
						new ReplaceableAttribute().withName("MESSAGE").withValue(mes),
						new ReplaceableAttribute().withName("TYPE").withValue(ErrorType.RECORD_ACTIVITY.toString()),
						new ReplaceableAttribute().withName("TENANT").withValue(tenant.toString()))
						);
			}

			dao.saveErrorLog(simpleDBItems);
			//logger.log(Level.INFO,"--------------Error Log Saved----------------");
		}
		return null;
	}


	@RequestMapping(value="admin/mock.do", method={RequestMethod.GET})
	public void mock () {

		UUID uuId = UUID.randomUUID();
		double price = new Double (109.00);
		UUID uuId2 = UUID.randomUUID();
		double price2 = new Double (791.00);
		UUID uuId3 = UUID.randomUUID();
		double price3 = new Double (452.00);
		UUID uuId4 = UUID.randomUUID();
		UUID uuId5 = UUID.randomUUID();
		UUID uuId6 = UUID.randomUUID();
		UUID uuId7 = UUID.randomUUID();
		UUID uuId8 = UUID.randomUUID();
		UUID uuId9 = UUID.randomUUID();
		Catalog c = new Catalog(uuId.toString(), "1001", "Test Item 1", "Test Category 1", "http://www.plaisio.gr/Sound-Vision/Home-Cinema/Televisions/Turbo-X-TXV-215FH-LED-TV-21-5-22L13A.htm",
				"http://www.plaisio.gr/ProductImages/250x250/1695304.JPG", 
				"Ανάλυση FullHD για υψηλής ευκρίνειας ταινίες και video, με ενσωματωμένο ψηφιακό δέκτη αλλά και θύρα USB για αναπαραγωγή αρχείων και καταγραφή τηλεοπτικών προγραμμάτων", price, 10);
		Catalog c2 = new Catalog(uuId2.toString(), "1002", "Test Item 2", "Test Category 2", "http://www.plaisio.gr/Laptop-and-Tablet/Notebook/Tablets-iPAD/Turbo-X-Tablet-Hive-DUKE.htm",
				"http://www.plaisio.gr/ProductImages/250x250/1667513.JPG", 
				"Γρήγορο και πολυτελές, με δύο κάμερες, μεγάλο αποθηκευτικό χώρο και το πιο σύγχρονο λειτουργικό Android 3.2", price2, 5);
		Catalog c3 = new Catalog(uuId3.toString(), "1003", "Test Item 3", "Test Category 3", "http://www.plaisio.gr/Laptop-and-Tablet/Notebook/Laptop/Turbo-X-Steel-D-820-432.htm",
				"http://www.plaisio.gr/ProductImages/250x250/1736744.JPG", 
				"Τριπύρηνος επεξεργαστής AMD Athlon για γρήγορη εκτέλεση εφαρμογών και προγραμμάτων, μεγάλη μνήμη RAM 4 GB DDR3 για άνετο multitasking και άπλετο αποθηκευτικό χώρο 320 GB", price3, 8);
		
		Catalog c4 = new Catalog(uuId4.toString(), "1004", "Test Item 4", "Test Category 4", "http://www.plaisio.gr/Gaming/Consoles/Microsoft-XBOX360/Microsoft-XBOX-360-4GB-Kinect-Holiday-2011-Value-Bundle-R7G-00008.htm",
				"http://www.plaisio.gr/ProductImages/250x250/1724258.JPG", 
				"Η δημοφιλής κονσόλα με το Kinect, τα παιχνίδια Carnival Games και Kinect Adventures και 3 μήνες δωρεάν συνδρομή Xbox Live Gold", price, 10);
		Catalog c5 = new Catalog(uuId5.toString(), "1005", "Test Item 5", "Test Category 5", "http://www.plaisio.gr/Peripherals/PC-Peripherals/Keyboards/Turbo-X-NumPad-WND100-JT-JP0027.htm",
				"http://www.plaisio.gr/ProductImages/250x250/1685287.JPG", 
				"Turbo-X NumPad WNP100 ", price2, 5);
		Catalog c6 = new Catalog(uuId6.toString(), "1006", "Test Item 6", "Test Category 6", "http://www.plaisio.gr/Peripherals/Printers/Scanners/HP-Scanjet-G3110-L2698A-BEL.htm",
				"http://www.plaisio.gr/ProductImages/250x250/1164155.JPG	", 
				"Με λειτουργία αποκατάστασης των φθαρμένων φωτογραφιών και ελληνικό λογισμικό OCR.", price3, 8);
		
		Catalog c7 = new Catalog(uuId7.toString(), "1020", "Test Item 20", "Test Category 20", "http://www.plaisio.gr/Gaming/Consoles/Microsoft-XBOX360/Microsoft-XBOX-360-4GB-Kinect-Holiday-2011-Value-Bundle-R7G-00008.htm",
				"http://www.plaisio.gr/ProductImages/250x250/1724258.JPG", 
				"Η δημοφιλής κονσόλα με το Kinect, τα παιχνίδια Carnival Games και Kinect Adventures και 3 μήνες δωρεάν συνδρομή Xbox Live Gold", price, 10);
		Catalog c8 = new Catalog(uuId8.toString(), "1021", "Test Item 21", "Test Category 21", "http://www.plaisio.gr/Peripherals/PC-Peripherals/Keyboards/Turbo-X-NumPad-WND100-JT-JP0027.htm",
				"http://www.plaisio.gr/ProductImages/250x250/1685287.JPG", 
				"Turbo-X NumPad WNP100 ", price2, 5);
		Catalog c9 = new Catalog(uuId9.toString(), "1022", "Test Item 22", "Test Category 22", "http://www.plaisio.gr/Peripherals/Printers/Scanners/HP-Scanjet-G3110-L2698A-BEL.htm",
				"http://www.plaisio.gr/ProductImages/250x250/1164155.JPG	", 
				"Με λειτουργία αποκατάστασης των φθαρμένων φωτογραφιών και ελληνικό λογισμικό OCR.", price3, 8);

		simpleDBItems.add(new ReplaceableItem().withName("cat"+uuId).withAttributes(
				new ReplaceableAttribute().withName("ID").withValue(c.getId()),
				new ReplaceableAttribute().withName("CUSTID").withValue(c.getCustId()),
				new ReplaceableAttribute().withName("ITEM").withValue(c.getItem()),
				new ReplaceableAttribute().withName("CATEGORY").withValue(c.getCategory()),
				new ReplaceableAttribute().withName("URL").withValue(c.getUrl()),
				new ReplaceableAttribute().withName("IMAGE").withValue(c.getImage()),
				new ReplaceableAttribute().withName("DESCRIPTION").withValue(c.getDescription()),
				new ReplaceableAttribute().withName("PRICE").withValue(Double.toString(c.getPrice())),
				new ReplaceableAttribute().withName("STOCK").withValue(Integer.toString(c.getStock())),
				new ReplaceableAttribute().withName("INSDATE").withValue(curDateTime.toString()))
				);
		simpleDBItems.add(new ReplaceableItem().withName("cat"+uuId2).withAttributes(
				new ReplaceableAttribute().withName("ID").withValue(c2.getId()),
				new ReplaceableAttribute().withName("CUSTID").withValue(c2.getCustId()),
				new ReplaceableAttribute().withName("ITEM").withValue(c2.getItem()),
				new ReplaceableAttribute().withName("CATEGORY").withValue(c2.getCategory()),
				new ReplaceableAttribute().withName("URL").withValue(c2.getUrl()),
				new ReplaceableAttribute().withName("IMAGE").withValue(c2.getImage()),
				new ReplaceableAttribute().withName("DESCRIPTION").withValue(c2.getDescription()),
				new ReplaceableAttribute().withName("PRICE").withValue(Double.toString(c2.getPrice())),
				new ReplaceableAttribute().withName("STOCK").withValue(Integer.toString(c2.getStock())),
				new ReplaceableAttribute().withName("INSDATE").withValue(curDateTime.toString()))
				);
		simpleDBItems.add(new ReplaceableItem().withName("cat"+uuId3).withAttributes(
				new ReplaceableAttribute().withName("ID").withValue(c3.getId()),
				new ReplaceableAttribute().withName("CUSTID").withValue(c3.getCustId()),
				new ReplaceableAttribute().withName("ITEM").withValue(c3.getItem()),
				new ReplaceableAttribute().withName("CATEGORY").withValue(c3.getCategory()),
				new ReplaceableAttribute().withName("URL").withValue(c3.getUrl()),
				new ReplaceableAttribute().withName("IMAGE").withValue(c3.getImage()),
				new ReplaceableAttribute().withName("DESCRIPTION").withValue(c3.getDescription()),
				new ReplaceableAttribute().withName("PRICE").withValue(Double.toString(c3.getPrice())),
				new ReplaceableAttribute().withName("STOCK").withValue(Integer.toString(c3.getStock())),
				new ReplaceableAttribute().withName("INSDATE").withValue(curDateTime.toString()))
				);
		simpleDBItems.add(new ReplaceableItem().withName("cat"+uuId4).withAttributes(
				new ReplaceableAttribute().withName("ID").withValue(c4.getId()),
				new ReplaceableAttribute().withName("CUSTID").withValue(c4.getCustId()),
				new ReplaceableAttribute().withName("ITEM").withValue(c4.getItem()),
				new ReplaceableAttribute().withName("CATEGORY").withValue(c4.getCategory()),
				new ReplaceableAttribute().withName("URL").withValue(c4.getUrl()),
				new ReplaceableAttribute().withName("IMAGE").withValue(c4.getImage()),
				new ReplaceableAttribute().withName("DESCRIPTION").withValue(c4.getDescription()),
				new ReplaceableAttribute().withName("PRICE").withValue(Double.toString(c4.getPrice())),
				new ReplaceableAttribute().withName("STOCK").withValue(Integer.toString(c4.getStock())),
				new ReplaceableAttribute().withName("INSDATE").withValue(curDateTime.toString()))
				);
		simpleDBItems.add(new ReplaceableItem().withName("cat"+uuId5).withAttributes(
				new ReplaceableAttribute().withName("ID").withValue(c5.getId()),
				new ReplaceableAttribute().withName("CUSTID").withValue(c5.getCustId()),
				new ReplaceableAttribute().withName("ITEM").withValue(c5.getItem()),
				new ReplaceableAttribute().withName("CATEGORY").withValue(c5.getCategory()),
				new ReplaceableAttribute().withName("URL").withValue(c5.getUrl()),
				new ReplaceableAttribute().withName("IMAGE").withValue(c5.getImage()),
				new ReplaceableAttribute().withName("DESCRIPTION").withValue(c5.getDescription()),
				new ReplaceableAttribute().withName("PRICE").withValue(Double.toString(c5.getPrice())),
				new ReplaceableAttribute().withName("STOCK").withValue(Integer.toString(c5.getStock())),
				new ReplaceableAttribute().withName("INSDATE").withValue(curDateTime.toString()))
				);
		simpleDBItems.add(new ReplaceableItem().withName("cat"+uuId6).withAttributes(
				new ReplaceableAttribute().withName("ID").withValue(c6.getId()),
				new ReplaceableAttribute().withName("CUSTID").withValue(c6.getCustId()),
				new ReplaceableAttribute().withName("ITEM").withValue(c6.getItem()),
				new ReplaceableAttribute().withName("CATEGORY").withValue(c6.getCategory()),
				new ReplaceableAttribute().withName("URL").withValue(c6.getUrl()),
				new ReplaceableAttribute().withName("IMAGE").withValue(c6.getImage()),
				new ReplaceableAttribute().withName("DESCRIPTION").withValue(c6.getDescription()),
				new ReplaceableAttribute().withName("PRICE").withValue(Double.toString(c6.getPrice())),
				new ReplaceableAttribute().withName("STOCK").withValue(Integer.toString(c6.getStock())),
				new ReplaceableAttribute().withName("INSDATE").withValue(curDateTime.toString()))
				);
		simpleDBItems.add(new ReplaceableItem().withName("cat"+uuId7).withAttributes(
				new ReplaceableAttribute().withName("ID").withValue(c7.getId()),
				new ReplaceableAttribute().withName("CUSTID").withValue(c7.getCustId()),
				new ReplaceableAttribute().withName("ITEM").withValue(c7.getItem()),
				new ReplaceableAttribute().withName("CATEGORY").withValue(c7.getCategory()),
				new ReplaceableAttribute().withName("URL").withValue(c7.getUrl()),
				new ReplaceableAttribute().withName("IMAGE").withValue(c7.getImage()),
				new ReplaceableAttribute().withName("DESCRIPTION").withValue(c7.getDescription()),
				new ReplaceableAttribute().withName("PRICE").withValue(Double.toString(c7.getPrice())),
				new ReplaceableAttribute().withName("STOCK").withValue(Integer.toString(c7.getStock())),
				new ReplaceableAttribute().withName("INSDATE").withValue(curDateTime.toString()))
				);
		simpleDBItems.add(new ReplaceableItem().withName("cat"+uuId8).withAttributes(
				new ReplaceableAttribute().withName("ID").withValue(c8.getId()),
				new ReplaceableAttribute().withName("CUSTID").withValue(c8.getCustId()),
				new ReplaceableAttribute().withName("ITEM").withValue(c8.getItem()),
				new ReplaceableAttribute().withName("CATEGORY").withValue(c8.getCategory()),
				new ReplaceableAttribute().withName("URL").withValue(c8.getUrl()),
				new ReplaceableAttribute().withName("IMAGE").withValue(c8.getImage()),
				new ReplaceableAttribute().withName("DESCRIPTION").withValue(c8.getDescription()),
				new ReplaceableAttribute().withName("PRICE").withValue(Double.toString(c8.getPrice())),
				new ReplaceableAttribute().withName("STOCK").withValue(Integer.toString(c8.getStock())),
				new ReplaceableAttribute().withName("INSDATE").withValue(curDateTime.toString()))
				);
		simpleDBItems.add(new ReplaceableItem().withName("cat"+uuId9).withAttributes(
				new ReplaceableAttribute().withName("ID").withValue(c9.getId()),
				new ReplaceableAttribute().withName("CUSTID").withValue(c9.getCustId()),
				new ReplaceableAttribute().withName("ITEM").withValue(c9.getItem()),
				new ReplaceableAttribute().withName("CATEGORY").withValue(c9.getCategory()),
				new ReplaceableAttribute().withName("URL").withValue(c9.getUrl()),
				new ReplaceableAttribute().withName("IMAGE").withValue(c9.getImage()),
				new ReplaceableAttribute().withName("DESCRIPTION").withValue(c9.getDescription()),
				new ReplaceableAttribute().withName("PRICE").withValue(Double.toString(c9.getPrice())),
				new ReplaceableAttribute().withName("STOCK").withValue(Integer.toString(c9.getStock())),
				new ReplaceableAttribute().withName("INSDATE").withValue(curDateTime.toString()))
				);
		dao.saveCatalog(simpleDBItems, "108");
		//logger.log(Level.INFO,"--------------Activity Saved----------------");

	}

	@RequestMapping(value="/home.do", method={RequestMethod.GET})
	public void doHome () {
		
		/*dao.getTenantsToUploadCatalog();*/

	}


	@RequestMapping(value="/servererror.do", method={RequestMethod.GET, RequestMethod.POST})
	public void serverError () {

	}


	@Autowired
	public void setRecommendationsDAO (RecommendationsDAO dao) {
		this.dao = dao;
	}

	@Autowired
	public void setNotificationManager (NotificationManager notificationManager) {
		this.notificationManager = notificationManager;
	}

	/**
	 * Method establishes the transformation of incoming date strings into Date objects
	 * @param binder the spring databinder object that we connect to the date editor
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		dateFormat.setLenient(true);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
	}

	public String getRandomPassword() { 
		StringBuffer password = new StringBuffer(20); 
		int next = RandomUtils.nextInt(13) + 8; 
		password.append(RandomStringUtils.randomAlphanumeric(next)); 
		return password.toString(); 
	} 



}
