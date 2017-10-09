/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.misc.automation.rest.internal.models;

import java.util.ArrayList;
import java.util.List;

public class CustomRuleDTO {
    private String uid;
    private String name;
    private String description;
    private List<CustomConditionDTO> conditions;
    private List<CustomActionDTO> actions;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<CustomConditionDTO> getConditions() {
        if (conditions == null) {
            conditions = new ArrayList<>();
        }
        return conditions;
    }

    public void setConditions(List<CustomConditionDTO> conditions) {
        this.conditions = conditions == null ? new ArrayList<>() : conditions;
    }

    public List<CustomActionDTO> getActions() {
        if (actions == null) {
            actions = new ArrayList<>();
        }
        return actions;
    }

    public void setActions(List<CustomActionDTO> actions) {
        this.actions = actions == null ? new ArrayList<>() : actions;
    }
}
