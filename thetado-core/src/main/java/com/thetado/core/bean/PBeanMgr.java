package com.thetado.core.bean;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.thetado.core.access.AbstractAccessor;
import com.thetado.core.distribute.AbstractDistribute;
import com.thetado.core.parser.AbstractParser;
import com.thetado.core.template.AbstractTempletBase;

public class PBeanMgr
{
	/**
	 * 采集
	 */
	private PBeanContainer<AccessorBean> accessorBeans;
	
	/**
	 * 解析
	 */
	private PBeanContainer<ParserBean> parserBeans;
	
	/**
	 * 模版
	 */
	private PBeanContainer<TemplateBean> templateBeans;
	
	/**
	 * 分发
	 */
	private PBeanContainer<DistributorBean> distributorBeans;
	
	/**
	 * 配置文件路径
	 */
	private static final String configFilePath = "." + File.separator + "conf" + 
    	File.separator + "pbean.xml";
	private Document doc;
	private static PBeanMgr instance = null;

	private PBeanMgr()
	{
		
		this.accessorBeans = new PBeanContainer<AccessorBean>();
		this.parserBeans = new PBeanContainer<ParserBean>();
		this.templateBeans = new PBeanContainer<TemplateBean>();
		this.distributorBeans = new PBeanContainer<DistributorBean>();

		init();
	}

	public static synchronized PBeanMgr getInstance()
	{
		if (instance == null)
		{
			instance = new PBeanMgr();
		}
		return instance;
	}

	private void init()
	{
		File f = new File(configFilePath);
		if ((!f.exists()) || (!f.isFile())) {
			return;
		}
		SAXReader reader = new SAXReader();
		try
		{
			this.doc = reader.read(new FileInputStream(configFilePath));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}

		loadPBean("/pbeans/accessors/accessor", this.accessorBeans, AccessorBean.class);
		loadPBean("/pbeans/parsers/parser", this.parserBeans, ParserBean.class);
    	loadPBean("/pbeans/distributors/distributor", this.distributorBeans, DistributorBean.class);
    	loadPBean("/pbeans/templates/template", this.templateBeans, TemplateBean.class);
	}

	@SuppressWarnings("unchecked")
	private <T extends PBean> void loadPBean(String PBeanXPath, PBeanContainer<T> container, Class<T> clazz)
	{
		List<Element> lst = this.doc.selectNodes(PBeanXPath);
		if (lst == null) {
			return;
		}
		for (Element e : lst)
		{
			PBean bean = null;
			try
			{
				bean = clazz.newInstance();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}

			if (bean == null) {
				continue;
			}
			boolean b = retrievePBean(e, bean);
			if (b)
				container.add((T)bean);
		}
	}

	private <T extends PBean> boolean retrievePBean(Element e, T bean)
	{
		if (bean == null) {
			return false;
		}
		String strID = e.attributeValue("id");
		if (strID == null)
			return false;
		int id = Integer.parseInt(strID);
		if (id < -1)
			return false;
		bean.setId(id);

		Element eName = e.element("name");
		if (eName != null) {
			bean.setName(eName.getText());
		}
		Element eDes = e.element("des");
		if (eDes != null) {
			bean.setDes(eDes.getText());
		}
		Element eBean = e.element("bean");
		if (eBean == null)
			return false;
		String strBean = eBean.getText();
		if ((strBean == null) || (strBean.trim().length() < 1))
			return false;
		bean.setBean(eBean.getText());

		Element eParams = e.element("params");
		if (eParams == null) {
			return true;
		}
		@SuppressWarnings("unchecked")
		List<Element> eParamLst = eParams.elements("param");
		if (eParamLst == null) {
			return true;
		}
		for (Element eP : eParamLst)
		{
			if (eP == null) {
				continue;
			}
			Element ePName = eP.element("name");
			if (ePName == null)
				continue;
			String strPName = ePName.getText();
			
			Element ePValue = eP.element("value");
			if (ePValue == null)
				continue;
			String strPValue = ePValue.getText();

			Param p = new Param(strPName, strPValue);
			bean.addParam(p);
		}

		return true;
	}

	public String getAccessorBeanName(int id)
	{
		return this.accessorBeans.getBeanByID(id);
	}

	public AbstractAccessor getAccessorBean(int id)
	{
		String beanClass = getAccessorBeanName(id);
		if (beanClass == null) {
			return null;
		}
		return (AbstractAccessor)toObject(beanClass);
	}

	public String getParserBeanName(int id)
	{
		return this.parserBeans.getBeanByID(id);
	}

	public AbstractParser getParserBean(int id)
	{
		String beanClass = getParserBeanName(id);
		if (beanClass == null) {
			return null;
		}
		return (AbstractParser)toObject(beanClass);
	}

	public String getDistributorBeanName(int id)
	{
		return this.distributorBeans.getBeanByID(id);
	}

	public AbstractDistribute getDistributorBean(int id)
	{
		String beanClass = getDistributorBeanName(id);
		if (beanClass == null) {
			return null;
		}
		return (AbstractDistribute)toObject(beanClass);
	}

	public String getTemplateBeanName(int id)
	{
		return this.templateBeans.getBeanByID(id);
	}

	public AbstractTempletBase getTemplateBean(int id)
	{
		String beanClass = getTemplateBeanName(id);
		if (beanClass == null) {
			return null;
		}
		return (AbstractTempletBase)toObject(beanClass);
	}

	private Object toObject(String className)
	{
		Object o = null;
		try
		{
			o = Class.forName(className).newInstance();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return o;
	}

	public static void main(String[] args)
	{
		getInstance().getAccessorBean(5);
	}
}
    