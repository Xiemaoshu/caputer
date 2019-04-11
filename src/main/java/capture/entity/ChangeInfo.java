
package capture.entity;

import java.util.Date;

/**
 * 企业信息变更记录Entity
 * @author 谢茂树
 * @version 2019-04-04
 */
public class ChangeInfo  {
	
	private String id;
	private String companyName;		// 企业名称
	private String projectName;		// 变更项目
	private Date changeDate;		// 变更日期
	private String beforeInfo;		// 变更前信息
	private String afterInfo;		// 变更后信息
	
	public ChangeInfo() {
		super();
	}




	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	public Date getChangeDate() {
		return changeDate;
	}

	public void setChangeDate(Date changeDate) {
		this.changeDate = changeDate;
	}
	
	public String getBeforeInfo() {
		return beforeInfo;
	}

	public void setBeforeInfo(String beforeInfo) {
		this.beforeInfo = beforeInfo;
	}
	
	public String getAfterInfo() {
		return afterInfo;
	}

	public void setAfterInfo(String afterInfo) {
		this.afterInfo = afterInfo;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}