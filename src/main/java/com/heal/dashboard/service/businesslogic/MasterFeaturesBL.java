package com.heal.dashboard.service.businesslogic;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.heal.dashboard.service.beans.MasterFeatureDetails;
import com.heal.dashboard.service.beans.MasterFeaturesBean;
import com.heal.dashboard.service.beans.UtilityBean;
import com.heal.dashboard.service.dao.mysql.FeaturesDao;
import com.heal.dashboard.service.exception.ClientException;
import com.heal.dashboard.service.exception.DataProcessingException;
import com.heal.dashboard.service.exception.ServerException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MasterFeaturesBL implements BusinessLogic<String, List<MasterFeaturesBean>,MasterFeatureDetails> {

    @Autowired
    private FeaturesDao featuresDao;
    @Override
    public UtilityBean<String> clientValidation(Object requestBody,String... params) throws ClientException {
        return null;
    }

    @Override
    public List<MasterFeaturesBean> serverValidation(UtilityBean<String> utilityBean) throws ServerException {

        List<MasterFeaturesBean> masterFeaturesBeans = featuresDao.getMasterFeatures();
        if (masterFeaturesBeans != null && !masterFeaturesBeans.isEmpty()){
            return masterFeaturesBeans;
        }else {
            log.error("there is no feature available");
        	throw new ServerException("there is feature component available ");
        }

    }

    @Override
    public MasterFeatureDetails process(List<MasterFeaturesBean> bean) throws DataProcessingException {
        return new MasterFeatureDetails(bean);
    }
}
