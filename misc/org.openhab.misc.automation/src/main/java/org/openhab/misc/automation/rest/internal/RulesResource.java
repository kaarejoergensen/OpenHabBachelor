/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.misc.automation.rest.internal;

import static org.eclipse.smarthome.automation.RulePredicates.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.eclipse.smarthome.automation.Rule;
import org.eclipse.smarthome.automation.RuleRegistry;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.ModuleTypeRegistry;
import org.eclipse.smarthome.automation.type.TriggerType;
import org.eclipse.smarthome.io.rest.JSONResponse;
import org.eclipse.smarthome.io.rest.RESTResource;
import org.openhab.misc.automation.rest.internal.mappers.CustomRuleDTOMapper;
import org.openhab.misc.automation.rest.internal.models.CustomRuleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;

/**
 *
 * @author Kaare Joergensen - Initial contribution
 */
@Path("rules2")
@Api("rules2")
public class RulesResource implements RESTResource {
    private final Logger logger = LoggerFactory.getLogger(RulesResource.class);

    private RuleRegistry ruleRegistry;
    private ModuleTypeRegistry moduleTypeRegistry;

    @Context
    private UriInfo uriInfo;

    protected void setRuleRegistry(RuleRegistry ruleRegistry) {
        this.ruleRegistry = ruleRegistry;
    }

    protected void unsetRuleRegistry(RuleRegistry ruleRegistry) {
        this.ruleRegistry = null;
    }

    protected void setModuleTypeRegistry(ModuleTypeRegistry moduleTypeRegistry) {
        this.moduleTypeRegistry = moduleTypeRegistry;
    }

    protected void unsetModuleTypeRegistry(ModuleTypeRegistry moduleTypeRegistry) {
        this.moduleTypeRegistry = null;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get available rules, optionally filtered by tags and/or prefix.", response = String.class, responseContainer = "Collection")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = String.class, responseContainer = "Collection") })
    public Response get(@QueryParam("prefix") final String prefix, @QueryParam("tags") final List<String> tags) {
        // match all
        Predicate<Rule> p = r -> true;

        // prefix parameter has been used
        if (null != prefix) {
            // works also for null prefix
            // (empty prefix used if searching for rules without prefix)
            p = p.and(hasPrefix(prefix));
        }

        // if tags is null or emty list returns all rules
        p = p.and(hasAllTags(tags));

        Collection<Rule> rules = ruleRegistry.stream().filter(p).collect(Collectors.toList());
        Rule rule = new Rule();
        rule.setName("HEJHEJ");
        rules.add(rule);

        return Response.ok(rules).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a rule.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created", responseHeaders = @ResponseHeader(name = "Location", description = "Newly created Rule", response = String.class)),
            @ApiResponse(code = 409, message = "Creation of the rule is refused. Rule with the same UID already exists."),
            @ApiResponse(code = 400, message = "Creation of the rule is refused. Missing required parameter.") })
    public Response create(@ApiParam(value = "rule data", required = true) CustomRuleDTO rule) throws IOException {
        try {
            Collection<TriggerType> triggers = moduleTypeRegistry.getTriggers(null);
            System.out.println(triggers.toString());
            Collection<ConditionType> conditions = moduleTypeRegistry.getConditions(null);
            Collection<ActionType> actions = moduleTypeRegistry.getActions(null);
            final Rule newRule = ruleRegistry.add(CustomRuleDTOMapper.map(rule));
            return Response.status(Status.CREATED)
                    .header("Location", "rules2/" + URLEncoder.encode(newRule.getUID(), "UTF-8")).build();

        } catch (IllegalArgumentException e) {
            String errMessage = "Creation of the rule is refused: " + e.getMessage();
            logger.warn("{}", errMessage);
            return JSONResponse.createErrorResponse(Status.CONFLICT, errMessage);

        } catch (RuntimeException e) {
            String errMessage = "Creation of the rule is refused: " + e.getMessage();
            logger.warn("{}", errMessage);
            return JSONResponse.createErrorResponse(Status.BAD_REQUEST, errMessage);
        }
    }

    @Override
    public boolean isSatisfied() {
        return ruleRegistry != null;
    }
}
