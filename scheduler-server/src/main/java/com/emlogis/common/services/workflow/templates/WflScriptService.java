package com.emlogis.common.services.workflow.templates;

import com.emlogis.common.services.common.GeneralJPARepository;
import com.emlogis.model.workflow.entities.WflSourceScript;

import java.util.List;

/**
 * Created by alexborlis on 19.02.15.
 */
public interface WflScriptService extends GeneralJPARepository<WflSourceScript, String> {

    List<WflSourceScript> findAllByName(String name);

    WflSourceScript merge(WflSourceScript template);
}
