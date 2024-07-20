package com.investmango.hrconsole.model;

import java.io.Serializable;
import java.util.List;

public class TotalEmpResponse implements Serializable {

	private List<TotalEmpResponseItem> totalEmpResponse;

	public List<TotalEmpResponseItem> getTotalEmpResponse(){
		return totalEmpResponse;
	}

	public void setTotalEmpResponse(List<TotalEmpResponseItem> totalEmpResponse) {
		this.totalEmpResponse = totalEmpResponse;
	}
}