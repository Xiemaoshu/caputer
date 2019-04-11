package capture.service;

import capture.entity.ChangeInfo;
import capture.mapper.ChangeInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CaptureService {
    @Autowired
    private ChangeInfoMapper changeInfoMapper;

    /**
     * 根据一个企业名称返回企业的变更记录
     * @param companyName
     * @return
     */
    public List<ChangeInfo> getByName(String companyName){
        ChangeInfo temp = new ChangeInfo();
        temp.setCompanyName(companyName);
        List<ChangeInfo> results = changeInfoMapper.findListByName(temp);
        return results;
    }
}
