
package capture.mapper;

import capture.entity.ChangeInfo;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 企业信息变更记录MAPPER接口
 * @author 谢茂树
 * @version 2019-04-04
 */
@Repository
public interface ChangeInfoMapper  {
    List<ChangeInfo> findListByName(ChangeInfo changeInfo);
    /**
     * 获取 cpms_change_info 表中总记录数
     * @return
     */
	public Integer getAllCount();

    /**
     * 根据企业名称获取该企业的变更记录
     * @param company
     * @return
     */
    public Integer getCountByCompany(String company);

    /**
     * 进行批量添加操作
     */
	public void insertByBatch(List<ChangeInfo> infos);

    /**
     * 根据一个企业名称删除该企业的所有变更记录
     * @param company
     */
	public void deleteByCompany(String company);

    /**
     * 获取已有企业的所有企业名称
     * @return
     */
    public List<String> getAllCompanyName();
}