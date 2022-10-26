package com.emlogis.workflow.callback;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.List;

/**
 * Created by alexborlis on 03.02.15.
 */
@Stateless
@LocalBean
public class EmlogisUserGroupCallback implements org.kie.api.task.UserGroupCallback {

    @Override
    public boolean existsUser(String userId) {
        return true;
    }

    @Override
    public boolean existsGroup(String groupId) {
        return true;
    }

    @Override
    public List<String> getGroupsForUser(String userId, List<String> groupIds, List<String> allExistingGroupIds) {
        return null;
    }
}
