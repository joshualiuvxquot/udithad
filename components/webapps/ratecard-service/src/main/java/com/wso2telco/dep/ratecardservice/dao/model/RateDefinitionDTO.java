package com.wso2telco.dep.ratecardservice.dao.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RateDefinitionDTO {

	private Integer rateDefId;
	private String rateDefName;
	private String rateDefDesc;
	private Integer rateDefDefault;
	private CurrencyDTO currency;
	private RateTypeDTO rateType;
	private Integer rateDefCategoryBase;
	private TariffDTO tariff;
	
	public Integer getRateDefId() {
		return rateDefId;
	}
	public void setRateDefId(Integer rateDefId) {
		this.rateDefId = rateDefId;
	}
	public String getRateDefName() {
		return rateDefName;
	}
	public void setRateDefName(String rateDefName) {
		this.rateDefName = rateDefName;
	}
	public String getRateDefDesc() {
		return rateDefDesc;
	}
	public void setRateDefDesc(String rateDefDesc) {
		this.rateDefDesc = rateDefDesc;
	}
	public Integer getRateDefDefault() {
		return rateDefDefault;
	}
	public void setRateDefDefault(Integer rateDefDefault) {
		this.rateDefDefault = rateDefDefault;
	}
	public CurrencyDTO getCurrency() {
		return currency;
	}
	public void setCurrency(CurrencyDTO currency) {
		this.currency = currency;
	}
	public RateTypeDTO getRateType() {
		return rateType;
	}
	public void setRateType(RateTypeDTO rateType) {
		this.rateType = rateType;
	}
	public Integer getRateDefCategoryBase() {
		return rateDefCategoryBase;
	}
	public void setRateDefCategoryBase(Integer rateDefCategoryBase) {
		this.rateDefCategoryBase = rateDefCategoryBase;
	}
	public TariffDTO getTariff() {
		return tariff;
	}
	public void setTariff(TariffDTO tariff) {
		this.tariff = tariff;
	}
}
