package net.risesoft.service.dynamicrole.impl.v1;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import net.risesoft.api.platform.org.DepartmentApi;
import net.risesoft.api.platform.org.OrgUnitApi;
import net.risesoft.api.processadmin.RuntimeApi;
import net.risesoft.entity.DynamicRole;
import net.risesoft.enums.DynamicRoleRangesEnum;
import net.risesoft.enums.platform.OrgTypeEnum;
import net.risesoft.model.platform.OrgUnit;
import net.risesoft.model.platform.Position;
import net.risesoft.model.processadmin.ProcessInstanceModel;
import net.risesoft.service.dynamicrole.AbstractDynamicRoleMember;
import net.risesoft.y9.Y9LoginUserHolder;

/**
 * 当前流程的启动人员所在部门领导
 *
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/22
 */
@Service
@RequiredArgsConstructor
public class DeptPropCategory extends AbstractDynamicRoleMember {

    private final OrgUnitApi orgUnitApi;

    private final DepartmentApi departmentApi;

    private final RuntimeApi runtimeApi;

    @Override
    public List<Position> getPositionList(String processInstanceId, DynamicRole dynamicRole) {
        String tenantId = Y9LoginUserHolder.getTenantId();
        String userId = Y9LoginUserHolder.getOrgUnitId();
        OrgUnit currentOrgUnit = orgUnitApi.getOrgUnit(tenantId, userId).getData();
        if (dynamicRole.isUseProcessInstanceId()) {
            ProcessInstanceModel processInstance = runtimeApi.getProcessInstance(tenantId, processInstanceId).getData();
            userId = processInstance.getStartUserId();
        }
        boolean isInherit = !dynamicRole.getRanges().equals(DynamicRoleRangesEnum.DEPT.getValue());
        OrgUnit orgUnit = orgUnitApi.getOrgUnitPersonOrPosition(tenantId, userId).getData();
        List<OrgUnit> orgUnitList = departmentApi
            .listDepartmentPropOrgUnits(tenantId, orgUnit.getParentId(), dynamicRole.getDeptPropCategory(), isInherit)
            .getData();
        orgUnitList.remove(currentOrgUnit);
        List<Position> positionList = new ArrayList<>();
        for (OrgUnit orgUnit0 : orgUnitList) {
            if (orgUnit0.getOrgType() == OrgTypeEnum.POSITION) {
                Position position = (Position)orgUnit0;
                positionList.add(position);
            }
        }
        return positionList;
    }
}